package org.puregxl.site.rag.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.puregxl.site.framework.independence.NoMQDuplicateConsume;
import org.puregxl.site.framework.mq.UploadResumeExecuteTaskEvent;
import org.puregxl.site.rag.config.RustfsProperties;
import org.puregxl.site.rag.dao.entity.UserResumeAnalysis;
import org.puregxl.site.rag.dao.entity.UserResumeFile;
import org.puregxl.site.rag.dao.mapper.UserResumeAnalysisMapper;
import org.puregxl.site.rag.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.rag.dto.resume.ResumeAnalysisDTO;
import org.puregxl.site.rag.llm.profile.ProfileGenerateResult;
import org.puregxl.site.rag.llm.resume.ResumeAnalysisConverter;
import org.puregxl.site.rag.llm.resume.client.ResumeAnalysisLlmClient;
import org.puregxl.site.rag.llm.resume.prompt.ResumeAnalysisLlmPrompt;
import org.puregxl.site.rag.mq.base.MessageWrapper;
import org.puregxl.site.rag.parse.ParseResult;
import org.puregxl.site.rag.parse.TikaParseService;
import org.puregxl.site.rag.service.impl.FileServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_ANALYSIS_GROUP;
import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_ANALYSIS_TOPIC;

@Slf4j(topic = "JobBackedUserResumeAnalysisConsumer")
@Component
@RocketMQMessageListener(
        topic = UPLOAD_RESUME_ANALYSIS_TOPIC,
        consumerGroup = UPLOAD_RESUME_ANALYSIS_GROUP
)
@RequiredArgsConstructor
public class JobBackedUserResumeAnalysisConsumer implements RocketMQListener<MessageWrapper<UploadResumeExecuteTaskEvent>> {

    private final FileServiceImpl fileService;

    private final TikaParseService tikaParseService;

    private final UserResumeFileMapper userResumeFileMapper;

    private final UserResumeAnalysisMapper userResumeAnalysisMapper;

    private final ResumeAnalysisLlmClient resumeAnalysisLlmClient;

    private final RustfsProperties rustfsProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @NoMQDuplicateConsume(
            keyPrefix = "mq:resume:analysis",
            key = "#messageWrapper.message.resumeId",
            keyTimeout = 3600L
    )
    public void onMessage(MessageWrapper<UploadResumeExecuteTaskEvent> messageWrapper) {
        log.info("[消费者] - 用户简历分析处理 - 执行消费逻辑，消息体:{}", JSON.toJSONString(messageWrapper));
        UploadResumeExecuteTaskEvent message = messageWrapper.getMessage();
        if (message == null) {
            log.warn("[消费者] - 用户简历分析处理 - 消息体异常，缺少message对象: {}", JSON.toJSONString(messageWrapper));
            return;
        }

        String resumeId = message.getResumeId();
        if (!StringUtils.hasText(resumeId)) {
            log.warn("[消费者] - 用户简历分析处理 - 简历ID为空: {}", JSON.toJSONString(message));
            return;
        }

        UserResumeFile userResumeFile = getResumeFile(resumeId);
        if (userResumeFile == null) {
            log.warn("[消费者] - 用户简历分析处理 - 未找到简历文件记录, resumeId:{}", resumeId);
            return;
        }

        try {
            MultipartFile multipartFile = fileService.downloadMultipartFile(userResumeFile.getResumeId(), rustfsProperties.getBucketName());
            ParseResult parseResult = tikaParseService.parseFile(multipartFile);
            String resumeText = parseResult == null ? "" : parseResult.getContent();
            if (!StringUtils.hasText(resumeText)) {
                upsertAnalysis(ResumeAnalysisConverter.failed(resumeId, userResumeFile.getUserId(), "简历文本解析为空"));
                log.warn("[消费者] - 用户简历分析处理 - 简历文本解析为空, userId:{}, resumeId:{}",
                        userResumeFile.getUserId(), resumeId);
                return;
            }

            AnalysisBuildResult buildResult = buildValidatedAnalysis(resumeText, userResumeFile);
            if (!buildResult.valid()) {
                upsertAnalysis(ResumeAnalysisConverter.failed(resumeId, userResumeFile.getUserId(), String.join("；", buildResult.errors())));
                log.error("[消费者] - 用户简历分析处理 - 分析结果校验失败, userId:{}, resumeId:{}, reasons:{}",
                        userResumeFile.getUserId(), resumeId, buildResult.errors());
                return;
            }

            upsertAnalysis(buildResult.analysis());
            updateResumeScore(userResumeFile, buildResult.analysis().getScoreValue());
            log.info("[消费者] - 用户简历分析处理 - 分析入库成功, userId:{}, resumeId:{}, score:{}",
                    userResumeFile.getUserId(), resumeId, buildResult.analysis().getScoreValue());
        } catch (Exception ex) {
            upsertAnalysis(ResumeAnalysisConverter.failed(resumeId, userResumeFile.getUserId(), "简历分析异常: " + ex.getMessage()));
            log.error("[消费者] - 用户简历分析处理 - 执行失败, userId:{}, resumeId:{}",
                    userResumeFile.getUserId(), resumeId, ex);
        }
    }

