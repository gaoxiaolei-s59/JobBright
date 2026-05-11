package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("resume_certification")
public class ResumeCertification {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 简历业务ID
     */
    private String resumeId;

    /**
     * 类型：CERTIFICATE证书，AWARD奖项
     */
    private String itemType;

    /**
     * 证书/奖项名称
     */
    private String name;

    /**
     * 颁发机构/主办方
     */
    private String issuer;

    /**
     * 获得时间/颁发时间
     */
    private String issueDate;

    /**
     * 证书/奖项说明
     */
    private String description;

    /**
     * 证书/奖项链接
     */
    private String credentialUrl;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：ACTIVE启用，INACTIVE停用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标识：0未删除，1已删除
     */
    private Integer delFlag;
}
