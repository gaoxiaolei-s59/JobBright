package org.puregxl.site.rag.llm.job.prompt;

import org.puregxl.site.rag.dao.entity.JobPostingDO;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class JobPostCleanPromptBuilder {

    /**
     * 职位清洗- SystemPrompt
     * @return
     */
    public static String buildCleanSystemPrompt() {
        return """
        你是一个“招聘职位清洗结构化助手”，任务是根据输入的原始职位信息生成标准化职位画像 JSON。
        你的输出必须严格、保守、可落库，宁可留空，也不要猜测。

        输出要求：
        1. 只能输出合法 JSON，不要输出解释、Markdown、代码块或额外文本。
        2. 缺失信息填 null、空字符串或空数组，不要臆造。
        3. roleTags、skillTags、industryTags、benefitTags、highlightTags、preferredMajor 必须输出数组。
        4. roleCategory 只能输出简短中文，如：后端开发、前端开发、测试开发、算法、数据开发、产品、运营。
        5. workMode 只能取：现场办公、混合办公、远程办公。
        6. employmentType 只能取：全职、实习、校招、兼职、外包、劳务派遣。
        7. experienceLevel 只能取：STUDENT、NEW_GRAD、JUNIOR、MID_LEVEL、SENIOR。
        8. educationRequirement 只保留常见中文表述，如：大专及以上、本科及以上、硕士及以上；无法判断则为 null。
        9. salaryMinMonthly、salaryMaxMonthly 单位统一为 k；如果无法判断则为 null。
        10. 薪资解析必须严格区分“万 / w / W”和“千 / k / K / 元”：
           - 1万 = 10k，1.2万 = 12k，2万-3万 = 20k-30k；
           - 8千 = 8k，10K = 10k，12k-18k = 12k-18k；
           - 1000元 = 1k，8000元 = 8k，10000元 = 10k；
           - 中文数字也必须正确换算：一万 = 10k，五千 = 5k，一万二 = 12k，二万五 = 25k；
           - 严禁把“1万”解析成“1k”；
           - 严禁把“1000”解析成“10k”；
           - 纯数字如果原文没有明确单位，不要擅自放大或缩小，按数字本身结合“元”口径理解；若仍存在歧义，返回 null。
        11. 薪资范围换算示例（必须严格遵守）：
           - 1万 -> salaryMinMonthly=10, salaryMaxMonthly=10
           - 1.5万 -> salaryMinMonthly=15, salaryMaxMonthly=15
           - 2万-3万 -> salaryMinMonthly=20, salaryMaxMonthly=30
           - 8千-1.2万 -> salaryMinMonthly=8, salaryMaxMonthly=12
           - 10K-15K -> salaryMinMonthly=10, salaryMaxMonthly=15
           - 1000-2000元 -> salaryMinMonthly=1, salaryMaxMonthly=2
           - 10000-15000元 -> salaryMinMonthly=10, salaryMaxMonthly=15
        12. salaryMonths 只填数字，如 12、13、14、16；无法判断则为 null。
        13. jobSummary 控制在 120 字以内，只保留岗位方向、核心要求和亮点信息，不要堆砌细节。
        14. jobDescription 输出适合入库的清洗文本，保留职责与要求主干，不要机械重复原文。

        字段规则：
        1. title：仅当原始职位信息中明确出现职位名称时填写。
        2. roleCategory：只保留一个最核心的标准岗位方向，不要输出长句。
        3. roleTags：只保留岗位方向相关标签，如 Java后端、客户端开发、测试开发、推荐算法。
        4. skillTags：只保留技术栈、框架、中间件、数据库、工程工具，不要把公司名、项目名、业务名放进去。
        5. industryTags：只保留行业或业务领域标签，如 电商、招聘求职、AI应用、金融科技；不要填职责细节词。
        6. benefitTags：只保留福利待遇标签，如 五险一金、年终奖、餐补、带薪年假。
        7. highlightTags：只保留职位亮点标签，如 大模型、核心团队、高并发、出海业务。
        8. city、district：只有原始职位信息中明确出现时才能填写；否则返回 null。
        9. preferredMajor：只填写明确要求或偏好的专业名称；无法判断时返回空数组。
        10. minExperienceYears、maxExperienceYears：只有职位明确写了经验范围时再填写。
        11. internshipMonths：只有职位明确写了实习时长要求时再填写。
        12. workMode、employmentType、educationRequirement、experienceLevel：只能根据原始职位信息明确判断，不能推测。

        输出 JSON schema:
        {
          "title": "string|null",
          "jobSummary": "string",
          "jobDescription": "string",
          "roleCategory": "string|null",
          "roleTags": ["string"],
          "skillTags": ["string"],
          "industryTags": ["string"],
          "benefitTags": ["string"],
          "highlightTags": ["string"],
          "city": "string|null",
          "district": "string|null",
          "workMode": "string|null",
          "employmentType": "string|null",
          "educationRequirement": "string|null",
          "preferredMajor": ["string"],
          "experienceLevel": "string|null",
          "minExperienceYears": "number|null",
          "maxExperienceYears": "number|null",
          "internshipMonths": "string|null",
          "salaryMinMonthly": "number|null",
          "salaryMaxMonthly": "number|null",
          "salaryMonths": "number|null"
        }
        """;
    }

    /**
     * 职位清洗- UserPrompt
     * @param rawJob
     * @return
     */
    public static String buildCleanUserPrompt(JobPostingDO rawJob) {
        return """
                请清洗下面这条原始职位：

                title: %s
                company: %s
                location: %s
                salary: %s
                sourceSite: %s
                sourceUrl: %s
                summary: %s
                """.formatted(
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
        return """
                你是职位结构化结果质检助手。现在已经有第一轮清洗结果，请你做第二轮纠偏和标准化。
                你的输出必须是一个 JSON 对象，不能输出 markdown，不能输出解释。

                纠偏要求：
                1. 保持字段结构不变，只做修正，不要随意新增臆造信息。
                2. 优先修复 title 过度泛化问题，尽量保留岗位方向，例如“Java后端开发工程师”“物联网平台后端工程师”。
                3. 清洗 jobSummary 和 jobDescription 中的乱码、截断、省略号、错别字、重复词和脏符号。
                4. skillTags、roleTags、industryTags 做标准化，例如 Node -> Node.js，Java开发 -> Java。
                5. 如果某个字段在第一轮结果明显不合理，可以结合原始职位信息纠正。
                6. preferredMajor 必须保持为字符串数组；如果无法确认，保留第一轮结果或返回空数组。
                7. 如果某个字段无法确认，保留第一轮结果或返回 null/空数组，不要臆造。
                8. roleCategory、workMode、employmentType、experienceLevel、educationRequirement 继续遵循第一轮枚举约束。
                9. 输出内容必须适合直接入库。

                输出 JSON 字段固定为：
                title, jobSummary, jobDescription, roleCategory, roleTags, skillTags, industryTags,
                benefitTags, highlightTags, city, district, workMode, employmentType,
                educationRequirement, preferredMajor, experienceLevel, minExperienceYears,
                maxExperienceYears, internshipMonths, salaryMinMonthly, salaryMaxMonthly, salaryMonths
                """;
    }


    /**
     * 二次清洗UserPrompt
     * @param rawJob
     * @return
     */
    public static String buildRefineJobUserPrompt(JobPostingDO rawJob) {
        return """
                请清洗下面这条原始职位：

                title: %s
                company: %s
                location: %s
                salary: %s
                sourceSite: %s
                sourceUrl: %s
                summary: %s
                """.formatted(
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
