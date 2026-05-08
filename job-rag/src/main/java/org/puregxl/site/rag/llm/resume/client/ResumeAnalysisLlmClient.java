package org.puregxl.site.rag.llm.resume.client;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.rag.llm.InfraAiJsonLlmClient;
import org.puregxl.site.rag.llm.profile.ProfileGenerateResult;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ResumeAnalysisLlmClient {
    private final InfraAiJsonLlmClient infraAiJsonLlmClient;

    public ProfileGenerateResult generateAnalysis(String systemPrompt, String userPrompt) throws Exception {
        InfraAiJsonLlmClient.JsonLlmResult result = infraAiJsonLlmClient.chatJson(systemPrompt, userPrompt);
        return ProfileGenerateResult.builder()
                .parseResult(result.content())
                .model(result.modelId())
                .build();
    }

    public ProfileGenerateResult generateProfile(String systemPrompt, String userPrompt) throws Exception {
        return generateAnalysis(systemPrompt, userPrompt);
    }

    public ProfileGenerateResult generateProfileJson(String systemPrompt, String userPrompt) throws Exception {
        return generateAnalysis(systemPrompt, userPrompt);
    }
}
