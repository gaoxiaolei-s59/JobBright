package org.puregxl.site.jobbacked.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.apache.ibatis.annotations.Param;
import org.puregxl.site.jobbacked.dto.resp.UserResumePreviewV2QueryResult;


public interface UserResumeFileMapper extends BaseMapper<UserResumeFile> {

    UserResumePreviewV2QueryResult selectResumePreviewV2(@Param("resumeId") String resumeId, @Param("userId") Long userId);
}
