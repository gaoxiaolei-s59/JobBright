package org.puregxl.site.jobbacked.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCaptchaResponse {

    /**
     * 验证码业务 key
     */
    private String captchaKey;

    /**
     * 图形验证码 Data URL，可直接给前端 img.src 使用
     */
    private String imageData;

    /**
     * 过期时间，单位秒
     */
    private Long expiresIn;
}
