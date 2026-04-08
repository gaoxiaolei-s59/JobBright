package org.puregxl.site.clawler.dto;

import org.puregxl.site.clawler.entity.JobPosting;

import java.util.List;

public record ZhaopinBatchCrawlResponse(
        int startPage,
        int endPage,
        int pageSize,
        int targetCount,
        int totalFetched,
        int insertedCount,
        int updatedCount,
        List<JobPosting> postings
) {
}
