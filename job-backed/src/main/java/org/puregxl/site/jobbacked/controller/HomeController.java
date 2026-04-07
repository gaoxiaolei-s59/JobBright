package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.resp.HomeOverviewResponse;
import org.puregxl.site.jobbacked.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 首页概览信息
     * @return
     */
    @GetMapping("/overview")
    public Result<HomeOverviewResponse> getOverview() {
        return Results.success(homeService.getOverview());
    }



}
