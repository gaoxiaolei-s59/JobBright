package org.puregxl.site.infra.chat;

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
    String chat(ChatRequest chatRequest, ModelTarget modelTarget);
}
