package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.entity.SysRolePermission;
import com.aeisp.system.entity.SysUserRole;
import com.aeisp.system.mapper.SysPermissionMapper;
import com.aeisp.system.mapper.SysRolePermissionMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.system.vo.MenuRoutesVO;
import com.aeisp.system.vo.MenuVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

    @Mock
    private SysPermissionMapper sysPermissionMapper;
    @Mock
    private SysRolePermissionMapper sysRolePermissionMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @InjectMocks
    private SysMenuServiceImpl sysMenuService;

    @Test
    void testGetMenuTreeReturnsTreeStructure() {
        SysPermission parent = new SysPermission();
        parent.setId(1L);
        parent.setParentId(0L);
        parent.setPermissionName("系统管理");
        parent.setMenuType(0);
        parent.setSortOrder(1);
        parent.setDeleted(CommonConstants.DELETED_NO);

        SysPermission child = new SysPermission();
        child.setId(2L);
        child.setParentId(1L);
        child.setPermissionName("用户管理");
        child.setMenuType(1);
        child.setSortOrder(1);
        child.setRoutePath("/system/user");
        child.setDeleted(CommonConstants.DELETED_NO);

        when(sysPermissionMapper.selectList(any())).thenReturn(List.of(parent, child));

        List<MenuVO> tree = sysMenuService.getMenuTree();
        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getPermissionName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("用户管理", tree.get(0).getChildren().get(0).getPermissionName());
    }

    @Test
    void testGetMenuTreeWithMultipleRoots() {
        SysPermission menu1 = new SysPermission();
        menu1.setId(1L);
        menu1.setParentId(0L);
        menu1.setPermissionName("系统管理");
        menu1.setDeleted(CommonConstants.DELETED_NO);

        SysPermission menu2 = new SysPermission();
        menu2.setId(2L);
        menu2.setParentId(0L);
        menu2.setPermissionName("用户模块");
        menu2.setDeleted(CommonConstants.DELETED_NO);

        when(sysPermissionMapper.selectList(any())).thenReturn(List.of(menu1, menu2));

        List<MenuVO> tree = sysMenuService.getMenuTree();
        assertEquals(2, tree.size());
    }

    @Test
    void testGetRoleMenuIds() {
        SysRolePermission rp1 = new SysRolePermission();
        rp1.setRoleId(1L);
        rp1.setPermissionId(1L);
        SysRolePermission rp2 = new SysRolePermission();
        rp2.setRoleId(1L);
        rp2.setPermissionId(2L);
        when(sysRolePermissionMapper.selectList(any())).thenReturn(List.of(rp1, rp2));

        List<Long> ids = sysMenuService.getRoleMenuIds(1L);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
    }

    @Test
    void testGetRoleMenuIdsReturnsEmpty() {
        when(sysRolePermissionMapper.selectList(any())).thenReturn(List.of());
        List<Long> ids = sysMenuService.getRoleMenuIds(1L);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testCreateMenu() {
        SysPermission permission = new SysPermission();
        permission.setPermissionName("新菜单");
        permission.setMenuType(0);
        when(sysPermissionMapper.insert(permission)).thenReturn(1);

        assertTrue(sysMenuService.createMenu(permission));
    }

    @Test
    void testUpdateMenu() {
        SysPermission permission = new SysPermission();
        permission.setId(1L);
        permission.setPermissionName("更新后");
        when(sysPermissionMapper.updateById(permission)).thenReturn(1);

        assertTrue(sysMenuService.updateMenu(permission));
    }

    @Test
    void testDeleteMenuWithChildren() {
        when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

        assertFalse(sysMenuService.deleteMenu(1L));
        verify(sysPermissionMapper, never()).deleteById(any());
    }

    @Test
    void testDeleteMenuWithoutChildren() {
        when(sysPermissionMapper.selectCount(any())).thenReturn(0L);
        when(sysPermissionMapper.deleteById(1L)).thenReturn(1);

        assertTrue(sysMenuService.deleteMenu(1L));
    }

    @Test
    void getUserRoutes_shouldReturnMenuTreeAndPermissions() {
        Long userId = 1L;
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(1L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole));

        when(sysPermissionMapper.selectPermissionCodesByRoleIds(List.of(1L)))
                .thenReturn(List.of("system:menu:list", "system:menu:manage"));

        SysPermission dir = new SysPermission();
        dir.setId(1L);
        dir.setPermissionName("系统管理");
        dir.setPermissionCode("system:admin");
        dir.setParentId(0L);
        dir.setMenuType(0);
        dir.setSortOrder(1);
        dir.setRoutePath("/system");
        dir.setIcon("Setting");
        dir.setIsVisible(1);

        SysPermission menu = new SysPermission();
        menu.setId(2L);
        menu.setPermissionName("菜单管理");
        menu.setPermissionCode("system:menu:list");
        menu.setParentId(1L);
        menu.setMenuType(1);
        menu.setSortOrder(1);
        menu.setRoutePath("/system/menu");
        menu.setComponent("system/menu/index.vue");
        menu.setIsVisible(1);

        when(sysPermissionMapper.selectList(any())).thenReturn(List.of(dir, menu));

        MenuRoutesVO result = sysMenuService.getUserRoutes(userId);

        assertNotNull(result);
        assertNotNull(result.getMenus());
        assertEquals(1, result.getMenus().size());
        assertEquals("系统管理", result.getMenus().get(0).getPermissionName());
        assertEquals(1, result.getMenus().get(0).getChildren().size());
        assertEquals("菜单管理", result.getMenus().get(0).getChildren().get(0).getPermissionName());
        assertTrue(result.getPermissions().contains("system:menu:list"));
        assertEquals(2, result.getPermissions().size());
    }

    @Test
    void getUserRoutes_shouldFilterMenuWithoutPermission() {
        Long userId = 1L;
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(1L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(sysPermissionMapper.selectPermissionCodesByRoleIds(List.of(1L)))
                .thenReturn(List.of("system:menu:list"));

        SysPermission dir = new SysPermission();
        dir.setId(1L);
        dir.setPermissionName("系统管理");
        dir.setPermissionCode("system:admin");
        dir.setParentId(0L);
        dir.setMenuType(0);
        dir.setSortOrder(1);
        dir.setIsVisible(1);

        SysPermission menu = new SysPermission();
        menu.setId(2L);
        menu.setPermissionName("菜单管理");
        menu.setPermissionCode("system:menu:list");
        menu.setParentId(1L);
        menu.setMenuType(1);
        menu.setSortOrder(1);
        menu.setIsVisible(1);

        SysPermission noPermMenu = new SysPermission();
        noPermMenu.setId(3L);
        noPermMenu.setPermissionName("无权限菜单");
        noPermMenu.setPermissionCode("test:admin");
        noPermMenu.setParentId(1L);
        noPermMenu.setMenuType(1);
        noPermMenu.setSortOrder(2);
        noPermMenu.setIsVisible(1);

        when(sysPermissionMapper.selectList(any())).thenReturn(List.of(dir, menu, noPermMenu));

        MenuRoutesVO result = sysMenuService.getUserRoutes(userId);

        assertEquals(1, result.getMenus().size());
        assertEquals(1, result.getMenus().get(0).getChildren().size());
        assertEquals("菜单管理", result.getMenus().get(0).getChildren().get(0).getPermissionName());
    }

    @Test
    void createMenu_shouldFillDefaults() {
        SysPermission permission = new SysPermission();
        permission.setPermissionName("测试");
        permission.setPermissionCode("test:create");
        permission.setParentId(0L);
        permission.setMenuType(1);
        // 不设 resourceType 和 action

        when(sysPermissionMapper.insert(any(SysPermission.class))).thenReturn(1);

        boolean result = sysMenuService.createMenu(permission);

        assertTrue(result);
        assertEquals("test", permission.getResourceType());
        assertEquals("read", permission.getAction());
        verify(sysPermissionMapper).insert(permission);
    }
}