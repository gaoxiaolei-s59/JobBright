package org.puregxl.site.rag.llm.job.client;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.rag.llm.InfraAiJsonLlmClient;
import org.springframework.stereotype.Service;

/**
 * 用户画像系统封装
 */
@Service
@RequiredArgsConstructor
public class JobPostCleanLlmClient {

    private final InfraAiJsonLlmClient infraAiJsonLlmClient;

    public String cleanJob(String systemPrompt, String userPrompt) throws Exception {
        return infraAiJsonLlmClient.chatJson(systemPrompt, userPrompt).content();
    }
}
