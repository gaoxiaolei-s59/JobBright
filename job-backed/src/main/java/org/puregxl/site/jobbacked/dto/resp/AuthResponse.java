package org.puregxl.site.jobbacked.dto.resp;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private UserProfileResponse user;
}
