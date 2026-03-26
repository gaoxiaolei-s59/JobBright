package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse{
    private Long id;

    private String username;


    private String email;


    private String displayName;


    private String role;
}
