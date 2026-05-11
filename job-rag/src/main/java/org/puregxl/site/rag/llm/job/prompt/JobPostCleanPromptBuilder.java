package org.puregxl.site.rag.llm.job.prompt;

import org.puregxl.site.rag.dao.entity.JobPostingDO;
import org.puregxl.site.rag.llm.prompt.PromptTemplateLoader;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class JobPostCleanPromptBuilder {

    /**
     * 职位清洗- SystemPrompt
     * @return
     */
    public static String buildCleanSystemPrompt() {
        return PromptTemplateLoader.load("prompts/job-clean/system.md");
    }

    /**
     * 职位清洗- UserPrompt
     * @param rawJob
     * @return
     */
    public static String buildCleanUserPrompt(JobPostingDO rawJob) {
        return PromptTemplateLoader.format(
                "prompts/job-clean/user.md",
                defaultString(rawJob.getTitle()),
                defaultString(rawJob.getCompany()),
                defaultString(rawJob.getLocation()),
                defaultString(rawJob.getSalary()),
                defaultString(rawJob.getSourceSite()),
                defaultString(rawJob.getSourceUrl()),
                defaultString(rawJob.getSummary())
        );
    }

    /**
     * 二次清洗llm
     * @return
     */
    public static String buildRefineJobSystemPrompt() {
        return PromptTemplateLoader.load("prompts/job-clean/refine-system.md");
    }


    /**
     * 二次清洗UserPrompt
     * @param rawJob
     * @return
     */
    public static String buildRefineJobUserPrompt(JobPostingDO rawJob) {
        return PromptTemplateLoader.format(
                "prompts/job-clean/refine-user.md",
                defaultString(rawJob.getTitle()),
                defaultString(rawJob.getCompany()),
                defaultString(rawJob.getLocation()),
                defaultString(rawJob.getSalary()),
                defaultString(rawJob.getSourceSite()),
                defaultString(rawJob.getSourceUrl()),
                defaultString(rawJob.getSummary())
        );
    }
}
