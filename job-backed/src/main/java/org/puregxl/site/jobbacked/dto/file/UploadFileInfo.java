package org.puregxl.site.jobbacked.dto.file;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadFileInfo {

    /**
     * 对象存储桶名称
     */
    private String bucketName;

    /**
     * 对象存储中的文件路径
     */
    private String objectKey;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 文件访问地址
     */
    private String objectUrl;

    /**
     * 文件二进制内容
     */
    private byte[] content;
}
