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
@TableName("user_resume_analysis")
public class UserResumeAnalysis {

    /** 主键ID */
    private Long id;

    /** 简历ID */
    private String resumeId;

    /** 用户ID */
    private Long userId;

    /** 简历评分 */
    private Integer scoreValue;

    /** 评分等级 */
    private String grade;

    /** 评分标签 */
    private String label;

    /** 紧急修复项数量 */
    private Integer urgentFixCount;

    /** 关键修复项数量 */
    private Integer criticalFixCount;

    /** 可选优化项数量 */
    private Integer optionalFixCount;

    /** 基础画像JSON */
    private String profileJson;

    /** 技能分组JSON */
    private String skillGroupsJson;

    /** 项目经历JSON */
    private String projectsJson;

    /** 分析亮点JSON */
    private String highlightsJson;

    /** 问题列表JSON */
    private String issuesJson;

    /** 分析摘要 */
    private String analysisSummary;

    /** 完整分析JSON */
    private String rawAnalysisJson;

    /** 状态 INIT/SUCCESS/FAILED */
    private String status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    private Integer delFlag;
}
