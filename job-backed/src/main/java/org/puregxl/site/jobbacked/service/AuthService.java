package org.puregxl.site.jobbacked.service;

import jakarta.validation.Valid;
import org.puregxl.site.jobbacked.dto.req.LoginRequest;
import org.puregxl.site.jobbacked.dto.req.RegisterRequest;
import org.puregxl.site.jobbacked.dto.resp.AuthResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfileResponse;


public interface AuthService {

    AuthResponse register(@Valid RegisterRequest request);

    AuthResponse login(@Valid LoginRequest request);

    UserProfileResponse currentUser(Long currentUserId);
}
