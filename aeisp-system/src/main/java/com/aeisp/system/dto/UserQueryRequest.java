package com.aeisp.system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户列表查询请求参数。
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageParam {

    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名（模糊查询）。
     */
    private String username;

    /**
     * 真实姓名（模糊查询）。
     */
    private String realName;

    /**
     * 手机号码（模糊查询）。
     */
    private String phone;

    /**
     * 账号状态：0-禁用，1-正常。
     */
    private Integer status;
}
