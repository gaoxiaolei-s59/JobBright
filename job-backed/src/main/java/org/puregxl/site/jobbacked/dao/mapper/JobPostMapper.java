package org.puregxl.site.jobbacked.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.apache.ibatis.annotations.Param;

public interface JobPostMapper extends BaseMapper<JobPost> {

    /**
     * 分页查询职位
     * @param page
     * @param req
     * @return
     */
    IPage<JobPost> selectRecommendJobPageV2(IPage<JobPost> page, @Param("req") JobPageRequestV2 req);
}
