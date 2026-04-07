//package org.puregxl.site.rag.testParse;
//
//import org.apache.tika.exception.TikaException;
//import org.junit.jupiter.api.Test;
//import org.puregxl.site.rag.dao.entity.UserResumeProfile;
//import org.puregxl.site.rag.parse.TikaParseService;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//
//public class test {
//
//    @Test
//    void test() throws TikaException, IOException {
//        TikaParseService parseService = new TikaParseService();
//        ResumeChunker resumeChunker = new ResumeChunker();
//
//        String filePath = "src/test/java/org/puregxl/site/rag/testParse/高晓雷-实习.pdf"; // 本地文件路径
//
//        MultipartFile file;
//        try (InputStream inputStream = new FileInputStream(filePath)) {
//            file = new MockMultipartFile(
//                    "file",                 // 表单字段名
//                    "demo.pdf",             // 原始文件名
//                    "application/pdf",      // contentType
//                    inputStream             // 文件内容
//            );
//        }
//
//        ParseResult result = parseService.parseFile(file);
//        ResumeChunker.ChunkingResult chunkingResult = resumeChunker.chunk(result.getContent());
//
//        System.out.println("===== Sections =====");
//        for (ResumeChunker.SectionBlock section : chunkingResult.sections()) {
//            System.out.println(section.sortNo() + " -> " + section.name() + " (" + section.lines().size() + " lines)");
//        }
//
//        System.out.println("=======================================================");
//        System.out.println("===== Entries =====");
//        for (ResumeChunker.EntryBlock entry : chunkingResult.entries()) {
//            System.out.println("section=" + entry.sectionName()
//                    + ", type=" + entry.entryType()
//                    + ", title=" + entry.entryTitle()
//                    + ", time=" + entry.timeRange()
//                    + ", tech=" + entry.techStack());
//            System.out.println(entry.contentText());
//            System.out.println("-------------------------------------------------------");
//        }
//
//        System.out.println("=======================================================");
//        System.out.println("===== Chunks =====");
//        for (ResumeChunker.ChunkBlock chunk : chunkingResult.chunks()) {
//            System.out.println("section=" + chunk.sectionName()
//                    + ", title=" + chunk.entryTitle()
//                    + ", part=" + chunk.partIndex() + "/" + chunk.partTotal()
//                    + ", chars=" + chunk.charCount()
//                    + ", overlap=" + chunk.overlapChars());
//            System.out.println(chunk.chunkText());
//            System.out.println("-------------------------------------------------------");
//        }
//
//        System.out.println("=======================================================");
//        System.out.println(result.getMetadata());
//    }
//
//    @Test
//    void testGenerateResumeProfile() throws Exception {
//        TikaParseService parseService = new TikaParseService();
//
//        String filePath = "src/test/java/org/puregxl/site/rag/testParse/test1.pdf";
//        MultipartFile file;
//        try (InputStream inputStream = new FileInputStream(filePath)) {
//            file = new MockMultipartFile("file", "demo.pdf", "application/pdf", inputStream);
//        }
//
//        ParseResult parseResult = parseService.parseFile(file);
//        String systemPrompt = ResumeProfilePromptBuilder.buildSystemPrompt();
//        String userPrompt = parseResult.getContent();
//
//        System.out.println("===== System Prompt =====");
//        System.out.println(systemPrompt);
//        System.out.println("=======================================================");
//        System.out.println("===== User Prompt =====");
//        System.out.println(userPrompt);
//
//        String apiKey = "sk-rjtfqcpnhpzonswkebygmaqnqvibqcndgqxqfxghizuguthf";
//
//        ResumeProfileLlmClient llmClient = new ResumeProfileLlmClient(apiKey);
//        String profileJson = llmClient.generateProfileJson(systemPrompt, userPrompt);
//
//        System.out.println("=======================================================");
//        System.out.println("===== Resume Profile JSON =====");
//        System.out.println(profileJson);
//
//        ResumeProfileDTO dto = ResumeProfileConverter.toDto(profileJson);
//        System.out.println("=======================================================");
//        System.out.println("===== Resume Profile DTO =====");
//        System.out.println(dto);
//
//        UserResumeProfile userResumeProfile = ResumeProfileConverter.toEntity(
//                dto,
//                profileJson,
//                "test_resume_id"
//        );
//        System.out.println("=======================================================");
//        System.out.println("===== UserResumeProfile Entity =====");
//        System.out.println(userResumeProfile);
//
//        UserResumeProfile cleanedProfile = ResumeProfilePostProcessor.clean(userResumeProfile);
//        System.out.println("=======================================================");
//        System.out.println("===== Cleaned UserResumeProfile Entity =====");
//        System.out.println(cleanedProfile);
//    }
//}
