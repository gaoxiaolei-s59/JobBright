package org.puregxl.site.rag.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.puregxl.site.rag.mq.base.MessageWrapper;
import org.puregxl.site.rag.mq.event.LlmCallLogEvent;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static org.puregxl.site.framework.common.constant.RocketMqConstant.LLM_CALL_LOG_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmCallLogProducer {

    private static final long SEND_TIMEOUT_MS = 2000L;

    private final RocketMQTemplate rocketMQTemplate;

    public void send(LlmCallLogEvent event) {
        String keys = StringUtils.hasText(event.getCallId()) ? event.getCallId() : UUID.randomUUID().toString();
        Message<?> message = MessageBuilder
                .withPayload(new MessageWrapper<>(keys, event))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .build();

        rocketMQTemplate.syncSend(LLM_CALL_LOG_TOPIC, message, SEND_TIMEOUT_MS);
        log.debug("[大模型调用日志] MQ发送成功, callId={}, modelId={}", keys, event.getModelId());
    }
}
