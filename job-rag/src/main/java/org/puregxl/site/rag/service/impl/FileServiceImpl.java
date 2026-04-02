package org.puregxl.site.rag.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.rag.config.RustfsProperties;
import org.puregxl.site.rag.dao.entity.UserResumeFile;
import org.puregxl.site.rag.dao.mapper.UserResumeFileMapper;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String STATUS_UPLOADED = "UPLOADED";
    private static final Integer CURRENT_VERSION = 1;

    private final UserResumeFileMapper userResumeFileMapper;

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

        String resumeId = IdUtil.fastSimpleUUID();
        String fileExt = getFileExt(originalFilename);
        String objectKey = buildObjectKey(resumeId, fileExt);
        //创建桶
        ensureBucketExists();
        putObject(file, objectKey);

        String objectUrl = buildObjectUrl(objectKey);
        UserResumeFile userResumeFile = UserResumeFile.builder()
                .resumeId(resumeId)
                .fileName(originalFilename)
                .fileExt(fileExt)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .objectKey(objectKey)
                .objectUrl(objectUrl)
                .isCurrent(CURRENT_VERSION)
                .build();
        userResumeFileMapper.insert(userResumeFile);

        return UploadFileResponse.builder()
                .resumeId(resumeId)
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
     * @param resumeId
     * @return
     */
    @Override
    public UploadFileResponse getFile(String resumeId) {
        UserResumeFile userResumeFile = getUserResumeFile(resumeId);
        return UploadFileResponse.builder()
                .resumeId(userResumeFile.getResumeId())
                .fileName(userResumeFile.getFileName())
                .fileSize(userResumeFile.getFileSize())
                .bucketName(rustfsProperties.getBucketName())
                .objectKey(userResumeFile.getObjectKey())
                .objectUrl(userResumeFile.getObjectUrl())
                .status(STATUS_UPLOADED)
                .build();
    }

    /**
     * 下载文件接口
     * @param resumeId
     * @return
     */
    @Override
    public DownloadFileResponse downloadFile(String resumeId) {
        UserResumeFile userResumeFile = getUserResumeFile(resumeId);
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(rustfsProperties.getBucketName())
                    .key(userResumeFile.getObjectKey())
                    .build());
            return DownloadFileResponse.builder()
                    .fileName(userResumeFile.getFileName())
                    .contentType(userResumeFile.getContentType())
                    .fileSize(userResumeFile.getFileSize())
                    .content(objectBytes.asByteArray())
                    .build();
        } catch (S3Exception exception) {
            throw new ClientException("读取对象存储文件失败");
        }
    }

    /**
     * url获取文件
     * @return
     */
    @Override
    public DownloadFileResponse downloadFileByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new ClientException("文件URL不能为空");
        }
        try {
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection httpURLConnection) {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.connect();
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode >= 400) {
                    throw new ClientException("根据URL下载文件失败，响应码：" + responseCode);
                }
            }

            byte[] content;
            try (InputStream inputStream = urlConnection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                content = outputStream.toByteArray();
            }

            String fileName = resolveFileName(url, urlConnection);
            String contentType = normalizeContentType(urlConnection.getContentType(), fileName);
            long fileSize = urlConnection.getContentLengthLong() > 0
                    ? urlConnection.getContentLengthLong()
                    : content.length;

            return DownloadFileResponse.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .content(content)
                    .build();
        } catch (IOException exception) {
            throw new ClientException("根据URL下载文件失败");
        }
    }

    /**
     * 根据url下载文件转换成mutipartFile
     * @param fileUrl
     * @return
     */
    @Override
    public MultipartFile downloadMultipartFileByUrl(String fileUrl) {
        DownloadFileResponse response = downloadFileByUrl(fileUrl);
        return new InMemoryMultipartFile(
                "file",
                response.getFileName(),
                response.getContentType(),
                response.getContent()
        );
    }

    private UserResumeFile getUserResumeFile(String resumeId) {
        if (!StringUtils.hasText(resumeId)) {
            throw new ClientException("简历ID不能为空");
        }
        LambdaQueryWrapper<UserResumeFile> queryWrapper = Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getResumeId, resumeId)
                .eq(UserResumeFile::getDelFlag, 0);
        UserResumeFile userResumeFile = userResumeFileMapper.selectOne(queryWrapper);
        if (userResumeFile == null) {
            throw new ClientException("简历文件不存在");
        }
        return userResumeFile;
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

    private String buildObjectKey(String resumeId, String fileExt) {
        String suffix = StringUtils.hasText(fileExt) ? "." + fileExt : "";
        return "rag/upload/" + resumeId + suffix;
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

    private String resolveFileName(URL url, URLConnection urlConnection) {
        String headerValue = urlConnection.getHeaderField("Content-Disposition");
        if (StringUtils.hasText(headerValue)) {
            String[] parts = headerValue.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("filename=")) {
                    return trimmed.substring("filename=".length()).replace("\"", "");
                }
            }
        }
        String path = url.getPath();
        String fileName = Paths.get(path).getFileName() == null ? "" : Paths.get(path).getFileName().toString();
        return StringUtils.hasText(fileName) ? fileName : IdUtil.fastSimpleUUID();
    }

    private String normalizeContentType(String contentType, String fileName) {
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        String fileExt = getFileExt(fileName);
        if ("pdf".equalsIgnoreCase(fileExt)) {
            return "application/pdf";
        }
        if (Arrays.asList("doc", "docx").contains(fileExt.toLowerCase())) {
            return "application/octet-stream";
        }
        return "application/octet-stream";
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = Objects.requireNonNullElseGet(content, () -> new byte[0]);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}
