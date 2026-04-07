package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.enums.ExperienceLevelEnum;
import org.puregxl.site.jobbacked.common.enums.JobPostStatusEnum;
import org.puregxl.site.jobbacked.dao.entity.Company;
import org.puregxl.site.jobbacked.dao.entity.JobPost;
import org.puregxl.site.jobbacked.dao.entity.UserJobAction;
import org.puregxl.site.jobbacked.dao.mapper.CompanyMapper;
import org.puregxl.site.jobbacked.dao.mapper.JobPostMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserJobActionMapper;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.AppliedJobResponse;
import org.puregxl.site.jobbacked.dto.resp.FavoritesJobResponse;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobResponse;
import org.puregxl.site.jobbacked.service.JobService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
// 收藏/投递既包含查询也包含写入，所以保留默认读写事务。
@Transactional
public class JobServiceImpl implements JobService {

    private final UserJobActionMapper userJobActionMapper;

    private final JobPostMapper jobPostMapper;

    private final CompanyMapper companyMapper;


    /**
     * 获取当前用户的收藏职位列表。
     * 返回结构与推荐列表保持一致，方便前端直接复用卡片展示逻辑。
     * @param requestParam
     * @return
     */
    @Override
    public FavoritesJobResponse getFavoritesJob(JobPageRequestV2 requestParam) {
        List<RecommendJobResponse> records = queryUserJobs(requestParam, true);
        return FavoritesJobResponse.builder()
                .total(records.size())
                .hasMore(hasMore(requestParam, records.size()))
                .records(paginate(records, requestParam))
                .build();
    }

    /**
     * 喜欢职位 - 可能会有并发问题？
     *
     * @param jobId
     */
    @Override
    public void favoritesJob(String jobId) {
        UserJobAction userJobAction = getUserJobAction(jobId);

        /**如果没有查询到 - 执行新增操作**/
        if (userJobAction == null) {
            UserJobAction userJobActionBuild = UserJobAction.builder()
                    .userId(UserContext.getUserId())
                    .jobId(jobId)
                    .liked(1)
                    .applied(0)
                    .appliedTime(null)
                    .lastViewTime(new Date())
                    .build();

            userJobActionMapper.insert(userJobActionBuild);
        } else {
            /**如果已经喜欢**/
            if (userJobAction.getLiked() == 1) {
                throw new ClientException("已经在喜爱列表中 - 请勿重复点击");
            }
            //执行插入操作
            userJobAction.setLiked(1);
            userJobActionMapper.updateById(userJobAction);
        }
    }

    /**
     * 取消收藏职位。
     *
     * @param jobId
     */
    @Override
    public void cancelFavoritesJob(String jobId) {
        UserJobAction userJobAction = getUserJobAction(jobId);
        if (userJobAction == null || !Objects.equals(userJobAction.getLiked(), 1)) {
            throw new ClientException("当前职位未收藏，无法取消");
        }
        userJobAction.setLiked(0);
        userJobAction.setLastViewTime(new Date());
        userJobActionMapper.updateById(userJobAction);
    }

    /**
     * 投递职位
     * @param jobId
     */
    @Override
    public void applyJob(String jobId) {
        UserJobAction userJobAction = getUserJobAction(jobId);

        /**如果没有查询到 - 执行新增操作**/
        if (userJobAction == null) {
            UserJobAction userJobActionBuild = UserJobAction.builder()
                    .userId(UserContext.getUserId())
                    .jobId(jobId)
                    .liked(0)
                    .applied(1)
                    .appliedTime(null)
                    .lastViewTime(new Date())
                    .build();

            userJobActionMapper.insert(userJobActionBuild);
        } else {
            /**如果已经喜欢**/
            if (userJobAction.getLiked() == 1) {
                throw new ClientException("已经在喜爱列表中 - 请勿重复点击");
            }
            //执行插入操作
            userJobAction.setApplied(1);
            userJobActionMapper.updateById(userJobAction);
        }
    }

    /**
     * 取消投递职位。
     *
     * @param jobId
     */
    @Override
    public void cancelApplyJob(String jobId) {
        UserJobAction userJobAction = getUserJobAction(jobId);
        if (userJobAction == null || !Objects.equals(userJobAction.getApplied(), 1)) {
            throw new ClientException("当前职位未标记投递，无法取消");
        }
        userJobAction.setApplied(0);
        userJobAction.setAppliedTime(null);
        userJobAction.setLastViewTime(new Date());
        userJobActionMapper.updateById(userJobAction);
    }

    @Override
    public AppliedJobResponse getAppliedJob(JobPageRequestV2 requestParam) {
        List<RecommendJobResponse> records = queryUserJobs(requestParam, false);
        return AppliedJobResponse.builder()
                .total(records.size())
                .hasMore(hasMore(requestParam, records.size()))
                .records(paginate(records, requestParam))
                .build();
    }

