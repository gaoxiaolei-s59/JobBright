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
@TableName("resume_analysis_report")
public class ResumeAnalysisReport {

    /**
     * 主键ID
     */
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
     * 简历分数：0-100
     */
    private Integer scoreValue;

    /**
     * 评分等级，例如：A、B、C、D
     */
    private String scoreGrade;

    /**
     * 评分描述：EXCELLENT、GOOD、NORMAL、WEAK
     */
    private String scoreLevel;

    /**
     * 整体分析总结
     */
    private String analysisSummary;

    /**
     * 紧急问题数量
     */
    private Integer urgentIssueCount;

    /**
     * 重要问题数量
     */
    private Integer criticalIssueCount;

    /**
     * 可选问题数量
     */
    private Integer optionalIssueCount;

    /**
     * 分析状态：PROCESSING处理中，ANALYZED已分析，FAILED失败
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
