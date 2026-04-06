package org.puregxl.site.jobbacked.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("job_post")
public class JobPost {

    /** 主键ID */
    private Long id;

    /** 职位业务ID */
    private String jobId;

    /** 公司业务ID */
    private String companyId;

    /** 职位名称 */
    private String title;

    /** 职位简介 */
    private String jobSummary;

    /** 职位详情描述 */
    private String jobDescription;

    /** 角色类别 */
    private String roleCategory;

    /** 角色标签，JSON数组或逗号分隔 */
    private String roleTags;

    /** 技能标签，JSON数组或逗号分隔 */
    private String skillTags;

    /** 行业标签，JSON数组或逗号分隔 */
    private String industryTags;

    /** 福利标签，JSON数组或逗号分隔 */
    private String benefitTags;

    /** 岗位亮点标签，JSON数组或逗号分隔 */
    private String highlightTags;

    /** 工作地点 */
    private String location;

    /** 城市 */
    private String city;

    /** 区县/区域 */
    private String district;

    /** 办公方式 */
    private String workMode;

    /** 用工类型 */
    private String employmentType;

    /** 学历要求 */
    private String educationRequirement;

    /** 优先专业 */
    private String preferredMajor;

    /** 经验等级枚举值：STUDENT / NEW_GRAD / JUNIOR */
    private String experienceLevel;

    /** 最低经验年限 */
    private Integer minExperienceYears;

    /** 最高经验年限 */
    private Integer maxExperienceYears;

    /** 实习时长要求（月） */
    private Integer internshipMonths;

    /** 月薪下限 */
    private Integer salaryMinMonthly;

    /** 月薪上限 */
    private Integer salaryMaxMonthly;

    /** 年薪月数 */
    private Integer salaryMonths;

    /** 投递人数 */
    private Integer applicantCount;

    /** 投递链接 */
    private String applyUrl;

    /** 发布时间 */
    private Date postedAt;

    /** 职位状态枚举值：ONLINE / OFFLINE / DRAFT / EXPIRED */
    private String status;

    /** 国家 */
    private String country;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    private Integer delFlag;
}
