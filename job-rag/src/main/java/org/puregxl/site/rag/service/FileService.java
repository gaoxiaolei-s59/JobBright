package org.puregxl.site.rag.service;

import org.puregxl.site.rag.dto.resp.DownloadFileResponse;
import org.puregxl.site.rag.dto.resp.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    UploadFileResponse uploadFile(MultipartFile file);

    UploadFileResponse getFile(String fileId);

    DownloadFileResponse downloadFile(String fileId);

    DownloadFileResponse downloadFileByUrl(String fileUrl);

    MultipartFile downloadMultipartFileByUrl(String fileUrl);
}
