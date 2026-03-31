package org.puregxl.site.rag.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("rag_file")
public class RagFile {

    /** 主键ID */
    private Long id;

    /** 文件业务唯一标识 */
    private String fileId;

    /** 原始文件名 */
    private String fileName;

    /** 文件后缀 */
    private String fileExt;

    /** 文件类型 */
    private String contentType;

    /** 文件大小(byte) */
    private Long fileSize;

    /** 对象存储桶名 */
    private String bucketName;

    /** 对象存储路径 */
    private String objectKey;

    /** 文件访问地址 */
    private String objectUrl;

    /** 文件状态 */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    private Integer delFlag;
}
