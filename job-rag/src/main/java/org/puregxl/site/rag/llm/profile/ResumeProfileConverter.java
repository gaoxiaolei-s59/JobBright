package org.puregxl.site.rag.llm.profile;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import org.puregxl.site.rag.dao.entity.UserResumeProfile;
import org.puregxl.site.rag.dto.profile.ResumeProfileDTO;

import java.util.List;

public final class ResumeProfileConverter {

    private ResumeProfileConverter() {
    }

    public static ResumeProfileDTO toDto(String profileJson) {
        return JSONUtil.toBean(profileJson, ResumeProfileDTO.class);
    }

    public static UserResumeProfile toEntity(ResumeProfileDTO dto, String profileJson, String resumeId) {
        return UserResumeProfile.builder()
                .profileId(IdUtil.fastSimpleUUID())
                .resumeId(resumeId)
                .candidateName(trimToNull(dto.getCandidate_name()))
                .targetRoles(writeArrayField(dto.getTarget_roles()))
                .seniority(trimToNull(dto.getSeniority()))
                .educationLevel(trimToNull(dto.getEducation_level()))
                .schoolName(trimToNull(dto.getSchool_name()))
                .majorName(trimToNull(dto.getMajor_name()))
                .schoolTags(writeArrayField(dto.getSchool_tags()))
                .coreSkills(writeArrayField(dto.getCore_skills()))
                .projectTags(writeArrayField(dto.getProject_tags()))
                .industryTags(writeArrayField(dto.getIndustry_tags()))
                .strengths(writeArrayField(dto.getStrengths()))
                .preferredCities(writeArrayField(dto.getPreferred_cities()))
                .preferredJobType(trimToNull(dto.getPreferred_job_type()))
                .internshipMonths(trimToNull(dto.getInternship_months()))
                .salaryExpectation(trimToNull(dto.getSalary_expectation()))
                .workYears(trimToNull(dto.getWork_years()))
                .resumeSummary(trimToNull(dto.getResume_summary()))
                .profileJson(profileJson)
                .llmModel("Qwen/Qwen2.5-32B-Instruct")
                .promptVersion("resume_profile_v1")
                .status("SUCCESS")
                .build();
    }

    public static UserResumeProfile toEntity(String profileJson, String resumeId) {
        return toEntity(toDto(profileJson), profileJson, resumeId);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String writeArrayField(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return JSONUtil.toJsonStr(values.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .toList());
    }
}
