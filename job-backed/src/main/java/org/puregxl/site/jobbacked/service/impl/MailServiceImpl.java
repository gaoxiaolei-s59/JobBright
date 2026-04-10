package org.puregxl.site.jobbacked.service.impl;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.service.MailService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String MAIL_CODE_PREFIX = "job:backed:mail:code:%s";

    private static final String MAIL_LOCK_PREFIX = "job:backed:mail:lock:%s";

    private final RedissonClient redissonClient;


    /**
     * 发送邮箱验证码
     * @param email 收件邮箱
     * @return 本次发送的验证码
     */
    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        String code = generateCode();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        RLock lock = redissonClient.getLock(String.format(MAIL_LOCK_PREFIX, normalizedEmail));

        boolean locked;
        try {
            locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientException("验证码发送被中断，请稍后重试");
        }

        if (!locked)  {
            throw new ClientException("已经发送过邮箱了，稍后再试");
        }

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(normalizedEmail);
            helper.setSubject("验证码邮件");

            String content = buildHtmlContent(code);
            helper.setText(content, true);

            mailSender.send(mimeMessage);

            log.info("验证码邮件发送成功, email={}", normalizedEmail);

            //保存验证码到redis - 1分钟有效期

            stringRedisTemplate.opsForValue().set(
                    String.format(MAIL_CODE_PREFIX, normalizedEmail),
                    code,
                    60,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("验证码邮件发送失败, email={}", normalizedEmail, e);
            throw new ClientException("发送邮件失败，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new ClientException("邮箱不能为空");
        }
        String normalized = email.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        normalized = normalized.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ClientException("邮箱不能为空");
        }
        return normalized;
    }

    /**
     * 生成6位验证码
     */
    private String generateCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 构建HTML邮件内容
     */
    private String buildHtmlContent(String code) {
        return """
                <div style="font-family: Arial, sans-serif; color: #333;">
                    <h2>邮箱验证码</h2>
                    <p>你好，你的验证码是：</p>
                    <div style="font-size: 24px; font-weight: bold; color: #2d8cf0; margin: 16px 0;">
                        %s
                    </div>
                    <p>5分钟内有效，请尽快完成验证。</p>
                    <p style="color: #999; font-size: 12px;">如果不是你本人操作，请忽略此邮件。</p>
                </div>
                """.formatted(code);
    }
}
