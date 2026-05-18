package com.aeisp.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体。
 *
 * <p>对应数据库表 {@code sys_role_permission}，维护角色与权限的多对多关系。</p>
 *
 * <p><b>注意：</b>该实体不继承 {@link com.aeisp.common.entity.BaseEntity}，仅包含基础关联字段。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 权限 ID。
     */
    private Long permissionId;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
