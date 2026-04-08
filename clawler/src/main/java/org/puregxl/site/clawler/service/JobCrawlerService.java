package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.puregxl.site.clawler.crawler.CrawlResult;
import org.puregxl.site.clawler.crawler.HtmlJobCrawler;
import org.puregxl.site.clawler.crawler.SiteCrawlConfig;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.mapper.JobPostingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobCrawlerService {

    private final HtmlJobCrawler htmlJobCrawler;
    private final JobPostingMapper jobPostingMapper;

    public JobCrawlerService(HtmlJobCrawler htmlJobCrawler, JobPostingMapper jobPostingMapper) {
        this.htmlJobCrawler = htmlJobCrawler;
        this.jobPostingMapper = jobPostingMapper;
    }

    @Transactional
    public CrawlResult crawl(SiteCrawlConfig config) {
        List<JobPosting> fetchedPostings = htmlJobCrawler.crawl(config);
        List<JobPosting> savedPostings = new ArrayList<>();

        int insertedCount = 0;
        int updatedCount = 0;
        for (JobPosting fetched : fetchedPostings) {
            JobPosting saved = jobPostingMapper.selectOne(Wrappers.lambdaQuery(JobPosting.class)
                    .eq(JobPosting::getSourceKey, fetched.getSourceKey())
                    .last("limit 1"));
            if (saved == null) {
                saved = new JobPosting();
            }
            copyFields(saved, fetched);
            boolean isNew = saved.getId() == null;
            persist(saved);
            savedPostings.add(saved);

            if (isNew) {
                insertedCount++;
            } else {
                updatedCount++;
            }
        }

        return new CrawlResult(fetchedPostings.size(), insertedCount, updatedCount, savedPostings);
    }

    private void copyFields(JobPosting target, JobPosting source) {
        target.setJobId(source.getJobId());
        target.setSourceSite(source.getSourceSite());
        target.setCompanyId(source.getCompanyId());
        target.setTitle(source.getTitle());
        target.setCompany(source.getCompany());
        target.setLocation(source.getLocation());
        target.setSalary(source.getSalary());
        target.setSummary(source.getSummary());
        target.setSourceUrl(source.getSourceUrl());
        target.setSourceKey(source.getSourceKey());
        target.setCrawledAt(source.getCrawledAt());
    }

    private void persist(JobPosting posting) {
        LocalDateTime now = LocalDateTime.now();
        if (posting.getCrawledAt() == null) {
            posting.setCrawledAt(now);
        }
        if (posting.getId() == null) {
            posting.setCreatedAt(now);
            posting.setUpdatedAt(now);
            jobPostingMapper.insert(posting);
            return;
        }
        posting.setUpdatedAt(now);
        jobPostingMapper.updateById(posting);
    }
}
