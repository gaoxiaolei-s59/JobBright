package org.puregxl.site.clawler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.puregxl.site.clawler.dto.ZhaopinBatchCrawlRequest;
import org.puregxl.site.clawler.dto.ZhaopinBatchCrawlResponse;
import org.puregxl.site.clawler.dto.ZhaopinCrawlResponse;
import org.puregxl.site.clawler.dto.ZhaopinFetchedJob;
import org.puregxl.site.clawler.dto.ZhaopinSearchRequest;
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
public class ZhaopinCrawlerService {

    private static final DateTimeFormatter PUBLISH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ZhaopinClient zhaopinClient;
    private final JobPostingMapper jobPostingMapper;
    private final ZhaopinRawPersistenceService zhaopinRawPersistenceService;
    private final CompanyPersistenceService companyPersistenceService;

    public ZhaopinCrawlerService(
            ZhaopinClient zhaopinClient,
            JobPostingMapper jobPostingMapper,
            ZhaopinRawPersistenceService zhaopinRawPersistenceService,
            CompanyPersistenceService companyPersistenceService
    ) {
        this.zhaopinClient = zhaopinClient;
        this.jobPostingMapper = jobPostingMapper;
        this.zhaopinRawPersistenceService = zhaopinRawPersistenceService;
        this.companyPersistenceService = companyPersistenceService;
    }

    @Transactional
    public ZhaopinCrawlResponse crawl(ZhaopinSearchRequest request) {
        List<ZhaopinFetchedJob> fetched = zhaopinClient.searchJobs(request);
        PersistResult persistResult = persistFetchedJobs(fetched);

        int page = request.page() == null ? 1 : request.page();
        int pageSize = request.pageSize() == null ? fetched.size() : request.pageSize();
        return new ZhaopinCrawlResponse(
                page,
                pageSize,
                fetched.size(),
                persistResult.insertedCount(),
                persistResult.updatedCount(),
                persistResult.saved()
        );
    }

    @Transactional
    public ZhaopinBatchCrawlResponse crawlBatch(ZhaopinBatchCrawlRequest request) {
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
            ZhaopinSearchRequest pageRequest = new ZhaopinSearchRequest(
                    request.keyword(),
                    request.cityId(),
                    currentPage,
                    pageSize,
                    request.workExperience(),
                    request.education(),
                    request.companyType(),
                    request.employmentType()
            );
            List<ZhaopinFetchedJob> fetched = zhaopinClient.searchJobs(pageRequest);
            if (fetched.isEmpty()) {
                break;
            }

            int remaining = targetCount - totalFetched;
            List<ZhaopinFetchedJob> currentBatch = fetched.size() > remaining ? fetched.subList(0, remaining) : fetched;
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

        return new ZhaopinBatchCrawlResponse(
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

    private PersistResult persistFetchedJobs(List<ZhaopinFetchedJob> fetched) {
        List<JobPosting> saved = new ArrayList<>();
        int insertedCount = 0;
        int updatedCount = 0;

        for (ZhaopinFetchedJob fetchedJob : fetched) {
            JobPosting fetchedPosting = fetchedJob.posting();
            fetchedPosting.setPublishTime(parsePublishTime(fetchedJob.publishTimeText()));
            Company company = companyPersistenceService.saveOrUpdate(
                    "zhaopin",
                    fetchedJob.companyId() == null ? null : String.valueOf(fetchedJob.companyId()),
                    fetchedJob.posting().getCompany(),
                    fetchedJob.companyLogoUrl(),
                    fetchedJob.companySize(),
                    fetchedJob.industryName(),
                    fetchedJob.companyProperty(),
                    null,
                    fetchedJob.rawJson()
            );
            fetchedPosting.setCompanyId(company == null ? null : company.getCompanyId());
            fetchedPosting.setJobId(BusinessIdGenerator.generateJobPostId(
                    fetchedPosting.getSourceSite(),
                    fetchedJob.positionNumber() == null ? (fetchedJob.jobId() == null ? null : String.valueOf(fetchedJob.jobId())) : fetchedJob.positionNumber(),
                    fetchedPosting.getSourceKey()
            ));
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
            zhaopinRawPersistenceService.saveOrUpdate(fetchedJob);
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
