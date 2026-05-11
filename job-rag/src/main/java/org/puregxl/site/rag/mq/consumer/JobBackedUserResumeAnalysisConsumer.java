package org.puregxl.site.rag.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.puregxl.site.framework.independence.NoMQDuplicateConsume;
import org.puregxl.site.framework.mq.UploadResumeExecuteTaskEvent;
import org.puregxl.site.rag.config.RustfsProperties;
import org.puregxl.site.rag.dao.entity.ResumeAnalysisIssue;
import org.puregxl.site.rag.dao.entity.ResumeAnalysisReport;
import org.puregxl.site.rag.dao.entity.ResumeAnalysisSection;
import org.puregxl.site.rag.dao.entity.ResumeCertification;
import org.puregxl.site.rag.dao.entity.ResumeProfile;
import org.puregxl.site.rag.dao.entity.ResumeProjectExperience;
import org.puregxl.site.rag.dao.entity.ResumeSkill;
import org.puregxl.site.rag.dao.entity.ResumeWorkExperience;
import org.puregxl.site.rag.dao.entity.UserResumeFile;
import org.puregxl.site.rag.dao.mapper.ResumeAnalysisIssueMapper;
import org.puregxl.site.rag.dao.mapper.ResumeAnalysisReportMapper;
import org.puregxl.site.rag.dao.mapper.ResumeAnalysisSectionMapper;
import org.puregxl.site.rag.dao.mapper.ResumeCertificationMapper;
import org.puregxl.site.rag.dao.mapper.ResumeProfileMapper;
import org.puregxl.site.rag.dao.mapper.ResumeProjectExperienceMapper;
import org.puregxl.site.rag.dao.mapper.ResumeSkillMapper;
import org.puregxl.site.rag.dao.mapper.ResumeWorkExperienceMapper;
import org.puregxl.site.rag.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.rag.llm.InfraAiJsonLlmClient;
import org.puregxl.site.rag.llm.prompt.PromptTemplateLoader;
import org.puregxl.site.rag.mq.base.MessageWrapper;
import org.puregxl.site.rag.parse.ParseResult;
import org.puregxl.site.rag.parse.TikaParseService;
import org.puregxl.site.rag.service.impl.FileServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_ANALYSIS_GROUP;
import static org.puregxl.site.framework.common.constant.RocketMqConstant.UPLOAD_RESUME_ANALYSIS_TOPIC;

@Slf4j(topic = "JobBackedUserResumeAnalysisConsumer")
@Component
@RocketMQMessageListener(
        topic = UPLOAD_RESUME_ANALYSIS_TOPIC,
        consumerGroup = UPLOAD_RESUME_ANALYSIS_GROUP
)
@RequiredArgsConstructor
public class JobBackedUserResumeAnalysisConsumer implements RocketMQListener<MessageWrapper<UploadResumeExecuteTaskEvent>> {

    private static final String STRUCTURED_PROMPT_DIR = "prompts/resume-structured/";

    private final FileServiceImpl fileService;

    private final TikaParseService tikaParseService;

    private final UserResumeFileMapper userResumeFileMapper;

    private final ResumeProfileMapper resumeProfileMapper;

    private final ResumeSkillMapper resumeSkillMapper;

    private final ResumeWorkExperienceMapper resumeWorkExperienceMapper;

    private final ResumeProjectExperienceMapper resumeProjectExperienceMapper;

    private final ResumeCertificationMapper resumeCertificationMapper;

    private final ResumeAnalysisReportMapper resumeAnalysisReportMapper;

    private final ResumeAnalysisSectionMapper resumeAnalysisSectionMapper;

    private final ResumeAnalysisIssueMapper resumeAnalysisIssueMapper;

    private final InfraAiJsonLlmClient llmClient;

