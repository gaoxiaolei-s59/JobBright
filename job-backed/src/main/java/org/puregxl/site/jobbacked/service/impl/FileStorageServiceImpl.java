package org.puregxl.site.jobbacked.service.impl;

import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.config.RustfsProperties;
import org.puregxl.site.jobbacked.dto.file.UploadFileInfo;
import org.puregxl.site.jobbacked.mq.event.UploadResumeExecuteTaskEvent;
import org.puregxl.site.jobbacked.mq.producer.JobBackedUserResumeProduceTemplate;
import org.puregxl.site.jobbacked.service.FileStorageService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;

    private final RustfsProperties rustfsProperties;


    @Override
    public UploadFileInfo uploadFile(UploadFileInfo fileInfo) {
        if (fileInfo == null || fileInfo.getContent() == null || fileInfo.getContent().length == 0) {
            throw new ClientException("上传文件内容不能为空");
        }
        ensureBucketExists(fileInfo.getBucketName());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(fileInfo.getBucketName())
                    .key(fileInfo.getObjectKey())
                    .contentType(fileInfo.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(fileInfo.getContent()));
            return fileInfo;
        } catch (S3Exception exception) {
            throw new ClientException("上传文件到对象存储失败");
        }
    }

    private void ensureBucketExists(String bucketName) {
        if (!Boolean.TRUE.equals(rustfsProperties.getCreateBucketIfMissing())) {
            return;
        }
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (NoSuchBucketException exception) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
                return;
            }
            throw new ClientException("检查对象存储桶失败");
        }
    }
}
