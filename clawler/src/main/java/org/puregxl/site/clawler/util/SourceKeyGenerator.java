package org.puregxl.site.clawler.util;

import org.puregxl.site.clawler.entity.JobPosting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SourceKeyGenerator {

    private SourceKeyGenerator() {
    }

    public static String generate(JobPosting posting) {
        String raw = String.join("|",
                TextCleaner.defaultString(posting.getSourceSite()),
                TextCleaner.defaultString(posting.getSourceUrl()),
                TextCleaner.defaultString(posting.getTitle()),
                TextCleaner.defaultString(posting.getCompany()),
                TextCleaner.defaultString(posting.getLocation()));
        return sha256(raw);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }
}
