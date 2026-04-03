package org.puregxl.site.rag.mq.consumer;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.rag.dao.entity.UserResumeFile;
import org.puregxl.site.rag.dao.entity.UserResumeProfile;
import org.puregxl.site.rag.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.rag.dao.mapper.UserResumeProfileMapper;
import org.puregxl.site.rag.dto.profile.ResumeProfileDTO;
import org.puregxl.site.rag.dto.resp.DownloadFileResponse;
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
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
    /**
     * 自动检测解析器
     */
    private final Parser parser = new AutoDetectParser();

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
        //使用tika提取文档的内容
        MultipartFile multipartFile = fileService.downloadMultipartFileByUrl(fileAddress);
        ParseResult parseResult = tikaParseService.parseFile(multipartFile);
        String systemPrompt = ResumeProfilePromptBuilder.buildSystemPrompt();
        String userPrompt = parseResult.getContent();
        log.info("[消费者] - 用户简历画像处理 - 文件下载成功: fileName={}, fileSize={}",
                multipartFile.getOriginalFilename(), multipartFile.getSize());
        //调用大模型客户端提取用户画像
        String userProfile;
        try {
            userProfile = resumeProfileLlmClient.generateProfileJson(systemPrompt, userPrompt);
            //封装成dto
            ResumeProfileDTO userProfiledDTO = ResumeProfileConverter.toDto(userProfile);
            UserResumeProfile userResumeProfile = ResumeProfileConverter.toEntity(
                    userProfiledDTO,
                    userProfile,
                    userResumeFile.getResumeId()
            );
            userResumeProfileMapper.insert(userResumeProfile);
        } catch (Exception ex) {
            log.error("大模型客户端调用失败, userId:{}, resumeId:{}", userResumeFile.getUserId(), userResumeFile.getResumeId(), ex);
        }

        log.info("[消费者] - 用户简历画像处理 - 文件下载成功: fileName={}, fileSize={}",
                multipartFile.getOriginalFilename(), multipartFile.getSize());

    }

}
