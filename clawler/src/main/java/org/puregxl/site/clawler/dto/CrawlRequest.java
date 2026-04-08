package org.puregxl.site.clawler.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.puregxl.site.clawler.crawler.FieldSelector;
import org.puregxl.site.clawler.crawler.SiteCrawlConfig;

public record CrawlRequest(
        @NotBlank String siteName,
        @NotBlank String pageUrl,
        @NotBlank String itemSelector,
        @Valid @NotNull FieldSelector title,
        @Valid FieldSelector company,
        @Valid FieldSelector location,
        @Valid FieldSelector salary,
        @Valid FieldSelector summary,
        @Valid FieldSelector jobLink,
        Integer maxItems
) {
    public SiteCrawlConfig toConfig() {
        return new SiteCrawlConfig(
                siteName,
                pageUrl,
                itemSelector,
                title,
                company,
                location,
                salary,
                summary,
                jobLink,
                maxItems
        );
    }
}
