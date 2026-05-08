package org.puregxl.site.rag.stream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatStreamCancelResponse {

    private String streamId;

    private boolean cancelled;
}
