package org.puregxl.site.jobbacked.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "job.semaphore")
public class SemaphoreProperties {
    /**
     * Redisson 信号量名称
     */
    @NotBlank
    private String name = "job:upload";

    /**
     * 最大并发数
     */
    @Min(1)
    private Integer maxConcurrent = 10;

    /**
     * 获取许可最大等待时间（秒）
     */
    @Min(0)
    private Integer maxWaitSeconds = 30;

    /**
     * permit 自动释放时间（秒）
     */
    @Min(1)
    private Integer leaseSeconds = 30;
}
