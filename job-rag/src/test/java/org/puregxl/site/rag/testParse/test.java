package org.puregxl.site.rag.testParse;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


public class test {

    @Test
    void test() throws TikaException, IOException {
        TikaParseService parseService = new TikaParseService();

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
        System.out.println(result.getContent());
        System.out.println("=======================================================");
        System.out.println(result.getMetadata());
    }


}
