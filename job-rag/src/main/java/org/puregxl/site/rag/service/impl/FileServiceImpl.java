package org.puregxl.site.rag.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Put;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.rag.config.RustfsProperties;
import org.puregxl.site.rag.dao.entity.RagFile;
import org.puregxl.site.rag.dao.mapper.FileMapper;
import org.puregxl.site.rag.dto.resp.DownloadFileResponse;
import org.puregxl.site.rag.dto.resp.UploadFileResponse;
import org.puregxl.site.rag.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String STATUS_UPLOADED = "UPLOADED";

    private final FileMapper fileMapper;

    private final S3Client s3Client;

    private final RustfsProperties rustfsProperties;

    /**
     * 上传文件接口
     * @param file
     * @return
     */
    @Override
    public UploadFileResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new ClientException("文件名不能为空");
        }

        String fileId = IdUtil.fastSimpleUUID();
        String fileExt = getFileExt(originalFilename);
        String objectKey = buildObjectKey(fileId, fileExt);
        //创建桶
        ensureBucketExists();
        putObject(file, objectKey);

        String objectUrl = buildObjectUrl(objectKey);
        RagFile ragFile = RagFile.builder()
                .fileId(fileId)
                .fileName(originalFilename)
                .fileExt(fileExt)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .bucketName(rustfsProperties.getBucketName())
                .objectKey(objectKey)
                .objectUrl(objectUrl)
                .status(STATUS_UPLOADED)
                .build();
        fileMapper.insert(ragFile);

        return UploadFileResponse.builder()
                .fileId(fileId)
                .fileName(originalFilename)
                .fileSize(file.getSize())
                .bucketName(rustfsProperties.getBucketName())
                .objectKey(objectKey)
                .objectUrl(objectUrl)
                .status(STATUS_UPLOADED)
                .build();
    }

    /**
     * 获取文件
     * @param fileId
     * @return
     */
    @Override
    public UploadFileResponse getFile(String fileId) {
        RagFile ragFile = getRagFile(fileId);
        return UploadFileResponse.builder()
                .fileId(ragFile.getFileId())
                .fileName(ragFile.getFileName())
                .fileSize(ragFile.getFileSize())
                .bucketName(ragFile.getBucketName())
                .objectKey(ragFile.getObjectKey())
                .objectUrl(ragFile.getObjectUrl())
                .status(ragFile.getStatus())
                .build();
    }

    /**
     * 下载文件接口
     * @param fileId
     * @return
     */
    @Override
    public DownloadFileResponse downloadFile(String fileId) {
        RagFile ragFile = getRagFile(fileId);
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(ragFile.getBucketName())
                    .key(ragFile.getObjectKey())
                    .build());
            return DownloadFileResponse.builder()
                    .fileName(ragFile.getFileName())
                    .contentType(ragFile.getContentType())
                    .fileSize(ragFile.getFileSize())
                    .content(objectBytes.asByteArray())
                    .build();
        } catch (S3Exception exception) {
            throw new ClientException("读取对象存储文件失败");
        }
    }

    private RagFile getRagFile(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            throw new ClientException("文件ID不能为空");
        }
        LambdaQueryWrapper<RagFile> queryWrapper = Wrappers.lambdaQuery(RagFile.class)
                .eq(RagFile::getFileId, fileId)
                .eq(RagFile::getDelFlag, 0);
        RagFile ragFile = fileMapper.selectOne(queryWrapper);
        if (ragFile == null) {
            throw new ClientException("文件不存在");
        }
        return ragFile;
    }

    /**
     * 插入到云存储中
     * @param file
     * @param objectKey
     */
    private void putObject(MultipartFile file, String objectKey) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(rustfsProperties.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException exception) {
            throw new ClientException("读取上传文件失败");
        } catch (S3Exception exception) {
            throw new ClientException("上传文件到对象存储失败");
        }
    }

    private void ensureBucketExists() {
        if (!Boolean.TRUE.equals(rustfsProperties.getCreateBucketIfMissing())) {
            return;
        }
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(rustfsProperties.getBucketName())
                    .build());
        } catch (NoSuchBucketException exception) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(rustfsProperties.getBucketName())
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(rustfsProperties.getBucketName())
                        .build());
                return;
            }
            throw new ClientException("检查对象存储桶失败");
        }
    }

    private String buildObjectKey(String fileId, String fileExt) {
        String suffix = StringUtils.hasText(fileExt) ? "." + fileExt : "";
        return "rag/upload/" + fileId + suffix;
    }

    private String buildObjectUrl(String objectKey) {
        return rustfsProperties.getUrl() + "/" + rustfsProperties.getBucketName() + "/" + objectKey;
    }

    private String getFileExt(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase();
    }
}
