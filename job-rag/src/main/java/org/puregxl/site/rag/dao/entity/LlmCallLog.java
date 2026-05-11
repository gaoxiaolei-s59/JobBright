package org.puregxl.site.rag.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("llm_call_log")
public class LlmCallLog {

    private Long id;

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

    private Date createTime;

    private Date updateTime;

    private Integer delFlag;
}
