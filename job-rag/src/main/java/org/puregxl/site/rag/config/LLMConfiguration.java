package org.puregxl.site.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfiguration {

    /**
     * LLM 接口地址
     */
    private String apiUrl = "https://api.siliconflow.cn/v1/chat/completions";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型兜底列表，按顺序尝试
     */
    private List<String> models = List.of(
            "Pro/MiniMaxAI/MiniMax-M2.5",
            "Qwen/Qwen2.5-32B-Instruct",
            "deepseek-ai/DeepSeek-V3.2",
            "Pro/MiniMaxAI/MiniMax-M2.5"
    );

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 连接超时时间，单位秒
     */
    private Integer connectTimeoutSeconds = 30;

    /**
     * 请求超时时间，单位秒
     */
    private Integer requestTimeoutSeconds = 120;

    /**
     * 最大返回 token 数
     */
    private Integer maxTokens = 800;

    /**
     * 温度参数
     */
    private Double temperature = 0.1D;

    /**
     * Prompt 版本
     */
    private String promptVersion = "resume_profile_v1";
}
