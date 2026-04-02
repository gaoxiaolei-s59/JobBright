package org.puregxl.site.jobbacked.mq.producer;

import cn.hutool.core.util.StrUtil;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.puregxl.site.jobbacked.mq.base.BaseSendExtendDTO;
import org.puregxl.site.jobbacked.mq.base.MessageWrapper;
import org.puregxl.site.jobbacked.mq.event.UploadResumeExecuteTaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.util.UUID;
import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_TOPIC;



@Component
@ConditionalOnBean(RocketMQTemplate.class)
public class JobBackedUserResumeProduceTemplate extends AbstractCommonSendProduceTemplate<UploadResumeExecuteTaskEvent>{

    public JobBackedUserResumeProduceTemplate(RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }


    @Override
    protected BaseSendExtendDTO buildBaseSendExtendDTO(UploadResumeExecuteTaskEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("用户简历切块")
                .topic(UPLOAD_RESUME_TOPIC)
                .sentTimeout(2000L)
                .build();
    }


    @Override
    protected Message<?> buildMessage(UploadResumeExecuteTaskEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        return MessageBuilder
                .withPayload(new MessageWrapper(keys, messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
