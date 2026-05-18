package com.aeisp.user.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量创建用户请求。
 *
 * @author AEISP Team
 */
@Data
public class UserBatchCreateRequest {

    /**
     * 待创建的用户列表。
     */
    @NotEmpty(message = "用户列表不能为空")
    @Valid
    private List<UserCreateRequest> users;
}
