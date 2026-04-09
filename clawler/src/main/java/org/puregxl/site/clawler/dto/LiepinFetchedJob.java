package org.puregxl.site.clawler.dto;

import org.puregxl.site.clawler.entity.JobPosting;

public record LiepinFetchedJob(
        JobPosting posting,
        String sourceJobId,
        String sourceCompanyId,
        String companySize,
        String industryName,
        String companyLogoUrl,
        String publishTimeText,
        String companyStage,
        String rawJson
) {
}
