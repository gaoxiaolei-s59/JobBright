package org.puregxl.site.rag.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResponse {

    /**
     * 文件业务ID
     */
    private String fileId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 对象存储Key
     */
    private String objectKey;

    /**
     * 文件访问地址
     */
    private String objectUrl;

    /**
     * 上传状态
     */
    private String status;
}
