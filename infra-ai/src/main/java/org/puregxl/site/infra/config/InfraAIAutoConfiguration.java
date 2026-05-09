package org.puregxl.site.infra.config;

import okhttp3.OkHttpClient;
import org.puregxl.site.infra.chat.BaiLianChatClient;
import org.puregxl.site.infra.chat.ChatClient;
import org.puregxl.site.infra.chat.LLMService;
import org.puregxl.site.infra.chat.LLMServiceImpl;
import org.puregxl.site.infra.chat.SiliconFlowChatClient;
import org.puregxl.site.infra.embedding.EmbeddingClient;
import org.puregxl.site.infra.embedding.EmbeddingService;
import org.puregxl.site.infra.embedding.EmbeddingServiceImpl;
import org.puregxl.site.infra.embedding.SiliconFlowEmbeddingClient;
import org.puregxl.site.infra.model.ModelHealthStore;
import org.puregxl.site.infra.model.ModelSelector;
import org.puregxl.site.infra.rerank.BaiLianRerankClient;
import org.puregxl.site.infra.rerank.RerankClient;
import org.puregxl.site.infra.rerank.RerankService;
import org.puregxl.site.infra.rerank.RerankServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(AIModelProperties.class)
public class InfraAIAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient infraAiOkHttpClient(AIModelProperties aiModelProperties) {
        AIModelProperties.Http http = aiModelProperties.getHttp();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(http.getConnectTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(http.getReadTimeoutSeconds()))
                .writeTimeout(Duration.ofSeconds(http.getWriteTimeoutSeconds()));
        if (http.getCallTimeoutSeconds() != null && http.getCallTimeoutSeconds() > 0) {
            builder.callTimeout(Duration.ofSeconds(http.getCallTimeoutSeconds()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelHealthStore modelHealthStore(AIModelProperties aiModelProperties) {
        return new ModelHealthStore(aiModelProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelSelector modelSelector(AIModelProperties aiModelProperties, ModelHealthStore modelHealthStore) {
        return new ModelSelector(aiModelProperties, modelHealthStore);
    }

    @Bean
    @ConditionalOnMissingBean(name = "baiLianChatClient")
    public BaiLianChatClient baiLianChatClient(OkHttpClient infraAiOkHttpClient) {
        return new BaiLianChatClient(infraAiOkHttpClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "siliconFlowChatClient")
    public SiliconFlowChatClient siliconFlowChatClient(OkHttpClient infraAiOkHttpClient) {
        return new SiliconFlowChatClient(infraAiOkHttpClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "siliconFlowEmbeddingClient")
    public SiliconFlowEmbeddingClient siliconFlowEmbeddingClient(OkHttpClient infraAiOkHttpClient) {
        return new SiliconFlowEmbeddingClient(infraAiOkHttpClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "baiLianRerankClient")
    public BaiLianRerankClient baiLianRerankClient(OkHttpClient infraAiOkHttpClient) {
        return new BaiLianRerankClient(infraAiOkHttpClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public LLMService llmService(ModelSelector modelSelector,
                                 ModelHealthStore modelHealthStore,
                                 List<ChatClient> chatClients) {
        return new LLMServiceImpl(modelSelector, modelHealthStore, chatClients);
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(ModelSelector modelSelector,
                                             ModelHealthStore modelHealthStore,
                                             List<EmbeddingClient> embeddingClients) {
        return new EmbeddingServiceImpl(modelSelector, modelHealthStore, embeddingClients);
    }

    @Bean
    @ConditionalOnMissingBean
    public RerankService rerankService(ModelSelector modelSelector,
                                       ModelHealthStore modelHealthStore,
                                       List<RerankClient> rerankClients) {
        return new RerankServiceImpl(modelSelector, rerankClients, modelHealthStore);
    }
}
