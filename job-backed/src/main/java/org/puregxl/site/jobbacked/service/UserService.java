package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.resp.UserOnboardingStatusResponse;
import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;

public interface UserService {

    UserDashboardResponse getDashboard();

    UserOnboardingStatusResponse getOnboardingStatus();
}
