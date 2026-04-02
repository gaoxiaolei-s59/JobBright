package org.puregxl.site.rag.parse;

import lombok.*;

import java.util.Map;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ParseResult {
    /**
     * 检测到的 MIME 类型
     */
    private String mimeType;

    /**
     * 提取的文本内容
     */
    private String content;

    /**
     * 提取的元数据
     */
    private Map<String, String> metadata;

    /**
     * 文本长度（字符数）
     */
    private int contentLength;


    // 静态工厂方法
    public static ParseResult success(String mimeType, String content, Map<String, String> metadata) {
        ParseResult result = new ParseResult();
        result.setMimeType(mimeType);
        result.setContent(content);
        result.setContentLength(content != null ? content.length() : 0);
        result.setMetadata(metadata);
        return result;
    }


}
