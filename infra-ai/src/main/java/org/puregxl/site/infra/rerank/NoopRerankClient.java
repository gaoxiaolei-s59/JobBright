package org.puregxl.site.infra.rerank;

import org.puregxl.site.infra.convention.RetrievedChunk;
import org.puregxl.site.infra.enums.ModelProvider;
import org.puregxl.site.infra.model.ModelTarget;

import java.util.List;

public class NoopRerankClient implements RerankClient{
    @Override
    public String provider() {
        return ModelProvider.NOOP.getId();
    }

    @Override
    public List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates, int topN, ModelTarget modelTarget) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        if (topN <= 0 || candidates.size() <= topN) {
            return candidates;
        }

        return candidates.stream()
                .limit(topN)
                .toList();
    }
}
