package org.puregxl.site.jobbacked.task;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.jobbacked.service.FileStorageService;
import org.puregxl.site.jobbacked.service.impl.FileStorageServiceImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownLoadCompanyLogo {
    private final FileStorageService fileStorageService;

    /**
     * 定时任务 - 定期把公司的logo下载到本地的云存储中
     */
    public void run() {

    }
}