    /**
     * 收藏列表与投递列表共用同一套查询逻辑：
     * 1. 先查用户行为表，锁定当前用户已收藏/已投递的职位
     * 2. 再批量查询职位和公司信息
     * 3. 最后组装成推荐卡片格式，并应用筛选条件
     */
    private List<RecommendJobResponse> queryUserJobs(JobPageRequestV2 requestParam, boolean favoritesOnly) {
        long currentUserId = UserContext.getUserId();
        if (currentUserId <= 0) {
            return Collections.emptyList();
        }

        JobPageRequestV2 request = requestParam == null ? new JobPageRequestV2() : requestParam;

        // 根据开关决定查询收藏列表还是投递列表。
        LambdaQueryWrapper<UserJobAction> actionQueryWrapper = Wrappers.lambdaQuery(UserJobAction.class)
                .eq(UserJobAction::getUserId, currentUserId)
                .eq(UserJobAction::getDelFlag, 0)
                .eq(favoritesOnly, UserJobAction::getLiked, 1)
                .eq(!favoritesOnly, UserJobAction::getApplied, 1)
                .orderByDesc(UserJobAction::getUpdateTime)
                .orderByDesc(UserJobAction::getLastViewTime)
                .orderByDesc(UserJobAction::getId);

        List<UserJobAction> actions = userJobActionMapper.selectList(actionQueryWrapper);
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> actionJobIds = actions.stream()
                .map(UserJobAction::getJobId)
                .filter(StringUtils::hasText)
                .toList();

        Map<String, UserJobAction> actionMap = actions.stream()
                .filter(action -> StringUtils.hasText(action.getJobId()))
                .collect(Collectors.toMap(UserJobAction::getJobId, Function.identity(), (left, right) -> left));

        List<JobPost> jobPosts = jobPostMapper.selectList(Wrappers.lambdaQuery(JobPost.class)
                .in(JobPost::getJobId, actionJobIds)
                .eq(JobPost::getDelFlag, 0)
                .eq(JobPost::getStatus, JobPostStatusEnum.ONLINE.getCode()));
        if (jobPosts == null || jobPosts.isEmpty()) {
            return Collections.emptyList();
        }

        // 按用户行为的时间顺序回放职位列表，避免数据库 in 查询打乱原始顺序。
        Map<String, Integer> actionOrderMap = buildActionOrderMap(actionJobIds);
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
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (left, right) -> left));

        return jobPosts.stream()
                .sorted(Comparator.comparingInt(jobPost -> actionOrderMap.getOrDefault(jobPost.getJobId(), Integer.MAX_VALUE)))
                .map(jobPost -> toResponse(jobPost, companyMap.get(jobPost.getCompanyId()), actionMap.get(jobPost.getJobId())))
                .filter(record -> matchesFilters(record, request))
                .toList();
    }

    private UserJobAction getUserJobAction(String jobId) {
        long userId = UserContext.getUserId();
        return userJobActionMapper.selectOne(Wrappers.lambdaQuery(UserJobAction.class)
                .eq(UserJobAction::getUserId, userId)
                .eq(UserJobAction::getJobId, jobId));
    }

    private Map<String, Integer> buildActionOrderMap(List<String> actionJobIds) {
        Map<String, Integer> actionOrderMap = new java.util.HashMap<>();
        for (int index = 0; index < actionJobIds.size(); index++) {
            actionOrderMap.putIfAbsent(actionJobIds.get(index), index);
        }
        return actionOrderMap;
    }

    private boolean hasMore(JobPageRequestV2 requestParam, int total) {
        JobPageRequestV2 request = requestParam == null ? new JobPageRequestV2() : requestParam;
        long current = request.getCurrent() <= 0 ? 1 : request.getCurrent();
        long size = request.getSize() <= 0 ? 10 : request.getSize();
        return current * size < total;
    }

    /**
     * 这里做的是内存分页，因为当前先按用户行为表筛出职位，再统一做卡片组装和过滤。
     */
    private List<RecommendJobResponse> paginate(List<RecommendJobResponse> records, JobPageRequestV2 requestParam) {
        JobPageRequestV2 request = requestParam == null ? new JobPageRequestV2() : requestParam;
        int total = records.size();
        int current = request.getCurrent() <= 0 ? 1 : (int) request.getCurrent();
        int size = request.getSize() <= 0 ? 10 : (int) request.getSize();
        int fromIndex = Math.min((current - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        return records.subList(fromIndex, toIndex);
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

    private boolean matchesFilters(RecommendJobResponse record, JobPageRequestV2 request) {
        return containsIgnoreCase(record.getTitle(), request.getKeyword())
                || containsIgnoreCase(record.getCompanyName(), request.getKeyword())
                || containsIgnoreCase(record.getMeta(), request.getKeyword())
                ? matchesRemainingFilters(record, request)
                : !StringUtils.hasText(request.getKeyword()) && matchesRemainingFilters(record, request);
    }

    private boolean matchesRemainingFilters(RecommendJobResponse record, JobPageRequestV2 request) {
        if (StringUtils.hasText(request.getCountry()) && !"中国大陆".equals(request.getCountry())) {
            return false;
        }
        if (!containsIgnoreCase(record.getTitle(), request.getTitle())) {
            return false;
        }
        if (!containsIgnoreCase(record.getCity(), request.getCity())) {
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
        if (!equalsIfPresent(record.getEducationRequirement(), request.getEducationRequirement())) {
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
