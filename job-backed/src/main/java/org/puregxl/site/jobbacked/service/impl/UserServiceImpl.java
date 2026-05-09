package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.context.UserInfoDTO;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.entity.UserResumeProfile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeProfileMapper;
import org.puregxl.site.jobbacked.dto.req.UserProfilePreferencesUpdateRequest;
import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;
import org.puregxl.site.jobbacked.dto.resp.UserOnboardingStatusResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfilePreferencesResponse;
import org.puregxl.site.jobbacked.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PLAN_NAME = "免费版";
    private static final int DEFAULT_RESUME_SCORE = 82;
    private static final int DEFAULT_PROFILE_COMPLETION_RATE = 42;

    private final UserResumeFileMapper userResumeFileMapper;
    private final UserResumeProfileMapper userResumeProfileMapper;

    @Override
    public UserDashboardResponse getDashboard() {
        UserInfoDTO userInfo = UserContext.getDTO();
        UserResumeFile currentResume = getCurrentResume();
        Integer profileCompletionRate = calculateProfileCompletionRate(userInfo);

        return UserDashboardResponse.builder()
                .displayName(resolveDisplayName(userInfo))
                .planName(DEFAULT_PLAN_NAME)
                .resumeScore(resolveResumeScore(currentResume))
                .profileCompletionRate(profileCompletionRate)
                .tips(buildTips(userInfo, currentResume))
                .build();
    }

    @Override
    public UserOnboardingStatusResponse getOnboardingStatus() {
        UserResumeFile currentResume = getCurrentResume();
        UserResumeProfile latestProfile = getLatestProfile();
        boolean profileCompleted = isProfileCompleted(latestProfile);
        boolean resumeUploaded = currentResume != null;
        return UserOnboardingStatusResponse.builder()
                .profileCompleted(profileCompleted)
                .resumeUploaded(resumeUploaded)
                .onboardingCompleted(profileCompleted && resumeUploaded)
                .build();
    }

    @Override
    public UserProfilePreferencesResponse getProfilePreferences() {
        UserResumeProfile latestProfile = getLatestProfile();
        if (latestProfile == null) {
            return UserProfilePreferencesResponse.builder()
                    .targetRole("")
                    .expectedCity("")
                    .jobTypes(Collections.emptyList())
                    .openToRemote(true)
                    .requireVisaSupport(false)
                    .build();
        }
        List<String> targetRoles = parseTagList(latestProfile.getTargetRoles());
        List<String> preferredCities = parseTagList(latestProfile.getPreferredCities());
        List<String> jobTypes = parseTagList(latestProfile.getPreferredJobType());
        PreferenceRemark remark = parsePreferenceRemark(latestProfile.getRemark());
        String expectedCity = preferredCities.stream()
                .filter(city -> !"远程".equals(city))
                .findFirst()
                .orElse("");
        boolean openToRemote = remark.isOpenToRemote() || preferredCities.contains("远程");
        return UserProfilePreferencesResponse.builder()
                .targetRole(targetRoles.isEmpty() ? "" : targetRoles.get(0))
                .expectedCity(expectedCity)
                .jobTypes(jobTypes)
                .openToRemote(openToRemote)
                .requireVisaSupport(remark.isRequireVisaSupport())
                .build();
    }

    @Override
    public void updateProfilePreferences(UserProfilePreferencesUpdateRequest request) {
        if (request == null) {
            throw new ClientException("求职偏好不能为空");
        }
        String targetRole = trimToEmpty(request.getTargetRole());
        String expectedCity = trimToEmpty(request.getExpectedCity());
        List<String> jobTypes = normalizeList(request.getJobTypes());
        boolean openToRemote = request.getOpenToRemote() == null || request.getOpenToRemote();

        if (!StringUtils.hasText(targetRole)) {
            throw new ClientException("目标岗位不能为空");
        }
        if (jobTypes.isEmpty()) {
            throw new ClientException("求职类型不能为空");
        }
        if (!StringUtils.hasText(expectedCity) && !openToRemote) {
            throw new ClientException("期望城市不能为空");
        }

        long currentUserId = UserContext.getUserId();
        UserResumeProfile latestProfile = getLatestProfile();
        List<String> preferredCities = new ArrayList<>();
        if (StringUtils.hasText(expectedCity)) {
            preferredCities.add(expectedCity);
        }
        if (openToRemote) {
            preferredCities.add("远程");
        }

        UserResumeProfile profile = UserResumeProfile.builder()
                .userId(currentUserId)
                .targetRoles(JSONUtil.toJsonStr(List.of(targetRole)))
                .preferredCities(JSONUtil.toJsonStr(preferredCities))
                .preferredJobType(JSONUtil.toJsonStr(jobTypes))
                .status("SUCCESS")
                .remark(JSONUtil.toJsonStr(new PreferenceRemark(openToRemote, Boolean.TRUE.equals(request.getRequireVisaSupport()))))
                .delFlag(0)
                .build();
        if (latestProfile == null) {
            profile.setProfileId(IdUtil.fastSimpleUUID());
            userResumeProfileMapper.insert(profile);
            return;
        }
        profile.setId(latestProfile.getId());
        userResumeProfileMapper.updateById(profile);
    }

    private UserResumeFile getCurrentResume() {
        long currentUserId = UserContext.getUserId();
        if (currentUserId <= 0) {
            return null;
        }
        return userResumeFileMapper.selectOne(Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, currentUserId)
                .eq(UserResumeFile::getIsCurrent, 1)
                .eq(UserResumeFile::getDelFlag, 0)
                .last("limit 1"));
    }

    private UserResumeProfile getLatestProfile() {
        long currentUserId = UserContext.getUserId();
        if (currentUserId <= 0) {
            return null;
        }
        return userResumeProfileMapper.selectOne(Wrappers.lambdaQuery(UserResumeProfile.class)
                .eq(UserResumeProfile::getUserId, currentUserId)
                .eq(UserResumeProfile::getDelFlag, 0)
                .orderByDesc(UserResumeProfile::getUpdateTime)
                .orderByDesc(UserResumeProfile::getId)
                .last("limit 1"));
    }

    private String resolveDisplayName(UserInfoDTO userInfo) {
        if (userInfo == null) {
            return "";
        }
        if (StringUtils.hasText(userInfo.getDisplayName())) {
            return userInfo.getDisplayName();
        }
        if (StringUtils.hasText(userInfo.getUserName())) {
            return userInfo.getUserName();
        }
        return "";
    }

    private Integer resolveResumeScore(UserResumeFile currentResume) {
        if (currentResume == null) {
            return 0;
        }
        if (currentResume.getScore() != null) {
            return currentResume.getScore().intValue();
        }
        return DEFAULT_RESUME_SCORE;
    }

    private Integer calculateProfileCompletionRate(UserInfoDTO userInfo) {
        if (userInfo == null) {
            return 0;
        }
        int completionRate = 0;
        if (StringUtils.hasText(userInfo.getUserName())) {
            completionRate += 14;
        }
        if (StringUtils.hasText(userInfo.getEmail())) {
            completionRate += 14;
        }
        if (StringUtils.hasText(userInfo.getDisplayName())) {
            completionRate += 14;
        }
        return completionRate == 0 ? DEFAULT_PROFILE_COMPLETION_RATE : completionRate;
    }

    private List<String> buildTips(UserInfoDTO userInfo, UserResumeFile currentResume) {
        List<String> tips = new ArrayList<>();
        if (currentResume == null) {
            tips.add("上传最新简历");
        }
        if (userInfo == null || !StringUtils.hasText(userInfo.getDisplayName())) {
            tips.add("完善个人资料");
        } else {
            tips.add("设置期望城市");
        }
        tips.add("补充岗位关键词");
        return tips;
    }

    private boolean isProfileCompleted(UserResumeProfile profile) {
        if (profile == null) {
            return false;
        }
        List<String> targetRoles = parseTagList(profile.getTargetRoles());
        List<String> preferredCities = parseTagList(profile.getPreferredCities());
        List<String> preferredJobTypes = parseTagList(profile.getPreferredJobType());
        return !targetRoles.isEmpty() && !preferredCities.isEmpty() && !preferredJobTypes.isEmpty();
    }

    private List<String> parseTagList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        String sanitized = raw.trim();
        if (sanitized.startsWith("[") && sanitized.endsWith("]")) {
            sanitized = sanitized.substring(1, sanitized.length() - 1);
        }
        if (!StringUtils.hasText(sanitized)) {
            return Collections.emptyList();
        }
        return List.of(sanitized.split("[,，|/]")).stream()
                .map(item -> item.replace("\"", "").trim())
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(this::trimToEmpty)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private PreferenceRemark parsePreferenceRemark(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new PreferenceRemark(false, false);
        }
        try {
            return JSONUtil.toBean(raw, PreferenceRemark.class);
        } catch (Exception ignored) {
            return new PreferenceRemark(false, false);
        }
    }

    private static class PreferenceRemark {
        private Boolean openToRemote;
        private Boolean requireVisaSupport;

        public PreferenceRemark() {
        }

        private PreferenceRemark(Boolean openToRemote, Boolean requireVisaSupport) {
            this.openToRemote = openToRemote;
            this.requireVisaSupport = requireVisaSupport;
        }

        public Boolean getOpenToRemote() {
            return openToRemote;
        }

        public void setOpenToRemote(Boolean openToRemote) {
            this.openToRemote = openToRemote;
        }

        public Boolean getRequireVisaSupport() {
            return requireVisaSupport;
        }

        public void setRequireVisaSupport(Boolean requireVisaSupport) {
            this.requireVisaSupport = requireVisaSupport;
        }

        private boolean isOpenToRemote() {
            return Boolean.TRUE.equals(openToRemote);
        }

        private boolean isRequireVisaSupport() {
            return Boolean.TRUE.equals(requireVisaSupport);
        }
    }
}
