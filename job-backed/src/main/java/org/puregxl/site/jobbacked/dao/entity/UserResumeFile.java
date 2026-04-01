package org.puregxl.site.jobbacked.dao.entity;

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
@TableName("user_resume_file")
public class UserResumeFile {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 简历ID */
    private String resumeId;

    /** 原始文件名 */
    private String fileName;

    /** 文件后缀 */
    private String fileExt;

    /** 文件类型 */
    private String contentType;

    /** 文件大小(byte) */
    private Long fileSize;

    /** 对象存储路径 */
    private String objectKey;

    /** 文件访问地址 */
    private String objectUrl;

    /** 是否当前生效版本 0-否 1-是 */
    private Integer isCurrent;

    /** 用户简历分数 */
    private Double score;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    private Integer delFlag;
}
