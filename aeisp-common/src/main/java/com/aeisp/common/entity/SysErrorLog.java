package com.aeisp.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常报错日志实体。
 *
 * <p>记录系统运行中的异常和错误，用于故障排查。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("sys_error_log")
public class SysErrorLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 异常类型。
     */
    private String errorType;

    /**
     * 异常信息。
     */
    private String errorMessage;

    /**
     * 错误堆栈。
     */
    private String stackTrace;

    /**
     * 请求URL。
     */
    private String requestUrl;

    /**
     * 请求参数JSON。
     */
    private String requestParams;

    /**
     * 受影响用户ID。
     */
    private Long userId;

    /**
     * 发生异常的模块。
     */
    private String module;

    /**
     * 严重程度：1-低，2-中，3-高，4-紧急。
     */
    private Integer severity;

    /**
     * 发生时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
