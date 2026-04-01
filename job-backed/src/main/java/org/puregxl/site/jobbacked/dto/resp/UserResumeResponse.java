package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResumeResponse {
    /**
     * 简历ID
     */
    private String resumeId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 状态
     */
    private String status;

    /**
     * 上传时间
     */
    private Date uploadTime;
}
