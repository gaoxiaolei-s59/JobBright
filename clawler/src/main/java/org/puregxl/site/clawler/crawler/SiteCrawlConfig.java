package org.puregxl.site.clawler.crawler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SiteCrawlConfig(
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
}
