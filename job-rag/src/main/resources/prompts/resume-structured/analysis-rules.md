生成适合前端 Analysis Summary / Analysis Highlights / Urgent Issues UI 展示的分析数据。
report 对应 resume_analysis_report；sections 对应 resume_analysis_section；issues 对应 resume_analysis_issue。
report.analysisSummary 使用 1 段完整自然语言总结，说明候选人定位、优势、整体等级和关键缺口。
scoreValue 范围 0 到 100；scoreGrade 只能是 A、B、C、D；
scoreLevel 只能是 EXCELLENT、GOOD、NORMAL、WEAK。
sectionCode 使用大写下划线，例如 IMPACT_ACHIEVEMENTS、STYLE_SECTIONS、CONTENT_COMPLETENESS、TECHNICAL_DEPTH。
sectionTitle 使用适合 UI 展示的英文标题，例如 Impact & Achievements。
每个 section 都要有 whyImportantContent，用来解释为什么这一类问题重要。
issueLevel 只能是 URGENT、CRITICAL、OPTIONAL。
issueType 使用大写下划线，例如 LACK_OF_ACCOMPLISHMENT、MISSING_SUMMARY。
title 要短，适合作为粉色问题卡片的标题。
relatedCount 表示同类问题数量；severityScore 范围 1 到 100。
不要因为没有手机号/邮箱就过度扣分；优先看简历内容质量。
