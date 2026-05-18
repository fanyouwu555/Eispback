package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.system.entity.SysPermission;
import com.aeisp.system.service.SysPermissionService;
import com.aeisp.system.vo.SysPermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限点管理控制器。
 *
 * <p>提供权限点列表查询接口，路径前缀 {@code /api/v1/system/permissions}。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/system/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询所有权限点列表。
     *
     * @return 权限点列表
     */
    @GetMapping
    public Result<List<SysPermissionVO>> listAll() {
        List<SysPermission> permissions = sysPermissionService.listAll();
        List<SysPermissionVO> voList = permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    private SysPermissionVO convertToVO(SysPermission permission) {
        SysPermissionVO vo = new SysPermissionVO();
        vo.setId(permission.getId());
        vo.setPermissionName(permission.getPermissionName());
        vo.setPermissionCode(permission.getPermissionCode());
        vo.setDescription(permission.getDescription());
        return vo;
    }
}
