package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.service.UserResumeService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/resume")
@RequiredArgsConstructor
public class UserResumeController {

    private final UserResumeService userResumeService;

    @PostMapping("/upload")
    public Result<Void> uploadUserResume(@RequestParam("file") MultipartFile file) {
        userResumeService.uploadUserResume(file);
        return Results.success();
    }
}
