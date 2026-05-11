package org.puregxl.site.rag.llm.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.puregxl.site.infra.convention.ChatClientResult;
import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.enums.ModelCapability;
import org.puregxl.site.infra.http.ModelClientException;
import org.puregxl.site.infra.http.ModelUrlResolver;
import org.puregxl.site.infra.model.ModelTarget;
import org.puregxl.site.rag.mq.event.LlmCallLogEvent;
import org.puregxl.site.rag.mq.producer.LlmCallLogProducer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LlmCallLogAspect {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1024;

    private final LlmCallLogProducer llmCallLogProducer;

    @Around("@annotation(org.puregxl.site.infra.annotation.LlmCallLogged)")
    public Object recordLlmCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ChatRequest request = findArgument(joinPoint.getArgs(), ChatRequest.class);
        ModelTarget target = findArgument(joinPoint.getArgs(), ModelTarget.class);

        if (request == null || target == null) {
            return joinPoint.proceed();
        }

        String traceId = resolveTraceId(request);
        String callId = UUID.randomUUID().toString();
        Instant start = Instant.now();
        try {
            Object result = joinPoint.proceed();
            sendLog(buildEvent(request, target, result, null, start, traceId, callId));
            return result;
        } catch (Throwable throwable) {
            sendLog(buildEvent(request, target, null, throwable, start, traceId, callId));
            throw throwable;
        }
    }

    private String resolveTraceId(ChatRequest request) {
        if (StringUtils.hasText(request.getTraceId())) {
            return request.getTraceId();
        }
        String traceId = UUID.randomUUID().toString();
        request.setTraceId(traceId);
        return traceId;
    }

    private LlmCallLogEvent buildEvent(ChatRequest request,
                                       ModelTarget target,
                                       Object result,
                                       Throwable throwable,
                                       Instant start,
                                       String traceId,
                                       String callId) {
        ChatClientResult chatClientResult = result instanceof ChatClientResult value ? value : null;
        String response = chatClientResult != null ? chatClientResult.getContent() : result instanceof String text ? text : null;
        Long durationMs = Duration.between(start, Instant.now()).toMillis();
        ModelClientException modelException = findModelClientException(throwable);

        return LlmCallLogEvent.builder()
                .traceId(traceId)
                .callId(callId)
                .scene(blankToNull(request.getScene()))
                .bizId(blankToNull(request.getBizId()))
                .userId(request.getUserId())
                .provider(target.getCandidate().getProvider())
                .modelId(target.getId())
                .modelName(target.getCandidate().getModel())
                .endpoint(resolveEndpoint(target))
                .thinking(Boolean.TRUE.equals(request.getThinking()))
                .responseFormat(blankToNull(request.getResponseFormat()))
                .temperature(request.getTemperature() == null ? null : BigDecimal.valueOf(request.getTemperature()))
                .maxTokens(request.getMaxTokens())
                .status(throwable == null ? "SUCCESS" : "FAILED")
                .durationMs(durationMs)
                .httpStatus(modelException == null ? null : modelException.getStatusCode())
                .errorType(modelException == null || modelException.getErrorType() == null
                        ? null : modelException.getErrorType().name())
                .errorCode(throwable == null ? null : throwable.getClass().getSimpleName())
                .errorMessage(throwable == null ? null : limit(throwable.getMessage(), MAX_ERROR_MESSAGE_LENGTH))
                .promptChars(countPromptChars(request.getMessages()))
                .responseChars(response == null ? null : response.length())
                .promptHash(hashMessages(request.getMessages()))
                .responseHash(hashText(response))
                .inputTokens(chatClientResult == null ? null : chatClientResult.getInputTokens())
                .outputTokens(chatClientResult == null ? null : chatClientResult.getOutputTokens())
                .totalTokens(chatClientResult == null ? null : chatClientResult.getTotalTokens())
                .build();
    }

    private void sendLog(LlmCallLogEvent event) {
        try {
            llmCallLogProducer.send(event);
        } catch (Exception exception) {
            log.warn("[大模型调用日志] MQ发送失败, callId={}, modelId={}",
                    event.getCallId(), event.getModelId(), exception);
        }
    }

    private String resolveEndpoint(ModelTarget target) {
        try {
            return ModelUrlResolver.resolveUrl(target.getProvider(), target.getCandidate(), ModelCapability.CHAT);
        } catch (Exception exception) {
            return null;
        }
    }

    private Integer countPromptChars(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        return messages.stream()
                .filter(message -> message != null && message.getContent() != null)
                .mapToInt(message -> message.getContent().length())
                .sum();
    }

    private String hashMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (ChatMessage message : messages) {
            if (message == null) {
                continue;
            }
            builder.append(message.getRole()).append(':')
                    .append(message.getContent() == null ? "" : message.getContent())
                    .append('\n');
        }
        return hashText(builder.toString());
    }

    private String hashText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception exception) {
            return null;
        }
    }

    private ModelClientException findModelClientException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ModelClientException modelClientException) {
                return modelClientException;
            }
            current = current.getCause();
        }
        return null;
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String blankToNull(String text) {
        return StringUtils.hasText(text) ? text : null;
    }

    private <T> T findArgument(Object[] args, Class<T> type) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        return null;
    }
}
