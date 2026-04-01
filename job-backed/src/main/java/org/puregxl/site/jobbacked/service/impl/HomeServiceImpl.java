package org.puregxl.site.jobbacked.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.context.UserInfoDTO;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.resp.HomeOverviewResponse;
import org.puregxl.site.jobbacked.service.HomeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final int DEFAULT_FRESH_JOB_COUNT = 1286;
    private static final int DEFAULT_AVERAGE_MATCH_RATE = 82;
    private static final int DEFAULT_AVERAGE_SHORTLIST_MINUTES = 9;
    private static final int DEFAULT_RESUME_SCORE = 82;
    private static final int DEFAULT_PROFILE_COMPLETION_RATE = 42;

    private final UserResumeFileMapper userResumeFileMapper;

    @Override
    public HomeOverviewResponse getOverview() {
        String currentUserId = UserContext.getUserId();
        UserResumeFile currentResume = userResumeFileMapper.selectOne(Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, Long.parseLong(currentUserId))
                .eq(UserResumeFile::getIsCurrent, 1));

        return HomeOverviewResponse.builder()
                .freshJobCount(DEFAULT_FRESH_JOB_COUNT)
                .averageMatchRate(DEFAULT_AVERAGE_MATCH_RATE)
                .averageShortlistMinutes(DEFAULT_AVERAGE_SHORTLIST_MINUTES)
                .resumeScore(currentResume == null ? 0 : DEFAULT_RESUME_SCORE)
                .profileCompletionRate(calculateProfileCompletionRate(UserContext.getDTO()))
                .build();
    }

    private Integer calculateProfileCompletionRate(UserInfoDTO userInfoDTO) {
        if (userInfoDTO == null) {
            return 0;
        }
        int completionRate = 0;
        if (StringUtils.hasText(userInfoDTO.getUsername())) {
            completionRate += 14;
        }
        if (StringUtils.hasText(userInfoDTO.getEmail())) {
            completionRate += 14;
        }
        if (StringUtils.hasText(userInfoDTO.getDisplayName())) {
            completionRate += 14;
        }
        return completionRate == 0 ? DEFAULT_PROFILE_COMPLETION_RATE : completionRate;
    }
}
