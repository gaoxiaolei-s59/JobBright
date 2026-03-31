package org.puregxl.site.jobbacked.service;

import org.puregxl.site.jobbacked.dto.file.UploadFileInfo;

public interface FileStorageService {
    UploadFileInfo uploadFile(UploadFileInfo fileInfo);
}
