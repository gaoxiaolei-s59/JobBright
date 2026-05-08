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
@TableName("user_resume_analysis")
public class UserResumeAnalysis {

    private Long id;
    private String resumeId;
    private Long userId;
    private Integer scoreValue;
    private String grade;
    private String label;
    private Integer urgentFixCount;
    private Integer criticalFixCount;
    private Integer optionalFixCount;
    private String profileJson;
    private String skillGroupsJson;
    private String projectsJson;
    private String highlightsJson;
    private String issuesJson;
    private String analysisSummary;
    private String rawAnalysisJson;
    private String status;
    private Date createTime;
    private Date updateTime;
    private Integer delFlag;
}
