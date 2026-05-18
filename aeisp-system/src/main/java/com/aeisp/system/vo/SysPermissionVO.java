package com.aeisp.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 权限点 VO。
 *
 * <p>用于返回权限点基本信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class SysPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限 ID。
     */
    private Long id;

    /**
     * 权限名称。
     */
    private String permissionName;

    /**
     * 权限编码。
     */
    private String permissionCode;

    /**
     * 权限描述。
     */
    private String description;
}
