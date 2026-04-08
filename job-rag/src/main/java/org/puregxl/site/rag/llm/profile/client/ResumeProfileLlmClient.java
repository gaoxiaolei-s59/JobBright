package org.puregxl.site.rag.llm.profile.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.framework.exception.ServiceException;
import org.puregxl.site.rag.config.LLMConfiguration;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户画像系统封装
 */
@Service
@RequiredArgsConstructor
public class ResumeProfileLlmClient {

    private final LLMConfiguration llmConfiguration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateProfileJson(String systemPrompt, String userPrompt) throws Exception {
        if (llmConfiguration.getApiKey() == null || llmConfiguration.getApiKey().isBlank()) {
            throw new ClientException("LLM API Key 未配置");
        }

        List<String> models = resolveModels();
        List<String> errors = new ArrayList<>();

        for (String model : models) {
            Exception lastException = null;
            for (int attempt = 1; attempt <= llmConfiguration.getMaxRetries(); attempt++) {
                try {
                    return doGenerateProfileJson(model, systemPrompt, userPrompt);
                } catch (HttpTimeoutException timeoutException) {
                    lastException = timeoutException;
                    System.err.println("LLM 调用超时，模型=" + model + "，第 " + attempt + " 次尝试失败: " + timeoutException.getMessage());
                } catch (Exception exception) {
                    lastException = exception;
                    System.err.println("LLM 调用失败，模型=" + model + "，第 " + attempt + " 次尝试失败: " + exception.getMessage());
                }

                if (attempt < llmConfiguration.getMaxRetries()) {
                    Thread.sleep(1000L * attempt);
                }
            }
            if (lastException != null) {
                errors.add("model=" + model + ", error=" + lastException.getMessage());
            }
        }

        throw new RuntimeException("所有 LLM 模型调用均失败: " + String.join(" | ", errors));
    }

    private String doGenerateProfileJson(String model, String systemPrompt, String userPrompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", llmConfiguration.getTemperature(),
                "max_tokens", llmConfiguration.getMaxTokens(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(llmConfiguration.getConnectTimeoutSeconds()))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(llmConfiguration.getApiUrl()))
                .header("Authorization", "Bearer " + llmConfiguration.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(llmConfiguration.getRequestTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("LLM 调用失败，模型=" + model + "，状态码：" + response.statusCode() + "，响应：" + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new RuntimeException("LLM 返回内容为空：" + response.body());
        }

        String profileJson = contentNode.asText().trim();
        objectMapper.readTree(profileJson);
        return profileJson;
    }

    private List<String> resolveModels() {
        if (llmConfiguration.getModels() == null || llmConfiguration.getModels().isEmpty()) {
            throw new ServiceException("模型没有配置 - 检查配置项");
        }
        return llmConfiguration.getModels();
    }
}