    private final RustfsProperties rustfsProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    @NoMQDuplicateConsume(
            keyPrefix = "mq:resume:analysis",
            key = "#messageWrapper.message.resumeId",
            keyTimeout = 3600L
    )
    public void onMessage(MessageWrapper<UploadResumeExecuteTaskEvent> messageWrapper) {
        log.info("[消费者] - 用户简历分析处理 - 执行消费逻辑，消息体:{}", JSON.toJSONString(messageWrapper));
        UploadResumeExecuteTaskEvent message = messageWrapper.getMessage();
        if (message == null) {
            log.warn("[消费者] - 用户简历分析处理 - 消息体异常，缺少message对象: {}", JSON.toJSONString(messageWrapper));
            return;
        }

        String resumeId = message.getResumeId();
        if (!StringUtils.hasText(resumeId)) {
            log.warn("[消费者] - 用户简历分析处理 - 简历ID为空: {}", JSON.toJSONString(message));
            return;
        }

        UserResumeFile userResumeFile = getResumeFile(resumeId);
        if (userResumeFile == null) {
            log.warn("[消费者] - 用户简历分析处理 - 未找到简历文件记录, resumeId:{}", resumeId);
            return;
        }

        try {
            MultipartFile multipartFile = fileService.downloadMultipartFile(userResumeFile.getResumeId(), rustfsProperties.getBucketName());
            ParseResult parseResult = tikaParseService.parseFile(multipartFile);
            String resumeText = parseResult == null ? "" : parseResult.getContent();

            if (!StringUtils.hasText(resumeText)) {
                saveFailedAnalysisReport(resumeId, userResumeFile.getUserId(), "简历文本解析为空");
                log.warn("[消费者] - 用户简历分析处理 - 简历文本解析为空, userId:{}, resumeId:{}",
                        userResumeFile.getUserId(), resumeId);
                return;
            }

            ParsedResumeResult result = buildStructuredResumeResult(resumeText, userResumeFile);
            saveParsedResult(result);
            updateResumeScore(userResumeFile, result.getAnalysisReport() == null ? null : result.getAnalysisReport().getScoreValue());
            log.info("[消费者] - 用户简历分析处理 - 分析入库成功, userId:{}, resumeId:{}, score:{}",
                    userResumeFile.getUserId(),
                    resumeId,
                    result.getAnalysisReport() == null ? null : result.getAnalysisReport().getScoreValue());
        } catch (Exception ex) {
            saveFailedAnalysisReport(resumeId, userResumeFile.getUserId(), "简历分析异常: " + ex.getMessage());
            log.error("[消费者] - 用户简历分析处理 - 执行失败, userId:{}, resumeId:{}",
                    userResumeFile.getUserId(), resumeId, ex);
        }
    }

    private UserResumeFile getResumeFile(String resumeId) {
        return userResumeFileMapper.selectOne(
                Wrappers.lambdaQuery(UserResumeFile.class)
                        .eq(UserResumeFile::getResumeId, resumeId)
                        .eq(UserResumeFile::getDelFlag, 0)
                        .last("limit 1")
        );
    }

    private ParsedResumeResult buildStructuredResumeResult(String resumeText, UserResumeFile userResumeFile) throws Exception {
        ProfileResult profile = askJson(
                buildSystemPrompt(),
                buildUserPrompt("基础信息", resumeText, "profile-rules.md", "profile-schema.json"),
                ProfileResult.class
        );

        SkillResult skills = askJson(
                buildSystemPrompt(),
                buildUserPrompt("技能", resumeText, "skills-rules.md", "skills-schema.json"),
                SkillResult.class
        );

        WorkResult workExperiences = askJson(
                buildSystemPrompt(),
                buildUserPrompt("工作经历", resumeText, "work-rules.md", "work-schema.json"),
                WorkResult.class
        );

        ProjectAndCertificationResult projectsAndCertifications = askJson(
                buildSystemPrompt(),
                buildUserPrompt("项目经历、证书和奖项", resumeText, "project-cert-rules.md", "project-cert-schema.json"),
                ProjectAndCertificationResult.class
        );

        AnalysisResult analysis = askJson(
                buildSystemPrompt(),
                buildUserPrompt("简历分析报告、分析分组和优化问题", resumeText, "analysis-rules.md", "analysis-schema.json"),
                AnalysisResult.class
        );
        fillAnalysisDefaults(userResumeFile.getUserId(), userResumeFile.getResumeId(), analysis);

        return ParsedResumeResult.builder()
                .userId(userResumeFile.getUserId())
                .resumeId(userResumeFile.getResumeId())
                .resumeName(userResumeFile.getFileName())
                .profile(profile == null ? null : profile.getProfile())
                .skills(skills == null ? List.of() : safeList(skills.getSkills()))
                .workExperiences(workExperiences == null ? List.of() : safeList(workExperiences.getWorkExperiences()))
                .projectExperiences(projectsAndCertifications == null ? List.of() : safeList(projectsAndCertifications.getProjectExperiences()))
                .certifications(projectsAndCertifications == null ? List.of() : safeList(projectsAndCertifications.getCertifications()))
                .analysisReport(analysis == null ? null : analysis.getReport())
                .analysisSections(analysis == null ? List.of() : safeList(analysis.getSections()))
                .analysisIssues(analysis == null ? List.of() : safeList(analysis.getIssues()))
                .build();
    }

