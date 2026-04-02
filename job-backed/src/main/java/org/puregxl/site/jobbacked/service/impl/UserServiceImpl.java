package org.puregxl.site.jobbacked.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.context.UserInfoDTO;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;
import org.puregxl.site.jobbacked.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PLAN_NAME = "免费版";
    private static final int DEFAULT_RESUME_SCORE = 82;
    private static final int DEFAULT_PROFILE_COMPLETION_RATE = 42;

    private final UserResumeFileMapper userResumeFileMapper;

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

    private UserResumeFile getCurrentResume() {
        String currentUserId = UserContext.getUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return null;
        }
        return userResumeFileMapper.selectOne(Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, Long.parseLong(currentUserId))
                .eq(UserResumeFile::getIsCurrent, 1)
                .eq(UserResumeFile::getDelFlag, 0)
                .last("limit 1"));
    }

    private String resolveDisplayName(UserInfoDTO userInfo) {
        if (userInfo == null) {
            return "";
        }
        if (StringUtils.hasText(userInfo.getDisplayName())) {
            return userInfo.getDisplayName();
        }
        if (StringUtils.hasText(userInfo.getUsername())) {
            return userInfo.getUsername();
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
        if (StringUtils.hasText(userInfo.getUsername())) {
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
}
