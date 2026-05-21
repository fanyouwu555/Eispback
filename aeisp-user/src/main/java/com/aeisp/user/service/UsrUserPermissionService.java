package com.aeisp.user.service;

import com.aeisp.user.entity.UsrUserPermission;

import java.util.List;

public interface UsrUserPermissionService {

    List<UsrUserPermission> getByUserId(Long userId);

    boolean updatePermissions(Long userId, List<UsrUserPermission> permissions);

    UsrUserPermission getByUserIdAndKey(Long userId, String permKey);

    /**
     * 清除用户所有自定义权限，恢复默认。
     *
     * @param userId 用户 ID
     * @return true 表示清除成功
     */
    boolean clearPermissions(Long userId);
}