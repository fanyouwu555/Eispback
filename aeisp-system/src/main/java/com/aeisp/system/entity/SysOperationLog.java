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
     * 操作人用户名。
     */
    private String username;

    /**
     * 操作模块，如 {@code 用户管理}、{@code 角色管理}。
     */
    private String module;

    /**
     * 操作类型，如 {@code 新增}、{@code 修改}、{@code 删除}、{@code 查询}。
     */
    private String operation;

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
     * 操作人 IP 地址。
     */
    private String ip;

    /**
     * 请求执行时长，单位毫秒。
     */
    private Long duration;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
