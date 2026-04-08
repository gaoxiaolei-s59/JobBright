package org.puregxl.site.clawler.util;

public final class TextCleaner {

    private TextCleaner() {
    }

    public static String clean(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static String defaultString(String input) {
        return input == null ? "" : input.trim();
    }

    public static String limit(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }
}
