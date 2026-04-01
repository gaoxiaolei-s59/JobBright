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

    /** 工作地点 */
    private String location;

    /** 办公方式 */
    private String workMode;

    /** 用工类型 */
    private String employmentType;

    /** 经验等级枚举值：STUDENT / NEW_GRAD / JUNIOR */
    private String experienceLevel;

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
