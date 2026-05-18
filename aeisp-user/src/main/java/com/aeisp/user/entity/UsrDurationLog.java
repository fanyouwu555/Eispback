package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户时长消耗日志实体（预留）。
 *
 * <p>记录用户平台使用时长的消耗明细，包括模型调用、仿真运行、编译调试等场景。
 * 不继承 {@link com.aeisp.common.entity.BaseEntity}，使用独立轻量结构。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_duration_log")
public class UsrDurationLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户 ID。
     */
    private Long userId;

    /**
     * 消耗发生时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTime;

    /**
     * 消耗类型：1-模型调用，2-仿真运行，3-编译调试。
     */
    private Integer consumeType;

    /**
     * 消耗时长（分钟）。
     */
    private Long consumeDuration;

    /**
     * 关联项目 ID（预留字段）。
     */
    private Long projectId;

    /**
     * 消耗描述。
     */
    private String description;
}
