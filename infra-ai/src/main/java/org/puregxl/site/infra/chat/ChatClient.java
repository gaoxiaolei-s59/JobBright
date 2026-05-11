package org.puregxl.site.infra.chat;

import org.puregxl.site.infra.annotation.LlmCallLogged;
import org.puregxl.site.infra.convention.ChatClientResult;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.model.ModelTarget;

public interface ChatClient {
    /**
     * 获取配置商
     * @return
     */
    String provider();

    /**
     * 发起一次调用
     * @param chatRequest
     * @param modelTarget
     * @return
     */
    default String chat(ChatRequest chatRequest, ModelTarget modelTarget) {
        return chatWithResult(chatRequest, modelTarget).getContent();
    }

    /**
     * 发起一次调用并返回模型 usage 信息
     *
     * @param chatRequest
     * @param modelTarget
     * @return
     */
    @LlmCallLogged
    ChatClientResult chatWithResult(ChatRequest chatRequest, ModelTarget modelTarget);
}
