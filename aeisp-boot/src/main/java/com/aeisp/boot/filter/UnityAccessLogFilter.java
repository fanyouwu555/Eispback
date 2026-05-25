package com.aeisp.boot.filter;

import com.aeisp.common.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Unity 客户端 API 访问日志过滤器。
 *
 * <p>拦截所有 API 请求，记录请求方法、URI、参数、响应状态、耗时、用户信息等，
 * 以 JSON Lines 格式输出到独立的 access log 文件，便于排查 Unity 客户端问题。</p>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>自动提取 JWT 认证用户信息（userId、username、userType）</li>
 *   <li>生成 traceId 并写入 MDC，方便全链路追踪</li>
 *   <li>自动跳过 multipart 上传和下载/导出接口的 body 捕获，避免内存溢出</li>
 *   <li>请求/响应 body 超过 10KB 时自动截断</li>
 *   <li>对 password、token、authorization、secret 等字段进行脱敏</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class UnityAccessLogFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS_LOG");
    private static final int MAX_BODY_LENGTH = 10240;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/assets/")
                || uri.equals("/favicon.ico")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);

        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/");
        boolean isDownloadOrExport = isDownloadOrExportEndpoint(request.getRequestURI());

        HttpServletRequest requestToUse = isMultipart ? request : new ContentCachingRequestWrapper(request);
        HttpServletResponse responseToUse = isDownloadOrExport ? response : new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                String requestBody = resolveRequestBody(requestToUse, isMultipart);
                String responseBody = resolveResponseBody(responseToUse, isDownloadOrExport);

                ObjectNode logNode = objectMapper.createObjectNode();
                logNode.put("time", LocalDateTime.now().format(DATE_FORMATTER));
                logNode.put("traceId", traceId);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                    logNode.put("userId", userDetails.getUserId());
                    logNode.put("username", userDetails.getUsername());
                    logNode.put("userType", userDetails.getUserType());
                } else {
                    logNode.putNull("userId");
                    logNode.putNull("username");
                    logNode.putNull("userType");
                }

                logNode.put("ip", getClientIp(request));
                logNode.put("method", request.getMethod());
                logNode.put("uri", request.getRequestURI());
                logNode.put("query", request.getQueryString());
                logNode.put("status", responseToUse.getStatus());
                logNode.put("duration", duration);
                logNode.put("requestBody", maskSensitive(requestBody));
                logNode.put("responseBody", maskSensitive(responseBody));

                ACCESS_LOG.info(objectMapper.writeValueAsString(logNode));
            } catch (Exception e) {
                log.warn("记录访问日志失败: {}", e.getMessage());
            } finally {
                MDC.remove("traceId");
                if (responseToUse instanceof ContentCachingResponseWrapper wrapper) {
                    try {
                        wrapper.copyBodyToResponse();
                    } catch (IOException e) {
                        log.warn("复制响应体失败: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private String resolveRequestBody(HttpServletRequest request, boolean isMultipart) {
        if (isMultipart) {
            return "[multipart/form-data]";
        }
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return null;
            }
            if (buf.length > MAX_BODY_LENGTH) {
                return "[body too large, " + buf.length + " bytes]";
            }
            return new String(buf, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String resolveResponseBody(HttpServletResponse response, boolean isDownloadOrExport) {
        if (isDownloadOrExport) {
            return "[binary/download]";
        }
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return null;
            }
            if (buf.length > MAX_BODY_LENGTH) {
                return "[body too large, " + buf.length + " bytes]";
            }
            return new String(buf, StandardCharsets.UTF_8);
        }
        return null;
    }

    private boolean isDownloadOrExportEndpoint(String uri) {
        return uri.contains("/download") || uri.contains("/export");
    }

    private String maskSensitive(String body) {
        if (body == null) {
            return null;
        }
        return body.replaceAll("(?i)(\"password\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"")
                .replaceAll("(?i)(\"token\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"")
                .replaceAll("(?i)(\"authorization\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"")
                .replaceAll("(?i)(\"secret\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"")
                .replaceAll("(?i)(\"oldPassword\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"")
                .replaceAll("(?i)(\"newPassword\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
    }

    private String getClientIp(HttpServletRequest request) {
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

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
