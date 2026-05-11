package org.puregxl.site.rag.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.puregxl.site.framework.independence.NoMQDuplicateConsume;
import org.puregxl.site.rag.dao.entity.LlmCallLog;
import org.puregxl.site.rag.dao.mapper.LlmCallLogMapper;
import org.puregxl.site.rag.mq.base.MessageWrapper;
import org.puregxl.site.rag.mq.event.LlmCallLogEvent;
import org.springframework.stereotype.Component;

import static org.puregxl.site.framework.common.constant.RocketMqConstant.LLM_CALL_LOG_GROUP;
import static org.puregxl.site.framework.common.constant.RocketMqConstant.LLM_CALL_LOG_TOPIC;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = LLM_CALL_LOG_TOPIC,
        consumerGroup = LLM_CALL_LOG_GROUP
)
@RequiredArgsConstructor
public class LlmCallLogConsumer implements RocketMQListener<MessageWrapper<LlmCallLogEvent>> {

    private final LlmCallLogMapper llmCallLogMapper;

    @Override
    @NoMQDuplicateConsume(
            key = "#messageWrapper.keys",
            keyPrefix = "mq:llm-call-log:",
            keyTimeout = 3600
    )
    public void onMessage(MessageWrapper<LlmCallLogEvent> messageWrapper) {
        if (messageWrapper == null || messageWrapper.getMessage() == null) {
            log.warn("[大模型调用日志] 消息体为空");
            return;
        }
        LlmCallLogEvent event = messageWrapper.getMessage();
        llmCallLogMapper.insert(convert(event));
        log.debug("[大模型调用日志] 入库成功, callId={}, modelId={}", event.getCallId(), event.getModelId());
    }

    private LlmCallLog convert(LlmCallLogEvent event) {
        return LlmCallLog.builder()
                .traceId(event.getTraceId())
                .callId(event.getCallId())
                .scene(event.getScene())
                .bizId(event.getBizId())
                .userId(event.getUserId())
                .provider(event.getProvider())
                .modelId(event.getModelId())
                .modelName(event.getModelName())
                .endpoint(event.getEndpoint())
                .thinking(event.getThinking())
                .responseFormat(event.getResponseFormat())
                .temperature(event.getTemperature())
                .maxTokens(event.getMaxTokens())
                .status(event.getStatus())
                .durationMs(event.getDurationMs())
                .httpStatus(event.getHttpStatus())
                .errorType(event.getErrorType())
                .errorCode(event.getErrorCode())
                .errorMessage(event.getErrorMessage())
                .promptChars(event.getPromptChars())
                .responseChars(event.getResponseChars())
                .promptHash(event.getPromptHash())
                .responseHash(event.getResponseHash())
                .inputTokens(event.getInputTokens())
                .outputTokens(event.getOutputTokens())
                .totalTokens(event.getTotalTokens())
                .build();
    }
}
