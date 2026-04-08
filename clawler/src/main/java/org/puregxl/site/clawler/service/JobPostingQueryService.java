package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.mapper.JobPostingMapper;
import org.puregxl.site.clawler.util.TextCleaner;
import org.springframework.stereotype.Service;

@Service
public class JobPostingQueryService {

    private final JobPostingMapper jobPostingMapper;

    public JobPostingQueryService(JobPostingMapper jobPostingMapper) {
        this.jobPostingMapper = jobPostingMapper;
    }

    public Page<JobPosting> query(String keyword, String sourceSite, long current, long size) {
        Page<JobPosting> page = new Page<>(current, size);
        String cleanedKeyword = TextCleaner.defaultString(keyword);
        String cleanedSourceSite = TextCleaner.defaultString(sourceSite);
        return jobPostingMapper.selectPage(page, Wrappers.lambdaQuery(JobPosting.class)
                .like(!cleanedKeyword.isBlank(), JobPosting::getTitle, cleanedKeyword)
                .like(!cleanedSourceSite.isBlank(), JobPosting::getSourceSite, cleanedSourceSite)
                .orderByDesc(JobPosting::getUpdatedAt));
    }
}
