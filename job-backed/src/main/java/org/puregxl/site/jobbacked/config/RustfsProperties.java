package org.puregxl.site.jobbacked.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rustfs")
public class RustfsProperties {

    /**
     * 访问地址
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
    private String bucketName = "default";

    /**
     * 是否自动创建桶
     */
    private Boolean createBucketIfMissing = true;
}