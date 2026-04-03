package org.puregxl.site.framework.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResumeExecuteTaskEvent {

    /**
     * 简历ID
     */
    private String resumeId;
}
