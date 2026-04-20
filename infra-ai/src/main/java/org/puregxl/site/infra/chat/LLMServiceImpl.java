package org.puregxl.site.infra.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.errorcode.BaseErrorCode;
import org.puregxl.site.framework.exception.RemoteException;
import org.puregxl.site.infra.convention.ChatRequest;
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
public class LLMServiceImpl implements LLMService {

    private final ModelSelector modelSelector;
    private final ModelHealthStore modelHealthStore;
    private final Map<String, ChatClient> clientMap;

    public LLMServiceImpl(ModelSelector modelSelector, ModelHealthStore modelHealthStore, List<ChatClient> chatClients) {
        this.modelSelector = modelSelector;
        this.modelHealthStore = modelHealthStore;
        clientMap = chatClients.stream().collect(Collectors.toMap(ChatClient::provider, Function.identity()));
    }

    @Override
    public String doChat(ChatRequest request) {
        List<ModelTarget> modelTargets = modelSelector.selectChatCandidates(request.getThinking());

        if (modelTargets == null || modelTargets.isEmpty()) {
            throw new RemoteException("No available chat model candidates", BaseErrorCode.REMOTE_ERROR);
        }

        for (ModelTarget modelTarget : modelTargets) {
            String modelTargetId = modelTarget.getId();
            String provider = modelTarget.getCandidate().getProvider();

            ChatClient chatClient = clientMap.get(provider);
            if (chatClient == null) {
                log.warn("未找到对应的ChatClient, provider={}, modelTarget={}", provider, modelTarget);
                continue;
            }

            if (!modelHealthStore.allowCall(modelTargetId)) {
                log.warn("模型当前不可调用, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget);
                continue;
            }

            try {
                String result = chatClient.chat(request, modelTarget);
                modelHealthStore.markSuccess(modelTargetId);
                return result;
            } catch (Exception e) {
                log.warn("大模型调用失败, modelTargetId={}, modelTarget={}", modelTargetId, modelTarget, e);
                modelHealthStore.markFailure(modelTargetId);
            }
        }

        throw new RemoteException("ALL CHAT Model Fail, messages", BaseErrorCode.REMOTE_ERROR);
    }
}
