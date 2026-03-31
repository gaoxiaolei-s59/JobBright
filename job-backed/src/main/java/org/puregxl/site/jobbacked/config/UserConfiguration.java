package org.puregxl.site.jobbacked.config;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.puregxl.site.framework.exception.ClientException;
import org.puregxl.site.jobbacked.common.constant.AuthConstant;
import org.puregxl.site.jobbacked.common.context.UserContext;
import org.puregxl.site.jobbacked.common.context.UserInfoDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class UserConfiguration implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders(AuthConstant.USER_TOKEN_HEADER)
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * 添加用户信息传递过滤器至相关路径拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserTransmitInterceptor(stringRedisTemplate))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/health", "/error");
    }

    @RequiredArgsConstructor
    static class UserTransmitInterceptor implements HandlerInterceptor {

        private final StringRedisTemplate stringRedisTemplate;

        @Override
        public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
            if (request != null && HttpMethod.OPTIONS.matches(request.getMethod())) {
                return true;
            }
            String token = request == null ? null : request.getHeader(AuthConstant.USER_TOKEN_HEADER);
            if (token == null || token.isBlank()) {
                throw new ClientException("请先登录");
            }
            String userJson = stringRedisTemplate.opsForValue().get(AuthConstant.USER_LOGIN_KEY_PREFIX + token);
            if (userJson == null || userJson.isBlank()) {
                throw new ClientException("登录状态已失效，请重新登录");
            }
            UserInfoDTO userInfoDTO = JSONUtil.toBean(userJson, UserInfoDTO.class);
            UserContext.setUserContext(userInfoDTO);
            return true;
        }

        @Override
        public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception exception) throws Exception {
            UserContext.removeUserContext();
        }
    }

}
