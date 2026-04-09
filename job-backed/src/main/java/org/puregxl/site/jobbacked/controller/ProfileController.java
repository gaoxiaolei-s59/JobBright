package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.req.UserProfilePreferencesUpdateRequest;
import org.puregxl.site.jobbacked.dto.resp.UserProfilePreferencesResponse;
import org.puregxl.site.jobbacked.service.ProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/preferences")
    public Result<UserProfilePreferencesResponse> getPreferences() {
        return Results.success(profileService.getPreferences());
    }

    @PutMapping("/preferences")
    public Result<Void> updatePreferences(@RequestBody UserProfilePreferencesUpdateRequest request) {
        profileService.updatePreferences(request);
        return Results.success();
    }
}
