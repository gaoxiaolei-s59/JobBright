package org.puregxl.site.rag.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_posting")
public class JobPostingDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 职位业务ID
     */
    private String jobId;

    /**
     * 来源站点
     */
    private String sourceSite;

    /**
     * 公司业务ID
     */
    private String companyId;

    /**
     * 职位标题
     */
    private String title;

    /**
     * 公司名称
     */
    private String company;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 薪资
     */
    private String salary;

    /**
     * 职位摘要
     */
    private String summary;

    /**
     * 来源链接
     */
    private String sourceUrl;

    /**
     * 来源唯一键
     */
    private String sourceKey;

    /**
     * 抓取时间
     */
    private LocalDateTime crawledAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}