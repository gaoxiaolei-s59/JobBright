

package org.puregxl.site.infra.embedding;


import okhttp3.OkHttpClient;
import org.puregxl.site.infra.enums.ModelProvider;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiliconFlowEmbeddingClient extends AbstractOpenAIStyleEmbeddingClient {

    public SiliconFlowEmbeddingClient(OkHttpClient syncHttpClient) {
        super(syncHttpClient);
    }

    @Override
    public String provider() {
        return ModelProvider.SILICON_FLOW.getId();
    }

    @Override
    public List<Float> embed(String text, ModelTarget target) {
        return doEmbed(text, target);
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts, ModelTarget target) {
        return doEmbedBatch(texts, target);
    }

    @Override
    protected int maxBatchSize() {
        return 32;
    }
}
