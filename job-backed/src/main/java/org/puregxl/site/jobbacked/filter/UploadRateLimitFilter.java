package org.puregxl.site.jobbacked.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.site.jobbacked.config.SemaphoreProperties;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UploadRateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;

    private final SemaphoreProperties semaphoreProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {

        if (!isUploadRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        //获取信号量配置
        RPermitExpirableSemaphore permitExpirableSemaphore = redissonClient.getPermitExpirableSemaphore(semaphoreProperties.getName());
        String permit = null;

        try {
            permit = permitExpirableSemaphore.tryAcquire(
                    semaphoreProperties.getMaxWaitSeconds(),
                    semaphoreProperties.getLeaseSeconds(),
                    TimeUnit.SECONDS
            );

            if (permit == null) {
                //获取许可证失败
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"429\",\"message\":\"当前上传人数过多，请稍后再试\"}");
                return; // 不调用 chain.doFilter()，请求到此为止

            }
            // 获取许可成功，继续处理请求
            filterChain.doFilter(request, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"500\",\"message\":\"获取上传许可失败\"}");
        } finally {
            if (permit != null) {
                boolean released = permitExpirableSemaphore.tryRelease(permit);
                if (!released) {
                    log.warn("upload permit already expired or released, permitId={}", permit);
                }
            }
        }

    }


    private static final String Urlprefix = "user/resume/upload";

    private boolean isUploadRequest(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return false;
        }
        String url = request.getRequestURI();
        if (url == null) {
            return false;
        }
        if (!url.contains(Urlprefix)) {
            return false;
        }

        return true;
    }

}
