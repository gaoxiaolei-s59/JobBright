package org.puregxl.site.jobbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dto.resp.UserResumePreviewResponse;
import org.puregxl.site.jobbacked.dto.resp.UserResumeResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserResumeService extends IService<UserResumeFile> {
    void uploadUserResume(MultipartFile file);


    UserResumeResponse getResumeMessage();

    UserResumePreviewResponse getResumePreview(String resumeId);

    ResponseEntity<Resource> getResumeFile(String resumeId);
}
