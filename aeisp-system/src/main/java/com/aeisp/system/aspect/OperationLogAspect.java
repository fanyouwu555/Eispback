package com.aeisp.system.aspect;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.entity.SysOperationLog;
import com.aeisp.system.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面。
 *
 * <p>拦截所有标记了 {@link OperationLog} 注解的方法，自动记录操作日志。
 * 包含请求信息、执行时长、操作人、操作结果等。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService sysOperationLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.aeisp.system.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            saveLog(operationLog, joinPoint, result, exception, duration);
        }
    }

    private void saveLog(OperationLog operationLog, ProceedingJoinPoint joinPoint,
                         Object result, Exception exception, long duration) {
        try {
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setOperationType(operationLog.module());
            logEntity.setOperationTypeLabel(operationLog.operation());
            logEntity.setSensitivity(operationLog.sensitivity());
            logEntity.setDuration(duration);
            logEntity.setCreatedAt(LocalDateTime.now());

            // 请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setRequestUrl(request.getRequestURI());
                logEntity.setIpAddress(getClientIp(request));
            }

            // 请求参数
            if (operationLog.recordParams() && joinPoint.getArgs().length > 0) {
                try {
                    Object arg = joinPoint.getArgs()[0];
                    if (!(arg instanceof HttpServletRequest)) {
                        logEntity.setRequestParams(objectMapper.writeValueAsString(arg));
                    }
                } catch (Exception e) {
                    log.warn("记录请求参数失败: {}", e.getMessage());
                }
            }

            // 响应数据
            if (operationLog.recordResponse() && result != null) {
                try {
                    logEntity.setResponseData(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.warn("记录响应数据失败: {}", e.getMessage());
                }
            }

            // 操作人信息（通过反射获取，避免依赖 aeisp-boot 的 CustomUserDetails）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    logEntity.setOperatorUsername(userDetails.getUsername());
                }
                // 反射获取 userId 字段（CustomUserDetails 中有该字段）
                try {
                    Method userIdMethod = principal.getClass().getMethod("getUserId");
                    Object userId = userIdMethod.invoke(principal);
                    if (userId instanceof Number) {
                        logEntity.setUserId(((Number) userId).longValue());
                    }
                } catch (NoSuchMethodException e) {
                    // principal 没有 getUserId 方法，忽略
                } catch (Exception e) {
                    log.warn("获取 userId 失败: {}", e.getMessage());
                }
            }

            // 状态与错误信息
            if (exception != null) {
                logEntity.setStatus(CommonConstants.STATUS_DISABLED);
                String msg = exception.getMessage();
                logEntity.setErrorMsg(msg != null && msg.length() > 512 ? msg.substring(0, 512) : msg);
            } else {
                logEntity.setStatus(CommonConstants.STATUS_ENABLED);
            }

            sysOperationLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
