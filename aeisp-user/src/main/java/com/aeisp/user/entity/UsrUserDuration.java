package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户时长表实体。
 *
 * <p>记录每个用户账户中的可用计算时长。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_user_duration")
public class UsrUserDuration {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，外键关联 usr_user.id，唯一索引。
     */
    private Long userId;

    /**
     * 累计获得时长（分钟），历史累计不减少。
     */
    private Integer totalGrantedMinutes;

    /**
     * 累计消耗时长（分钟）。
     */
    private Integer totalConsumedMinutes;

    /**
     * 当前剩余可用时长（分钟）。
     */
    private Integer remainingMinutes;

    /**
     * 当前批次时长的过期日期，NULL表示无固定过期时间。
     */
    private LocalDate expiryDate;

    /**
     * 时长预警阈值（分钟），低于此值触发预警通知。
     */
    private Integer warningThreshold;

    /**
     * 最后一次消耗时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastConsumedAt;

    /**
     * 最后更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
