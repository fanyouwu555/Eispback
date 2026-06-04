package com.aeisp.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为日志实体。
 *
 * <p>记录用户在前端的关键行为，用于用户行为分析和产品优化。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("sys_user_behavior_log")
public class SysUserBehaviorLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 行为类型：LOGIN/LOGOUT/CREATE_PROJECT/MODEL_CALL/RECHARGE/等。
     */
    private String behaviorType;

    /**
     * 行为详情JSON。（JSONB类型，插入时设为null避免类型转换问题）
     */
    private String behaviorDetail;

    /**
     * 用户IP。
     */
    private String ipAddress;

    /**
     * 设备信息JSON。（JSONB类型，插入时设为null避免类型转换问题）
     */
    private String deviceInfo;

    /**
     * 行为时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
