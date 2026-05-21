package com.aeisp.system.entity;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体。
 *
 * <p>对应数据库表 {@code sys_role}，定义系统中的角色信息，用于 RBAC 权限控制。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色编码，全局唯一，如 {@code super_admin}、{@code admin}。
     */
    private String roleCode;

    /**
     * 角色描述。
     */
    private String description;

    /**
     * 角色状态：{@link CommonConstants#STATUS_DISABLED} 禁用，{@link CommonConstants#STATUS_ENABLED} 正常。
     */
    private Integer status;

    /**
     * 是否系统内置角色：0-自定义，1-系统内置（不可删除）。
     */
    private Integer isSystem;

    /**
     * 数据权限范围：ALL-全部数据，SELF-仅本人数据。
     */
    private String dataScope;
}
