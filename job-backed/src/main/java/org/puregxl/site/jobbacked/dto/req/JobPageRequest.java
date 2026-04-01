package org.puregxl.site.jobbacked.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;


@Data
public class JobPageRequest extends Page<Object> {


    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 国家
     */
    private String country;

    /**
     * 职位名称
     */
    private String jobTitle;

    /**
     * 经验等级
     */
    private String experienceLevel;

    /**
     * 工作类型
     */
    private String jobType;

    /**
     * 工作模式
     */
    private String workMode;

    /**
     * 发布时间
     */
    private String datePosted;

    /**
     * 行业
     */
    private String industry;
}
