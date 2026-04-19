package org.puregxl.site.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.infra.config.AIModelProperties;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ModelTarget {
    /**
     * id
     */
    private String id;

    /**
     * 模型基础信息
     * -id: qwen3-rerank
     * provider: bailian
     * model: qwen3-rerank
     * priority: 1
     */
    private AIModelProperties.ModelCandidate candidate;

    /**
     * 配置商信息
     * ollama:
     * url: http://localhost:11434
     * endpoints:
     * chat: /v1/chat/completions
     * embedding: /v1/embeddings
     */
    private AIModelProperties.ProviderConfig provider;
}
