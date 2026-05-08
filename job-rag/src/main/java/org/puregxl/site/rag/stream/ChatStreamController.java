package org.puregxl.site.rag.stream;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag/stream")
public class ChatStreamController {

    private final ChatStreamService chatStreamService;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatStreamRequest request) {
        return chatStreamService.stream(request);
    }

    @DeleteMapping("/chat/{streamId}")
    public Result<ChatStreamCancelResponse> cancel(@PathVariable String streamId) {
        boolean cancelled = chatStreamService.cancel(streamId);
        return Results.success(new ChatStreamCancelResponse(streamId, cancelled));
    }
}
