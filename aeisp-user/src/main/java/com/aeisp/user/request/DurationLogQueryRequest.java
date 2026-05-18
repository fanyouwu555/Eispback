package com.aeisp.user.request;

import com.aeisp.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时长消耗日志分页查询请求。
 *
 * @author AEISP Team
 */
@Data
public class DurationLogQueryRequest {

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
     * 消耗类型：1-模型调用，2-仿真运行，3-编译调试。
     */
    private Integer consumeType;

    /**
     * 消耗时间起始。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeStart;

    /**
     * 消耗时间截止。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeEnd;
}
