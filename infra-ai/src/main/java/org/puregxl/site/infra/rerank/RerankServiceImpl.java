package org.puregxl.site.infra.rerank;

import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.Model;
import org.puregxl.site.infra.convention.RetrievedChunk;
import org.puregxl.site.infra.model.ModelHealthStore;
import org.puregxl.site.infra.model.ModelSelector;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RerankServiceImpl implements RerankService{

    private final Map<String, RerankClient> map;
    private final ModelSelector modelSelector;
    private final ModelHealthStore modelHealthStore;
    public RerankServiceImpl(ModelSelector modelSelector, List<RerankClient> rerankClients, ModelHealthStore modelHealthStore) {
        this.modelSelector = modelSelector;
        this.modelHealthStore = modelHealthStore;
        this.map = rerankClients.stream()
                .collect(Collectors.toMap(RerankClient::provider, Function.identity()));
    }

    @Override
    public List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates, int topN) {
        List<ModelTarget> modelTargets = modelSelector.selectRerankCandidates();
        for (ModelTarget modelTarget : modelTargets) {
            if (modelHealthStore.allowCall(modelTarget.getId())) {

            }
        }

        return List.of();
    }
}
