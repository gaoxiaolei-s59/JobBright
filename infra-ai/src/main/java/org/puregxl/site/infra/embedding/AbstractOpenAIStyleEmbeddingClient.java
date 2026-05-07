package org.puregxl.site.infra.embedding;

import cn.hutool.core.collection.CollUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.puregxl.site.infra.config.AIModelProperties;
import org.puregxl.site.infra.enums.ModelCapability;
import org.puregxl.site.infra.http.*;
import org.puregxl.site.infra.model.ModelTarget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AbstractOpenAIStyleEmbeddingClient implements EmbeddingClient{
    protected final OkHttpClient httpClient;

    protected AbstractOpenAIStyleEmbeddingClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // ==================== 子类钩子方法 ====================

    /**
     * 是否要求提供商配置 API Key，默认 true
     */
    protected boolean requiresApiKey() {
        return true;
    }

    /**
     * 子类可覆写此方法添加提供商特有的请求体字段
     * 默认实现：添加 encoding_format=float
     */
    protected void customizeRequestBody(JsonObject body, ModelTarget target) {
        body.addProperty("encoding_format", "float");
    }

    /**
     * 单次批量请求上限，子类可按 provider 能力覆写
     */
    protected int maxBatchSize() {
        return 16;
    }


    // ==================== 模板方法：核心请求逻辑 ====================

    protected List<Float> doEmbed(String text, ModelTarget target) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<List<Float>> results = doEmbedBatch(List.of(text), target);
        return results.isEmpty() ? List.of() : results.get(0);
    }

    protected List<List<Float>> doEmbedBatch(List<String> texts, ModelTarget target) {
        if (CollUtil.isEmpty(texts)) {
            return List.of();
        }

        List<String> normalizedTexts = texts.stream()
                .map(text -> text == null ? "" : text)
                .toList();

        List<List<Float>> results = new ArrayList<>();
        int batchSize = Math.max(1, maxBatchSize());
        for (int fromIndex = 0; fromIndex < normalizedTexts.size(); fromIndex += batchSize) {
            int toIndex = Math.min(fromIndex + batchSize, normalizedTexts.size());
            results.addAll(doEmbedChunk(normalizedTexts.subList(fromIndex, toIndex), target));
        }
        return results;
    }

    private List<List<Float>> doEmbedChunk(List<String> texts, ModelTarget target) {
        AIModelProperties.ProviderConfig provider = HttpResponseHelper.requireProvider(target, provider());
        if (requiresApiKey()) {
            HttpResponseHelper.requireApiKey(provider, provider());
        }

        JsonObject requestBody = buildRequestBody(texts, target);
        Request request = newAuthorizedRequest(provider, target)
                .post(RequestBody.create(requestBody.toString(), HttpMediaTypes.JSON))
                .build();

        JsonObject responseJson;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = HttpResponseHelper.readBody(response.body());
                log.warn("{} embedding 请求失败: status={}, body={}", provider(), response.code(), body);
                throw new ModelClientException(
                        provider() + " embedding 请求失败: HTTP " + response.code(),
                        ModelClientErrorType.fromHttpStatus(response.code()),
                        response.code()
                );
            }
            responseJson = HttpResponseHelper.parseJson(response.body(), provider());
        } catch (IOException exception) {
            throw new ModelClientException(
                    provider() + " embedding 请求失败: " + exception.getMessage(),
                    ModelClientErrorType.NETWORK_ERROR,
                    null,
                    exception
            );
        }

        return parseEmbeddingResponse(responseJson);
    }

    private JsonObject buildRequestBody(List<String> texts, ModelTarget target) {
        JsonObject body = new JsonObject();
        body.addProperty("model", HttpResponseHelper.requireModel(target, provider()));

        if (texts.size() == 1) {
            body.addProperty("input", texts.get(0));
        } else {
            JsonArray inputArray = new JsonArray();
            for (String text : texts) {
                inputArray.add(text);
            }
            body.add("input", inputArray);
        }

        Integer dimension = target != null && target.getCandidate() != null
                ? target.getCandidate().getDimension()
                : null;
        if (dimension != null && dimension > 0) {
            body.addProperty("dimensions", dimension);
        }

        customizeRequestBody(body, target);
        return body;
    }

    private Request.Builder newAuthorizedRequest(AIModelProperties.ProviderConfig provider, ModelTarget target) {
        Request.Builder builder = new Request.Builder()
                .url(ModelUrlResolver.resolveUrl(provider, target.getCandidate(), ModelCapability.EMBEDDING));
        if (requiresApiKey()) {
            builder.addHeader("Authorization", "Bearer " + provider.getApiKey());
        }
        builder.addHeader("Content-Type", "application/json");
        return builder;
    }

    private List<List<Float>> parseEmbeddingResponse(JsonObject responseJson) {
        if (responseJson == null || !responseJson.has("data")) {
            throw new ModelClientException(provider() + " embedding 响应缺少 data", ModelClientErrorType.INVALID_RESPONSE, null);
        }

        JsonArray data = responseJson.getAsJsonArray("data");
        if (data == null) {
            throw new ModelClientException(provider() + " embedding data 为空", ModelClientErrorType.INVALID_RESPONSE, null);
        }

        List<List<Float>> results = new ArrayList<>();
        for (JsonElement item : data) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject row = item.getAsJsonObject();
            if (!row.has("embedding") || !row.get("embedding").isJsonArray()) {
                throw new ModelClientException(provider() + " embedding 响应缺少 embedding", ModelClientErrorType.INVALID_RESPONSE, null);
            }

            JsonArray embeddingArray = row.getAsJsonArray("embedding");
            List<Float> vector = new ArrayList<>(embeddingArray.size());
            for (JsonElement value : embeddingArray) {
                vector.add(value.getAsFloat());
            }
            results.add(Collections.unmodifiableList(vector));
        }
        return results;
    }

}
