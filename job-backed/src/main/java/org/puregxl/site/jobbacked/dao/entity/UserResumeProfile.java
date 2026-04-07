package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户简历画像表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_resume_profile")
public class UserResumeProfile {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 画像业务唯一标识
     */
    private String profileId;

    /**
     * 关联简历resume_id
     */
    private String resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 目标岗位列表
     */
    private String targetRoles;

    /**
     * 职级，如实习/初级/中级
     */
    private String seniority;

    /**
     * 学历层级
     */
    private String educationLevel;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 学校标签，如985/211/双一流
     */
    private String schoolTags;

    /**
     * 核心技能列表
     */
    private String coreSkills;

    /**
     * 项目标签列表
     */
    private String projectTags;

    /**
     * 行业标签列表
     */
    private String industryTags;

    /**
     * 优势标签列表
     */
    private String strengths;

    /**
     * 意向城市列表
     */
    private String preferredCities;

    /**
     * 求职类型，如实习/校招/社招
     */
    private String preferredJobType;

    /**
     * 可实习时长
     */
    private String internshipMonths;

    /**
     * 薪资期望
     */
    private String salaryExpectation;

    /**
     * 工作年限/实习年限描述
     */
    private String workYears;

    /**
     * 简历摘要
     */
    private String resumeSummary;

    /**
     * 完整画像JSON
     */
    private String profileJson;

    /**
     * 生成画像的模型
     */
    private String llmModel;

    /**
     * Prompt版本
     */
    private String promptVersion;

    /**
     * 状态 INIT/SUCCESS/FAILED
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识 0-未删除 1-已删除
     */
    private Integer delFlag;
}