    private void saveParsedResult(ParsedResumeResult result) {
        deleteExistingResumeData(result.getResumeId());
        saveProfile(result);
        saveSkills(result);
        saveWorkExperiences(result);
        saveProjectExperiences(result);
        saveCertifications(result);

        Long reportId = saveAnalysisReport(result);
        Map<String, Long> sectionIdMap = saveAnalysisSections(result, reportId);
        saveAnalysisIssues(result, reportId, sectionIdMap);
    }

    private void deleteExistingResumeData(String resumeId) {
        resumeAnalysisIssueMapper.delete(Wrappers.lambdaQuery(ResumeAnalysisIssue.class)
                .eq(ResumeAnalysisIssue::getResumeId, resumeId));
        resumeAnalysisSectionMapper.delete(Wrappers.lambdaQuery(ResumeAnalysisSection.class)
                .eq(ResumeAnalysisSection::getResumeId, resumeId));
        resumeAnalysisReportMapper.delete(Wrappers.lambdaQuery(ResumeAnalysisReport.class)
                .eq(ResumeAnalysisReport::getResumeId, resumeId));
        resumeCertificationMapper.delete(Wrappers.lambdaQuery(ResumeCertification.class)
                .eq(ResumeCertification::getResumeId, resumeId));
        resumeProjectExperienceMapper.delete(Wrappers.lambdaQuery(ResumeProjectExperience.class)
                .eq(ResumeProjectExperience::getResumeId, resumeId));
        resumeWorkExperienceMapper.delete(Wrappers.lambdaQuery(ResumeWorkExperience.class)
                .eq(ResumeWorkExperience::getResumeId, resumeId));
        resumeSkillMapper.delete(Wrappers.lambdaQuery(ResumeSkill.class)
                .eq(ResumeSkill::getResumeId, resumeId));
        resumeProfileMapper.delete(Wrappers.lambdaQuery(ResumeProfile.class)
                .eq(ResumeProfile::getResumeId, resumeId));
    }

    private void saveProfile(ParsedResumeResult result) {
        Profile profile = result.getProfile();
        Date now = new Date();
        ResumeProfile entity = new ResumeProfile();
        entity.setUserId(result.getUserId());
        entity.setResumeId(result.getResumeId());
        entity.setResumeName(result.getResumeName());
        if (profile != null) {
            entity.setName(profile.getName());
            entity.setTitle(profile.getTitle());
            entity.setEmail(profile.getEmail());
            entity.setPhone(profile.getPhone());
            entity.setLocation(profile.getLocation());
            entity.setLinkedinText(profile.getLinkedinText());
            entity.setLinkedinUrl(profile.getLinkedinUrl());
            entity.setGithubText(profile.getGithubText());
            entity.setGithubUrl(profile.getGithubUrl());
            entity.setOtherLinkText(profile.getOtherLinkText());
            entity.setOtherLinkUrl(profile.getOtherLinkUrl());
        }
        entity.setLastAnalyzeTime(now);
        entity.setAnalyzeStatus("ANALYZED");
        entity.setStatus("ACTIVE");
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(0);
        resumeProfileMapper.insert(entity);
    }

    private void saveSkills(ParsedResumeResult result) {
        Date now = new Date();
        for (Skill skill : safeList(result.getSkills())) {
            if (skill == null || !StringUtils.hasText(skill.getSkillName())) {
                continue;
            }
            ResumeSkill entity = new ResumeSkill();
            entity.setResumeId(result.getResumeId());
            entity.setCategory(skill.getCategory());
            entity.setSkillName(skill.getSkillName());
            entity.setSortOrder(defaultNumber(skill.getSortOrder(), 0));
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeSkillMapper.insert(entity);
        }
    }

