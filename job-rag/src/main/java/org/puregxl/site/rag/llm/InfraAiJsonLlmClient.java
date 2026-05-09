package org.puregxl.site.rag.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.infra.chat.LLMService;
import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.convention.ChatResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InfraAiJsonLlmClient {

    private static final int DEFAULT_MAX_TOKENS = 3000;

    private static final double DEFAULT_TEMPERATURE = 0.1D;

    private final LLMService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonLlmResult chatJson(String systemPrompt, String userPrompt) throws Exception {
        ChatRequest request = ChatRequest.builder()
                .temperature(DEFAULT_TEMPERATURE)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .responseFormat("json_object")
                .messages(List.of(
                        ChatMessage.system(systemPrompt),
                        ChatMessage.user(userPrompt)
                ))
                .build();

        ChatResult chatResult = llmService.doChatWithResult(request);
        String content = chatResult.getContent().trim();
        objectMapper.readTree(content);
        return new JsonLlmResult(content, chatResult.getModelId());
    }

    public record JsonLlmResult(String content, String modelId) {
    }
}
