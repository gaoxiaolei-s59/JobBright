package org.puregxl.site.jobbacked.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.dto.req.LoginRequest;
import org.puregxl.site.jobbacked.dto.req.RegisterRequest;
import org.puregxl.site.jobbacked.dto.resp.AuthResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfileResponse;
import org.puregxl.site.jobbacked.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    /**
     * 用户注册
     * @param request
     * @return
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Results.success();
    }


    /**
     * 用户登录
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Results.success(authService.login(request));
    }

    /**
     * 检查当前用户状态
     * @return
     */
    @GetMapping("/me")
    public Result<UserProfileResponse> currentUser() {
        long currentUserId = UserContext.getUserId();
        return Results.success(authService.currentUser(currentUserId));
    }
}
