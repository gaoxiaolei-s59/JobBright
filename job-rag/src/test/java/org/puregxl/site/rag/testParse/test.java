package org.puregxl.site.rag.testParse;

import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class test {

    @Test
    void test() throws TikaException, IOException {
        TikaParseService parseService = new TikaParseService();
        ResumeChunker resumeChunker = new ResumeChunker();

        String filePath = "src/test/java/org/puregxl/site/rag/testParse/高晓雷-实习.pdf"; // 本地文件路径

        MultipartFile file;
        try (InputStream inputStream = new FileInputStream(filePath)) {
            file = new MockMultipartFile(
                    "file",                 // 表单字段名
                    "demo.pdf",             // 原始文件名
                    "application/pdf",      // contentType
                    inputStream             // 文件内容
            );
        }

        ParseResult result = parseService.parseFile(file);
        ResumeChunker.ChunkingResult chunkingResult = resumeChunker.chunk(result.getContent());

        System.out.println("===== Sections =====");
        for (ResumeChunker.SectionBlock section : chunkingResult.sections()) {
            System.out.println(section.sortNo() + " -> " + section.name() + " (" + section.lines().size() + " lines)");
        }

        System.out.println("=======================================================");
        System.out.println("===== Entries =====");
        for (ResumeChunker.EntryBlock entry : chunkingResult.entries()) {
            System.out.println("section=" + entry.sectionName()
                    + ", type=" + entry.entryType()
                    + ", title=" + entry.entryTitle()
                    + ", time=" + entry.timeRange()
                    + ", tech=" + entry.techStack());
            System.out.println(entry.contentText());
            System.out.println("-------------------------------------------------------");
        }

        System.out.println("=======================================================");
        System.out.println("===== Chunks =====");
        for (ResumeChunker.ChunkBlock chunk : chunkingResult.chunks()) {
            System.out.println("section=" + chunk.sectionName()
                    + ", title=" + chunk.entryTitle()
                    + ", part=" + chunk.partIndex() + "/" + chunk.partTotal()
                    + ", chars=" + chunk.charCount()
                    + ", overlap=" + chunk.overlapChars());
            System.out.println(chunk.chunkText());
            System.out.println("-------------------------------------------------------");
        }

        System.out.println("=======================================================");
        System.out.println(result.getMetadata());
    }
}
