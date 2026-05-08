package org.puregxl.site.infra.chat;

import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.convention.ChatResult;

import java.util.List;

public interface LLMService {

    /**
     * 同步调用简单方法
     * @param userPrompt
     * @return
     */
    default String doChat(String userPrompt) {
        return doChat(ChatRequest.builder()
                .messages(List.of(ChatMessage.user(userPrompt)))
                .build());
    };

    /**
     * 同步调用
     * @param chatRequest
     * @return
     */
    String doChat(ChatRequest chatRequest);

    /**
     * 同步调用，并返回实际命中的模型信息。
     *
     * @param chatRequest
     * @return
     */
    ChatResult doChatWithResult(ChatRequest chatRequest);


    /**
     * 根据modelid调用
     * @param chatRequest
     * @param modelId
     * @return
     */
    String doChat(ChatRequest chatRequest, String modelId);

    /**
     * 根据modelid调用，并返回实际命中的模型信息。
     *
     * @param chatRequest
     * @param modelId
     * @return
     */
    ChatResult doChatWithResult(ChatRequest chatRequest, String modelId);

}
