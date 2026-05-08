package org.puregxl.site.rag.llm.resume.prompt;

/**
 * 简历分析 Prompt，目标是让大模型输出可以直接映射到
 * user_resume_analysis 的结构化 JSON。
 */
public final class ResumeAnalysisLlmPrompt {

    private ResumeAnalysisLlmPrompt() {
    }

    public static String buildSystemPrompt() {
        return """
                你是一个简历分析助手，需要根据候选人的原始简历文本输出严格 JSON。
                
                输出要求：
                1. 只能输出 JSON，不要输出 Markdown、解释或额外文本。
                2. 字段名必须严格使用 camelCase。
                3. 不要编造简历中没有的信息；缺失内容使用空字符串或空数组。
                4. 分数 scoreValue 为 0-100 的整数。
                5. grade 只能是 A/B/C，label 只能是 EXCELLENT/GOOD/FAIR。
                6. urgentIssues 只放最需要立即修改的问题，最多 5 条。
                7. skillGroups 最多 5 组，每组 items 最多 8 个。
                8. projects 只提取简历中真实出现的项目，最多 5 个。
                
                JSON Schema：
                {
                  "score": {
                    "grade": "A",
                    "label": "EXCELLENT",
                    "scoreValue": 88,
                    "urgentFixCount": 0,
                    "criticalFixCount": 0,
                    "optionalFixCount": 0,
                    "summary": "string"
                  },
                  "profile": {
                    "name": "string",
                    "title": "string",
                    "location": "string",
                    "status": "ACTIVE"
                  },
                  "analysisSummary": "string",
                  "analysisHighlights": [
                    {"title": "string", "description": "string"}
                  ],
                  "urgentIssues": [
                    {"title": "string", "description": "string"}
                  ],
                  "skillGroups": [
                    {"title": "string", "items": ["string"]}
                  ],
                  "projects": [
                    {"name": "string", "technologies": ["string"], "bullets": ["string"]}
                  ]
                }
                """;
    }

    public static String buildResumeAnalysisUserPrompt(String resumeText) {
        return """
                请从下面这份原始简历内容中，提取结构化信息。
                
                要求：
                1. 严格按照 system prompt 的 JSON 结构输出。
                2. 只输出 JSON，不要输出解释。
                3. 不要猜测，不要补全不存在的信息。
                4. 如果字段缺失，返回空字符串或空数组。
                5. 忽略模板占位词、分析建议、评分信息、修复建议等非简历原始内容。
                
                【原始简历内容开始】
                %s
                【原始简历内容结束】
                """.formatted(resumeText == null ? "" : resumeText);
    }

    public static String buildResumeAnalysisUserPrompt() {
        return buildResumeAnalysisUserPrompt("");
    }
}
