package org.puregxl.site.infra.chat;

import org.puregxl.site.infra.annotation.LlmCallLogged;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.convention.ChatResult;
import org.puregxl.site.infra.model.ModelTarget;

public interface ChatClient {
    /**
     * 获取配置商
     * @return
     */
    String provider();

    /**
     * 发起一次调用并返回内容、模型和 usage 信息
     *
     * @param chatRequest
     * @param modelTarget
     * @return
     */
    @LlmCallLogged
    ChatResult doChat(ChatRequest chatRequest, ModelTarget modelTarget);
}
