package org.puregxl.site.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rustfs")
public class RustfsProperties {

    /**
     * RustFS/S3 访问地址
     */
    private String url;

    /**
     * Access Key
     */
    private String accessKeyId;

    /**
     * Secret Key
     */
    private String secretAccessKey;


    /**
     * 若桶不存在是否自动创建
     */
    private Boolean createBucketIfMissing = true;
}
