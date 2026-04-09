package org.puregxl.site.clawler.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LiepinBatchCrawlRequest(
        @NotBlank String keyword,
        String cityCode,
        @Min(1) Integer startPage,
        @Min(1) @Max(90) Integer pageSize,
        @Min(1) @Max(1000) Integer targetCount,
        @Min(1) Integer maxPages
) {
}
