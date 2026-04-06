package org.puregxl.site.jobbacked.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendJobResponse {

    /**
     * 职位业务ID
     */
    private String jobId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司Logo地址
     */
    private String companyLogo;

    /**
     * 职位标题
     */
    private String title;

    /**
     * 职位补充信息
     */
    private String meta;

    /**
     * 发布时间描述
     */
    private String postedAt;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 城市
     */
    private String city;

    /**
     * 区域
     */
    private String district;

    /**
     * 工作模式
     */
    private String workMode;

    /**
     * 用工类型
     */
    private String employmentType;

    /**
     * 经验等级枚举值：STUDENT / NEW_GRAD / JUNIOR
     */
    private String experienceLevel;

    /**
     * 学历要求
     */
    private String educationRequirement;

    /**
     * 优先专业
     */
    private String preferredMajor;

    /**
     * 角色类别
     */
    private String roleCategory;

    /**
     * 实习时长要求（月）
     */
    private Integer internshipMonths;

    /**
     * 职位摘要
     */
    private String jobSummary;

    /**
     * 技能标签
     */
    private List<String> skillTags;

    /**
     * 岗位亮点
     */
    private List<String> highlightTags;

    /**
     * 投递人数
     */
    private Integer applicantCount;

    /**
     * 匹配分数
     */
    private Integer matchScore;

    /**
     * 匹配标签
     */
    private String matchLabel;

    /**
     * 匹配原因
     */
    private String matchReason;

    /**
     * 投递链接
     */
    private String applyUrl;

    /**
     * 是否已收藏
     */
    private Boolean liked;

    /**
     * 是否已投递
     */
    private Boolean applied;
}
