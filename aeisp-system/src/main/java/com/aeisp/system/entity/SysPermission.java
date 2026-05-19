package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限点实体。
 *
 * <p>对应数据库表 {@code sys_permission}，定义系统中的细粒度权限点，用于 RBAC 权限控制。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 权限名称。
     */
    private String permissionName;

    /**
     * 权限编码，全局唯一，如 {@code user:create}、{@code user:read}。
     */
    private String permissionCode;

    /**
     * 资源类型：user/model/template/order/notification/system。
     */
    private String resourceType;

    /**
     * 操作类型：create/read/update/delete/manage。
     */
    private String action;

    /**
     * 权限描述。
     */
    private String description;
}
