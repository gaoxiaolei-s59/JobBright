package org.puregxl.site.jobbacked.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.puregxl.site.jobbacked.dao.entity.JobPost;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPageRequestV2 extends Page<JobPost> {

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
    private String title;

    /**
     * 经验等级
     */
    private String experienceLevel;

    /**
     * 用工类型
     */
    private String employmentType;

    /**
     * 工作模式
     */
    private String workMode;

    /**
     * 发布时间
     */
    private String datePosted;

    /**
     * 行业名称
     */
    private String industryName;


}
