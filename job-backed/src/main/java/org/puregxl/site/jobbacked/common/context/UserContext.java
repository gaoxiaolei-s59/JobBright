package org.puregxl.site.jobbacked.common.context;

import org.apache.catalina.User;

import java.util.Optional;

public class UserContext {

    private static final ThreadLocal<UserInfoDTO> userContext = new ThreadLocal<>();

    /**
     * 设置用户上下文
     * @param userInfoDto
     */
    public static void setUserContext(UserInfoDTO userInfoDto) {
        userContext.set(userInfoDto);
    }

    /**
     * 删除用户上下文
     */
    public static void removeUserContext() {
        userContext.remove();
    }

    /**
     * 获取用户名
     * @return
     */
    public static String getUserName() {
        UserInfoDTO userInfoDto = userContext.get();
        return Optional.ofNullable(userInfoDto).map(UserInfoDTO::getUserName).orElse(null);
    }


    /**
     * 获取用户id
     * @return
     */
    public static String getUserId() {
        UserInfoDTO userInfoDto = userContext.get();
        return Optional.ofNullable(userInfoDto).map(UserInfoDTO::getUserId).orElse(null);
    }

    public static UserInfoDTO getDTO() {
        return userContext.get();
    }



}
