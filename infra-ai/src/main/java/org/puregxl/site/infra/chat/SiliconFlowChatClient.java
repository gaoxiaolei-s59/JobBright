package org.puregxl.site.infra.chat;

import okhttp3.OkHttpClient;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.enums.ModelProvider;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;

@Service
public class SiliconFlowChatClient extends AbstractChatClient{

    public SiliconFlowChatClient(OkHttpClient syncHttpClient) {
        super(syncHttpClient);
    }

    @Override
    public String provider() {
        return ModelProvider.SILICON_FLOW.getId();
    }

    @Override
    public String chat(ChatRequest chatRequest, ModelTarget modelTarget) {
        return doChat(chatRequest, modelTarget);
    }
}
