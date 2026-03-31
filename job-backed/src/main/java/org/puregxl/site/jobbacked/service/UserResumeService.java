package org.puregxl.site.jobbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.springframework.web.multipart.MultipartFile;

public interface UserResumeService extends IService<UserResumeFile> {
    void uploadUserResume(MultipartFile file);
}
