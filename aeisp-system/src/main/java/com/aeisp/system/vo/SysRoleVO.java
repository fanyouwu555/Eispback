package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色 VO。
 *
 * <p>用于返回角色信息及关联的权限列表。</p>
 *
 * @author AEISP Team
 */
@Data
public class SysRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID。
     */
    private Long id;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色编码。
     */
    private String roleCode;

    /**
     * 角色描述。
     */
    private String description;

    /**
     * 角色状态：0-禁用，1-正常。
     */
    private Integer status;

    /**
     * 是否系统内置角色：0-自定义，1-系统内置。
     */
    private Integer isSystem;

    /**
     * 数据权限范围：ALL-全部数据，SELF-仅本人数据。
     */
    private String dataScope;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 权限列表。
     */
    private List<SysPermissionVO> permissions;
}
