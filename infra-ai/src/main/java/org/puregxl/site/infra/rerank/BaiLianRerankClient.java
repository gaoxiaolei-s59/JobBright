package org.puregxl.site.infra.rerank;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.puregxl.site.infra.config.AIModelProperties;
import org.puregxl.site.infra.convention.RetrievedChunk;
import org.puregxl.site.infra.enums.ModelCapability;
import org.puregxl.site.infra.enums.ModelProvider;
import org.puregxl.site.infra.http.HttpMediaTypes;
import org.puregxl.site.infra.http.HttpResponseHelper;
import org.puregxl.site.infra.http.ModelClientErrorType;
import org.puregxl.site.infra.http.ModelClientException;
import org.puregxl.site.infra.http.ModelUrlResolver;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaiLianRerankClient implements RerankClient{

    private final OkHttpClient httpClient;

    private final Gson gson = new Gson();

    @Override
    public String provider() {
        return ModelProvider.BAI_LIAN.getId();
    }


    /**
     * rerank
     * @param query      用户问题
     * @param candidates 向量检索出来的一批候选文档（通常是 topK 的 3~5 倍）
     * @param topN       最终希望保留的条数（喂给大模型的 K）
     * @param modelTarget 模型基础信息
     * @return
     */
    @Override
    public List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates, int topN, ModelTarget modelTarget) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (!StringUtils.hasText(query)) {
            return candidates.stream()
                    .limit(Math.max(topN, 0))
                    .toList();
        }

        AIModelProperties.ProviderConfig providerConfig = HttpResponseHelper.requireProvider(modelTarget, provider());
        HttpResponseHelper.requireApiKey(providerConfig, provider());
        String model = HttpResponseHelper.requireModel(modelTarget, provider());
        int resolvedTopN = resolveTopN(topN, candidates.size());

        JsonObject requestBody = buildRequestBody(query, candidates, resolvedTopN, model);
        Request request = new Request.Builder()
                .url(ModelUrlResolver.resolveUrl(providerConfig, modelTarget.getCandidate(), ModelCapability.RERANK))
                .addHeader("Authorization", "Bearer " + providerConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), HttpMediaTypes.JSON))
                .build();

        JsonObject responseJson;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = HttpResponseHelper.readBody(response.body());
                log.warn("{} rerank 请求失败: status={}, body={}", provider(), response.code(), body);
                throw new ModelClientException(
                        provider() + " rerank 请求失败: HTTP " + response.code(),
                        ModelClientErrorType.fromHttpStatus(response.code()),
                        response.code()
                );
            }
            responseJson = HttpResponseHelper.parseJson(response.body(), provider());
        } catch (IOException exception) {
            throw new ModelClientException(
                    provider() + " rerank 请求失败: " + exception.getMessage(),
                    ModelClientErrorType.NETWORK_ERROR,
                    null,
                    exception
            );
        }

        return parseResults(responseJson, candidates, resolvedTopN);
    }

    private JsonObject buildRequestBody(String query, List<RetrievedChunk> candidates, int topN, String model) {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);

        JsonObject input = new JsonObject();
        input.addProperty("query", query);

        JsonArray documents = new JsonArray();
        for (RetrievedChunk candidate : candidates) {
            documents.add(candidate == null ? "" : candidate.getText());
        }
        input.add("documents", documents);
        body.add("input", input);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("top_n", topN);
        parameters.addProperty("return_documents", true);
        body.add("parameters", parameters);
        return body;
    }

    private List<RetrievedChunk> parseResults(JsonObject responseJson, List<RetrievedChunk> candidates, int topN) {
        if (responseJson == null || !responseJson.has("output")) {
            throw new ModelClientException(provider() + " rerank 响应缺少 output", ModelClientErrorType.INVALID_RESPONSE, null);
        }

        JsonObject output = responseJson.getAsJsonObject("output");
        if (output == null || !output.has("results")) {
            throw new ModelClientException(provider() + " rerank 响应缺少 results", ModelClientErrorType.INVALID_RESPONSE, null);
        }

        JsonArray results = output.getAsJsonArray("results");
        if (results == null) {
            throw new ModelClientException(provider() + " rerank 响应 results 为空", ModelClientErrorType.INVALID_RESPONSE, null);
        }

        List<RetrievedChunk> reranked = new ArrayList<>();
        for (JsonElement element : results) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject result = element.getAsJsonObject();
            int index = result.has("index") ? result.get("index").getAsInt() : -1;
            if (index < 0 || index >= candidates.size()) {
                continue;
            }

            RetrievedChunk original = candidates.get(index);
            String text = original.getText();
            if (result.has("document") && result.get("document").isJsonObject()) {
                JsonObject document = result.getAsJsonObject("document");
                if (document.has("text") && !document.get("text").isJsonNull()) {
                    text = document.get("text").getAsString();
                }
            }

            Float score = result.has("relevance_score") && !result.get("relevance_score").isJsonNull()
                    ? result.get("relevance_score").getAsFloat()
                    : original.getScore();

            reranked.add(RetrievedChunk.builder()
                    .id(original.getId())
                    .text(text)
                    .score(score)
                    .build());

            if (reranked.size() >= topN) {
                break;
            }
        }
        return reranked;
    }

    private int resolveTopN(int topN, int candidateSize) {
        if (topN <= 0) {
            return candidateSize;
        }
        return Math.min(topN, candidateSize);
    }
}
