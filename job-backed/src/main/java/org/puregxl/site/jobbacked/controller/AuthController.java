package org.puregxl.site.jobbacked.controller;

import jakarta.validation.Valid;
import org.puregxl.site.framework.common.ApiResponse;
import org.puregxl.site.framework.context.AuthContext;
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
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> currentUser() {
        return ApiResponse.success(authService.currentUser(AuthContext.getCurrentUserId()));
    }
}
