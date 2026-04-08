package org.puregxl.site.clawler.crawler;

import org.puregxl.site.clawler.entity.JobPosting;

import java.util.List;

public record CrawlResult(
        int totalFetched,
        int insertedCount,
        int updatedCount,
        List<JobPosting> postings
) {
}
