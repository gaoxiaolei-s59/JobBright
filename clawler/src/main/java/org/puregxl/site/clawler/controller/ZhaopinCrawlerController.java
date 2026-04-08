package org.puregxl.site.clawler.controller;

import jakarta.validation.Valid;

import org.puregxl.site.clawler.dto.ZhaopinBatchCrawlRequest;
import org.puregxl.site.clawler.dto.ZhaopinBatchCrawlResponse;
import org.puregxl.site.clawler.dto.ZhaopinCrawlResponse;
import org.puregxl.site.clawler.dto.ZhaopinSearchRequest;
import org.puregxl.site.clawler.service.ZhaopinCrawlerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zhaopin")
public class ZhaopinCrawlerController {

    private final ZhaopinCrawlerService zhaopinCrawlerService;

    public ZhaopinCrawlerController(ZhaopinCrawlerService zhaopinCrawlerService) {
        this.zhaopinCrawlerService = zhaopinCrawlerService;
    }

    @PostMapping("/crawl")
    public ZhaopinCrawlResponse crawl(@Valid @RequestBody ZhaopinSearchRequest request) {
        return zhaopinCrawlerService.crawl(request);
    }

    @PostMapping("/crawl/batch")
    public ZhaopinBatchCrawlResponse crawlBatch(@Valid @RequestBody ZhaopinBatchCrawlRequest request) {
        return zhaopinCrawlerService.crawlBatch(request);
    }
}
