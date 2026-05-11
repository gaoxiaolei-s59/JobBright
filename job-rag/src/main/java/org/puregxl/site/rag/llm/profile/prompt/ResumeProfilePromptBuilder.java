package org.puregxl.site.rag.llm.profile.prompt;

import org.puregxl.site.rag.llm.prompt.PromptTemplateLoader;

public final class ResumeProfilePromptBuilder {

    private ResumeProfilePromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return PromptTemplateLoader.load("prompts/resume-profile/system.md");
    }

}
