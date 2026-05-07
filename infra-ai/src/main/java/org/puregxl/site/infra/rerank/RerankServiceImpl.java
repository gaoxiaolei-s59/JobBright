package org.puregxl.site.infra.rerank;

import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.errorcode.BaseErrorCode;
import org.puregxl.site.framework.exception.RemoteException;
import org.puregxl.site.infra.convention.RetrievedChunk;
import org.puregxl.site.infra.model.ModelHealthStore;
import org.puregxl.site.infra.model.ModelSelector;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<ModelTarget> modelTargets = modelSelector.selectRerankCandidates();
        if (modelTargets == null || modelTargets.isEmpty()) {
            throw new RemoteException("No available rerank model candidates", BaseErrorCode.REMOTE_ERROR);
        }

        for (ModelTarget modelTarget : modelTargets) {
            String modelTargetId = modelTarget.getId();
            String provider = modelTarget.getCandidate().getProvider();
            RerankClient rerankClient = map.get(provider);
            if (rerankClient == null) {
                log.warn("未找到对应的RerankClient, provider={}, modelTarget={}", provider, modelTarget);
                continue;
            }

            if (!modelHealthStore.allowCall(modelTargetId)) {
                log.warn("Rerank模型当前不可调用, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget);
                continue;
            }

            try {
                List<RetrievedChunk> result = rerankClient.rerank(query, candidates, topN, modelTarget);
                modelHealthStore.markSuccess(modelTargetId);
                return result;
            } catch (Exception exception) {
                log.warn("Rerank调用失败, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget, exception);
                modelHealthStore.markFailure(modelTargetId);
            }
        }

        throw new RemoteException("ALL RERANK Model Fail", BaseErrorCode.REMOTE_ERROR);
    }
}
