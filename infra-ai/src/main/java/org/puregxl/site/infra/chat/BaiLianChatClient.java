package org.puregxl.site.infra.chat;

import okhttp3.OkHttpClient;
import org.puregxl.site.infra.convention.ChatRequest;
import org.puregxl.site.infra.enums.ModelProvider;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;

@Service
public class BaiLianChatClient extends AbstractChatClient{

    public BaiLianChatClient(OkHttpClient syncHttpClient) {
        super(syncHttpClient);
    }

    @Override
    public String provider() {
        return ModelProvider.BAI_LIAN.getId();
    }

    @Override
    public String chat(ChatRequest chatRequest, ModelTarget modelTarget) {
        return doChat(chatRequest, modelTarget);
    }
}
