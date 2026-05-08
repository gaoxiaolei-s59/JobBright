package org.puregxl.site.rag.stream;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.puregxl.site.framework.errorcode.BaseErrorCode;
import org.puregxl.site.framework.exception.RemoteException;
import org.puregxl.site.infra.config.AIModelProperties;
import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.enums.ModelCapability;
import org.puregxl.site.infra.http.HttpMediaTypes;
import org.puregxl.site.infra.http.ModelUrlResolver;
import org.puregxl.site.infra.model.ModelSelector;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatStreamService {

    private static final long SSE_TIMEOUT_MS = 180_000L;

    private final OkHttpClient infraAiOkHttpClient;

    private final ModelSelector modelSelector;

    private final AIModelProperties aiModelProperties;

    private final Gson gson = new Gson();

    private final Map<String, ActiveChatStream> activeStreams = new ConcurrentHashMap<>();

    public SseEmitter stream(ChatStreamRequest request) {
        ModelTarget modelTarget = selectModel(request);
        String streamId = StrUtil.blankToDefault(request.getStreamId(), UUID.randomUUID().toString());
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        Request httpRequest = buildHttpRequest(request, modelTarget);
        Call call = infraAiOkHttpClient.newCall(httpRequest);
        ActiveChatStream activeStream = new ActiveChatStream(call, emitter);

        ActiveChatStream previous = activeStreams.put(streamId, activeStream);
        if (previous != null) {
            previous.cancel();
        }

        emitter.onCompletion(() -> {
            activeStream.cancel();
            cleanup(streamId, activeStream);
        });
        emitter.onTimeout(() -> {
            activeStream.cancel();
            cleanup(streamId, activeStream);
        });
        emitter.onError(error -> {
            activeStream.cancel();
            cleanup(streamId, activeStream);
        });

        send(emitter, "open", Map.of(
                "streamId", streamId,
                "modelId", modelTarget.getId(),
                "provider", modelTarget.getCandidate().getProvider()
        ));

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                boolean cancelled = call.isCanceled() || activeStream.isClosed();
                cleanup(streamId, activeStream);
                if (cancelled) {
                    send(emitter, "cancelled", Map.of("streamId", streamId));
                    emitter.complete();
                    return;
                }
                log.warn("大模型流式调用失败, streamId={}, modelTarget={}", streamId, modelTarget, e);
                completeWithError(emitter, "大模型流式调用失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        String body = response.body() == null ? "" : response.body().string();
                        throw new RemoteException("大模型流式调用失败: HTTP " + response.code() + ", " + body,
                                BaseErrorCode.REMOTE_ERROR);
                    }
                    readStream(streamId, activeStream, emitter, response.body());
                } catch (Exception e) {
                    if (call.isCanceled() || activeStream.isClosed()) {
                        send(emitter, "cancelled", Map.of("streamId", streamId));
                        emitter.complete();
                        return;
                    }
                    log.warn("读取大模型流式响应失败, streamId={}, modelTarget={}", streamId, modelTarget, e);
                    completeWithError(emitter, e.getMessage());
                } finally {
                    cleanup(streamId, activeStream);
                }
            }
        });

        return emitter;
    }

    public boolean cancel(String streamId) {
        ActiveChatStream activeStream = activeStreams.remove(streamId);
        return activeStream != null && activeStream.cancel();
    }

    private ModelTarget selectModel(ChatStreamRequest request) {
        List<ModelTarget> candidates = modelSelector.selectChatCandidates(Boolean.TRUE.equals(request.getThinking()));
        if (candidates == null || candidates.isEmpty()) {
            throw new RemoteException("No available chat model candidates", BaseErrorCode.REMOTE_ERROR);
        }
        if (StrUtil.isBlank(request.getModelId())) {
            return candidates.get(0);
        }
        return candidates.stream()
                .filter(candidate -> request.getModelId().equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new RemoteException("No available chat model candidate for modelId=" + request.getModelId(),
                        BaseErrorCode.REMOTE_ERROR));
    }

    private Request buildHttpRequest(ChatStreamRequest request, ModelTarget modelTarget) {
        AIModelProperties.ProviderConfig provider = modelTarget.getProvider();
        String url = ModelUrlResolver.resolveUrl(provider, modelTarget.getCandidate(), ModelCapability.CHAT);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(buildRequestBody(request, modelTarget).toString(), HttpMediaTypes.JSON));

        if (provider != null && StrUtil.isNotBlank(provider.getApiKey())) {
            builder.addHeader("Authorization", "Bearer " + provider.getApiKey());
        }
        return builder.build();
    }

    private JsonObject buildRequestBody(ChatStreamRequest request, ModelTarget modelTarget) {
        JsonObject body = new JsonObject();
        body.addProperty("model", modelTarget.getCandidate().getModel());
        body.addProperty("stream", true);
        body.add("messages", buildMessages(request));

        addIfPresent(body, "temperature", request.getTemperature());
        addIfPresent(body, "top_p", request.getTopP());
        addIfPresent(body, "top_k", request.getTopK());
        addIfPresent(body, "max_tokens", request.getMaxTokens());
        if (Boolean.TRUE.equals(request.getThinking())) {
            body.addProperty("enable_thinking", true);
        }
        return body;
    }

    private JsonArray buildMessages(ChatStreamRequest request) {
        JsonArray messages = new JsonArray();
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            addMessage(messages, "system", request.getSystemPrompt());
        }
        if (request.getMessages() != null) {
            for (ChatMessage message : request.getMessages()) {
                if (message != null && message.getRole() != null && StrUtil.isNotBlank(message.getContent())) {
                    addMessage(messages, toOpenAiRole(message.getRole()), message.getContent());
                }
            }
        }
        if (StrUtil.isNotBlank(request.getUserPrompt())) {
            addMessage(messages, "user", request.getUserPrompt());
        }
        return messages;
    }

    private void addMessage(JsonArray messages, String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        messages.add(message);
    }

    private String toOpenAiRole(ChatMessage.Role role) {
        return switch (role) {
            case SYSTEM -> "system";
            case USER -> "user";
            case ASSISTANT -> "assistant";
        };
    }

    private void addIfPresent(JsonObject body, String key, Number value) {
        if (value != null) {
            body.addProperty(key, value);
        }
    }

    private void readStream(String streamId, ActiveChatStream activeStream, SseEmitter emitter, ResponseBody body) throws IOException {
        if (body == null) {
            throw new IOException("大模型响应体为空");
        }

        int chunkSize = Math.max(1, aiModelProperties.getStream().getMessageChunkSize());
        StringBuilder buffer = new StringBuilder();
        BufferedSource source = body.source();
        String line;
        while (!activeStream.isClosed() && (line = source.readUtf8Line()) != null) {
            if (StrUtil.isBlank(line) || !line.startsWith("data:")) {
                continue;
            }

            String data = line.substring("data:".length()).trim();
            if ("[DONE]".equals(data)) {
                flushDelta(emitter, buffer);
                send(emitter, "done", Map.of("streamId", streamId));
                emitter.complete();
                return;
            }

            String delta = extractDelta(data);
            if (StrUtil.isBlank(delta)) {
                continue;
            }
            buffer.append(delta);
            if (buffer.length() >= chunkSize) {
                flushDelta(emitter, buffer);
            }
        }

        if (activeStream.isClosed()) {
            send(emitter, "cancelled", Map.of("streamId", streamId));
        } else {
            flushDelta(emitter, buffer);
            send(emitter, "done", Map.of("streamId", streamId));
        }
        emitter.complete();
    }

    private String extractDelta(String data) {
        JsonObject root = gson.fromJson(data, JsonObject.class);
        if (root == null || !root.has("choices")) {
            return "";
        }
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        JsonObject choice = choices.get(0).getAsJsonObject();
        if (choice == null) {
            return "";
        }

        String content = readContent(choice, "delta");
        if (StrUtil.isNotBlank(content)) {
            return content;
        }
        return readContent(choice, "message");
    }

    private String readContent(JsonObject choice, String objectKey) {
        if (!choice.has(objectKey) || !choice.get(objectKey).isJsonObject()) {
            return "";
        }
        JsonObject data = choice.getAsJsonObject(objectKey);
        JsonElement content = data.get("content");
        if (content == null || content.isJsonNull()) {
            return "";
        }
        return content.getAsString();
    }

    private void flushDelta(SseEmitter emitter, StringBuilder buffer) {
        if (buffer.isEmpty()) {
            return;
        }
        String text = buffer.toString();
        buffer.setLength(0);
        send(emitter, "delta", Map.of("text", text));
    }

    private void completeWithError(SseEmitter emitter, String message) {
        send(emitter, "error", Map.of("message", StrUtil.blankToDefault(message, "大模型流式调用失败")));
        emitter.complete();
    }

    private void send(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void cleanup(String streamId, ActiveChatStream activeStream) {
        activeStream.close();
        activeStreams.remove(streamId, activeStream);
    }
}
