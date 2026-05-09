package org.puregxl.site.jobbacked.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.constant.AuthConstant;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.context.UserInfoDTO;
import org.puregxl.site.jobbacked.dao.entity.UserAccount;
import org.puregxl.site.jobbacked.dao.mapper.UserAccountMapper;
import org.puregxl.site.jobbacked.dto.req.LoginRequest;
import org.puregxl.site.jobbacked.dto.req.RegisterRequest;
import org.puregxl.site.jobbacked.dto.resp.AuthResponse;
import org.puregxl.site.jobbacked.dto.resp.UserProfileResponse;
import org.puregxl.site.jobbacked.service.AuthService;
import org.puregxl.site.jobbacked.service.CaptchaService;
import org.puregxl.site.jobbacked.service.FileStorageService;
import org.puregxl.site.jobbacked.service.MailService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.puregxl.site.jobbacked.common.constant.AuthConstant.USER_LOGIN_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountMapper userAccountMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final MailService mailService;

    private final CaptchaService captchaService;

    private static final String MAIL_CODE_PREFIX = "job:backed:mail:code:%s";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(LoginRequest requestParam) {
        String account = requestParam.getAccount();
        String password = requestParam.getPassword();

        captchaService.validateLoginCaptcha(requestParam.getCaptchaKey(), requestParam.getCaptchaCode());

        LambdaQueryWrapper<UserAccount> queryWrapper = Wrappers.lambdaQuery(UserAccount.class)
                .and(wrapper -> wrapper
                        .eq(UserAccount::getUsername, account)
                        .or()
                        .eq(UserAccount::getEmail, account))
                .eq(UserAccount::getDelFlag, 0);
        UserAccount userAccount = userAccountMapper.selectOne(queryWrapper);

        if (userAccount == null) {
            throw new ClientException("用户不存在");
        }

        if (!userAccount.getPassword().equals(password)) {
            throw new ClientException("密码错误");
        }

        //登录成功 - 生成用户令牌
        String token = UUID.randomUUID().toString();
        UserInfoDTO userInfoDTO = new UserInfoDTO(userAccount.getId(), userAccount.getUsername(), userAccount.getEmail(), userAccount.getDisplayName());
        stringRedisTemplate.opsForValue().set(USER_LOGIN_KEY_PREFIX + token, JSONUtil.toJsonStr(userInfoDTO), 30, TimeUnit.MINUTES);

        return AuthResponse.builder()
                .accessToken(token).build();
    }

    @Override
    public UserProfileResponse currentUser(Long currentUserId) {
        return BeanUtil.toBean(UserContext.getDTO(), UserProfileResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest requestParam) {

        LambdaQueryWrapper<UserAccount> queryWrapperOne = Wrappers.lambdaQuery(UserAccount.class)
                .eq(UserAccount::getUsername, requestParam.getUsername())
                .or()
                .eq(UserAccount::getEmail, requestParam.getEmail());

        UserAccount userAccount = userAccountMapper.selectOne(queryWrapperOne);

        if (userAccount != null) {
            throw new ClientException("用户已经存在");
        }

        //验证验证码是否正确
        String code = stringRedisTemplate.opsForValue().get(String.format(MAIL_CODE_PREFIX, requestParam.getEmail()));
        if (StrUtil.isBlank(code) || Objects.equals(code, requestParam.getCode())) {
            throw new ClientException("验证码错误");
        }

        UserAccount build = UserAccount.builder()
                .username(requestParam.getUsername())
                .email(requestParam.getEmail()).
                password(requestParam.getPassword()).
                displayName(requestParam.getDisplayName()).
                build();

        userAccountMapper.insert(build);
    }

}
