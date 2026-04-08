package org.puregxl.site.clawler.dto;

import org.puregxl.site.clawler.entity.JobPosting;

import java.util.List;

public record ZhaopinCrawlResponse(
        int page,
        int pageSize,
        int totalFetched,
        int insertedCount,
        int updatedCount,
        List<JobPosting> postings
) {
}
