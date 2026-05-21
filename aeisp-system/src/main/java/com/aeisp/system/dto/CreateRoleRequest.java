package com.aeisp.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建角色请求参数。
 *
 * @author AEISP Team
 */
@Data
public class CreateRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称。
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50")
    private String roleName;

    /**
     * 角色编码。
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过 50")
    private String roleCode;

    /**
     * 角色描述。
     */
    @Size(max = 200, message = "角色描述长度不能超过 200")
    private String description;

    /**
     * 是否系统内置角色：0-自定义，1-系统内置。
     */
    private Integer isSystem;

    /**
     * 数据权限范围：ALL-全部数据，SELF-仅本人数据。
     */
    private String dataScope;

    /**
     * 权限 ID 列表。
     */
    private List<Long> permissionIds;
}
