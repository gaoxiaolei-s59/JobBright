package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfilePreferencesResponse {

    private String targetRole;

    private String expectedCity;

    private List<String> jobTypes;

    private Boolean openToRemote;

    private Boolean requireVisaSupport;
}
