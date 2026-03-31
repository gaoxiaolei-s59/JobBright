package org.puregxl.site.rag.controller;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.result.Result;
import org.puregxl.site.framework.web.Results;
import org.puregxl.site.rag.dto.resp.DownloadFileResponse;
import org.puregxl.site.rag.dto.resp.UploadFileResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.puregxl.site.rag.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<UploadFileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        return Results.success(fileService.uploadFile(file));
    }

    /**
     * 获取文件详情接口
     * @param fileId
     * @return
     */
    @GetMapping("/{fileId}")
    public Result<UploadFileResponse> getFile(@PathVariable("fileId") String fileId) {
        return Results.success(fileService.getFile(fileId));
    }

    /**
     * 下载文件接口
     * @param fileId
     * @return
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("fileId") String fileId) {
        DownloadFileResponse response = fileService.downloadFile(fileId);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (response.getContentType() != null && !response.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(response.getContentType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(response.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(response.getFileName())
                        .build()
                        .toString())
                .body(new ByteArrayResource(response.getContent()));
    }
}
