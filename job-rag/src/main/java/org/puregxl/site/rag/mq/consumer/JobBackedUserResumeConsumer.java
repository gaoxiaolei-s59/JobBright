package org.puregxl.site.rag.mq.consumer;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.puregxl.site.rag.dao.entity.UserResumeFile;
import org.puregxl.site.rag.dao.entity.UserResumeProfile;
import org.puregxl.site.rag.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.rag.dao.mapper.UserResumeProfileMapper;
import org.puregxl.site.rag.dto.profile.ResumeProfileDTO;
import org.puregxl.site.rag.mq.base.MessageWrapper;
import org.puregxl.site.rag.mq.event.UploadResumeExecuteTaskEvent;
import org.puregxl.site.rag.parse.ParseResult;
import org.puregxl.site.rag.parse.TikaParseService;
import org.puregxl.site.rag.profile.ResumeProfileConverter;
import org.puregxl.site.rag.profile.client.ResumeProfileLlmClient;
import org.puregxl.site.rag.profile.prompt.ResumeProfilePromptBuilder;
import org.puregxl.site.rag.service.impl.FileServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_GROUP;
import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_TOPIC;

@Slf4j(topic = "JobBackedUserResumeConsumer")
@Component
@RocketMQMessageListener(
        topic = UPLOAD_RESUME_TOPIC,
        consumerGroup = UPLOAD_RESUME_GROUP
)
@RequiredArgsConstructor
public class JobBackedUserResumeConsumer implements RocketMQListener<MessageWrapper<UploadResumeExecuteTaskEvent>> {

    private final FileServiceImpl fileService;

    private final TikaParseService tikaParseService;

    private final UserResumeFileMapper userResumeFileMapper;

    private final ResumeProfileLlmClient resumeProfileLlmClient;

    private final UserResumeProfileMapper userResumeProfileMapper;

    @Override
    public void onMessage(MessageWrapper<UploadResumeExecuteTaskEvent> messageWrapper) {
        log.info("[消费者] - 用户简历画像处理 - 执行消费逻辑，消息体:{}", JSON.toJSONString(messageWrapper));
        UploadResumeExecuteTaskEvent message = messageWrapper.getMessage();
        if (message == null) {
            log.warn("[消费者]-优惠券传播可靠处理-消息体异常，缺少message对象: {}", JSON.toJSONString(messageWrapper));
            return;
        }

        String fileAddress = message.getFileAddress();
        if (!StringUtils.hasText(fileAddress)) {
            log.warn("[消费者] - 用户简历画像处理 - 文件地址为空: {}", JSON.toJSONString(message));
            return;
        }

        String fileId = message.getFileId();

        if (fileId == null) {
            log.warn("[消费者] - 用户简历画像处理 - 文件id为空: {}", JSON.toJSONString(message));
            return;
        }

        UserResumeFile userResumeFile = userResumeFileMapper.selectById(fileId);
        if (userResumeFile == null) {
            log.warn("[消费者] - 用户简历画像处理 - 未找到简历文件记录, fileId:{}", fileId);
            return;
        }
        //使用tika提取文档的内容
        MultipartFile multipartFile = fileService.downloadMultipartFileByUrl(fileAddress);
        ParseResult parseResult = tikaParseService.parseFile(multipartFile);
        String systemPrompt = ResumeProfilePromptBuilder.buildSystemPrompt();
        String userPrompt = parseResult.getContent();
        log.info("[消费者] - 用户简历画像处理 - 文件下载成功: fileName={}, fileSize={}",
                multipartFile.getOriginalFilename(), multipartFile.getSize());
        //调用大模型客户端提取用户画像
        try {
            ProfileBuildResult profileBuildResult = buildValidatedProfile(systemPrompt, userPrompt, userResumeFile);
            if (!profileBuildResult.valid()) {
                log.error("用户简历画像校验失败, userId:{}, resumeId:{}, reasons:{}",
                        userResumeFile.getUserId(),
                        userResumeFile.getResumeId(),
                        profileBuildResult.errors());
                return;
            }

            userResumeProfileMapper.insert(profileBuildResult.profile());
            log.info("用户简历画像入库成功, userId:{}, resumeId:{}, targetRoles:{}, seniority:{}",
                    userResumeFile.getUserId(),
                    userResumeFile.getResumeId(),
                    profileBuildResult.dto().getTarget_roles(),
                    profileBuildResult.dto().getSeniority());
        } catch (Exception ex) {
            log.error("大模型客户端调用失败, userId:{}, resumeId:{}", userResumeFile.getUserId(), userResumeFile.getResumeId(), ex);
        }
    }