    private void saveWorkExperiences(ParsedResumeResult result) {
        Date now = new Date();
        for (WorkExperience workExperience : safeList(result.getWorkExperiences())) {
            if (workExperience == null || !StringUtils.hasText(workExperience.getCompanyName())) {
                continue;
            }
            ResumeWorkExperience entity = new ResumeWorkExperience();
            entity.setResumeId(result.getResumeId());
            entity.setCompanyName(workExperience.getCompanyName());
            entity.setPositionTitle(workExperience.getPositionTitle());
            entity.setStartDate(workExperience.getStartDate());
            entity.setEndDate(workExperience.getEndDate());
            entity.setDescription(toJson(workExperience.getDescription()));
            entity.setSortOrder(defaultNumber(workExperience.getSortOrder(), 0));
            entity.setStatus("ACTIVE");
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeWorkExperienceMapper.insert(entity);
        }
    }

    private void saveProjectExperiences(ParsedResumeResult result) {
        Date now = new Date();
        for (ProjectExperience projectExperience : safeList(result.getProjectExperiences())) {
            if (projectExperience == null || !StringUtils.hasText(projectExperience.getProjectName())) {
                continue;
            }
            ResumeProjectExperience entity = new ResumeProjectExperience();
            entity.setResumeId(result.getResumeId());
            entity.setProjectName(projectExperience.getProjectName());
            entity.setRoleTitle(projectExperience.getRoleTitle());
            entity.setStartDate(projectExperience.getStartDate());
            entity.setEndDate(projectExperience.getEndDate());
            entity.setTechStack(toJson(projectExperience.getTechStack()));
            entity.setDescription(toJson(projectExperience.getDescription()));
            entity.setSortOrder(defaultNumber(projectExperience.getSortOrder(), 0));
            entity.setStatus("ACTIVE");
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeProjectExperienceMapper.insert(entity);
        }
    }

    private void saveCertifications(ParsedResumeResult result) {
        Date now = new Date();
        for (Certification certification : safeList(result.getCertifications())) {
            if (certification == null || !StringUtils.hasText(certification.getName())) {
                continue;
            }
            ResumeCertification entity = new ResumeCertification();
            entity.setResumeId(result.getResumeId());
            entity.setItemType(defaultText(certification.getItemType(), "CERTIFICATE"));
            entity.setName(certification.getName());
            entity.setIssuer(certification.getIssuer());
            entity.setIssueDate(certification.getIssueDate());
            entity.setDescription(certification.getDescription());
            entity.setCredentialUrl(certification.getCredentialUrl());
            entity.setSortOrder(defaultNumber(certification.getSortOrder(), 0));
            entity.setStatus("ACTIVE");
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeCertificationMapper.insert(entity);
        }
    }

    private Long saveAnalysisReport(ParsedResumeResult result) {
        AnalysisReport report = result.getAnalysisReport();
        if (report == null) {
            saveFailedAnalysisReport(result.getResumeId(), result.getUserId(), "大模型未返回分析报告");
            return null;
        }
        Date now = new Date();
        ResumeAnalysisReport entity = new ResumeAnalysisReport();
        entity.setUserId(result.getUserId());
        entity.setResumeId(result.getResumeId());
        entity.setScoreValue(report.getScoreValue());
        entity.setScoreGrade(report.getScoreGrade());
        entity.setScoreLevel(report.getScoreLevel());
        entity.setAnalysisSummary(report.getAnalysisSummary());
        entity.setUrgentIssueCount(defaultNumber(report.getUrgentIssueCount(), 0));
        entity.setCriticalIssueCount(defaultNumber(report.getCriticalIssueCount(), 0));
        entity.setOptionalIssueCount(defaultNumber(report.getOptionalIssueCount(), 0));
        entity.setAnalyzeStatus(defaultText(report.getAnalyzeStatus(), "ANALYZED"));
        entity.setStatus(defaultText(report.getStatus(), "ACTIVE"));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(0);
        resumeAnalysisReportMapper.insert(entity);
        if (entity.getId() != null) {
            return entity.getId();
        }
        ResumeAnalysisReport saved = resumeAnalysisReportMapper.selectOne(Wrappers.lambdaQuery(ResumeAnalysisReport.class)
                .eq(ResumeAnalysisReport::getResumeId, result.getResumeId())
                .last("limit 1"));
        return saved == null ? null : saved.getId();
    }

