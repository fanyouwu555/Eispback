package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 时长消耗记录视图对象。
 *
 * <p>用于展示用户时长消耗的明细记录。</p>
 *
 * @author AEISP Team
 */
@Data
public class DurationConsumeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID。
     */
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 消耗时间。
     */
    private LocalDateTime consumeTime;

    /**
     * 消耗类型：model_call-模型调用, sim_run-仿真运行, debug-编译调试。
     */
    private String consumeType;

    /**
     * 消耗时长（分钟）。
     */
    private Long consumeDuration;

    /**
     * 关联项目 ID。
     */
    private Long projectId;

    /**
     * 描述信息。
     */
    private String description;
}
