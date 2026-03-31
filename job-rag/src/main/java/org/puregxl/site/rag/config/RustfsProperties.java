package org.puregxl.site.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rustfs")
public class RustfsProperties {

    /**
     * S3 兼容访问地址
     */
    private String url;

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String secretAccessKey;

    /**
     * 默认文件桶
     */
    private String bucketName = "job-rag";

    /**
     * 是否自动创建桶
     */
    private Boolean createBucketIfMissing = true;
}
