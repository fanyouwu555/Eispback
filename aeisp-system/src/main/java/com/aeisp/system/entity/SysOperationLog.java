package com.aeisp.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体。
 *
 * <p>对应数据库表 {@code sys_operation_log}，记录系统后台的操作行为，用于审计追踪。</p>
 *
 * <p><b>注意：</b>该实体不继承 {@link com.aeisp.common.entity.BaseEntity}，因为日志数据量巨大，不需要逻辑删除功能。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
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
     * 操作人角色名（冗余存储）。
     */
    private String roleName;

    /**
     * 操作类型编码，如 {@code UPDATE_USER_STATUS}。
     */
    private String operationType;

    /**
     * 操作类型中文标签。
     */
    private String operationTypeLabel;

    /**
     * 操作目标类型：user/model/template/order/notification/system。
     */
    private String targetType;

    /**
     * 操作目标ID。
     */
    private String targetId;

    /**
     * 操作详情JSON，包含变更前后的数据快照。
     */
    private String operationDetail;

    /**
     * HTTP 请求方法，如 {@code GET}、{@code POST}、{@code PUT}、{@code DELETE}。
     */
    private String requestMethod;

    /**
     * 请求 URL。
     */
    private String requestUrl;

    /**
     * 请求参数，JSON 格式。
     */
    private String requestParams;

    /**
     * 响应数据，JSON 格式（可选）。
     */
    private String responseData;

    /**
     * 操作状态：0-失败，1-成功。
     */
    private Integer status;

    /**
     * 错误信息，操作失败时记录。
     */
    private String errorMsg;

    /**
     * 操作人IP地址。
     */
    private String ipAddress;

    /**
     * 请求执行时长，单位毫秒。
     */
    private Long duration;

    /**
     * 敏感度：1-普通，2-敏感（涉及资金/权限变更）。
     */
    private Integer sensitivity;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
