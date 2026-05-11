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
@TableName("resume_analysis_section")
public class ResumeAnalysisSection {

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
     * 分组编码：IMPACT_ACHIEVEMENTS、STYLE_SECTIONS等
     */
    private String sectionCode;

    /**
     * 分组标题，例如：Impact & Achievements
     */
    private String sectionTitle;

    /**
     * 重要性标题
     */
    private String whyImportantTitle;

    /**
     * 为什么重要的说明
     */
    private String whyImportantContent;

    /**
     * 按钮文案
     */
    private String actionText;

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
