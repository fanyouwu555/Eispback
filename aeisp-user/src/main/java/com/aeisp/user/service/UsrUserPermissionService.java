package com.aeisp.user.service;

import com.aeisp.user.entity.UsrUserPermission;

import java.util.List;

public interface UsrUserPermissionService {

    List<UsrUserPermission> getByUserId(Long userId);

    boolean updatePermissions(Long userId, List<UsrUserPermission> permissions);

    UsrUserPermission getByUserIdAndKey(Long userId, String permKey);
}