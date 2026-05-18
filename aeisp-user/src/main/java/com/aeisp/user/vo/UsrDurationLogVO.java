package com.aeisp.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户时长消耗日志 VO。
 *
 * @author AEISP Team
 */
@Data
public class UsrDurationLogVO {

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 消耗时间。
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
     * 关联项目 ID。
     */
    private Long projectId;

    /**
     * 描述。
     */
    private String description;
}