    private UserResumeFile getResumeFile(String resumeId) {
        return userResumeFileMapper.selectOne(
                Wrappers.lambdaQuery(UserResumeFile.class)
                        .eq(UserResumeFile::getResumeId, resumeId)
                        .eq(UserResumeFile::getDelFlag, 0)
                        .last("limit 1")
        );
    }

    private AnalysisBuildResult buildValidatedAnalysis(String resumeText, UserResumeFile userResumeFile) throws Exception {
        List<String> errors = new ArrayList<>();
        String systemPrompt = ResumeAnalysisLlmPrompt.buildSystemPrompt();
        String userPrompt = ResumeAnalysisLlmPrompt.buildResumeAnalysisUserPrompt(resumeText);

        for (int round = 1; round <= 2; round++) {
            ProfileGenerateResult analysisResult = resumeAnalysisLlmClient.generateAnalysis(systemPrompt, userPrompt);
            String analysisJson = analysisResult.getParseResult();
            ResumeAnalysisDTO dto = ResumeAnalysisConverter.toDto(analysisJson);
            List<String> validationErrors = validateAnalysis(dto);
            if (validationErrors.isEmpty()) {
                UserResumeAnalysis analysis = ResumeAnalysisConverter.toEntity(
                        dto,
                        analysisJson,
                        userResumeFile.getResumeId(),
                        userResumeFile.getUserId()
                );
                return new AnalysisBuildResult(true, analysis, dto, List.of());
            }

            errors = validationErrors;
            log.warn("[消费者] - 用户简历分析处理 - 第{}次生成校验失败, userId:{}, resumeId:{}, reasons:{}",
                    round, userResumeFile.getUserId(), userResumeFile.getResumeId(), validationErrors);
        }

        return new AnalysisBuildResult(false, null, null, errors);
    }

    private List<String> validateAnalysis(ResumeAnalysisDTO dto) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("分析DTO为空");
            return errors;
        }
        if (dto.getScore() == null) {
            errors.add("score为空");
            return errors;
        }
        Integer scoreValue = dto.getScore().getScoreValue();
        if (scoreValue == null || scoreValue < 0 || scoreValue > 100) {
            errors.add("scoreValue不合法");
        }
        if (!isAllowedValue(dto.getScore().getGrade(), List.of("A", "B", "C"))) {
            errors.add("grade不合法");
        }
        if (!isAllowedValue(dto.getScore().getLabel(), List.of("EXCELLENT", "GOOD", "FAIR"))) {
            errors.add("label不合法");
        }
        if (!StringUtils.hasText(dto.getAnalysisSummary())
                && !StringUtils.hasText(dto.getScore().getSummary())) {
            errors.add("analysisSummary为空");
        }
        return errors;
    }

    private boolean isAllowedValue(String value, List<String> allowedValues) {
        return StringUtils.hasText(value) && allowedValues.contains(value);
    }

    private void upsertAnalysis(UserResumeAnalysis analysis) {
        UserResumeAnalysis existing = userResumeAnalysisMapper.selectOne(
                Wrappers.lambdaQuery(UserResumeAnalysis.class)
                        .eq(UserResumeAnalysis::getResumeId, analysis.getResumeId())
                        .last("limit 1")
        );
        if (existing == null) {
            userResumeAnalysisMapper.insert(analysis);
            return;
        }
        userResumeAnalysisMapper.update(null, Wrappers.lambdaUpdate(UserResumeAnalysis.class)
                .eq(UserResumeAnalysis::getId, existing.getId())
                .set(UserResumeAnalysis::getUserId, analysis.getUserId())
                .set(UserResumeAnalysis::getScoreValue, analysis.getScoreValue())
                .set(UserResumeAnalysis::getGrade, analysis.getGrade())
                .set(UserResumeAnalysis::getLabel, analysis.getLabel())
                .set(UserResumeAnalysis::getUrgentFixCount, analysis.getUrgentFixCount())
                .set(UserResumeAnalysis::getCriticalFixCount, analysis.getCriticalFixCount())
                .set(UserResumeAnalysis::getOptionalFixCount, analysis.getOptionalFixCount())
                .set(UserResumeAnalysis::getProfileJson, analysis.getProfileJson())
                .set(UserResumeAnalysis::getSkillGroupsJson, analysis.getSkillGroupsJson())
                .set(UserResumeAnalysis::getProjectsJson, analysis.getProjectsJson())
                .set(UserResumeAnalysis::getHighlightsJson, analysis.getHighlightsJson())
                .set(UserResumeAnalysis::getIssuesJson, analysis.getIssuesJson())
                .set(UserResumeAnalysis::getAnalysisSummary, analysis.getAnalysisSummary())
                .set(UserResumeAnalysis::getRawAnalysisJson, analysis.getRawAnalysisJson())
                .set(UserResumeAnalysis::getStatus, analysis.getStatus())
                .set(UserResumeAnalysis::getDelFlag, 0));
    }

    private void updateResumeScore(UserResumeFile userResumeFile, Integer scoreValue) {
        if (scoreValue == null) {
            return;
        }
        UserResumeFile update = UserResumeFile.builder()
                .id(userResumeFile.getId())
                .score(scoreValue.doubleValue())
                .build();
        userResumeFileMapper.updateById(update);
    }

    private record AnalysisBuildResult(
            boolean valid,
            UserResumeAnalysis analysis,
            ResumeAnalysisDTO dto,
            List<String> errors
    ) {
    }
}