    private ProfileBuildResult buildValidatedProfile(String systemPrompt, String userPrompt, UserResumeFile userResumeFile) throws Exception {
        List<String> errors = new ArrayList<>();

        //两次重试
        for (int round = 1; round <= 2; round++) {
            String userProfileJson = resumeProfileLlmClient.generateProfileJson(systemPrompt, userPrompt);
            ResumeProfileDTO dto = ResumeProfileConverter.toDto(userProfileJson);
            List<String> validationErrors = validateProfile(dto, userPrompt);
            if (validationErrors.isEmpty()) {
                UserResumeProfile profile = ResumeProfileConverter.toEntity(dto, userProfileJson, userResumeFile.getResumeId());
                return new ProfileBuildResult(true, profile, dto, List.of());
            }

            errors = validationErrors;
            log.warn("用户简历画像校验未通过，第{}次生成失败, userId:{}, resumeId:{}, reasons:{}",
                    round,
                    userResumeFile.getUserId(),
                    userResumeFile.getResumeId(),
                    validationErrors);
        }

        return new ProfileBuildResult(false, null, null, errors);
    }

    private List<String> validateProfile(ResumeProfileDTO dto, String sourceText) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("画像DTO为空");
            return errors;
        }
        if (!StringUtils.hasText(dto.getCandidate_name())) {
            errors.add("candidate_name为空");
        }
        if (dto.getTarget_roles() == null || dto.getTarget_roles().isEmpty()) {
            errors.add("target_roles为空");
        }
        if (!isAllowedValue(dto.getSeniority(), List.of("实习", "应届", "初级", "中级", "高级"))) {
            errors.add("seniority不合法");
        }
        if (!isAllowedValue(dto.getEducation_level(), List.of("大专", "本科", "硕士", "博士", "未知"))) {
            errors.add("education_level不合法");
        }
        if (!isAllowedValue(dto.getPreferred_job_type(), List.of("实习", "校招", "社招"))) {
            errors.add("preferred_job_type不合法");
        }
        if (dto.getPreferred_cities() != null
                && !dto.getPreferred_cities().isEmpty()
                && !containsExplicitCityIntent(sourceText)) {
            errors.add("preferred_cities存在明显臆造");
        }
        if (dto.getSchool_tags() != null
                && dto.getSchool_tags().stream().anyMatch(tag -> tag != null && tag.equals(dto.getSchool_name()))) {
            errors.add("school_tags与school_name重复");
        }
        if (!StringUtils.hasText(dto.getResume_summary())) {
            errors.add("resume_summary为空");
        }
        return errors;
    }

    private boolean isAllowedValue(String value, List<String> allowedValues) {
        return StringUtils.hasText(value) && allowedValues.contains(value);
    }

    private boolean containsExplicitCityIntent(String content) {
        return content.contains("意向城市")
                || content.contains("工作地点")
                || content.contains("base")
                || content.contains("上海")
                || content.contains("杭州")
                || content.contains("深圳")
                || content.contains("广州")
                || content.contains("成都");
    }

    private record ProfileBuildResult(
            boolean valid,
            UserResumeProfile profile,
            ResumeProfileDTO dto,
            List<String> errors
    ) {
    }

}
