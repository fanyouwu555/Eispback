package com.aeisp.system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新角色请求参数。
 *
 * @author AEISP Team
 */
@Data
public class UpdateRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID。
     */
    @NotNull(message = "角色 ID 不能为空")
    private Long id;

    /**
     * 角色名称。
     */
    @Size(max = 50, message = "角色名称长度不能超过 50")
    private String roleName;

    /**
     * 角色编码。
     */
    @Size(max = 50, message = "角色编码长度不能超过 50")
    private String roleCode;

    /**
     * 角色描述。
     */
    @Size(max = 200, message = "角色描述长度不能超过 200")
    private String description;

    /**
     * 账号状态：0-禁用，1-正常。
     */
    private Integer status;

    /**
     * 权限 ID 列表。
     */
    private List<Long> permissionIds;
}
