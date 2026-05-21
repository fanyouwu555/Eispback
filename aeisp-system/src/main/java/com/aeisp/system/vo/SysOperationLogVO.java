package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志 VO。
 *
 * <p>用于返回操作日志的详细信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class SysOperationLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 操作人 ID。
     */
    private Long userId;

    /**
     * 操作人用户名（冗余存储）。
     */
    private String operatorUsername;

    /**
     * 操作人角色名。
     */
    private String roleName;

    /**
     * 操作类型编码。
     */
    private String operationType;

    /**
     * 操作类型中文标签。
     */
    private String operationTypeLabel;

    /**
     * 操作目标类型。
     */
    private String targetType;

    /**
     * 操作目标ID。
     */
    private String targetId;

    /**
     * 操作详情JSON。
     */
    private String operationDetail;

    /**
     * HTTP 请求方法。
     */
    private String requestMethod;

    /**
     * 请求 URL。
     */
    private String requestUrl;

    /**
     * 请求参数（JSON）。
     */
    private String requestParams;

    /**
     * 响应数据（JSON）。
     */
    private String responseData;

    /**
     * 操作状态：0-失败，1-成功。
     */
    private Integer status;

    /**
     * 错误信息。
     */
    private String errorMsg;

    /**
     * 操作人IP地址。
     */
    private String ipAddress;

    /**
     * 执行时长（毫秒）。
     */
    private Long duration;

    /**
     * 敏感度：1-普通，2-敏感。
     */
    private Integer sensitivity;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
