package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.AppliedJobResponse;
import org.puregxl.site.jobbacked.dto.resp.FavoritesJobResponse;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobListResponse;
import org.puregxl.site.jobbacked.service.JobService;
import org.puregxl.site.jobbacked.service.RecommendService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final RecommendService recommendService;

    private final JobService jobService;

    /**
     * 推荐职位列表。
     * 支持分页，并会带回当前用户对职位的收藏/投递状态。
     *
     * @return
     */
    @GetMapping("/recommended")
    public Result<RecommendJobListResponse> getRecommendJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(recommendService.getRecommendJobsV2(requestParam));
    }


    /**
     * 获取当前用户已收藏的职位列表。
     * 返回结构与推荐列表一致，前端可以直接复用职位卡片组件。
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/favorites")
    public Result<FavoritesJobResponse> getFavoritesJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(jobService.getFavoritesJob(requestParam));
    }


    /**
     * 获取当前用户已投递的职位列表。
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/applied")
    public Result<AppliedJobResponse> getAppliedJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(jobService.getAppliedJob(requestParam));
    }

    /**
     * 标记职位为收藏。
     *
     * @param jobId
     * @return
     */
    @PostMapping("/{jobId}/favorite")
    public Result<Void> favoritesJob(@PathVariable String jobId) {
        jobService.favoritesJob(jobId);
        return Results.success();
    }

    /**
     * 取消职位收藏标记。
     *
     * @param jobId
     * @return
     */
    @DeleteMapping("/{jobId}/favorite")
    public Result<Void> cancelFavoritesJob(@PathVariable String jobId) {
        jobService.cancelFavoritesJob(jobId);
        return Results.success();
    }


    /**
     * 标记职位为已投递。
     *
     * @param jobId
     * @return
     */
    @PostMapping("/{jobId}/apply")
    public Result<Void> applyJob(@PathVariable String jobId) {
        jobService.applyJob(jobId);
        return Results.success();
    }

    /**
     * 取消职位已投递标记。
     *
     * @param jobId
     * @return
     */
    @DeleteMapping("/{jobId}/apply")
    public Result<Void> cancelApplyJob(@PathVariable String jobId) {
        jobService.cancelApplyJob(jobId);
        return Results.success();
    }

}