    private Map<String, Long> saveAnalysisSections(ParsedResumeResult result, Long reportId) {
        Date now = new Date();
        Map<String, Long> sectionIdMap = new HashMap<>();
        for (AnalysisSection section : safeList(result.getAnalysisSections())) {
            if (section == null || !StringUtils.hasText(section.getSectionCode())) {
                continue;
            }
            ResumeAnalysisSection entity = new ResumeAnalysisSection();
            entity.setResumeId(result.getResumeId());
            entity.setReportId(reportId);
            entity.setSectionCode(section.getSectionCode());
            entity.setSectionTitle(section.getSectionTitle());
            entity.setWhyImportantTitle(defaultText(section.getWhyImportantTitle(), "Why This Is Important"));
            entity.setWhyImportantContent(section.getWhyImportantContent());
            entity.setActionText(defaultText(section.getActionText(), "Learn why this matters"));
            entity.setSortOrder(defaultNumber(section.getSortOrder(), 0));
            entity.setStatus(defaultText(section.getStatus(), "ACTIVE"));
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeAnalysisSectionMapper.insert(entity);
            Long sectionId = entity.getId();
            if (sectionId == null) {
                ResumeAnalysisSection saved = resumeAnalysisSectionMapper.selectOne(Wrappers.lambdaQuery(ResumeAnalysisSection.class)
                        .eq(ResumeAnalysisSection::getResumeId, result.getResumeId())
                        .eq(ResumeAnalysisSection::getSectionCode, entity.getSectionCode())
                        .last("limit 1"));
                sectionId = saved == null ? null : saved.getId();
            }
            sectionIdMap.put(entity.getSectionCode(), sectionId);
        }
        return sectionIdMap;
    }

    private void saveAnalysisIssues(ParsedResumeResult result, Long reportId, Map<String, Long> sectionIdMap) {
        Date now = new Date();
        for (AnalysisIssue issue : safeList(result.getAnalysisIssues())) {
            if (issue == null || !StringUtils.hasText(issue.getTitle())) {
                continue;
            }
            ResumeAnalysisIssue entity = new ResumeAnalysisIssue();
            entity.setResumeId(result.getResumeId());
            entity.setReportId(reportId);
            entity.setSectionId(sectionIdMap.get(issue.getSectionCode()));
            entity.setIssueLevel(issue.getIssueLevel());
            entity.setIssueType(issue.getIssueType());
            entity.setTitle(issue.getTitle());
            entity.setRelatedCount(defaultNumber(issue.getRelatedCount(), 1));
            entity.setDescription(issue.getDescription());
            entity.setSuggestion(issue.getSuggestion());
            entity.setExample(issue.getExample());
            entity.setSeverityScore(defaultNumber(issue.getSeverityScore(), 0));
            entity.setSortOrder(defaultNumber(issue.getSortOrder(), 0));
            entity.setStatus(defaultText(issue.getStatus(), "PENDING"));
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setDelFlag(0);
            resumeAnalysisIssueMapper.insert(entity);
        }
    }

    private void saveFailedAnalysisReport(String resumeId, Long userId, String reason) {
        deleteExistingResumeData(resumeId);
        Date now = new Date();
        ResumeAnalysisReport report = new ResumeAnalysisReport();
        report.setUserId(userId);
        report.setResumeId(resumeId);
        report.setAnalysisSummary(reason);
        report.setUrgentIssueCount(0);
        report.setCriticalIssueCount(0);
        report.setOptionalIssueCount(0);
        report.setAnalyzeStatus("FAILED");
        report.setStatus("ACTIVE");
        report.setCreateTime(now);
        report.setUpdateTime(now);
        report.setDelFlag(0);
        resumeAnalysisReportMapper.insert(report);
    }

    private void updateResumeScore(UserResumeFile userResumeFile, Integer scoreValue) {
        if (scoreValue == null) {
            return;
        }
        UserResumeFile update = UserResumeFile.builder()
                .id(userResumeFile.getId())
                .score(scoreValue.doubleValue())
                .build();
        userResumeFileMapper.updateById(update);
    }

    private String buildSystemPrompt() {
        return PromptTemplateLoader.load(STRUCTURED_PROMPT_DIR + "system.md");
    }

