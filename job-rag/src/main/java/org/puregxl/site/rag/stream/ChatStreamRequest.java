package org.puregxl.site.rag.stream;

import lombok.Data;
import org.puregxl.site.infra.convention.ChatMessage;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatStreamRequest {

    /**
     * 前端可传入自己的流ID，便于取消；不传则由服务端生成并通过首个SSE事件返回。
     */
    private String streamId;

    /**
     * 可选模型ID，不传则使用配置中的默认候选模型。
     */
    private String modelId;

    private String systemPrompt;

    private String userPrompt;

    private Double temperature;

    private Double topP;

    private Integer topK;

    private Integer maxTokens;

    private Boolean thinking;

    private List<ChatMessage> messages = new ArrayList<>();
}
