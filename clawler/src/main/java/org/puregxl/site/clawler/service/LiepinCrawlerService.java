package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.puregxl.site.clawler.dto.LiepinBatchCrawlRequest;
import org.puregxl.site.clawler.dto.LiepinBatchCrawlResponse;
import org.puregxl.site.clawler.dto.LiepinCrawlResponse;
import org.puregxl.site.clawler.dto.LiepinFetchedJob;
import org.puregxl.site.clawler.dto.LiepinSearchRequest;
import org.puregxl.site.clawler.entity.Company;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.mapper.JobPostingMapper;
import org.puregxl.site.clawler.util.BusinessIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiepinCrawlerService {

    private static final DateTimeFormatter PUBLISH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final LiepinClient liepinClient;
    private final JobPostingMapper jobPostingMapper;
    private final CompanyPersistenceService companyPersistenceService;

    public LiepinCrawlerService(
            LiepinClient liepinClient,
            JobPostingMapper jobPostingMapper,
            CompanyPersistenceService companyPersistenceService
    ) {
        this.liepinClient = liepinClient;
        this.jobPostingMapper = jobPostingMapper;
        this.companyPersistenceService = companyPersistenceService;
    }

    @Transactional
    public LiepinCrawlResponse crawl(LiepinSearchRequest request) {
        List<LiepinFetchedJob> fetched = liepinClient.searchJobs(request);
        PersistResult persistResult = persistFetchedJobs(fetched);
        int page = request.page() == null ? 1 : request.page();
        int pageSize = request.pageSize() == null ? fetched.size() : request.pageSize();
        return new LiepinCrawlResponse(
                page,
                pageSize,
                fetched.size(),
                persistResult.insertedCount(),
                persistResult.updatedCount(),
                persistResult.saved()
        );
    }

    @Transactional
    public LiepinBatchCrawlResponse crawlBatch(LiepinBatchCrawlRequest request) {
        int startPage = request.startPage() == null ? 1 : request.startPage();
        int pageSize = request.pageSize() == null ? 20 : request.pageSize();
        int targetCount = request.targetCount() == null ? 1000 : Math.min(request.targetCount(), 1000);
        int maxPages = request.maxPages() == null ? Math.max(1, (int) Math.ceil(targetCount / (double) pageSize)) : request.maxPages();

        List<JobPosting> saved = new ArrayList<>();
        int totalFetched = 0;
        int insertedCount = 0;
        int updatedCount = 0;
        int currentPage = startPage;
        int endPage = startPage - 1;

        while (currentPage < startPage + maxPages && totalFetched < targetCount) {
            LiepinSearchRequest pageRequest = new LiepinSearchRequest(
                    request.keyword(),
                    request.cityCode(),
                    currentPage,
                    pageSize
            );
            List<LiepinFetchedJob> fetched = liepinClient.searchJobs(pageRequest);
            if (fetched.isEmpty()) {
                break;
            }

            int remaining = targetCount - totalFetched;
            List<LiepinFetchedJob> currentBatch = fetched.size() > remaining ? fetched.subList(0, remaining) : fetched;
            PersistResult persistResult = persistFetchedJobs(currentBatch);
            saved.addAll(persistResult.saved());
            totalFetched += currentBatch.size();
            insertedCount += persistResult.insertedCount();
            updatedCount += persistResult.updatedCount();
            endPage = currentPage;

            if (fetched.size() < pageSize || currentBatch.size() < fetched.size()) {
                break;
            }
            currentPage++;
        }

        return new LiepinBatchCrawlResponse(
                startPage,
                endPage,
                pageSize,
                targetCount,
                totalFetched,
                insertedCount,
                updatedCount,
                saved
        );
    }

    private PersistResult persistFetchedJobs(List<LiepinFetchedJob> fetched) {
        List<JobPosting> saved = new ArrayList<>();
        int insertedCount = 0;
        int updatedCount = 0;

        for (LiepinFetchedJob fetchedJob : fetched) {
            JobPosting fetchedPosting = fetchedJob.posting();
            fetchedPosting.setPublishTime(parsePublishTime(fetchedJob.publishTimeText()));
            Company company = companyPersistenceService.saveOrUpdate(
                    "liepin",
                    fetchedJob.sourceCompanyId(),
                    fetchedPosting.getCompany(),
                    fetchedJob.companyLogoUrl(),
                    fetchedJob.companySize(),
                    fetchedJob.industryName(),
                    fetchedJob.companyStage(),
                    null,
                    fetchedJob.rawJson()
            );
            fetchedPosting.setCompanyId(company == null ? null : company.getCompanyId());
            fetchedPosting.setJobId(BusinessIdGenerator.generateJobPostId("liepin", fetchedJob.sourceJobId(), fetchedPosting.getSourceKey()));
            JobPosting target = jobPostingMapper.selectOne(Wrappers.lambdaQuery(JobPosting.class)
                    .eq(JobPosting::getSourceKey, fetchedPosting.getSourceKey())
                    .last("limit 1"));
            if (target == null) {
                target = new JobPosting();
            }
            copyFields(target, fetchedPosting);

            boolean isNew = target.getId() == null;
            persist(target);
            saved.add(target);
            if (isNew) {
                insertedCount++;
            } else {
                updatedCount++;
            }
        }
        return new PersistResult(saved, insertedCount, updatedCount);
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
        target.setPublishTime(source.getPublishTime());
        target.setCrawledAt(source.getCrawledAt());
    }

    private LocalDateTime parsePublishTime(String publishTime) {
        if (publishTime == null || publishTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(publishTime, PUBLISH_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            return null;
        }
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

    private record PersistResult(
            List<JobPosting> saved,
            int insertedCount,
            int updatedCount
    ) {
    }
}
