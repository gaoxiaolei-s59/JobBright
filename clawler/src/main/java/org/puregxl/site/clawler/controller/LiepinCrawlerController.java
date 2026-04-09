package org.puregxl.site.clawler.controller;

import jakarta.validation.Valid;
import org.puregxl.site.clawler.dto.LiepinBatchCrawlRequest;
import org.puregxl.site.clawler.dto.LiepinBatchCrawlResponse;
import org.puregxl.site.clawler.dto.LiepinCrawlResponse;
import org.puregxl.site.clawler.dto.LiepinSearchRequest;
import org.puregxl.site.clawler.service.LiepinCrawlerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/liepin")
public class LiepinCrawlerController {

    private final LiepinCrawlerService liepinCrawlerService;

    public LiepinCrawlerController(LiepinCrawlerService liepinCrawlerService) {
        this.liepinCrawlerService = liepinCrawlerService;
    }

    @PostMapping("/crawl")
    public LiepinCrawlResponse crawl(@Valid @RequestBody LiepinSearchRequest request) {
        return liepinCrawlerService.crawl(request);
    }

    @PostMapping("/crawl/batch")
    public LiepinBatchCrawlResponse crawlBatch(@Valid @RequestBody LiepinBatchCrawlRequest request) {
        return liepinCrawlerService.crawlBatch(request);
    }
}
