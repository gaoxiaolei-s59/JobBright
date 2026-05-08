package org.puregxl.site.rag.stream;

import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
class ActiveChatStream {

    private final Call call;

    private final SseEmitter emitter;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    boolean cancel() {
        if (!closed.compareAndSet(false, true)) {
            return false;
        }
        call.cancel();
        emitter.complete();
        return true;
    }

    void close() {
        closed.set(true);
    }

    boolean isClosed() {
        return closed.get();
    }
}
