package org.puregxl.site.rag.llm.resume.prompt;

import org.puregxl.site.rag.llm.prompt.PromptTemplateLoader;

/**
 * 简历分析 Prompt，目标是让大模型输出可以直接映射到
 * user_resume_analysis 的结构化 JSON。
 */
public final class ResumeAnalysisLlmPrompt {

    private ResumeAnalysisLlmPrompt() {
    }

    public static String buildSystemPrompt() {
        return PromptTemplateLoader.load("prompts/resume-analysis/system.md");
    }

    public static String buildResumeAnalysisUserPrompt(String resumeText) {
        return PromptTemplateLoader.format("prompts/resume-analysis/user.md", resumeText == null ? "" : resumeText);
    }

    public static String buildResumeAnalysisUserPrompt() {
        return buildResumeAnalysisUserPrompt("");
    }
}
