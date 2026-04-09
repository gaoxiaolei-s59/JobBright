package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.resp.UserOnboardingStatusResponse;
import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;
import org.puregxl.site.jobbacked.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public Result<UserDashboardResponse> getDashboard() {
        return Results.success(userService.getDashboard());
    }

    @GetMapping("/onboarding/status")
    public Result<UserOnboardingStatusResponse> getOnboardingStatus() {
        return Results.success(userService.getOnboardingStatus());
    }

}
