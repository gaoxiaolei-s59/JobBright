package org.puregxl.site.jobbacked.dto.req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResumeProfileUpdateRequest {

    /**
     * 简历业务ID
     */
    private String resumeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 职位标题
     */
    private String title;

    /**
     * 状态：ACTIVE启用，INACTIVE停用
     */
    private String status;

    /**
     * 所在地
     */
    private String location;

}
