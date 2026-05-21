package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.dto.CreateRoleRequest;
import com.aeisp.system.dto.UpdateRoleRequest;
import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.entity.SysRolePermission;
import com.aeisp.system.mapper.SysRolePermissionMapper;
import com.aeisp.system.service.SysPermissionService;
import com.aeisp.system.service.SysRoleService;
import com.aeisp.system.vo.SysPermissionVO;
import com.aeisp.system.vo.SysRoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理控制器。
 *
 * <p>提供角色的增删改查接口，路径前缀 {@code /api/v1/system/roles}。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionService sysPermissionService;

    /**
     * 查询角色列表。
     *
     * @param roleName 角色名称（可选）
     * @return 角色列表
     */
    @PreAuthorize("hasAuthority('system:role:manage')")
    @GetMapping
    public Result<List<SysRoleVO>> listAll(@RequestParam(required = false) String roleName) {
        List<SysRole> roles = sysRoleService.listAll(roleName);
        // 批量查询角色的权限关联
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        Map<Long, List<SysPermission>> permMap = Collections.emptyMap();
        if (!roleIds.isEmpty()) {
            List<SysRolePermission> rpList = sysRolePermissionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                            .in(SysRolePermission::getRoleId, roleIds));
            List<Long> permIds = rpList.stream().map(SysRolePermission::getPermissionId).toList();
            if (!permIds.isEmpty()) {
                List<SysPermission> allPerms = sysPermissionService.listAll();
                Map<Long, SysPermission> permById = allPerms.stream()
                        .collect(Collectors.toMap(SysPermission::getId, p -> p, (a, b) -> a));
                permMap = rpList.stream()
                        .collect(Collectors.groupingBy(
                                SysRolePermission::getRoleId,
                                Collectors.mapping(rp -> permById.get(rp.getPermissionId()),
                                        Collectors.toList())));
                // 过滤掉 null（权限已被删除的情况）
                permMap.replaceAll((k, v) -> v.stream().filter(p -> p != null).toList());
            }
        }
        Map<Long, List<SysPermission>> finalPermMap = permMap;
        List<SysRoleVO> voList = roles.stream().map(role -> {
            List<SysPermission> perms = finalPermMap.getOrDefault(role.getId(), Collections.emptyList());
            return convertToVO(role, perms);
        }).toList();
        return Result.success(voList);
    }

    /**
     * 创建角色。
     *
     * @param request 创建参数
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:role:manage')")
    @OperationLog(module = "角色管理", operation = "新增")
    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody CreateRoleRequest request) {
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setIsSystem(request.getIsSystem() != null ? request.getIsSystem() : 0);
        role.setDataScope(request.getDataScope() != null ? request.getDataScope() : "ALL");
        boolean success = sysRoleService.createRole(role, request.getPermissionIds());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建角色失败");
    }

    /**
     * 更新角色。
     *
     * @param id      角色 ID
     * @param request 更新参数
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:role:manage')")
    @OperationLog(module = "角色管理", operation = "修改", sensitivity = 2)
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        request.setId(id);
        SysRole role = new SysRole();
        role.setId(request.getId());
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setIsSystem(request.getIsSystem());
        role.setDataScope(request.getDataScope());
        boolean success = sysRoleService.updateRole(role, request.getPermissionIds());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新角色失败");
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:role:manage')")
    @OperationLog(module = "角色管理", operation = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        boolean success = sysRoleService.deleteRole(id);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "删除角色失败");
    }

    private static SysRoleVO convertToVO(SysRole role, List<SysPermission> permissions) {
        SysRoleVO vo = new SysRoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setIsSystem(role.getIsSystem());
        vo.setDataScope(role.getDataScope());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        if (permissions != null && !permissions.isEmpty()) {
            List<SysPermissionVO> permVOs = permissions.stream().map(p -> {
                SysPermissionVO pvo = new SysPermissionVO();
                pvo.setId(p.getId());
                pvo.setPermissionName(p.getPermissionName());
                pvo.setPermissionCode(p.getPermissionCode());
                pvo.setDescription(p.getDescription());
                return pvo;
            }).toList();
            vo.setPermissions(permVOs);
        }
        return vo;
    }
}
