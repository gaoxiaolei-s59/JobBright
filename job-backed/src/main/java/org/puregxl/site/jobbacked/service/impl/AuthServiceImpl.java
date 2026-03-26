package org.puregxl.site.jobbacked.service.impl;

import org.puregxl.site.jobbacked.dto.req.LoginRequest;
import org.puregxl.site.jobbacked.dto.req.RegisterRequest;
import org.puregxl.site.jobbacked.dto.resp.AuthResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfileResponse;
import org.puregxl.site.jobbacked.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 用户注册
     * @param request
     * @return
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        return null;
    }

    /**
     * 用户登录
     * @param request
     * @return
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    /**
     * 获取当前用户
     * @param currentUserId
     * @return
     */
    @Override
    public UserProfileResponse currentUser(Long currentUserId) {
        return null;
    }
}
