package org.puregxl.site.clawler.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.service.JobPostingQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    private final JobPostingQueryService jobPostingQueryService;

    public JobPostingController(JobPostingQueryService jobPostingQueryService) {
        this.jobPostingQueryService = jobPostingQueryService;
    }

    @GetMapping
    public Page<JobPosting> queryJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sourceSite,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return jobPostingQueryService.query(keyword, sourceSite, page, size);
    }
}
