package com.aeisp.boot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录用户信息 VO。
 *
 * @author AEISP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 用户类型：admin / user。
     */
    private String userType;

    /**
     * 角色列表。
     */
    private List<String> roles;

    /**
     * 权限编码列表。
     */
    private List<String> permissions;

    /**
     * API 访问密钥。
     */
    private String apiKey;

    /**
     * 租户 ID。
     */
    private String tenantId;
}
