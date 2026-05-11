package org.puregxl.site.rag.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.infra.chat.LLMService;
import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.convention.ChatResult;
import org.puregxl.site.framework.errorcode.BaseErrorCode;
import org.puregxl.site.framework.exception.RemoteException;
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
        String rawContent = chatResult.getContent();
        if (rawContent == null || rawContent.isBlank()) {
            throw new RemoteException("大模型返回内容为空, modelId=" + chatResult.getModelId(), BaseErrorCode.REMOTE_ERROR);
        }

        String content = rawContent.trim();
        objectMapper.readTree(content);
        return new JsonLlmResult(content, chatResult.getModelId());
    }

    public record JsonLlmResult(String content, String modelId) {
    }
}
