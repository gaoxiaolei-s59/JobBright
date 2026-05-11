package org.puregxl.site.infra.convention;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatClientResult {

    private String content;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;
}
