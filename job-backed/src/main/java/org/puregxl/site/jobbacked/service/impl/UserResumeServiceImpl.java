package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.config.RustfsProperties;
import org.puregxl.site.jobbacked.dao.entity.UserResumeFile;
import org.puregxl.site.jobbacked.dao.mapper.UserResumeFileMapper;
import org.puregxl.site.jobbacked.dto.file.UploadFileInfo;
import org.puregxl.site.jobbacked.dto.resp.UserResumeResponse;
import org.puregxl.site.jobbacked.service.FileStorageService;
import org.puregxl.site.jobbacked.service.UserResumeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.puregxl.site.framework.errorcode.BaseErrorCode.USER_RESUME_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserResumeServiceImpl extends ServiceImpl<UserResumeFileMapper, UserResumeFile> implements UserResumeService {

    private final UserResumeFileMapper userResumeFileMapper;

    private final RustfsProperties rustfsProperties;

    private static final String URL_PREFIX = "user/resume";

    private final FileStorageService fileStorageService;

    private static final String USER_RESUME_BUCKET_NAME = "user-resume";

    /**
     * 上传用户简历
     *
     * @param file
     */
    @Override
    public void uploadUserResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException("上传简历不能为空");
        }
        String currentUserId = UserContext.getUserId();
        if (!StringUtils.hasText(currentUserId)) {
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
                .eq(UserResumeFile::getUserId, Long.parseLong(currentUserId))
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
                .userId(Long.parseLong(currentUserId))
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
    }

    /**
     * 获取用户简历信息
     *
     * @return
     */
    @Override
    public UserResumeResponse getResumeMessage() {
        String userId = UserContext.getUserId();
        LambdaQueryWrapper<UserResumeFile> userResumeFileLambdaQueryWrapper = Wrappers.lambdaQuery(UserResumeFile.class)
                .eq(UserResumeFile::getUserId, userId)
                .eq(UserResumeFile::getIsCurrent, 1);
        UserResumeFile userResumeFile = userResumeFileMapper.selectOne(userResumeFileLambdaQueryWrapper);
        if (userResumeFile == null) {
            throw new ClientException(USER_RESUME_NOT_FOUND);
        }

        return UserResumeResponse.builder()
                .resumeId(userResumeFile.getResumeId())
                .fileName(userResumeFile.getFileName())
                .score(88) //测试数据
                .status("ACTIVE")
                .uploadTime(userResumeFile.getUpdateTime())
                .build();
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

}
