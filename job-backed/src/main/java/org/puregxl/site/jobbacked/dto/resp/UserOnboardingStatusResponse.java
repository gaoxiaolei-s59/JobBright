package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOnboardingStatusResponse {

    /** 是否已完成资料画像 */
    private Boolean profileCompleted;

    /** 是否已上传当前简历 */
    private Boolean resumeUploaded;

    /** 是否已完成新手引导 */
    private Boolean onboardingCompleted;
}
