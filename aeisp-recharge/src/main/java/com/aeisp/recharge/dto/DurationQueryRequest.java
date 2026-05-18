package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 时长消耗分页查询请求。
 *
 * <p>用于时长消耗记录列表的条件筛选和分页参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class DurationQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消耗类型：model_call-模型调用, sim_run-仿真运行, debug-编译调试。
     */
    private String consumeType;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 当前页码。
     */
    private Long pageNum = 1L;

    /**
     * 每页大小。
     */
    private Long pageSize = 10L;
}
