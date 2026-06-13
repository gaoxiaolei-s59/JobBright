package org.puregxl.site.rag.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.rag.dao.entity.JobPost;
import org.puregxl.site.rag.dao.entity.JobPostingDO;
import org.puregxl.site.rag.dao.mapper.JobPostingMapper;
import org.puregxl.site.rag.llm.job.client.JobPostCleanLlmClient;
import org.puregxl.site.rag.llm.job.prompt.JobPostCleanPromptBuilder;
import org.puregxl.site.rag.service.JobPostCleanPersistenceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 下载公司Logo定时任务
 */
@Component
@RequiredArgsConstructor
public class JobPostCleanTask {

    private final JobPostingMapper jobPostingMapper;
    private final JobPostCleanLlmClient jobPostCleanLlmClient;
    private final JobPostCleanPersistenceService jobPostCleanPersistenceService;

    /**
     * 最大线程数
     */
    private final Integer CLEANING_THREAD_COUNT = 5;

    /**
     * 线程池
     */
    ExecutorService executorService = Executors.newFixedThreadPool(CLEANING_THREAD_COUNT);
    /**
     * 批处理 定量批次
     */
    private static final Integer BATCH_SIZE = 200;

//    @Scheduled(fixedDelay = 500000)
    public void run() throws Exception {
        //1.先批量拉取一批职位(按照发布时间排序 - 优先拉取最新数据)
        List<JobPostingDO> jobPostingDOS = jobPostingMapper.selectList(Wrappers.lambdaQuery(JobPostingDO.class)
                .orderByDesc(JobPostingDO::getPublishTime)
                .last("limit " + BATCH_SIZE));
        JobPostingDO jobPostingDO = new JobPostingDO();
        List<Future<CleanJobResult>> futures = jobPostingDOS.stream()
                .map(rawJob -> executorService.submit(buildCleanTask(rawJob)))
                .toList();
        /**
         * 获取结果
         */
        for (Future<CleanJobResult> future : futures) {
            try {
                CleanJobResult result = future.get();
                JobPost jobPost = result.getJobPost();
                jobPostCleanPersistenceService.persistAndDelete(jobPost, result.getRawJobId());
                System.out.println("已清洗入库: " + jobPost.getJobId() + " -> " + jobPost.getTitle());
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw interruptedException;
            } catch (ExecutionException executionException) {
                Throwable cause = executionException.getCause() == null ? executionException : executionException.getCause();
                System.err.println("职位清洗失败，已跳过当前记录: " + cause.getMessage());
            }
        }

    }

    /**
     * 创建清洗任务
     *
     * @return
     */
    private Callable<CleanJobResult> buildCleanTask(JobPostingDO jobPostingDO) {
        return () -> {
            // 1.llm清洗
            JobPostCleanResult jobPostCleanResult = cleanByLlm(jobPostingDO);
            JobPost jobPost = buildJobPost(jobPostingDO, jobPostCleanResult);
            return new CleanJobResult(jobPostingDO.getId(), jobPost);
        };
    }




    /**
     * 先执行清洗操作
     *
     * @param jobPostingDO
     * @return
     */
    private JobPostCleanResult cleanByLlm(JobPostingDO jobPostingDO) throws Exception {
        // 1.封装用户系统prompt
        String userPrompt = JobPostCleanPromptBuilder.buildRefineJobUserPrompt(jobPostingDO);
        String systemPrompt = JobPostCleanPromptBuilder.buildCleanSystemPrompt();
        // 2.执行清洗操作
        String cleanJob = jobPostCleanLlmClient.cleanJob(systemPrompt, userPrompt);
        // 3.转换成第一版 job
        JobPostCleanResult bean = JSONUtil.toBean(cleanJob, JobPostCleanResult.class);
        return bean;
    }


