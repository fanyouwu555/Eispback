package com.aeisp.system.service;

import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.vo.MenuRoutesVO;
import com.aeisp.system.vo.MenuVO;

import java.util.List;

public interface SysMenuService {

    List<MenuVO> getMenuTree();

    List<Long> getRoleMenuIds(Long roleId);

    SysPermission getById(Long id);

    boolean createMenu(SysPermission permission);

    boolean updateMenu(SysPermission permission);

    boolean deleteMenu(Long id);

    // 新增
    MenuRoutesVO getUserRoutes(Long userId);
}