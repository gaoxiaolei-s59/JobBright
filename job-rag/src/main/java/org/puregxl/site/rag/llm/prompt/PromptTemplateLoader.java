package org.puregxl.site.rag.llm.prompt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class PromptTemplateLoader {

    private PromptTemplateLoader() {
    }

    public static String load(String path) {
        try (InputStream inputStream = PromptTemplateLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Prompt template not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt template: " + path, e);
        }
    }

    public static String format(String path, Object... args) {
        return load(path).formatted(args);
    }
}
