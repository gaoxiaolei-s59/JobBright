package org.puregxl.site.rag.profile.prompt;

public final class ResumeProfilePromptBuilder {

    private ResumeProfilePromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                你是一个简历画像结构化助手。
                请根据输入的简历结构化内容输出标准化候选人画像 JSON。
                只能输出合法 JSON，不要输出解释、Markdown 或额外文本。
                缺失信息填 null、空字符串或空数组，不要臆造。
                target_roles、school_tags、core_skills、project_tags、industry_tags、strengths、preferred_cities 必须是数组。
                seniority 只能取：实习、应届、初级、中级、高级。
                preferred_job_type 只能取：实习、校招、社招。
                education_level 只能取：大专、本科、硕士、博士、未知。
                resume_summary 控制在120字以内。
                输出 JSON schema:
                {
                  "candidate_name": "string|null",
                  "target_roles": ["string"],
                  "seniority": "string",
                  "education_level": "string",
                  "school_name": "string|null",
                  "major_name": "string|null",
                  "school_tags": ["string"],
                  "core_skills": ["string"],
                  "project_tags": ["string"],
                  "industry_tags": ["string"],
                  "strengths": ["string"],
                  "preferred_cities": ["string"],
                  "preferred_job_type": "string",
                  "internship_months": "string|null",
                  "salary_expectation": "string|null",
                  "work_years": "string|null",
                  "resume_summary": "string"
                }
                """;
    }

}
