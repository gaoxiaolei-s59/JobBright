package org.puregxl.site.infra.chat;

import org.puregxl.site.infra.convention.ChatMessage;
import org.puregxl.site.infra.convention.ChatRequest;

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


}
