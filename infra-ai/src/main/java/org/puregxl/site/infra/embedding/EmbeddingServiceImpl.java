package org.puregxl.site.infra.embedding;

import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.errorcode.BaseErrorCode;
import org.puregxl.site.framework.exception.RemoteException;
import org.puregxl.site.infra.model.ModelHealthStore;
import org.puregxl.site.infra.model.ModelSelector;
import org.puregxl.site.infra.model.ModelTarget;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService{
    private final ModelSelector modelSelector;
    private final ModelHealthStore modelHealthStore;
    private final Map<String, EmbeddingClient> clientMap;

    public EmbeddingServiceImpl(ModelSelector modelSelector,
                                ModelHealthStore modelHealthStore,
                                List<EmbeddingClient> embeddingClients) {
        this.modelSelector = modelSelector;
        this.modelHealthStore = modelHealthStore;
        this.clientMap = embeddingClients.stream()
                .collect(Collectors.toMap(EmbeddingClient::provider, Function.identity()));
    }

    @Override
    public List<Float> embed(String text) {
        List<ModelTarget> modelTargets = modelSelector.selectEmbeddingCandidates();
        if (modelTargets == null || modelTargets.isEmpty()) {
            throw new RemoteException("No available embedding model candidates", BaseErrorCode.REMOTE_ERROR);
        }
        return executeEmbed(text, modelTargets);
    }

    @Override
    public List<Float> embed(String text, String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return embed(text);
        }
        List<ModelTarget> modelTargets = modelSelector.selectEmbeddingCandidates().stream()
                .filter(each -> modelId.equals(each.getId()))
                .toList();
        if (modelTargets.isEmpty()) {
            throw new RemoteException("No available embedding model candidate for modelId=" + modelId, BaseErrorCode.REMOTE_ERROR);
        }
        return executeEmbed(text, modelTargets);
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        List<ModelTarget> modelTargets = modelSelector.selectEmbeddingCandidates();
        if (modelTargets == null || modelTargets.isEmpty()) {
            throw new RemoteException("No available embedding model candidates", BaseErrorCode.REMOTE_ERROR);
        }
        return executeEmbedBatch(texts, modelTargets);
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts, String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return embedBatch(texts);
        }
        List<ModelTarget> modelTargets = modelSelector.selectEmbeddingCandidates().stream()
                .filter(each -> modelId.equals(each.getId()))
                .toList();
        if (modelTargets.isEmpty()) {
            throw new RemoteException("No available embedding model candidate for modelId=" + modelId, BaseErrorCode.REMOTE_ERROR);
        }
        return executeEmbedBatch(texts, modelTargets);
    }

    @Override
    public int dimension() {
        List<ModelTarget> modelTargets = modelSelector.selectEmbeddingCandidates();
        if (modelTargets == null || modelTargets.isEmpty()) {
            return 0;
        }
        Integer dimension = modelTargets.get(0).getCandidate().getDimension();
        return dimension == null ? 0 : dimension;
    }

    private List<Float> executeEmbed(String text, List<ModelTarget> modelTargets) {
        for (ModelTarget modelTarget : modelTargets) {
            String modelTargetId = modelTarget.getId();
            String provider = modelTarget.getCandidate().getProvider();
            EmbeddingClient embeddingClient = clientMap.get(provider);
            if (embeddingClient == null) {
                log.warn("未找到对应的EmbeddingClient, provider={}, modelTarget={}", provider, modelTarget);
                continue;
            }
            if (!modelHealthStore.allowCall(modelTargetId)) {
                log.warn("Embedding模型当前不可调用, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget);
                continue;
            }
            try {
                List<Float> result = embeddingClient.embed(text, modelTarget);
                modelHealthStore.markSuccess(modelTargetId);
                return result;
            } catch (Exception exception) {
                log.warn("Embedding调用失败, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget, exception);
                modelHealthStore.markFailure(modelTargetId);
            }
        }
        throw new RemoteException("ALL EMBEDDING Model Fail", BaseErrorCode.REMOTE_ERROR);
    }

    private List<List<Float>> executeEmbedBatch(List<String> texts, List<ModelTarget> modelTargets) {
        for (ModelTarget modelTarget : modelTargets) {
            String modelTargetId = modelTarget.getId();
            String provider = modelTarget.getCandidate().getProvider();
            EmbeddingClient embeddingClient = clientMap.get(provider);
            if (embeddingClient == null) {
                log.warn("未找到对应的EmbeddingClient, provider={}, modelTarget={}", provider, modelTarget);
                continue;
            }
            if (!modelHealthStore.allowCall(modelTargetId)) {
                log.warn("Embedding模型当前不可调用, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget);
                continue;
            }
            try {
                List<List<Float>> result = embeddingClient.embedBatch(texts, modelTarget);
                modelHealthStore.markSuccess(modelTargetId);
                return result;
            } catch (Exception exception) {
                log.warn("Embedding批量调用失败, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget, exception);
                modelHealthStore.markFailure(modelTargetId);
            }
        }
        throw new RemoteException("ALL EMBEDDING Model Fail", BaseErrorCode.REMOTE_ERROR);
    }
}
