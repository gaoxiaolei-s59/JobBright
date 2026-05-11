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
@TableName("resume_analysis_issue")
public class ResumeAnalysisIssue {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 简历业务ID
     */
    private String resumeId;

    /**
     * 分析报告ID
     */
    private Long reportId;

    /**
     * 分析分组ID
     */
    private Long sectionId;

    /**
     * 问题等级：URGENT紧急，CRITICAL重要，OPTIONAL可选
     */
    private String issueLevel;

    /**
     * 问题类型：LACK_OF_ACCOMPLISHMENT、MISSING_SUMMARY等
     */
    private String issueType;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 关联问题数量
     */
    private Integer relatedCount;

    /**
     * 问题说明
     */
    private String description;

    /**
     * 优化建议
     */
    private String suggestion;

    /**
     * 修改示例
     */
    private String example;

    /**
     * 严重程度分数：1-100
     */
    private Integer severityScore;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 处理状态：PENDING待处理，APPLIED已采纳，IGNORED已忽略
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
