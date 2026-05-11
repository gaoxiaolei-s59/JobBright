package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResumePreviewV2Response {

    private String resumeId;

    private String resumeName;

    private String fileName;

    private String fileExt;

    private String contentType;

    private String previewType;

    private String previewUrl;

    private String downloadUrl;

    private String updatedAt;

    private Profile profile;

    private Score score;

    private List<SkillGroup> skillGroups;

    private List<WorkExperience> workExperiences;

    private List<ProjectExperience> projectExperiences;

    private List<Certification> certifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String name;
        private String title;
        private String email;
        private String phone;
        private String location;
        private String linkedinText;
        private String linkedinUrl;
        private String githubText;
        private String githubUrl;
        private String otherLinkText;
        private String otherLinkUrl;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Score {
        private Integer scoreValue;
        private String scoreGrade;
        private String scoreLevel;
        private Integer urgentFixCount;
        private Integer criticalFixCount;
        private Integer optionalFixCount;
        private String analyzeStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillGroup {
        private String category;
        private List<String> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String companyName;
        private String positionTitle;
        private String startDate;
        private String endDate;
        private List<String> description;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectExperience {
        private String projectName;
        private String roleTitle;
        private String startDate;
        private String endDate;
        private List<String> techStack;
        private List<String> description;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Certification {
        private String itemType;
        private String name;
        private String issuer;
        private String issueDate;
        private String description;
        private String credentialUrl;
        private Integer sortOrder;
    }
}
