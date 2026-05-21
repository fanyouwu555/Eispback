package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.service.SysMenuService;
import com.aeisp.system.vo.MenuRoutesVO;
import com.aeisp.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/tree")
    public Result<List<MenuVO>> getMenuTree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/role-menus/{roleId}")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return Result.success(sysMenuService.getRoleMenuIds(roleId));
    }

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) {
        SysPermission permission = sysMenuService.getById(id);
        if (permission == null) {
            return Result.error(ResultCode.NOT_FOUND, "菜单不存在");
        }
        return Result.success(permission);
    }

    @PreAuthorize("hasAuthority('system:menu:manage')")
    @OperationLog(module = "菜单管理", operation = "新增")
    @PostMapping
    public Result<Void> createMenu(@RequestBody SysPermission permission) {
        if (permission.getParentId() == null) permission.setParentId(0L);
        if (permission.getSortOrder() == null) permission.setSortOrder(0);
        boolean success = sysMenuService.createMenu(permission);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建菜单失败");
    }

    @PreAuthorize("hasAuthority('system:menu:manage')")
    @OperationLog(module = "菜单管理", operation = "修改", sensitivity = 2)
    @PutMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        boolean success = sysMenuService.updateMenu(permission);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新菜单失败");
    }

    @PreAuthorize("hasAuthority('system:menu:manage')")
    @OperationLog(module = "菜单管理", operation = "删除", sensitivity = 2)
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        boolean success = sysMenuService.deleteMenu(id);
        if (!success) {
            return Result.error(ResultCode.INTERNAL_ERROR, "删除失败，该菜单存在子菜单");
        }
        return Result.success();
    }

    @GetMapping("/routes")
    public Result<MenuRoutesVO> getUserRoutes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(ResultCode.UNAUTHORIZED, "未登录");
        }
        Object principal = authentication.getPrincipal();
        Long userId = null;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            try {
                Method userIdMethod = principal.getClass().getMethod("getUserId");
                Object idObj = userIdMethod.invoke(principal);
                if (idObj instanceof Number) {
                    userId = ((Number) idObj).longValue();
                }
            } catch (Exception e) {
                // ignore, userId stays null
            }
        }
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "无法识别用户");
        }
        return Result.success(sysMenuService.getUserRoutes(userId));
    }
}