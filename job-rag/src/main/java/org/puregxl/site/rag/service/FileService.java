package org.puregxl.site.rag.service;

import org.puregxl.site.rag.dto.resp.DownloadFileResponse;
import org.puregxl.site.rag.dto.resp.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {


    UploadFileResponse getFile(String resumeId, String buckName);

    DownloadFileResponse downloadFile(String resumeId, String buckName);

    DownloadFileResponse downloadFileByUrl(String fileUrl);

    MultipartFile downloadMultipartFileByUrl(String fileUrl);
}
