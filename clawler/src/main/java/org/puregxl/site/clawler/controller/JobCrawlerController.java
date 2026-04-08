package org.puregxl.site.clawler.controller;

import jakarta.validation.Valid;

import org.puregxl.site.clawler.crawler.CrawlResult;
import org.puregxl.site.clawler.dto.CrawlRequest;
import org.puregxl.site.clawler.service.JobCrawlerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crawler")
public class JobCrawlerController {

    private final JobCrawlerService jobCrawlerService;

    public JobCrawlerController(JobCrawlerService jobCrawlerService) {
        this.jobCrawlerService = jobCrawlerService;
    }

    @PostMapping("/crawl")
    @ResponseStatus(HttpStatus.OK)
    public CrawlResult crawl(@Valid @RequestBody CrawlRequest request) {
        return jobCrawlerService.crawl(request.toConfig());
    }
}
