package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户权限变更日志实体。
 *
 * <p>记录每次用户权限修改的详细变更记录，用于审计和追溯。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_user_permission_log")
public class UsrUserPermissionLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID。
     */
    private Long userId;

    /**
     * 权限键。
     */
    private String permKey;

    /**
     * 权限键标签（中文名称）。
     */
    private String permKeyLabel;

    /**
     * 变更前值。
     */
    private String oldValue;

    /**
     * 变更后值。
     */
    private String newValue;

    /**
     * 操作类型：UPDATE-修改，RESET-重置。
     */
    private String operationType;

    /**
     * 操作人ID。
     */
    private Long operatorId;

    /**
     * 操作人用户名。
     */
    private String operatorName;

    /**
     * 变更时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
