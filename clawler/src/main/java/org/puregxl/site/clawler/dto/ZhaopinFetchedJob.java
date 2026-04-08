package org.puregxl.site.clawler.dto;

import org.puregxl.site.clawler.entity.JobPosting;

import java.util.List;

public record ZhaopinFetchedJob(
        JobPosting posting,
        Long jobId,
        String positionNumber,
        Long companyId,
        String cityName,
        String cityDistrict,
        String salaryText,
        String workExperience,
        String education,
        String companySize,
        String companyProperty,
        String industryName,
        String companyLogoUrl,
        String publishTimeText,
        String rawJson,
        List<ZhaopinJobRawTagItem> tags
) {
}
