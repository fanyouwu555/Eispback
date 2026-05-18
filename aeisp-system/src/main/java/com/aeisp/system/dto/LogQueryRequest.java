package com.aeisp.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志列表查询请求参数。
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryRequest extends PageParam {

    private static final long serialVersionUID = 1L;

    /**
     * 操作人用户名（模糊查询）。
     */
    private String username;

    /**
     * 操作模块（模糊查询）。
     */
    private String module;

    /**
     * 操作状态：0-失败，1-成功。
     */
    private Integer status;

    /**
     * 开始时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
