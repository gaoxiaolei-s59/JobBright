package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.enums.ExperienceLevelEnum;
import org.puregxl.site.jobbacked.common.enums.JobPostStatusEnum;
import org.puregxl.site.jobbacked.dao.entity.Company;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dao.entity.UserJobAction;
import org.puregxl.site.jobbacked.dao.mapper.CompanyMapper;
import org.puregxl.site.jobbacked.dao.mapper.JobPostMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserJobActionMapper;
import org.puregxl.site.jobbacked.dto.req.JobPageRequest;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobListResponse;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobResponse;
import org.puregxl.site.jobbacked.service.RecommendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
// 推荐列表以读取为主，这里显式声明只读事务，避免误开启写事务语义。
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RecommendServiceImpl implements RecommendService {

    private final JobPostMapper jobPostMapper;

    private final CompanyMapper companyMapper;

    private final UserJobActionMapper userJobActionMapper;

    @Override
    public RecommendJobListResponse getRecommendJobs(JobPageRequest requestParam) {
        JobPageRequest request = requestParam == null ? new JobPageRequest() : requestParam;
        List<JobPost> jobPosts = jobPostMapper.selectList(Wrappers.lambdaQuery(JobPost.class)
                .eq(JobPost::getDelFlag, 0)
                .eq(JobPost::getStatus, JobPostStatusEnum.ONLINE.getCode())
                .orderByDesc(JobPost::getPostedAt)
                .orderByDesc(JobPost::getId));

        //如果没有数据返回空值
        if (jobPosts.isEmpty()) {
            return RecommendJobListResponse.builder()
                    .total(0)
                    .hasMore(false)
                    .records(Collections.emptyList())
                    .build();
        }

        Set<String> companyIds = jobPosts.stream()
                .map(JobPost::getCompanyId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap() :
                companyMapper.selectList(Wrappers.lambdaQuery(Company.class)
                                .in(Company::getCompanyId, companyIds)
                                .eq(Company::getDelFlag, 0))
                        .stream()
                        .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (left, right) -> left));

        Map<String, UserJobAction> actionMap = getUserJobActionMap(jobPosts);

        List<RecommendJobResponse> records = jobPosts.stream()
                .map(jobPost -> toResponse(jobPost, companyMap.get(jobPost.getCompanyId()), actionMap.get(jobPost.getJobId())))
                .filter(record -> matchesFilters(record, request))
                .sorted(Comparator.comparing(RecommendJobResponse::getMatchScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int total = records.size();
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        boolean hasMore = toIndex < total;

        return RecommendJobListResponse.builder()
                .total(total)
                .hasMore(hasMore)
                .records(records.subList(fromIndex, toIndex))
                .build();
    }

    @Override
    public RecommendJobListResponse getRecommendJobsV2(JobPageRequestV2 requestParam) {
        JobPageRequestV2 request = requestParam == null ? new JobPageRequestV2() : requestParam;

        // 兼容前端未传分页参数的场景，统一兜底默认值。
        if (request.getCurrent() == 0) {
            request.setCurrent(1);
        }
        if (request.getSize() == 0) {
            request.setSize(10);
        }

        IPage<JobPost> pageResult = jobPostMapper.selectRecommendJobPageV2(request, request);
        List<JobPost> jobPosts = pageResult.getRecords();

        if (jobPosts == null || jobPosts.isEmpty()) {
            return RecommendJobListResponse.builder()
                    .total(0)
                    .hasMore(false)
                    .records(Collections.emptyList())
                    .build();
        }

        List<String> companyIds = jobPosts.stream()
                .map(JobPost::getCompanyId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        Map<String, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectList(Wrappers.lambdaQuery(Company.class)
                        .in(Company::getCompanyId, companyIds)
                        .eq(Company::getDelFlag, 0))
                .stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));

        // 批量回填当前用户对这些职位的行为状态，避免前端二次请求。
        Map<String, UserJobAction> actionMap = getUserJobActionMap(jobPosts);

        List<RecommendJobResponse> records = jobPosts.stream()
                .map(jobPost -> toResponse(
                        jobPost,
                        companyMap.get(jobPost.getCompanyId()),
                        actionMap.get(jobPost.getJobId())
                ))
                .toList();

        return RecommendJobListResponse.builder()
                .total((int) pageResult.getTotal())
                .hasMore(pageResult.getCurrent() < pageResult.getPages())
                .records(records)
                .build();
    }


    private Map<String, UserJobAction> getUserJobActionMap(List<JobPost> jobPosts) {
        long currentUserId = UserContext.getUserId();
        if (currentUserId <= 0) {
            return Collections.emptyMap();
        }
        List<String> jobIds = jobPosts.stream()
                .map(JobPost::getJobId)
                .filter(StringUtils::hasText)
                .toList();
        if (jobIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userJobActionMapper.selectList(Wrappers.lambdaQuery(UserJobAction.class)
                        .eq(UserJobAction::getUserId, currentUserId)
                        .in(UserJobAction::getJobId, jobIds)
                        .eq(UserJobAction::getDelFlag, 0))
                .stream()
                .collect(Collectors.toMap(UserJobAction::getJobId, Function.identity(), (left, right) -> left));
    }

    private RecommendJobResponse toResponse(JobPost jobPost, Company company, UserJobAction action) {
        int matchScore = calculateMatchScore(jobPost, company);
        return RecommendJobResponse.builder()
                .jobId(jobPost.getJobId())
                .companyName(company == null ? "" : company.getCompanyName())
                .companyLogo(company == null ? null : company.getCompanyLogo())
                .title(jobPost.getTitle())
                .meta(buildMeta(company))
                .postedAt(formatPostedAt(jobPost.getPostedAt()))
                .salaryRange(formatSalaryRange(jobPost))
                .location(formatLocation(jobPost))
                .city(jobPost.getCity())
                .district(jobPost.getDistrict())
                .workMode(jobPost.getWorkMode())
                .employmentType(jobPost.getEmploymentType())
                .experienceLevel(ExperienceLevelEnum.normalize(jobPost.getExperienceLevel()))
                .educationRequirement(jobPost.getEducationRequirement())
                .preferredMajor(jobPost.getPreferredMajor())
                .roleCategory(jobPost.getRoleCategory())
                .internshipMonths(jobPost.getInternshipMonths())
                .jobSummary(firstNonBlank(jobPost.getJobSummary(), jobPost.getJobDescription()))
                .skillTags(parseTagList(jobPost.getSkillTags()))
                .highlightTags(parseTagList(jobPost.getHighlightTags()))
                .applicantCount(jobPost.getApplicantCount())
                .matchScore(matchScore)
                .matchLabel(matchScore >= 95 ? "高度匹配" : "较高匹配")
                .matchReason(buildMatchReason(jobPost, company, matchScore))
                .applyUrl(jobPost.getApplyUrl())
                .liked(action != null && Objects.equals(action.getLiked(), 1))
                .applied(action != null && Objects.equals(action.getApplied(), 1))
                .build();
    }




    private boolean matchesFilters(RecommendJobResponse record, JobPageRequest request) {
        return containsIgnoreCase(record.getTitle(), request.getKeyword())
                || containsIgnoreCase(record.getCompanyName(), request.getKeyword())
                || containsIgnoreCase(record.getMeta(), request.getKeyword())
                ? matchesRemainingFilters(record, request)
                : !StringUtils.hasText(request.getKeyword()) && matchesRemainingFilters(record, request);
    }

    private boolean matchesRemainingFilters(RecommendJobResponse record, JobPageRequest request) {
        if (StringUtils.hasText(request.getCountry()) && !"中国大陆".equals(request.getCountry())) {
            return false;
        }
        if (!containsIgnoreCase(record.getTitle(), request.getTitle())) {
            return false;
        }
        if (!equalsIfPresent(record.getExperienceLevel(), ExperienceLevelEnum.normalize(request.getExperienceLevel()))) {
            return false;
        }
        if (!equalsIfPresent(record.getEmploymentType(), request.getEmploymentType())) {
            return false;
        }
        if (!equalsIfPresent(record.getWorkMode(), request.getWorkMode())) {
            return false;
        }
        if (!containsIgnoreCase(record.getMeta(), request.getIndustryName())) {
            return false;
        }
        return matchesDatePosted(record.getPostedAt(), request.getDatePosted());
    }

    private boolean matchesDatePosted(String postedAt, String datePosted) {
        if (!StringUtils.hasText(datePosted)) {
            return true;
        }
        if (!StringUtils.hasText(postedAt)) {
            return false;
        }
        if ("24h".equals(datePosted)) {
            return postedAt.contains("小时前") || postedAt.contains("分钟前") || postedAt.contains("刚刚");
        }
        if ("3d".equals(datePosted)) {
            return !postedAt.contains("天前") || postedAt.startsWith("1 天前") || postedAt.startsWith("2 天前");
        }
        if ("7d".equals(datePosted)) {
            return true;
        }
        return true;
    }

    private boolean equalsIfPresent(String value, String expected) {
        return !StringUtils.hasText(expected) || Objects.equals(value, expected);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return !StringUtils.hasText(keyword)
                || (StringUtils.hasText(value) && value.toLowerCase().contains(keyword.toLowerCase()));
    }

    private String buildMeta(Company company) {
        if (company == null) {
            return "";
        }
        return Arrays.stream(new String[]{company.getCompanyName(), company.getIndustryName(), company.getCompanyStage()})
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" / "));
    }

    /**
     * 计算核心分数
     * @param jobPost
     * @param company
     * @return
     */
    private int calculateMatchScore(JobPost jobPost, Company company) {
        int score = 68;
        if (StringUtils.hasText(jobPost.getRoleCategory())) {
            score += 8;
        }
        if (StringUtils.hasText(jobPost.getTitle()) && (jobPost.getTitle().contains("后端") || jobPost.getTitle().contains("Java"))) {
            score += 10;
        }
        if (!parseTagList(jobPost.getSkillTags()).isEmpty()) {
            score += 8;
        }
        if (!parseTagList(jobPost.getHighlightTags()).isEmpty()) {
            score += 4;
        }
        if ("全职".equals(jobPost.getEmploymentType())) {
            score += 4;
        }
        if ("混合办公".equals(jobPost.getWorkMode())) {
            score += 3;
        }
        if (jobPost.getSalaryMaxMonthly() != null && jobPost.getSalaryMaxMonthly() >= 30) {
            score += 3;
        }
        if (company != null && StringUtils.hasText(company.getIndustryName()) && company.getIndustryName().contains("互联网")) {
            score += 5;
        }
        return Math.min(score, 98);
    }

    private String buildMatchReason(JobPost jobPost, Company company, int matchScore) {
        List<String> skillTags = parseTagList(jobPost.getSkillTags());
        if (!skillTags.isEmpty()) {
            return "命中核心技能：" + skillTags.stream().limit(3).collect(Collectors.joining("、"));
        }
        if (StringUtils.hasText(jobPost.getRoleCategory())) {
            return "岗位角色聚焦于" + jobPost.getRoleCategory() + "，便于和简历画像做定向匹配";
        }
        if (matchScore >= 95) {
            return "岗位方向和当前简历关键词高度重合";
        }
        if (company != null && StringUtils.hasText(company.getIndustryName())) {
            return company.getIndustryName() + "方向与后端岗位画像较为接近";
        }
        return "岗位画像匹配度较高";
    }

    private String formatPostedAt(Date postedAt) {
        if (postedAt == null) {
            return "";
        }
        Duration duration = Duration.between(postedAt.toInstant(), Instant.now());
        long minutes = Math.max(duration.toMinutes(), 0);
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }
        long days = duration.toDays();
        if (days < 7) {
            return days + " 天前";
        }
        return postedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private String formatLocation(JobPost jobPost) {
        if (StringUtils.hasText(jobPost.getLocation())) {
            return jobPost.getLocation();
        }
        return joinNonBlank(" / ", jobPost.getCity(), jobPost.getDistrict());
    }

    private String formatSalaryRange(JobPost jobPost) {
        if (jobPost.getSalaryMinMonthly() == null && jobPost.getSalaryMaxMonthly() == null) {
            return "";
        }
        String range = (jobPost.getSalaryMinMonthly() == null ? "面议" : jobPost.getSalaryMinMonthly() + "k")
                + "-"
                + (jobPost.getSalaryMaxMonthly() == null ? "面议" : jobPost.getSalaryMaxMonthly() + "k");
        if (jobPost.getSalaryMonths() != null && jobPost.getSalaryMonths() > 0) {
            return range + " * " + jobPost.getSalaryMonths() + "薪";
        }
        return range;
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    private String joinNonBlank(String delimiter, String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(delimiter));
    }

    private List<String> parseTagList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            if (raw.trim().startsWith("[")) {
                return JSONUtil.parseArray(raw).stream()
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .toList();
            }
        } catch (Exception ignored) {
        }
        return Arrays.stream(raw.split("[,，|/]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
    }


}
