package org.puregxl.site.jobbacked.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class UserProfilePreferencesUpdateRequest {

    private String targetRole;

    private String expectedCity;

    private List<String> jobTypes;

    private Boolean openToRemote;

    private Boolean requireVisaSupport;
}
