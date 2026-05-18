package com.aeisp.system.service.impl;

import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.mapper.SysPermissionMapper;
import com.aeisp.system.service.SysPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 权限点 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermission> listAll() {
        LambdaQueryWrapper<SysPermission> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByAsc(SysPermission::getId);
        return sysPermissionMapper.selectList(wrapper);
    }

    @Override
    public List<String> getPermissionCodesByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return sysPermissionMapper.selectPermissionCodesByRoleIds(roleIds);
    }
}
