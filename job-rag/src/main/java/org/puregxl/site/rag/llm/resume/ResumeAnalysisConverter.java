package org.puregxl.site.rag.llm.resume;

import cn.hutool.json.JSONUtil;
import org.puregxl.site.rag.dao.entity.UserResumeAnalysis;
import org.puregxl.site.rag.dto.resume.ResumeAnalysisDTO;
import org.springframework.util.StringUtils;

public final class ResumeAnalysisConverter {

    private ResumeAnalysisConverter() {
    }

    public static ResumeAnalysisDTO toDto(String analysisJson) {
        return JSONUtil.toBean(analysisJson, ResumeAnalysisDTO.class);
    }

    public static UserResumeAnalysis toEntity(
            ResumeAnalysisDTO dto,
            String analysisJson,
            String resumeId,
            Long userId) {
        ResumeAnalysisDTO.Score score = dto.getScore();
        return UserResumeAnalysis.builder()
                .resumeId(resumeId)
                .userId(userId)
                .scoreValue(score == null ? null : score.getScoreValue())
                .grade(score == null ? null : trimToNull(score.getGrade()))
                .label(score == null ? null : trimToNull(score.getLabel()))
                .urgentFixCount(score == null ? 0 : defaultInt(score.getUrgentFixCount()))
                .criticalFixCount(score == null ? 0 : defaultInt(score.getCriticalFixCount()))
                .optionalFixCount(score == null ? 0 : defaultInt(score.getOptionalFixCount()))
                .profileJson(writeJson(dto.getProfile()))
                .skillGroupsJson(writeJson(dto.getSkillGroups()))
                .projectsJson(writeJson(dto.getProjects()))
                .highlightsJson(writeJson(dto.getAnalysisHighlights()))
                .issuesJson(writeJson(dto.getUrgentIssues()))
                .analysisSummary(trimToNull(firstText(dto.getAnalysisSummary(), score == null ? null : score.getSummary())))
                .rawAnalysisJson(analysisJson)
                .status("SUCCESS")
                .delFlag(0)
                .build();
    }

    public static UserResumeAnalysis failed(String resumeId, Long userId, String reason) {
        return UserResumeAnalysis.builder()
                .resumeId(resumeId)
                .userId(userId)
                .analysisSummary(trimToNull(reason))
                .status("FAILED")
                .delFlag(0)
                .build();
    }

    private static String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        return JSONUtil.toJsonStr(value);
    }

    private static int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static String firstText(String first, String fallback) {
        return StringUtils.hasText(first) ? first : fallback;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
