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
public class UserResumePreviewResponse {

    /** 简历ID */
    private String resumeId;

    /** 文件名 */
    private String fileName;

    /** 文件后缀 */
    private String fileExt;

    /** 文件类型 */
    private String contentType;

    /** 预览方式 INLINE / DOWNLOAD */
    private String previewType;

    /** 预览地址 */
    private String previewUrl;

    /** 下载地址 */
    private String downloadUrl;

    /** 更新时间 */
    private String updatedAt;

    /** 简历评分 */
    private Score score;

    /** 简历基础画像 */
    private Profile profile;

    /** 联系方式 */
    private Contact contact;

    /** 分析总结 */
    private String analysisSummary;

    /** 分析亮点 */
    private List<Highlight> analysisHighlights;

    /** 紧急问题 */
    private List<Issue> urgentIssues;

    /** 技能分组 */
    private List<SkillGroup> skillGroups;

    /** 项目经历 */
    private List<Project> projects;

    /** 工作经历 */
    private List<WorkExperience> workExperiences;

    /** 证书与能力说明 */
    private List<Certification> certifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    public static class Highlight {
        private String title;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String title;
        private String description;
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
    public static class Project {
        private String name;
        private List<String> technologies;
        private List<String> bullets;
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
    public static class Certification {
        private String name;
        private String description;
    }
}
