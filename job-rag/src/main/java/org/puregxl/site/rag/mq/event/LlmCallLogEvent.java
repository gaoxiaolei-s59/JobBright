package org.puregxl.site.rag.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmCallLogEvent {

    private String traceId;

    private String callId;

    private String scene;

    private String bizId;

    private Long userId;

    private String provider;

    private String modelId;

    private String modelName;

    private String endpoint;

    private Boolean thinking;

    private String responseFormat;

    private BigDecimal temperature;

    private Integer maxTokens;

    private String status;

    private Long durationMs;

    private Integer httpStatus;

    private String errorType;

    private String errorCode;

    private String errorMessage;

    private Integer promptChars;

    private Integer responseChars;

    private String promptHash;

    private String responseHash;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

}
