package org.puregxl.site.rag.llm.resume.prompt;

import org.springframework.util.StringUtils;

/**
 * 简历分析 Prompt，目标是让大模型输出可以直接映射到
 * user_resume_analysis / user_resume_analysis_item 的结构化 JSON。
 */
public final class ResumeAnalysisLlmPrompt {
    public static String  buildResumeAnalysisUserPrompt() {
        return """
                请从下面这份原始简历内容中，提取结构化信息。
                
                要求：
                1. 严格按照 system prompt 的 JSON 结构输出。
                2. 只输出 JSON，不要输出解释。
                3. 不要猜测，不要补全不存在的信息。
                4. 如果字段缺失，返回 null、空字符串或空数组。
                5. 忽略模板占位词、分析建议、评分信息、修复建议等非简历原始内容。
                
                【原始简历内容开始】
                %s
                【原始简历内容结束】
                """;
    }



}
