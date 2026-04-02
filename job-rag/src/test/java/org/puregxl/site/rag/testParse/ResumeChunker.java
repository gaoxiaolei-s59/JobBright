package org.puregxl.site.rag.testParse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResumeChunker {

    private static final int MAX_CHUNK_SIZE = 420;
    private static final int OVERLAP_SIZE = 60;
    private static final Set<String> SECTION_TITLES = Set.of(
            "教育背景", "实习经验", "工作经历", "项目经历", "项目", "技能", "专业技能", "自我评价"
    );
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile(
            "(20\\d{2}[./]\\d{1,2})\\s*[-~至]\\s*(至今|20\\d{2}[./]\\d{1,2})"
    );
    private static final Map<String, String> TECH_ALIAS_MAP = buildTechAliasMap();

    public ChunkingResult chunk(String text) {
        List<String> lines = preprocessLines(text);
        List<SectionBlock> sections = splitSections(lines);
        List<EntryBlock> entries = buildEntries(sections);
        List<ChunkBlock> chunks = buildChunks(entries);
        return new ChunkingResult(sections, entries, chunks);
    }

    private List<String> preprocessLines(String text) {
        String normalized = text == null ? "" : text.replace("\r\n", "\n").replace("\r", "\n");
        String[] rawLines = normalized.split("\n");
        List<String> lines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String line = rawLine.replace('\u00A0', ' ').replaceAll("[ \\t]+", " ").trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<SectionBlock> splitSections(List<String> lines) {
        List<SectionBlock> sections = new ArrayList<>();
        String currentSection = "基本信息";
        List<String> buffer = new ArrayList<>();
        int sortNo = 0;

        for (String line : lines) {
            if (isSectionTitle(line)) {
                if (!buffer.isEmpty()) {
                    sections.add(new SectionBlock(currentSection, new ArrayList<>(buffer), sortNo++));
                    buffer.clear();
                }
                currentSection = normalizeSectionTitle(line);
                continue;
            }
            if ("项目经历".equals(currentSection) && looksLikeSkillSectionStart(line)) {
                if (!buffer.isEmpty()) {
                    sections.add(new SectionBlock(currentSection, new ArrayList<>(buffer), sortNo++));
                    buffer.clear();
                }
                currentSection = "技能";
            } else if (("项目经历".equals(currentSection) || "技能".equals(currentSection))
                    && looksLikeSummaryStart(line)) {
                if (!buffer.isEmpty()) {
                    sections.add(new SectionBlock(currentSection, new ArrayList<>(buffer), sortNo++));
                    buffer.clear();
                }
                currentSection = "自我评价";
            }
            buffer.add(line);
        }

        if (!buffer.isEmpty()) {
            sections.add(new SectionBlock(currentSection, new ArrayList<>(buffer), sortNo));
        }
        return sections;
    }

    private boolean isSectionTitle(String line) {
        return SECTION_TITLES.contains(line);
    }

    private String normalizeSectionTitle(String line) {
        if ("项目".equals(line)) {
            return "项目经历";
        }
        if ("专业技能".equals(line)) {
            return "技能";
        }
        return line;
    }

    private List<EntryBlock> buildEntries(List<SectionBlock> sections) {
        List<EntryBlock> entries = new ArrayList<>();
        int sortNo = 0;
        for (SectionBlock section : sections) {
            List<EntryBlock> sectionEntries = switch (section.name()) {
                case "基本信息" -> buildBasicEntries(section, sortNo);
                case "教育背景" -> buildEducationEntries(section, sortNo);
                case "实习经验", "工作经历" -> buildExperienceEntries(section, sortNo, "INTERNSHIP");
                case "项目经历" -> buildProjectEntries(section, sortNo);
                case "技能" -> buildSkillEntries(section, sortNo);
                case "自我评价" -> buildSummaryEntries(section, sortNo);
                default -> buildGenericEntries(section, sortNo);
            };
            entries.addAll(sectionEntries);
            sortNo += sectionEntries.size();
        }
        return entries;
    }

    private List<EntryBlock> buildBasicEntries(SectionBlock section, int startSortNo) {
        List<EntryBlock> result = new ArrayList<>();
        List<String> summaryLines = new ArrayList<>();
        List<String> contactLines = new ArrayList<>();
        for (String line : section.lines()) {
            if (isContactLine(line)) {
                contactLines.add(line);
            } else if (!looksLikeProjectBullet(line)) {
                summaryLines.add(line);
            }
        }

        int sortNo = startSortNo;
        if (!summaryLines.isEmpty()) {
            result.add(buildEntry(section.name(), "PROFILE", "顶部摘要", null, null, null,
                    List.of(), String.join("\n", summaryLines), sortNo++));
        }
        if (!contactLines.isEmpty()) {
            result.add(buildEntry(section.name(), "CONTACT", "联系方式", null, null, null,
                    List.of(), String.join("\n", contactLines), sortNo));
        }
        return result;
    }

    private List<EntryBlock> buildEducationEntries(SectionBlock section, int startSortNo) {
        List<EntryBlock> result = new ArrayList<>();
        List<String> lines = section.lines();
        if (lines.isEmpty()) {
            return result;
        }

        List<String> current = new ArrayList<>();
        int sortNo = startSortNo;
        for (String line : lines) {
            if (!current.isEmpty() && looksLikeEducationStart(line)) {
                result.add(buildStructuredEntry(section.name(), "EDUCATION", current, sortNo++));
                current = new ArrayList<>();
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            result.add(buildStructuredEntry(section.name(), "EDUCATION", current, sortNo));
        }
        return result;
    }

    private List<EntryBlock> buildExperienceEntries(SectionBlock section, int startSortNo, String entryType) {
        List<EntryBlock> result = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int sortNo = startSortNo;
        for (String line : section.lines()) {
            if (!current.isEmpty() && looksLikeCompanyRoleLine(line)) {
                result.add(buildStructuredEntry(section.name(), entryType, current, sortNo++));
                current = new ArrayList<>();
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            result.add(buildStructuredEntry(section.name(), entryType, current, sortNo));
        }
        return result;
    }

    private List<EntryBlock> buildProjectEntries(SectionBlock section, int startSortNo) {
        List<EntryBlock> result = new ArrayList<>();
        List<String> lines = section.lines();
        List<String> current = new ArrayList<>();
        int sortNo = startSortNo;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String nextLine = i + 1 < lines.size() ? lines.get(i + 1) : null;
            if (!current.isEmpty() && looksLikeProjectStart(current, line, nextLine)) {
                result.add(buildStructuredEntry(section.name(), "PROJECT", current, sortNo++));
                current = new ArrayList<>();
            }
            current.add(line);
        }

        if (!current.isEmpty()) {
            result.add(buildStructuredEntry(section.name(), "PROJECT", current, sortNo));
        }
        return result;
    }

    private List<EntryBlock> buildSkillEntries(SectionBlock section, int startSortNo) {
        List<EntryBlock> result = new ArrayList<>();
        int sortNo = startSortNo;
        for (String line : section.lines()) {
            result.add(buildEntry(section.name(), "SKILL", extractLabelTitle(line, "技能"), null, null, null,
                    collectTechStack(List.of(line)), line, sortNo++));
        }
        return result;
    }

    private List<EntryBlock> buildSummaryEntries(SectionBlock section, int startSortNo) {
        List<String> lines = section.lines();
        int projectStartIndex = findEmbeddedProjectStart(lines);
        if (projectStartIndex < 0) {
            return List.of(buildEntry(section.name(), "SUMMARY", "自我评价", null, null, null,
                    List.of(), String.join("\n", lines), startSortNo));
        }

        List<EntryBlock> result = new ArrayList<>();
        List<String> summaryLines = lines.subList(0, projectStartIndex);
        if (!summaryLines.isEmpty()) {
            result.add(buildEntry(section.name(), "SUMMARY", "自我评价", null, null, null,
                    List.of(), String.join("\n", summaryLines), startSortNo));
        }

        SectionBlock projectSection = new SectionBlock("项目经历", new ArrayList<>(lines.subList(projectStartIndex, lines.size())), 0);
        result.addAll(buildProjectEntries(projectSection, startSortNo + result.size()));
        return result;
    }

    private List<EntryBlock> buildGenericEntries(SectionBlock section, int startSortNo) {
        return List.of(buildEntry(section.name(), "GENERIC", section.name(), null, null, null,
                List.of(), String.join("\n", section.lines()), startSortNo));
    }

    private EntryBlock buildStructuredEntry(String sectionName, String entryType, List<String> lines, int sortNo) {
        String firstLine = lines.get(0);
        String entryTitle = firstLine;
        String orgName = null;
        String roleName = null;
        String timeRange = extractTimeRange(String.join("\n", lines));
        List<String> techStack = collectTechStack(lines);

        if ("EDUCATION".equals(entryType)) {
            orgName = extractBeforeTime(firstLine);
            entryTitle = orgName;
        } else if ("INTERNSHIP".equals(entryType)) {
            String withoutTime = removeTimeRange(firstLine);
            String[] parts = withoutTime.split("\\s+-\\s+", 2);
            orgName = parts[0].trim();
            roleName = parts.length > 1 ? parts[1].trim() : null;
            entryTitle = firstLine;
        } else if ("PROJECT".equals(entryType)) {
            entryTitle = firstLine;
        }

        return buildEntry(sectionName, entryType, entryTitle, orgName, roleName, timeRange,
                techStack, String.join("\n", lines), sortNo);
    }

    private EntryBlock buildEntry(String sectionName, String entryType, String entryTitle, String orgName,
                                  String roleName, String timeRange, List<String> techStack,
                                  String content, int sortNo) {
        return new EntryBlock(
                UUID.randomUUID().toString().replace("-", ""),
                sectionName,
                entryType,
                entryTitle,
                orgName,
                roleName,
                timeRange,
                techStack,
                content,
                sortNo
        );
    }

    private List<ChunkBlock> buildChunks(List<EntryBlock> entries) {
        List<ChunkBlock> chunks = new ArrayList<>();
        for (EntryBlock entry : entries) {
            List<String> splitParts = splitEntryContent(entry.contentText());
            int total = splitParts.size();
            for (int i = 0; i < total; i++) {
                String text = splitParts.get(i);
                int overlapChars = 0;
                if (i > 0) {
                    String previous = splitParts.get(i - 1);
                    String overlap = previous.substring(Math.max(0, previous.length() - OVERLAP_SIZE));
                    text = overlap + "\n" + text;
                    overlapChars = overlap.length();
                }
                chunks.add(new ChunkBlock(
                        UUID.randomUUID().toString().replace("-", ""),
                        entry.entryId(),
                        entry.sectionName(),
                        entry.entryType(),
                        entry.entryTitle(),
                        entry.orgName(),
                        entry.roleName(),
                        entry.timeRange(),
                        i,
                        total,
                        estimateTokenCount(text),
                        text.length(),
                        overlapChars,
                        text,
                        buildSummary(entry, text)
                ));
            }
        }
        return chunks;
    }

    private List<String> splitEntryContent(String content) {
        if (content.length() <= MAX_CHUNK_SIZE) {
            return List.of(content);
        }

        List<String> units = new ArrayList<>();
        for (String line : content.split("\n")) {
            if (line.contains("：") || line.contains(":")) {
                units.add(line);
                continue;
            }
            for (String sentence : line.split("(?<=[。；])")) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) {
                    units.add(trimmed);
                }
            }
        }

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String unit : units) {
            if (current.length() > 0 && current.length() + unit.length() + 1 > MAX_CHUNK_SIZE) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(unit);
        }
        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }
        return parts.isEmpty() ? List.of(content) : parts;
    }

    private boolean looksLikeEducationStart(String line) {
        return hasTimeRange(line) && line.contains("大学");
    }

    private boolean looksLikeCompanyRoleLine(String line) {
        return hasTimeRange(line) && line.contains(" - ");
    }

    private boolean looksLikeProjectStart(List<String> currentLines, String line, String nextLine) {
        if (!looksLikeProjectTitle(line, nextLine)) {
            return false;
        }
        if (!currentLines.isEmpty() && looksLikeTechStack(currentLines.get(currentLines.size() - 1))) {
            return false;
        }
        return true;
    }

    private boolean looksLikeProjectTitle(String line, String nextLine) {
        if (line.contains("：") || line.endsWith("：") || line.endsWith(":")) {
            return false;
        }
        if (line.startsWith("项目时间") || line.startsWith("项目描述") || line.startsWith("体验地址")
                || line.startsWith("核心亮点") || line.startsWith("参与项目")) {
            return false;
        }
        if (line.startsWith("http")) {
            return false;
        }
        if (hasTimeRange(line)) {
            return false;
        }
        if (looksLikeTechStack(line)) {
            return false;
        }
        if (nextLine == null) {
            return false;
        }
        if (!(looksLikeTechStack(nextLine) || nextLine.startsWith("项目时间") || nextLine.startsWith("体验地址"))) {
            return false;
        }
        return isLikelyProjectTitleText(line);
    }

    private int findEmbeddedProjectStart(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String nextLine = i + 1 < lines.size() ? lines.get(i + 1) : null;
            if (looksLikeProjectTitle(line, nextLine)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasTimeRange(String line) {
        return TIME_RANGE_PATTERN.matcher(line).find();
    }

    private String extractTimeRange(String text) {
        Matcher matcher = TIME_RANGE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String removeTimeRange(String text) {
        return TIME_RANGE_PATTERN.matcher(text).replaceFirst("").trim();
    }

    private String extractBeforeTime(String text) {
        Matcher matcher = TIME_RANGE_PATTERN.matcher(text);
        if (matcher.find()) {
            return text.substring(0, matcher.start()).trim();
        }
        return text;
    }

    private List<String> collectTechStack(List<String> lines) {
        LinkedHashSet<String> techs = new LinkedHashSet<>();
        for (String line : lines) {
            if (!looksLikeTechStack(line) && !line.startsWith("Java 基础与底层")) {
                continue;
            }
            for (String token : tokenizeTechLine(line)) {
                String tech = normalizeTechToken(token);
                if (tech != null) {
                    techs.add(tech);
                }
            }
        }
        return new ArrayList<>(techs);
    }

    private boolean looksLikeTechStack(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        int count = 0;
        for (String keyword : List.of("Java", "Spring", "Redis", "MySQL", "RocketMQ", "Kafka", "Docker", "Nacos",
                "Sentinel", "ShardingSphere", "Redisson", "EasyExcel", "Boot", "Cloud")) {
            if (line.contains(keyword)) {
                count++;
            }
        }
        return count >= 2;
    }

    private boolean containsTechKeyword(String word) {
        return word.matches(".*[A-Za-z].*") || List.of("中间件", "微服务", "数据库", "缓存").contains(word);
    }

    private List<String> tokenizeTechLine(String line) {
        String normalized = line
                .replace("：", " ")
                .replace(":", " ")
                .replace("（", " ")
                .replace("）", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace("、", " ")
                .replace("，", " ")
                .replace(",", " ")
                .replace("/", " ")
                .replace("+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(normalized.split(" "));
    }

    private String normalizeTechToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String cleaned = token
                .replaceAll("^[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fff]+", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}.+#\\-\\u4e00-\\u9fff]+$", "");
        if (cleaned.isBlank()) {
            return null;
        }
        if (cleaned.matches(".*[；。].*")) {
            return null;
        }
        String alias = TECH_ALIAS_MAP.get(cleaned.toLowerCase());
        if (alias != null) {
            return alias;
        }
        return null;
    }

    private boolean isLikelyProjectTitleText(String line) {
        if (line.length() < 4 || line.length() > 40) {
            return false;
        }
        return containsChinese(line) || line.contains("（") || line.contains("(");
    }

    private boolean containsChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch >= '\u4e00' && ch <= '\u9fff') {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeSkillSectionStart(String line) {
        return line.startsWith("Java 基础与底层：")
                || line.startsWith("开发框架：")
                || line.startsWith("数据库与缓存：")
                || line.startsWith("微服务基础组件：")
                || line.startsWith("消息队列与中间件：")
                || line.startsWith("工程与部署：")
                || line.startsWith("RAG 与大模型应用基础：");
    }

    private boolean looksLikeSummaryStart(String line) {
        return line.startsWith("扎实掌握")
                || line.contains("学习能力强")
                || line.contains("团队协作意识")
                || line.contains("问题排查");
    }

    private boolean isContactLine(String line) {
        return line.contains("@")
                || line.startsWith("GitHub：")
                || line.startsWith("求职意向：")
                || line.startsWith("状态：")
                || line.matches("^1\\d{2}.*")
                || line.matches(".*\\d{4}\\.\\d.*");
    }

    private boolean looksLikeProjectBullet(String line) {
        return line.contains("优化")
                || line.contains("治理")
                || line.contains("防护")
                || line.contains("异步")
                || line.contains("一致性")
                || line.contains("统计")
                || line.contains("分库分表");
    }

    private static Map<String, String> buildTechAliasMap() {
        Map<String, String> aliasMap = new HashMap<>();
        for (String tech : List.of(
                "Java", "Spring", "Spring Boot", "Spring Cloud", "Alibaba", "RocketMQ", "Kafka",
                "Redis", "MySQL", "MongoDB", "ShardingSphere", "Sentinel", "Nacos", "Redisson",
                "Docker", "EasyExcel", "XXL-Job", "JUC", "JVM", "GC", "MyBatis-Plus", "EXPLAIN",
                "Elasticsearch", "Lua", "Pipeline", "RAG", "Embedding", "Prompt Engineering"
        )) {
            aliasMap.put(tech.toLowerCase(), tech);
        }
        aliasMap.put("boot", "Spring Boot");
        aliasMap.put("cloud", "Spring Cloud");
        aliasMap.put("5.x", "RocketMQ");
        aliasMap.put("v2", "RocketMQ");
        aliasMap.put("xxl-job", "XXL-Job");
        aliasMap.put("mybatis-plus", "MyBatis-Plus");
        aliasMap.put("elasticsearch", "Elasticsearch");
        return aliasMap;
    }

    private String extractLabelTitle(String line, String defaultTitle) {
        int idx = line.indexOf('：');
        if (idx < 0) {
            idx = line.indexOf(':');
        }
        return idx > 0 ? line.substring(0, idx) : defaultTitle;
    }

    private int estimateTokenCount(String text) {
        return Math.max(1, text.length() / 2);
    }

    private String buildSummary(EntryBlock entry, String text) {
        String compact = text.replace('\n', ' ');
        String prefix = entry.sectionName() + " | " + entry.entryTitle();
        String body = compact.length() > 80 ? compact.substring(0, 80) + "..." : compact;
        return prefix + " | " + body;
    }

    public record SectionBlock(String name, List<String> lines, int sortNo) {
    }

    public record EntryBlock(
            String entryId,
            String sectionName,
            String entryType,
            String entryTitle,
            String orgName,
            String roleName,
            String timeRange,
            List<String> techStack,
            String contentText,
            int sortNo
    ) {
    }

    public record ChunkBlock(
            String chunkId,
            String entryId,
            String sectionName,
            String entryType,
            String entryTitle,
            String orgName,
            String roleName,
            String timeRange,
            int partIndex,
            int partTotal,
            int tokenCount,
            int charCount,
            int overlapChars,
            String chunkText,
            String summaryText
    ) {
    }

    public record ChunkingResult(
            List<SectionBlock> sections,
            List<EntryBlock> entries,
            List<ChunkBlock> chunks
    ) {
    }
}
