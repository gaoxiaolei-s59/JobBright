package org.puregxl.site.jobbacked.task;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.jobbacked.dao.mapper.CompanyMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 下载公司Logo定时任务
 */
@Component
@RequiredArgsConstructor
public class UploadCompanyLogo {

    private final CompanyMapper companyMapper;

    /**
     * 开发定时任务 - 定期执行来自爬虫端的job职位的清洗
     */
    @Scheduled(fixedRate = 5000)
    public void run() {
        System.out.println("每 5 秒执行一次");
    }

}
