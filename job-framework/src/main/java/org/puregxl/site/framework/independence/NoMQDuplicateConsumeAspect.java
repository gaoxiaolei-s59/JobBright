package org.puregxl.site.framework.independence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class NoMQDuplicateConsumeAspect {

    private static final String DEFAULT_KEY_PREFIX = "mq:consume:";

    private final StringRedisTemplate stringRedisTemplate;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@annotation(org.puregxl.site.framework.independence.NoMQDuplicateConsume)")
    public Object noMQRepeatConsume(ProceedingJoinPoint joinPoint) throws Throwable {
        NoMQDuplicateConsume annotation = getNoMQDuplicateConsumeAnnotation(joinPoint);
        Method method = getTargetMethod(joinPoint);
        String uniqueValue = resolveUniqueKey(joinPoint, method, annotation.key());
        String redisKey = buildRedisKey(annotation.keyPrefix(), uniqueValue);
        boolean firstConsume = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        redisKey,
                        UUID.randomUUID().toString(),
                        Duration.ofSeconds(annotation.keyTimeout())
                )
        );
        log.info("[MQ防重复消费] 检测到重复消费，检测。method={}, key={}", method.getName(), redisKey);
        if (!firstConsume) {
            log.warn("[MQ防重复消费] 检测到重复消费，已拦截。method={}, key={}", method.getName(), redisKey);
            return null;
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            stringRedisTemplate.delete(redisKey);
            throw throwable;
        }
    }

    public static NoMQDuplicateConsume getNoMQDuplicateConsumeAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        return getTargetMethod(joinPoint).getAnnotation(NoMQDuplicateConsume.class);
    }

    private static Method getTargetMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getDeclaredMethod(
                methodSignature.getName(),
                methodSignature.getMethod().getParameterTypes()
        );
    }

    private String resolveUniqueKey(ProceedingJoinPoint joinPoint, Method method, String keyExpression) {
        if (!StringUtils.hasText(keyExpression)) {
            throw new IllegalArgumentException("NoMQDuplicateConsume key 不能为空");
        }

        if (!keyExpression.contains("#")) {
            return keyExpression.trim();
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }

        Object value = expressionParser.parseExpression(keyExpression).getValue(context);
        if (value == null || !StringUtils.hasText(Objects.toString(value))) {
            throw new IllegalArgumentException("NoMQDuplicateConsume key 表达式解析结果为空: " + keyExpression);
        }
        return Objects.toString(value).trim();
    }

    private String buildRedisKey(String keyPrefix, String uniqueValue) {
        String prefix = StringUtils.hasText(keyPrefix) ? keyPrefix.trim() : DEFAULT_KEY_PREFIX;
        return prefix.endsWith(":") ? prefix + uniqueValue : prefix + ":" + uniqueValue;
    }
}