    /**
     * 封装最终入库的job
     *
     * @param rawJob
     * @param cleanResult
     * @return
     */
    private JobPost buildJobPost(JobPostingDO rawJob, JobPostCleanResult cleanResult) {
        LocalDateTime baseTime = firstNonNull(rawJob.getPublishTime(), rawJob.getUpdatedAt(), rawJob.getCreatedAt(), rawJob.getCrawledAt(), LocalDateTime.now());
        Date now = new Date();

        return JobPost.builder()
                .jobId(rawJob.getJobId())
                .companyId(rawJob.getCompanyId())
                .title(firstNonBlank(cleanResult.getTitle(), rawJob.getTitle()))
                .jobSummary(firstNonBlank(cleanResult.getJobSummary(), abbreviate(rawJob.getSummary(), 120)))
                .jobDescription(firstNonBlank(cleanResult.getJobDescription(), rawJob.getSummary()))
                .roleCategory(cleanResult.getRoleCategory())
                .roleTags(toJsonArray(cleanResult.getRoleTags()))
                .skillTags(toJsonArray(cleanResult.getSkillTags()))
                .industryTags(toJsonArray(cleanResult.getIndustryTags()))
                .benefitTags(toJsonArray(cleanResult.getBenefitTags()))
                .highlightTags(toJsonArray(cleanResult.getHighlightTags()))
                .location(firstNonBlank(rawJob.getLocation(), buildLocation(cleanResult)))
                .city(cleanResult.getCity())
                .district(cleanResult.getDistrict())
                .workMode(cleanResult.getWorkMode())
                .employmentType(cleanResult.getEmploymentType())
                .educationRequirement(cleanResult.getEducationRequirement())
                .preferredMajor(toDelimitedString(cleanResult.getPreferredMajor()))
                .experienceLevel(cleanResult.getExperienceLevel())
                .minExperienceYears(cleanResult.getMinExperienceYears())
                .maxExperienceYears(cleanResult.getMaxExperienceYears())
                .internshipMonths(cleanResult.getInternshipMonths())
                .salaryMinMonthly(cleanResult.getSalaryMinMonthly())
                .salaryMaxMonthly(cleanResult.getSalaryMaxMonthly())
                .salaryMonths(cleanResult.getSalaryMonths())
                .applicantCount(0)
                .applyUrl(rawJob.getSourceUrl())
                .postedAt(toDate(baseTime))
                .status("ONLINE")
                .country("中国大陆")
                .createTime(now)
                .updateTime(now)
                .delFlag(0)
                .build();
    }


    private String toDelimitedString(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> normalized = values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return null;
        }
        return String.join("、", normalized);
    }


    private String buildLocation(JobPostCleanResult cleanResult) {
        if (!StringUtils.hasText(cleanResult.getCity())) {
            return null;
        }
        return StringUtils.hasText(cleanResult.getDistrict())
                ? cleanResult.getCity() + " / " + cleanResult.getDistrict()
                : cleanResult.getCity();
    }


    private String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }


    private String toJsonArray(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> normalized = values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(normalized);
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }


    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }


    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 接收清洗后的职位
     */
    @Data
    public static class JobPostCleanResult {
        private String title;
        private String jobSummary;
        private String jobDescription;
        private String roleCategory;
        private List<String> roleTags = Collections.emptyList();
        private List<String> skillTags = Collections.emptyList();
        private List<String> industryTags = Collections.emptyList();
        private List<String> benefitTags = Collections.emptyList();
        private List<String> highlightTags = Collections.emptyList();
        private String city;
        private String district;
        private String workMode;
        private String employmentType;
        private String educationRequirement;
        private List<String> preferredMajor = Collections.emptyList();
        private String experienceLevel;
        private Integer minExperienceYears;
        private Integer maxExperienceYears;
        private Integer internshipMonths;
        private Integer salaryMinMonthly;
        private Integer salaryMaxMonthly;
        private Integer salaryMonths;
    }

    @Data
    @lombok.AllArgsConstructor
    private static class CleanJobResult {
        private Long rawJobId;
        private JobPost jobPost;
    }

}
