package org.puregxl.site.jobbacked.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.jobbacked.dto.resp.UserResumeResponse;
import org.puregxl.site.jobbacked.service.UserResumeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/resume")
@RequiredArgsConstructor
public class UserResumeController {

    private final UserResumeService userResumeService;

    /**
     * 上传简历接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<Void> uploadUserResume(@RequestParam("file") MultipartFile file) {
        userResumeService.uploadUserResume(file);
        return Results.success();
    }


    /**
     * 获取用户当前简历
     * @return
     */
    @GetMapping("/current")
    public Result<UserResumeResponse> getResumeMessage() {
        return Results.success(userResumeService.getResumeMessage());
    }
}
