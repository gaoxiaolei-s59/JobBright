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
@TableName("resume_project_experience")
public class ResumeProjectExperience {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 简历业务ID
     */
    private String resumeId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目角色/职位
     */
    private String roleTitle;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;

    /**
     * 技术栈，JSON数组
     */
    private String techStack;

    /**
     * 项目描述/项目成果，JSON数组或换行文本
     */
    private String description;

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
