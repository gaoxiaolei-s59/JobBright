package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.req.JobPageRequestV2;
import org.puregxl.site.jobbacked.dto.resp.RecommendJobListResponse;
import org.puregxl.site.jobbacked.service.RecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final RecommendService recommendService;

    /**
     * 推荐职位接口
     * @return
     */
    @GetMapping("/recommended")
    public Result<RecommendJobListResponse> getRecommendJob(@ModelAttribute JobPageRequestV2 requestParam) {
        return Results.success(recommendService.getRecommendJobsV2(requestParam));
    }


}
