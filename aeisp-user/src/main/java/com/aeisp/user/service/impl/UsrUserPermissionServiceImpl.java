package com.aeisp.user.service.impl;

import com.aeisp.user.entity.UsrUserPermission;
import com.aeisp.user.mapper.UsrUserPermissionMapper;
import com.aeisp.user.service.UsrUserPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsrUserPermissionServiceImpl implements UsrUserPermissionService {

    private final UsrUserPermissionMapper usrUserPermissionMapper;

    @Override
    public List<UsrUserPermission> getByUserId(Long userId) {
        return usrUserPermissionMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermissions(Long userId, List<UsrUserPermission> permissions) {
        LambdaQueryWrapper<UsrUserPermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UsrUserPermission::getUserId, userId);
        usrUserPermissionMapper.delete(wrapper);

        if (permissions != null && !permissions.isEmpty()) {
            for (UsrUserPermission perm : permissions) {
                perm.setUserId(userId);
                perm.setId(null);
                usrUserPermissionMapper.insert(perm);
            }
        }
        return true;
    }

    @Override
    public UsrUserPermission getByUserIdAndKey(Long userId, String permKey) {
        return usrUserPermissionMapper.selectByUserIdAndKey(userId, permKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearPermissions(Long userId) {
        LambdaQueryWrapper<UsrUserPermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UsrUserPermission::getUserId, userId);
        usrUserPermissionMapper.delete(wrapper);
        return true;
    }
}