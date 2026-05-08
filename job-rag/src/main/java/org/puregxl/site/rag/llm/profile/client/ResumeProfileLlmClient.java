package org.puregxl.site.rag.llm.profile.client;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.rag.llm.InfraAiJsonLlmClient;
import org.puregxl.site.rag.llm.profile.ProfileGenerateResult;
import org.springframework.stereotype.Service;

/**
 * 用户画像系统封装
 */
@Service
@RequiredArgsConstructor
public class ResumeProfileLlmClient {

    private final InfraAiJsonLlmClient infraAiJsonLlmClient;

    public ProfileGenerateResult generateProfile(String systemPrompt, String userPrompt) throws Exception {
        InfraAiJsonLlmClient.JsonLlmResult result = infraAiJsonLlmClient.chatJson(systemPrompt, userPrompt);
        return ProfileGenerateResult.builder()
                .parseResult(result.content())
                .model(result.modelId())
                .build();
    }

    public ProfileGenerateResult generateProfileJson(String systemPrompt, String userPrompt) throws Exception {
        return generateProfile(systemPrompt, userPrompt);
    }
}
