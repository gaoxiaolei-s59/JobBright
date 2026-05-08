package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.json.JSONUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.framework.mq.UploadResumeExecuteTaskEvent;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.config.RustfsProperties;
import org.puregxl.site.jobbacked.dao.entity.UserResumeAnalysis;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeAnalysisMapper;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.file.UploadFileInfo;
import org.puregxl.site.jobbacked.dto.resp.UserResumePreviewResponse;
import org.puregxl.site.jobbacked.dto.resp.UserResumeResponse;
import org.puregxl.site.jobbacked.mq.producer.JobBackedUserResumeAnalysisProduceTemplate;
import org.puregxl.site.jobbacked.mq.producer.JobBackedUserResumeProduceTemplate;
import org.puregxl.site.jobbacked.service.FileStorageService;
import org.puregxl.site.jobbacked.service.UserResumeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.puregxl.site.framework.errorcode.BaseErrorCode.USER_RESUME_NOT_FOUND;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserResumeServiceImpl extends ServiceImpl<UserResumeFileMapper, UserResumeFile> implements UserResumeService {

    private final UserResumeFileMapper userResumeFileMapper;

    private final UserResumeAnalysisMapper userResumeAnalysisMapper;

    private final RustfsProperties rustfsProperties;

    private static final String URL_PREFIX = "user/resume";

    private final FileStorageService fileStorageService;

    private static final String USER_RESUME_BUCKET_NAME = "user-resume";

    private final JobBackedUserResumeProduceTemplate jobBackedUserResumeProduceTemplate;

    private final JobBackedUserResumeAnalysisProduceTemplate jobBackedUserResumeAnalysisProduceTemplate;

    private final S3Client s3Client;

    /**
     * 上传用户简历
     *
     * @param file
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadUserResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException("上传简历不能为空");
        }
        long currentUserId = UserContext.getUserId();
        if (currentUserId <= 0) {
            throw new ClientException("请先登录");
        }

        //获取文件后缀
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new ClientException("文件名不能为空");
        }
        String fileExt = getFileExt(originalFilename);
        String resumeId = UUID.fastUUID().toString();
        String objectKey = buildObjectKey(resumeId, fileExt);

        //检查是否有当前已经生效的简历 - 如果没有把当前上传的简历设置成已经生效
        LambdaQueryWrapper<UserResumeFile> userResumeFileLambdaQueryWrapper = Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, currentUserId)
                .eq(UserResumeFile::getIsCurrent, 1);

        UserResumeFile currentFile = userResumeFileMapper.selectOne(userResumeFileLambdaQueryWrapper);
        if (currentFile != null) {
            currentFile.setIsCurrent(0);
            userResumeFileMapper.updateById(currentFile);
        }

        UploadFileInfo uploadFileInfo;
        try {
            uploadFileInfo = fileStorageService.uploadFile(UploadFileInfo.builder()
                    .bucketName(USER_RESUME_BUCKET_NAME)
                    .objectKey(objectKey)
                    .contentType(file.getContentType())
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .objectUrl(buildObjectUrl(USER_RESUME_BUCKET_NAME, objectKey))
                    .content(file.getBytes())
                    .build());
        } catch (IOException exception) {
            throw new ClientException("读取上传简历失败");
        }

        UserResumeFile userResumeFile = UserResumeFile.builder()
                .userId(currentUserId)
                .resumeId(resumeId)
                .fileName(originalFilename)
                .fileExt(fileExt)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .objectKey(uploadFileInfo.getObjectKey())
                .objectUrl(uploadFileInfo.getObjectUrl())
                .isCurrent(1)
                .build();

        userResumeFileMapper.insert(userResumeFile);


        //发送消息到下游 解析用户简历
        UploadResumeExecuteTaskEvent uploadEvent = UploadResumeExecuteTaskEvent.builder()
                .resumeId(userResumeFile.getResumeId())
                .userId(UserContext.getUserId())
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                jobBackedUserResumeProduceTemplate.sendMessage(uploadEvent);
                jobBackedUserResumeAnalysisProduceTemplate.sendMessage(uploadEvent);
            }
        });

    }

    /**
     * 获取用户简历信息
     *
     * @return
     */
    @Override
    public UserResumeResponse getResumeMessage() {
        long userId = UserContext.getUserId();
        LambdaQueryWrapper<UserResumeFile> userResumeFileLambdaQueryWrapper = Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, userId)
                .eq(UserResumeFile::getIsCurrent, 1);
        UserResumeFile userResumeFile = userResumeFileMapper.selectOne(userResumeFileLambdaQueryWrapper);
        if (userResumeFile == null) {
            throw new ClientException(USER_RESUME_NOT_FOUND);
        }
        UserResumeAnalysis analysis = getResumeAnalysis(userResumeFile.getResumeId(), userId);

        return UserResumeResponse.builder()
                .resumeId(userResumeFile.getResumeId())
                .fileName(userResumeFile.getFileName())
                .score(resolveResumeScore(userResumeFile, analysis))
                .status("ACTIVE")
                .uploadTime(userResumeFile.getUpdateTime())
                .build();
    }

    @Override
    public UserResumePreviewResponse getResumePreview(String resumeId) {
        UserResumeFile userResumeFile = getOwnedResumeByResumeId(resumeId);
        UserResumeAnalysis analysis = getResumeAnalysis(userResumeFile.getResumeId(), userResumeFile.getUserId());
        UserResumePreviewResponse preview = UserResumePreviewResponse.builder()
                .resumeId(userResumeFile.getResumeId())
                .fileName(userResumeFile.getFileName())
                .fileExt(userResumeFile.getFileExt())
                .contentType(resolveContentType(userResumeFile))
                .previewType(isInlinePreviewable(userResumeFile) ? "INLINE" : "DOWNLOAD")
                .previewUrl("/api/user/resume/" + userResumeFile.getResumeId() + "/file")
                .downloadUrl("/api/user/resume/" + userResumeFile.getResumeId() + "/file")
                .updatedAt(Optional.ofNullable(userResumeFile.getUpdateTime()).map(Object::toString).orElse(null))
                .build();
        fillAnalysisPreview(preview, userResumeFile, analysis);
        return preview;
    }

    @Override
    public ResponseEntity<Resource> getResumeFile(String resumeId) {
        UserResumeFile userResumeFile = getOwnedResumeByResumeId(resumeId);
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(USER_RESUME_BUCKET_NAME)
                    .key(userResumeFile.getObjectKey())
                    .build());

            ByteArrayResource resource = new ByteArrayResource(objectBytes.asByteArray());
            MediaType mediaType = resolveMediaType(userResumeFile);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(objectBytes.asByteArray().length)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                            .filename(userResumeFile.getFileName(), StandardCharsets.UTF_8)
                            .build()
                            .toString())
                    .body(resource);
        } catch (NoSuchKeyException exception) {
            throw new ClientException(USER_RESUME_NOT_FOUND);
        } catch (S3Exception exception) {
            throw new ClientException("读取简历文件失败");
        }
    }


    private String getFileExt(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase();
    }


    private String buildObjectKey(String fileId, String fileExt) {
        String suffix = StringUtils.hasText(fileExt) ? "." + fileExt : "";
        return URL_PREFIX + "/" + fileId + suffix;
    }


    private String buildObjectUrl(String bucketName, String objectKey) {
        return rustfsProperties.getUrl() + "/" + bucketName + "/" + objectKey;
    }

    private UserResumeFile getOwnedResumeByResumeId(String resumeId) {
        if (!StringUtils.hasText(resumeId)) {
            throw new ClientException("resumeId不能为空");
        }
        long userId = UserContext.getUserId();
        UserResumeFile userResumeFile = userResumeFileMapper.selectOne(Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, userId)
                .eq(UserResumeFile::getResumeId, resumeId)
                .eq(UserResumeFile::getDelFlag, 0)
                .last("limit 1"));
        if (userResumeFile == null) {
            throw new ClientException(USER_RESUME_NOT_FOUND);
        }
        return userResumeFile;
    }

    private String resolveContentType(UserResumeFile userResumeFile) {
        if (StringUtils.hasText(userResumeFile.getContentType())) {
            return userResumeFile.getContentType();
        }
        String fileExt = userResumeFile.getFileExt();
        if ("pdf".equalsIgnoreCase(fileExt)) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        if ("doc".equalsIgnoreCase(fileExt)) {
            return "application/msword";
        }
        if ("docx".equalsIgnoreCase(fileExt)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private MediaType resolveMediaType(UserResumeFile userResumeFile) {
        try {
            return MediaType.parseMediaType(resolveContentType(userResumeFile));
        } catch (Exception ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private boolean isInlinePreviewable(UserResumeFile userResumeFile) {
        return "pdf".equalsIgnoreCase(userResumeFile.getFileExt())
                || MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(resolveContentType(userResumeFile));
    }

    private UserResumeAnalysis getResumeAnalysis(String resumeId, Long userId) {
        if (!StringUtils.hasText(resumeId) || userId == null) {
            return null;
        }
        return userResumeAnalysisMapper.selectOne(Wrappers.lambdaQuery(UserResumeAnalysis.class)
                .eq(UserResumeAnalysis::getResumeId, resumeId)
                .eq(UserResumeAnalysis::getUserId, userId)
                .eq(UserResumeAnalysis::getDelFlag, 0)
                .last("limit 1"));
    }

    private Integer resolveResumeScore(UserResumeFile userResumeFile, UserResumeAnalysis analysis) {
        if (analysis != null && analysis.getScoreValue() != null) {
            return analysis.getScoreValue();
        }
        if (userResumeFile.getScore() != null) {
            return userResumeFile.getScore().intValue();
        }
        return 88;
    }

    private void fillAnalysisPreview(
            UserResumePreviewResponse preview,
            UserResumeFile userResumeFile,
            UserResumeAnalysis analysis) {
        if (analysis == null) {
            preview.setScore(buildScore(userResumeFile, null));
            return;
        }

        UserResumePreviewResponse rawPreview = parseObject(
                analysis.getRawAnalysisJson(),
                UserResumePreviewResponse.class
        );
        if (rawPreview != null) {
            copyAnalysisFields(preview, rawPreview);
        }

        preview.setScore(buildScore(userResumeFile, analysis));
        preview.setProfile(firstNonNull(
                parseObject(analysis.getProfileJson(), UserResumePreviewResponse.Profile.class),
                preview.getProfile()
        ));
        preview.setAnalysisSummary(firstText(analysis.getAnalysisSummary(), preview.getAnalysisSummary()));
        preview.setAnalysisHighlights(firstNonEmpty(
                parseList(analysis.getHighlightsJson(), UserResumePreviewResponse.Highlight.class),
                preview.getAnalysisHighlights()
        ));
        preview.setUrgentIssues(firstNonEmpty(
                parseList(analysis.getIssuesJson(), UserResumePreviewResponse.Issue.class),
                preview.getUrgentIssues()
        ));
        preview.setSkillGroups(firstNonEmpty(
                parseList(analysis.getSkillGroupsJson(), UserResumePreviewResponse.SkillGroup.class),
                preview.getSkillGroups()
        ));
        preview.setProjects(firstNonEmpty(
                parseList(analysis.getProjectsJson(), UserResumePreviewResponse.Project.class),
                preview.getProjects()
        ));
        preview.setUpdatedAt(Optional.ofNullable(analysis.getUpdateTime())
                .map(Object::toString)
                .orElse(preview.getUpdatedAt()));
    }

    private UserResumePreviewResponse.Score buildScore(UserResumeFile userResumeFile, UserResumeAnalysis analysis) {
        Integer scoreValue = resolveResumeScore(userResumeFile, analysis);
        if (analysis == null) {
            return UserResumePreviewResponse.Score.builder()
                    .grade(resolveGrade(scoreValue))
                    .label(resolveLabel(scoreValue))
                    .scoreValue(scoreValue)
                    .urgentFixCount(0)
                    .criticalFixCount(0)
                    .optionalFixCount(0)
                    .build();
        }
        return UserResumePreviewResponse.Score.builder()
                .grade(firstText(analysis.getGrade(), resolveGrade(scoreValue)))
                .label(firstText(analysis.getLabel(), resolveLabel(scoreValue)))
                .scoreValue(scoreValue)
                .urgentFixCount(Optional.ofNullable(analysis.getUrgentFixCount()).orElse(0))
                .criticalFixCount(Optional.ofNullable(analysis.getCriticalFixCount()).orElse(0))
                .optionalFixCount(Optional.ofNullable(analysis.getOptionalFixCount()).orElse(0))
                .summary(analysis.getAnalysisSummary())
                .build();
    }

    private void copyAnalysisFields(UserResumePreviewResponse target, UserResumePreviewResponse source) {
        target.setScore(firstNonNull(source.getScore(), target.getScore()));
        target.setProfile(firstNonNull(source.getProfile(), target.getProfile()));
        target.setAnalysisSummary(firstText(source.getAnalysisSummary(), target.getAnalysisSummary()));
        target.setAnalysisHighlights(firstNonEmpty(source.getAnalysisHighlights(), target.getAnalysisHighlights()));
        target.setUrgentIssues(firstNonEmpty(source.getUrgentIssues(), target.getUrgentIssues()));
        target.setSkillGroups(firstNonEmpty(source.getSkillGroups(), target.getSkillGroups()));
        target.setProjects(firstNonEmpty(source.getProjects(), target.getProjects()));
    }

    private String resolveGrade(Integer scoreValue) {
        if (scoreValue == null) {
            return "A";
        }
        if (scoreValue >= 85) {
            return "A";
        }
        if (scoreValue >= 70) {
            return "B";
        }
        return "C";
    }

    private String resolveLabel(Integer scoreValue) {
        if (scoreValue == null || scoreValue >= 85) {
            return "EXCELLENT";
        }
        if (scoreValue >= 70) {
            return "GOOD";
        }
        return "FAIR";
    }

    private <T> T parseObject(String raw, Class<T> type) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return JSONUtil.toBean(raw, type);
        } catch (Exception ignored) {
            return null;
        }
    }

    private <T> List<T> parseList(String raw, Class<T> type) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(raw), type);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private <T> T firstNonNull(T first, T fallback) {
        return first != null ? first : fallback;
    }

    private <T> List<T> firstNonEmpty(List<T> first, List<T> fallback) {
        return first != null && !first.isEmpty() ? first : fallback;
    }

    private String firstText(String first, String fallback) {
        return StringUtils.hasText(first) ? first : fallback;
    }

}