    private String buildUserPrompt(String task, String resumeText, String rulesFile, String schemaFile) {
        return PromptTemplateLoader.format(
                STRUCTURED_PROMPT_DIR + "user-template.md",
                task,
                PromptTemplateLoader.load(STRUCTURED_PROMPT_DIR + rulesFile),
                PromptTemplateLoader.load(STRUCTURED_PROMPT_DIR + schemaFile),
                resumeText
        );
    }

    private <T> T askJson(String systemPrompt, String userPrompt, Class<T> type) throws Exception {
        InfraAiJsonLlmClient.JsonLlmResult result = llmClient.chatJson(systemPrompt, userPrompt);
        return objectMapper.readValue(result.content(), type);
    }

    private void fillAnalysisDefaults(Long userId, String resumeId, AnalysisResult analysis) {
        if (analysis == null) {
            return;
        }
        if (analysis.getReport() != null) {
            analysis.getReport().setUserId(userId);
            analysis.getReport().setResumeId(resumeId);
            analysis.getReport().setAnalyzeStatus(defaultText(analysis.getReport().getAnalyzeStatus(), "ANALYZED"));
            analysis.getReport().setStatus(defaultText(analysis.getReport().getStatus(), "ACTIVE"));
        }
        if (analysis.getSections() != null) {
            for (AnalysisSection section : analysis.getSections()) {
                if (section == null) {
                    continue;
                }
                section.setResumeId(resumeId);
                section.setStatus(defaultText(section.getStatus(), "ACTIVE"));
            }
        }
        if (analysis.getIssues() != null) {
            for (AnalysisIssue issue : analysis.getIssues()) {
                if (issue == null) {
                    continue;
                }
                issue.setResumeId(resumeId);
                issue.setStatus(defaultText(issue.getStatus(), "PENDING"));
            }
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private Integer defaultNumber(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (Exception exception) {
            throw new IllegalStateException("JSON 序列化失败", exception);
        }
    }

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedResumeResult {
        private Long userId;
        private String resumeId;
        private String resumeName;
        private Profile profile;
        private List<Skill> skills;
        private List<WorkExperience> workExperiences;
        private List<ProjectExperience> projectExperiences;
        private List<Certification> certifications;
        private AnalysisReport analysisReport;
        private List<AnalysisSection> analysisSections;
        private List<AnalysisIssue> analysisIssues;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileResult {
        private Profile profile;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
        private String title;
        private String email;
        private String phone;
        private String location;
        private String linkedinText;
        private String linkedinUrl;
        private String githubText;
        private String githubUrl;
        private String otherLinkText;
        private String otherLinkUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisResult {
        private AnalysisReport report;
        private List<AnalysisSection> sections;
        private List<AnalysisIssue> issues;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisReport {
        private Long userId;
        private String resumeId;
        private Integer scoreValue;
        private String scoreGrade;
        private String scoreLevel;
        private String analysisSummary;
        private Integer urgentIssueCount;
        private Integer criticalIssueCount;
        private Integer optionalIssueCount;
        private String analyzeStatus;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisSection {
        private String resumeId;
        private Long reportId;
        private String sectionCode;
        private String sectionTitle;
        private String whyImportantTitle;
        private String whyImportantContent;
        private String actionText;
        private Integer sortOrder;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisIssue {
        private String resumeId;
        private Long reportId;
        private Long sectionId;
        private String sectionCode;
        private String issueLevel;
        private String issueType;
        private String title;
        private Integer relatedCount;
        private String description;
        private String suggestion;
        private String example;
        private Integer severityScore;
        private Integer sortOrder;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillResult {
        private List<Skill> skills;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Skill {
        private String category;
        private String skillName;
        private Integer sortOrder;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkResult {
        private List<WorkExperience> workExperiences;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkExperience {
        private String companyName;
        private String positionTitle;
        private String startDate;
        private String endDate;
        private List<String> description;
        private Integer sortOrder;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectAndCertificationResult {
        private List<ProjectExperience> projectExperiences;
        private List<Certification> certifications;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectExperience {
        private String projectName;
        private String roleTitle;
        private String startDate;
        private String endDate;
        private List<String> techStack;
        private List<String> description;
        private Integer sortOrder;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Certification {
        private String itemType;
        private String name;
        private String issuer;
        private String issueDate;
        private String description;
        private String credentialUrl;
        private Integer sortOrder;
    }
}
