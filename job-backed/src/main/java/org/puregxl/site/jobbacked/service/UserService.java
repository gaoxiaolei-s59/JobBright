package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.resp.UserDashboardResponse;
import org.puregxl.site.jobbacked.dto.resp.UserOnboardingStatusResponse;

public interface UserService {

    UserDashboardResponse getDashboard();

    UserOnboardingStatusResponse getOnboardingStatus();
}
