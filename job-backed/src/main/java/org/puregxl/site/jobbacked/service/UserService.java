package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.req.UserProfilePreferencesUpdateRequest;
import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;
import org.puregxl.site.jobbacked.dto.resp.UserOnboardingStatusResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfilePreferencesResponse;

public interface UserService {

    UserDashboardResponse getDashboard();

    UserOnboardingStatusResponse getOnboardingStatus();

    UserProfilePreferencesResponse getProfilePreferences();

    void updateProfilePreferences(UserProfilePreferencesUpdateRequest request);
}
