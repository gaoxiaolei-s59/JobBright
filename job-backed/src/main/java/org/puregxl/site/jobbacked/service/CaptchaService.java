package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.resp.LoginCaptchaResponse;

public interface CaptchaService {

    LoginCaptchaResponse generateLoginCaptcha();

    void validateLoginCaptcha(String captchaKey, String captchaCode);
}
