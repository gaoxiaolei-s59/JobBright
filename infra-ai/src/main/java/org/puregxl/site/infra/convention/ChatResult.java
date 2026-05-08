package org.puregxl.site.infra.convention;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResult {

    private String content;

    private String modelId;

    private String provider;

    private String model;
}
