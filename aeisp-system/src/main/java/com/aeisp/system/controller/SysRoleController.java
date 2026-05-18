package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.dto.CreateRoleRequest;
import com.aeisp.system.dto.UpdateRoleRequest;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.service.SysRoleService;
import com.aeisp.system.vo.SysRoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * 查询所有角色列表。
     *
     * @return 角色列表
     */
    @GetMapping
    public Result<List<SysRoleVO>> listAll() {
        List<SysRole> roles = sysRoleService.listAll();
        List<SysRoleVO> voList = roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 创建角色。
     *
     * @param request 创建参数
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody CreateRoleRequest request) {
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
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
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        request.setId(id);
        SysRole role = new SysRole();
        role.setId(request.getId());
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        boolean success = sysRoleService.updateRole(role, request.getPermissionIds());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新角色失败");
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        boolean success = sysRoleService.deleteRole(id);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "删除角色失败");
    }

    private SysRoleVO convertToVO(SysRole role) {
        SysRoleVO vo = new SysRoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        return vo;
    }
}
