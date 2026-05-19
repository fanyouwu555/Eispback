package com.aeisp.user.request;

import com.aeisp.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时长变更日志分页查询请求。
 *
 * @author AEISP Team
 */
@Data
public class DurationChangeLogQueryRequest {

    /**
     * 页码。
     */
    private Long pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小。
     */
    private Long pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 操作类型编码。
     */
    private String operationType;

    /**
     * 操作者类型：1-系统自动，2-管理员，3-用户本人。
     */
    private Integer operatorType;

    /**
     * 变更时间起始。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtStart;

    /**
     * 变更时间截止。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtEnd;
}
