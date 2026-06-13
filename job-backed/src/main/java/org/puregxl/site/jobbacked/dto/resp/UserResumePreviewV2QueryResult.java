package org.puregxl.site.jobbacked.dto.resp;

import lombok.Data;

import java.util.Date;

@Data
public class UserResumePreviewV2QueryResult {

    private String resumeId;

    private String resumeName;

    private String fileName;

    private String fileExt;

    private String contentType;

    private String previewType;

    private Date updatedAt;

    private String profileJson;

    private String scoreJson;

    private String skillGroupsJson;

    private String workExperiencesJson;

    private String projectExperiencesJson;

    private String certificationsJson;
}
