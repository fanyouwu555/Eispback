package com.aeisp.user.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserPermissionUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<PermissionItem> permissions;

    @Data
    public static class PermissionItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull(message = "权限键不能为空")
        private String permKey;

        private String permValue;

        private LocalDateTime expireAt;
    }
}