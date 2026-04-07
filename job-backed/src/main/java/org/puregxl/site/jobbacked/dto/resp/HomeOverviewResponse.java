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
public class HomeOverviewResponse {

    /**
     * 新鲜岗位数量
     */
    private Integer freshJobCount;

    /**
     * 平均匹配率
     */
    private Integer averageMatchRate;

    /**
     * 平均入围耗时（分钟）
     */
    private Integer averageShortlistMinutes;

    /**
     * 简历评分
     */
    private Integer resumeScore;

    /**
     * 用户资料完整度
     */
    private Integer profileCompletionRate;


}
