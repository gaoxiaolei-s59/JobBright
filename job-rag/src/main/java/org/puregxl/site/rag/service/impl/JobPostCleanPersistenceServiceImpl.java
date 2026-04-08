package org.puregxl.site.rag.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.rag.dao.entity.JobPost;
import org.puregxl.site.rag.dao.entity.JobPostingDO;
import org.puregxl.site.rag.dao.mapper.JobPostMapper;
import org.puregxl.site.rag.dao.mapper.JobPostingMapper;
import org.puregxl.site.rag.service.JobPostCleanPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobPostCleanPersistenceServiceImpl implements JobPostCleanPersistenceService {

    private final JobPostMapper jobPostMapper;
    private final JobPostingMapper jobPostingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistAndDelete(JobPost jobPost, Long rawJobId) {
        JobPost existing = jobPostMapper.selectOne(Wrappers.lambdaQuery(JobPost.class)
                .eq(JobPost::getJobId, jobPost.getJobId())
                .last("limit 1"));
        if (existing == null) {
            jobPostMapper.insert(jobPost);
        } else {
            jobPost.setId(existing.getId());
            jobPost.setCreateTime(existing.getCreateTime());
            jobPostMapper.updateById(jobPost);
        }
        jobPostingMapper.deleteById(rawJobId);
    }
}
