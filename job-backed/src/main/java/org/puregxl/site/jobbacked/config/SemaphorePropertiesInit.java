package org.puregxl.site.jobbacked.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SemaphorePropertiesInit {

    private final RedissonClient redissonClient;
    private final SemaphoreProperties semaphoreProperties;

    /**
     * 初始化信号量
     */
    @PostConstruct
    public void documentUploadSemaphoreInitialize() {
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(semaphoreProperties.getName());
        boolean initialized = semaphore.trySetPermits(semaphoreProperties.getMaxConcurrent());
        if (initialized) {
            log.info("Initialized document upload semaphore: name={}, maxConcurrent={}",
                    semaphoreProperties.getName(),
                    semaphoreProperties.getMaxConcurrent());
            return;
        }
        log.info("Document upload semaphore already exists, skip resetting permits: name={}, configuredMaxConcurrent={}, currentPermits={}",
                semaphoreProperties.getName(),
                semaphoreProperties.getMaxConcurrent(),
                semaphore.getPermits());
    }
}
