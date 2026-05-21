package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.entity.SysRolePermission;
import com.aeisp.system.mapper.SysPermissionMapper;
import com.aeisp.system.mapper.SysRolePermissionMapper;
import com.aeisp.system.service.SysMenuService;
import com.aeisp.system.vo.MenuVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<MenuVO> getMenuTree() {
        LambdaQueryWrapper<SysPermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysPermission::getDeleted, CommonConstants.DELETED_NO);
        wrapper.orderByAsc(SysPermission::getSortOrder);
        wrapper.orderByAsc(SysPermission::getId);
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(wrapper);
        return buildTree(allPermissions, 0L);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return sysRolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

    @Override
    public SysPermission getById(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    private void fillMenuDefaults(SysPermission permission) {
        // resourceType: 从 permissionCode 取第一个 : 之前的部分
        if (permission.getResourceType() == null || permission.getResourceType().isBlank()) {
            String code = permission.getPermissionCode();
            if (code != null && code.contains(":")) {
                permission.setResourceType(code.substring(0, code.indexOf(':')));
            } else {
                permission.setResourceType("system");
            }
        }
        // action: 按 menuType 推断
        if (permission.getAction() == null || permission.getAction().isBlank()) {
            Integer type = permission.getMenuType();
            if (type == null) type = 0;
            switch (type) {
                case 0 -> permission.setAction("manage");
                case 1 -> permission.setAction("read");
                case 2 -> {
                    String code = permission.getPermissionCode();
                    if (code != null && code.contains(":")) {
                        permission.setAction(code.substring(code.lastIndexOf(':') + 1));
                    } else {
                        permission.setAction("manage");
                    }
                }
                case 3 -> permission.setAction("read");
                default -> permission.setAction("manage");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(SysPermission permission) {
        fillMenuDefaults(permission);
        return sysPermissionMapper.insert(permission) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysPermission permission) {
        fillMenuDefaults(permission);
        return sysPermissionMapper.updateById(permission) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long id) {
        LambdaQueryWrapper<SysPermission> childWrapper = Wrappers.lambdaQuery();
        childWrapper.eq(SysPermission::getParentId, id);
        childWrapper.eq(SysPermission::getDeleted, CommonConstants.DELETED_NO);
        Long childCount = sysPermissionMapper.selectCount(childWrapper);
        if (childCount > 0) {
            return false;
        }
        return sysPermissionMapper.deleteById(id) > 0;
    }

    private List<MenuVO> buildTree(List<SysPermission> allPermissions, Long parentId) {
        List<MenuVO> menuList = new ArrayList<>();
        for (SysPermission permission : allPermissions) {
            Long pid = permission.getParentId() != null ? permission.getParentId() : 0L;
            if (pid.equals(parentId)) {
                MenuVO vo = convertToVO(permission);
                List<MenuVO> children = buildTree(allPermissions, permission.getId());
                vo.setChildren(children);
                menuList.add(vo);
            }
        }
        return menuList;
    }

    private MenuVO convertToVO(SysPermission permission) {
        MenuVO vo = new MenuVO();
        vo.setId(permission.getId());
        vo.setPermissionName(permission.getPermissionName());
        vo.setPermissionCode(permission.getPermissionCode());
        vo.setResourceType(permission.getResourceType());
        vo.setAction(permission.getAction());
        vo.setDescription(permission.getDescription());
        vo.setParentId(permission.getParentId());
        vo.setMenuType(permission.getMenuType());
        vo.setSortOrder(permission.getSortOrder());
        vo.setIcon(permission.getIcon());
        vo.setRoutePath(permission.getRoutePath());
        vo.setComponent(permission.getComponent());
        vo.setIsVisible(permission.getIsVisible());
        vo.setIsCache(permission.getIsCache());
        vo.setStatus(1);
        vo.setCreatedAt(permission.getCreatedAt());
        vo.setUpdatedAt(permission.getUpdatedAt());
        return vo;
    }
}