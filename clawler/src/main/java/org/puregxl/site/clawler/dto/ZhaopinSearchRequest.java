package org.puregxl.site.clawler.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ZhaopinSearchRequest(
        @NotBlank String keyword,
        @NotNull Integer cityId,
        @Min(1) Integer page,
        @Min(1) @Max(90) Integer pageSize,
        Integer workExperience,
        Integer education,
        Integer companyType,
        Integer employmentType
) {
}
