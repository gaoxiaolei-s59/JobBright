package org.puregxl.site.jobbacked.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.dao.entity.UserJobAction;
import org.puregxl.site.jobbacked.dao.mapper.UserJobActionMapper;
import org.puregxl.site.jobbacked.service.JobService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final UserJobActionMapper userJobActionMapper;

    /**
     * 喜欢职位 - 可能会有并发问题？
     * @param jobId
     */
    @Override
    public void favoritesJob(Long jobId) {

        long userId = UserContext.getUserId();
        LambdaQueryWrapper<UserJobAction> userJobActionLambdaQueryWrapper = Wrappers.lambdaQuery(UserJobAction.class)
                .eq(UserJobAction::getUserId, userId)
                .eq(UserJobAction::getJobId, jobId);

        UserJobAction userJobAction = userJobActionMapper.selectOne(userJobActionLambdaQueryWrapper);

        /**如果没有查询到 - 执行新增操作**/
        if (userJobAction == null) {
            UserJobAction.builder()
                    .userId(userId)
                    .jobId(jobId)
                    .liked(1)
                    .applied(0)
                    .appliedTime(null)
                    .lastViewTime(new Date())
                    .build();
        }

    }
}
