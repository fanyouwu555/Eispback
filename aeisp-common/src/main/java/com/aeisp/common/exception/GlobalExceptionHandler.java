package com.aeisp.common.exception;

import com.aeisp.common.Result;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.entity.SysErrorLog;
import com.aeisp.common.mapper.SysErrorLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常拦截器。
 *
 * <p>统一捕获并处理控制器层抛出的各类异常，将异常转换为标准 {@link Result} 响应格式返回给客户端。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SysErrorLogMapper sysErrorLogMapper;

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 标准错误响应
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        saveErrorLog(e, 2);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常。
     *
     * @param e 系统异常
     * @return 标准错误响应
     */
    @ExceptionHandler(SysException.class)
    public Result<Void> handleSysException(SysException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        saveErrorLog(e, 3);
        return Result.error(CommonErrorCode.SYSTEM_ERROR, e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解触发）。
     *
     * @param e 参数校验异常
     * @return 包含校验错误详情的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    /**
     * 处理数据库唯一键冲突异常。
     *
     * @param e 唯一键冲突异常
     * @return 标准错误响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据库唯一键冲突: {}", e.getMessage());
        String message = "数据已存在，无法重复添加";
        // 尝试提取更具体的错误信息
        if (e.getMessage() != null) {
            if (e.getMessage().contains("username")) {
                message = "用户名已存在";
            } else if (e.getMessage().contains("email")) {
                message = "邮箱已存在";
            } else if (e.getMessage().contains("phone")) {
                message = "手机号已存在";
            }
        }
        return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    /**
     * 处理数据库访问异常（包括 DuplicateKeyException 等）。
     *
     * @param e 数据库访问异常
     * @return 标准错误响应
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.warn("数据库访问异常: {}", e.getMessage());
        // 检查是否是唯一键冲突
        if (e instanceof DuplicateKeyException) {
            return handleDuplicateKeyException((DuplicateKeyException) e);
        }
        return Result.error(CommonErrorCode.SYSTEM_ERROR, "数据库操作失败");
    }

    /**
     * 处理 @Validated 方法参数校验异常（如 @RequestParam、@PathVariable 校验失败）。
     *
     * @param e 约束校验异常
     * @return 包含校验错误详情的响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("方法参数校验失败: {}", message);
        return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    /**
     * 处理参数绑定异常。
     *
     * @param e 参数绑定异常
     * @return 包含绑定错误详情的响应
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(CommonErrorCode.PARAM_BIND_FAILED, message);
    }

    /**
     * 处理权限不足异常。
     *
     * @param e 权限不足异常
     * @return 标准错误响应
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(CommonErrorCode.ACCESS_DENIED);
    }

    /**
     * 处理请求参数缺失异常。
     *
     * @param e 请求参数缺失异常
     * @return 标准错误响应
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(org.springframework.web.bind.MissingServletRequestParameterException e) {
        log.warn("请求参数缺失: {}", e.getMessage());
        return Result.error(CommonErrorCode.PARAM_MISSING, "请求参数缺失: " + e.getParameterName());
    }

    /**
     * 处理请求路径不存在异常（Spring 6 / Spring Boot 3）。
     *
     * @param e 资源未找到异常
     * @return 404 标准错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("请求资源不存在: {}", e.getResourcePath());
        return Result.error(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    /**
     * 处理请求方法不支持异常。
     *
     * @param e 方法不支持异常
     * @return 405 标准错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {} {}", e.getMethod(), e.getMessage());
        return Result.error(CommonErrorCode.METHOD_NOT_ALLOWED, "请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理事务系统异常（包括可能被包装的 DuplicateKeyException）。
     *
     * @param e 事务系统异常
     * @return 标准错误响应
     */
    @ExceptionHandler(TransactionSystemException.class)
    public Result<Void> handleTransactionSystemException(TransactionSystemException e) {
        log.error("事务系统异常: {}", e.getMessage(), e);
        // 检查是否是 DuplicateKeyException（可能被包装）
        Throwable cause = e.getOriginalException();
        if (cause != null) {
            String errorMsg = cause.getMessage();
            if (errorMsg != null && (errorMsg.contains("duplicate key") || errorMsg.contains("duplicate key value"))) {
                String message = "数据已存在，无法重复添加";
                if (errorMsg.contains("username")) {
                    message = "用户名已存在";
                } else if (errorMsg.contains("email")) {
                    message = "邮箱已存在";
                } else if (errorMsg.contains("phone")) {
                    message = "手机号已存在";
                }
                return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
            }
        }
        saveErrorLog(e, 3);
        return Result.error(CommonErrorCode.SYSTEM_ERROR);
    }

    /**
     * 处理运行时异常（包括可能被包装的 DuplicateKeyException）。
     *
     * @param e 运行时异常
     * @return 标准错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        // 检查是否是 DuplicateKeyException（可能被包装）
        String errorMsg = e.getMessage();
        if (errorMsg != null && (errorMsg.contains("duplicate key") || errorMsg.contains("duplicate key value"))) {
            String message = "数据已存在，无法重复添加";
            if (errorMsg.contains("username")) {
                message = "用户名已存在";
            } else if (errorMsg.contains("email")) {
                message = "邮箱已存在";
            } else if (errorMsg.contains("phone")) {
                message = "手机号已存在";
            }
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
        }
        saveErrorLog(e, 3);
        return Result.error(CommonErrorCode.SYSTEM_ERROR);
    }

    /**
     * 处理其他未知异常。
     *
     * @param e 未知异常
     * @return 标准错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        // 检查是否是 DuplicateKeyException（可能被包装）
        String errorMsg = e.getMessage();
        if (errorMsg != null && (errorMsg.contains("duplicate key") || errorMsg.contains("duplicate key value"))) {
            String message = "数据已存在，无法重复添加";
            if (errorMsg.contains("username")) {
                message = "用户名已存在";
            } else if (errorMsg.contains("email")) {
                message = "邮箱已存在";
            } else if (errorMsg.contains("phone")) {
                message = "手机号已存在";
            }
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
        }
        saveErrorLog(e, 3);
        return Result.error(CommonErrorCode.SYSTEM_ERROR);
    }

    private void saveErrorLog(Exception e, int severity) {
        try {
            SysErrorLog errorLog = new SysErrorLog();
            errorLog.setErrorType(e.getClass().getName());
            errorLog.setErrorMessage(e.getMessage());
            errorLog.setStackTrace(getStackTrace(e));
            errorLog.setSeverity(severity);
            errorLog.setCreatedAt(LocalDateTime.now());

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                errorLog.setRequestUrl(request.getRequestURI());
                errorLog.setRequestParams(request.getQueryString());
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                try {
                    errorLog.setUserId(Long.valueOf(userDetails.getUsername()));
                } catch (NumberFormatException ignored) {
                }
            }

            sysErrorLogMapper.insert(errorLog);
        } catch (Exception ex) {
            log.error("保存异常日志失败", ex);
        }
    }

    private static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() > 4000 ? trace.substring(0, 4000) : trace;
    }
}
