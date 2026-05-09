package org.puregxl.site.jobbacked.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResumeManualUpdateRequest {

    private Profile profile;

    private Contact contact;

    private String analysisSummary;

    private List<SkillGroup> skillGroups;

    private List<WorkExperience> workExperiences;

    private List<Project> projects;

    private List<Certification> certifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String name;
        private String title;
        private String location;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private String email;
        private String phone;
        private String linkedin;
        private String github;
        private String website;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillGroup {
        private String title;
        private List<String> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String company;
        private String role;
        private String period;
        private List<String> bullets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String name;
        private List<String> technologies;
        private List<String> bullets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Certification {
        private String name;
        private String description;
    }
}
