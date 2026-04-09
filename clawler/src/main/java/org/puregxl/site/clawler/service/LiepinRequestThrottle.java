package org.puregxl.site.clawler.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LiepinRequestThrottle {

    private final AtomicLong lastRequestAt = new AtomicLong(0);

    public void acquire(long minIntervalMillis) {
        long now = System.currentTimeMillis();
        long previous = lastRequestAt.get();
        long waitMillis = previous + minIntervalMillis - now;
        if (waitMillis > 0) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("请求被中断", exception);
            }
        }
        lastRequestAt.set(System.currentTimeMillis());
    }
}
