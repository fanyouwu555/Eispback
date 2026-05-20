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

    /**
     * 父菜单 ID，0 表示根节点。
     */
    private Long parentId;

    /**
     * 类型：0-目录，1-菜单，2-按钮，3-外链。
     */
    private Integer menuType;

    /**
     * 排序号。
     */
    private Integer sortOrder;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 路由路径。
     */
    private String routePath;

    /**
     * 组件路径。
     */
    private String component;

    /**
     * 是否可见：0-隐藏，1-显示。
     */
    private Integer isVisible;

    /**
     * 是否缓存：0-否，1-是。
     */
    private Integer isCache;
}
