package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.constant.AuthConstant;
import org.puregxl.site.jobbacked.dto.resp.LoginCaptchaResponse;
import org.puregxl.site.jobbacked.service.CaptchaService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CAPTCHA_LENGTH = 4;
    private static final long CAPTCHA_EXPIRES_IN_SECONDS = 120L;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public LoginCaptchaResponse generateLoginCaptcha() {
        String captchaKey = UUID.fastUUID().toString(true);
        String captchaCode = randomCaptchaCode();
        stringRedisTemplate.opsForValue().set(
                AuthConstant.buildCaptchaKey(captchaKey),
                captchaCode,
                CAPTCHA_EXPIRES_IN_SECONDS,
                TimeUnit.SECONDS
        );
        String svg = buildCaptchaSvg(captchaCode);
        String imageData = "data:image/svg+xml;base64," +
                Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return LoginCaptchaResponse.builder()
                .captchaKey(captchaKey)
                .imageData(imageData)
                .expiresIn(CAPTCHA_EXPIRES_IN_SECONDS)
                .build();
    }

    @Override
    public void validateLoginCaptcha(String captchaKey, String captchaCode) {
        if (StrUtil.hasBlank(captchaKey, captchaCode)) {
            throw new ClientException("请先完成图形验证码校验");
        }
        String redisKey = AuthConstant.buildCaptchaKey(captchaKey.trim());
        String expectedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isBlank(expectedCode)) {
            throw new ClientException("图形验证码已过期，请刷新后重试");
        }
        if (!expectedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new ClientException("图形验证码错误");
        }
        stringRedisTemplate.delete(redisKey);
    }

    private String randomCaptchaCode() {
        StringBuilder builder = new StringBuilder(CAPTCHA_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            builder.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return builder.toString().toUpperCase(Locale.ROOT);
    }

    private String buildCaptchaSvg(String captchaCode) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder textLayer = new StringBuilder();
        for (int i = 0; i < captchaCode.length(); i++) {
            int x = 22 + i * 24 + random.nextInt(-2, 3);
            int y = 34 + random.nextInt(-4, 5);
            int rotate = random.nextInt(-18, 19);
            String color = randomDarkColor();
            textLayer.append("<text x=\"").append(x)
                    .append("\" y=\"").append(y)
                    .append("\" transform=\"rotate(").append(rotate).append(' ').append(x).append(' ').append(y).append(")\" ")
                    .append("font-size=\"24\" font-family=\"Verdana, Arial, sans-serif\" font-weight=\"700\" fill=\"")
                    .append(color).append("\">")
                    .append(captchaCode.charAt(i))
                    .append("</text>");
        }

        StringBuilder lineLayer = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            lineLayer.append("<line x1=\"").append(random.nextInt(0, 120))
                    .append("\" y1=\"").append(random.nextInt(6, 42))
                    .append("\" x2=\"").append(random.nextInt(0, 120))
                    .append("\" y2=\"").append(random.nextInt(6, 42))
                    .append("\" stroke=\"").append(randomPastelColor())
                    .append("\" stroke-width=\"1.4\" />");
        }

        StringBuilder dotLayer = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            dotLayer.append("<circle cx=\"").append(random.nextInt(4, 116))
                    .append("\" cy=\"").append(random.nextInt(4, 44))
                    .append("\" r=\"").append(random.nextDouble(0.7, 1.6))
                    .append("\" fill=\"").append(randomPastelColor()).append("\" />");
        }

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="120" height="48" viewBox="0 0 120 48">
                  <rect width="120" height="48" rx="12" fill="#f7fbfa"/>
                  <rect x="1" y="1" width="118" height="46" rx="11" fill="none" stroke="#dbe8e5"/>
                  %s
                  %s
                  %s
                </svg>
                """.formatted(lineLayer, dotLayer, textLayer);
    }

    private String randomDarkColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "rgb(%d,%d,%d)".formatted(
                random.nextInt(24, 80),
                random.nextInt(36, 96),
                random.nextInt(64, 132)
        );
    }

    private String randomPastelColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "rgba(%d,%d,%d,0.45)".formatted(
                random.nextInt(120, 220),
                random.nextInt(180, 240),
                random.nextInt(180, 240)
        );
    }
}
