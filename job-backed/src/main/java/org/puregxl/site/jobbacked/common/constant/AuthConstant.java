package org.puregxl.site.jobbacked.common.constant;

public final class AuthConstant {

    private AuthConstant() {
    }

    /**
     * 前端传递登录 uuid 的请求头名称
     */
    public static final String USER_TOKEN_HEADER = "X-Access-Token";

    /**
     * Redis 登录态缓存 key 前缀
     */
    public static final String USER_LOGIN_KEY_PREFIX = "job_backed:user:login:";

    /**
     * 图形验证码缓存 key 前缀
     */
    public static final String LOGIN_CAPTCHA_KEY_PREFIX = "job_backed:auth:captcha:";


    public static String buildLoginKey(String token) {
        return USER_LOGIN_KEY_PREFIX + token;
    }

    public static String buildCaptchaKey(String captchaKey) {
        return LOGIN_CAPTCHA_KEY_PREFIX + captchaKey;
    }



}
