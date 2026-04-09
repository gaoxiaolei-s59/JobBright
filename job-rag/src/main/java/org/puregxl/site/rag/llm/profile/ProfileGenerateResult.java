package org.puregxl.site.rag.llm.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileGenerateResult {
    String parseResult;
    String model;
}
