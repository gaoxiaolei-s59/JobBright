package org.puregxl.site.rag.dao.entity;

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
@TableName("resume_profile")
public class ResumeProfile {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历业务ID
     */
    private String resumeId;

    /**
     * 简历名称，例如：我的简历-JdFXGP (1)
     */
    private String resumeName;

    /**
     * 姓名
     */
    private String name;

    /**
     * 职位标题
     */
    private String title;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 所在地
     */
    private String location;

    /**
     * LinkedIn展示文本
     */
    private String linkedinText;

    /**
     * LinkedIn链接
     */
    private String linkedinUrl;

    /**
     * GitHub展示文本
     */
    private String githubText;

    /**
     * GitHub链接
     */
    private String githubUrl;

    /**
     * 其他链接展示文本
     */
    private String otherLinkText;

    /**
     * 其他链接地址
     */
    private String otherLinkUrl;

    /**
     * 最后分析时间
     */
    private Date lastAnalyzeTime;

    /**
     * 分析状态：NOT_ANALYZED未分析，ANALYZED已分析，PROCESSING分析中
     */
    private String analyzeStatus;

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
