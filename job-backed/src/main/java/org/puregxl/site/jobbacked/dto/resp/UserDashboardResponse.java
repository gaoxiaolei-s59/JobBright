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
public class UserDashboardResponse {

    /**
     * 用户展示名称
     */
    private String displayName;

    /**
     * 套餐名称
     */
    private String planName;

    /**
     * 简历评分
     */
    private Integer resumeScore;

    /**
     * 用户资料完整度
     */
    private Integer profileCompletionRate;

    /**
     * 求职提醒
     */
    private List<String> tips;
}
