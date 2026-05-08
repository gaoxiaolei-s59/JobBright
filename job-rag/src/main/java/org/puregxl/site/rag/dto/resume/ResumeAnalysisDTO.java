package org.puregxl.site.rag.dto.resume;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisDTO {

    private Score score;
    private Profile profile;
    private String analysisSummary;
    private List<Highlight> analysisHighlights;
    private List<Issue> urgentIssues;
    private List<SkillGroup> skillGroups;
    private List<Project> projects;

    @Data
    public static class Score {
        private String grade;
        private String label;
        private Integer scoreValue;
        private Integer urgentFixCount;
        private Integer criticalFixCount;
        private Integer optionalFixCount;
        private String summary;
    }

    @Data
    public static class Profile {
        private String name;
        private String title;
        private String location;
        private String status;
    }

    @Data
    public static class Highlight {
        private String title;
        private String description;
    }

    @Data
    public static class Issue {
        private String title;
        private String description;
    }

    @Data
    public static class SkillGroup {
        private String title;
        private List<String> items;
    }

    @Data
    public static class Project {
        private String name;
        private List<String> technologies;
        private List<String> bullets;
    }
}
