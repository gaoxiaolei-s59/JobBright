package org.puregxl.site.rag.llm.profile;

import org.puregxl.site.rag.dto.profile.ResumeProfileDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ResumeProfilePostProcessor {

    private static final Set<String> CITY_HINTS = Set.of(
            "意向城市", "工作地点", "base", "上海", "杭州", "深圳", "广州", "成都", "北京"
    );

    private ResumeProfilePostProcessor() {
    }

    public static PostProcessResult clean(ResumeProfileDTO dto, String sourceText) {
        if (dto == null) {
            return new PostProcessResult(null, List.of("画像DTO为空，无法清洗"));
        }

        List<String> warnings = new ArrayList<>();
        dto.setSchool_tags(cleanSchoolTags(dto, warnings));
        dto.setPreferred_cities(cleanPreferredCities(dto, sourceText, warnings));
        dto.setTarget_roles(deduplicate(dto.getTarget_roles()));
        dto.setCore_skills(deduplicate(dto.getCore_skills()));
        dto.setProject_tags(deduplicate(dto.getProject_tags()));
        dto.setIndustry_tags(deduplicate(dto.getIndustry_tags()));
        dto.setStrengths(deduplicate(dto.getStrengths()));
        return new PostProcessResult(dto, warnings);
    }

    private static List<String> cleanSchoolTags(ResumeProfileDTO dto, List<String> warnings) {
        List<String> schoolTags = deduplicate(dto.getSchool_tags());
        if (schoolTags.isEmpty()) {
            return schoolTags;
        }

        List<String> cleaned = schoolTags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(tag -> !tag.equals(dto.getSchool_name()))
                .toList();

        if (cleaned.size() != schoolTags.size()) {
            warnings.add("school_tags与school_name重复，已自动清洗");
        }
        return cleaned;
    }

    private static List<String> cleanPreferredCities(ResumeProfileDTO dto, String sourceText, List<String> warnings) {
        List<String> preferredCities = deduplicate(dto.getPreferred_cities());
        if (preferredCities.isEmpty()) {
            return preferredCities;
        }
        if (!containsExplicitCityIntent(sourceText)) {
            warnings.add("preferred_cities存在明显臆造，已自动清洗");
            return List.of();
        }
        return preferredCities;
    }

    private static boolean containsExplicitCityIntent(String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return false;
        }
        return CITY_HINTS.stream().anyMatch(sourceText::contains);
    }

    private static List<String> deduplicate(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                deduplicated.add(value.trim());
            }
        }
        return new ArrayList<>(deduplicated);
    }

    public record PostProcessResult(
            ResumeProfileDTO dto,
            List<String> warnings
    ) {
    }
}
