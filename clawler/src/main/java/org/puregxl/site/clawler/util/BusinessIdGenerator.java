package org.puregxl.site.clawler.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class BusinessIdGenerator {

    private BusinessIdGenerator() {
    }

    public static String generateJobPostId(String sourceSite, String preferredUniqueValue, String sourceKey) {
        String site = normalizeSite(sourceSite);
        String uniqueValue = TextCleaner.isBlank(preferredUniqueValue) ? sourceKey : preferredUniqueValue;
        return "JOB-" + site + "-" + shortSha256(site + "|" + TextCleaner.defaultString(uniqueValue));
    }

    public static String generateCompanyId(String sourceSite, String preferredUniqueValue, String companyName) {
        String site = normalizeSite(sourceSite);
        String uniqueValue = TextCleaner.isBlank(preferredUniqueValue) ? companyName : preferredUniqueValue;
        return "COM-" + site + "-" + shortSha256(site + "|" + TextCleaner.defaultString(uniqueValue));
    }

    private static String normalizeSite(String sourceSite) {
        String cleaned = TextCleaner.defaultString(sourceSite).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return cleaned.isEmpty() ? "UNKNOWN" : cleaned;
    }

    private static String shortSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.substring(0, 16).toUpperCase();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }
}
