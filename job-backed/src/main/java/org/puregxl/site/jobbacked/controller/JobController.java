package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
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
     * 推荐职位接口
     *
     * @return
     */
    @GetMapping("/recommended")
    public Result<RecommendJobListResponse> getRecommendJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(recommendService.getRecommendJobsV2(requestParam));
    }


    /**
     * 获取喜欢的职位列表
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/favorites")
    public Result<RecommendJobListResponse> getFavoritesJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(JobService.getFavoritesJob(requestParam));
    }


    /**
     * 获取已经投递的职位列表
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/applied")
    public Result<RecommendJobListResponse> getAppliedJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(JobService.getAppliedJob(requestParam));
    }

    /**
     * 喜欢职位
     *
     * @param jobId
     * @return
     */
    @PostMapping("/api/jobs/{jobId}/favorite")
    public Result<Void> favoritesJob(@PathVariable Long jobId) {
        jobService.favoritesJob(jobId);
        return Results.success();
    }


    /**
     * 投递职位
     *
     * @param jobId
     * @return
     */
    @PostMapping("/api/jobs/{jobId}/apply")
    public Result<Void> applyJob(@PathVariable Long jobId) {
        jobService.applyJob(jobId);
        return Results.success();
    }


}
