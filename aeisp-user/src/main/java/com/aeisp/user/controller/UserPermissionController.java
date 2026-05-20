package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.user.entity.UsrUserPermission;
import com.aeisp.user.request.UserPermissionUpdateRequest;
import com.aeisp.user.service.UsrUserPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UsrUserPermissionService usrUserPermissionService;

    @PreAuthorize("hasAuthority('user:permission:read')")
    @GetMapping("/{userId}")
    public Result<List<UsrUserPermission>> getByUserId(@PathVariable Long userId) {
        return Result.success(usrUserPermissionService.getByUserId(userId));
    }

    @PreAuthorize("hasAuthority('user:permission:update')")
    @OperationLog(module = "用户管理", operation = "修改用户权限", sensitivity = 2)
    @PutMapping("/{userId}")
    public Result<Void> updatePermissions(@PathVariable Long userId,
                                           @Valid @RequestBody UserPermissionUpdateRequest request) {
        List<UsrUserPermission> permissions = request.getPermissions().stream()
                .map(item -> {
                    UsrUserPermission perm = new UsrUserPermission();
                    perm.setUserId(userId);
                    perm.setPermKey(item.getPermKey());
                    perm.setPermValue(item.getPermValue());
                    perm.setExpireAt(item.getExpireAt());
                    perm.setEffectiveAt(LocalDateTime.now());
                    return perm;
                })
                .toList();
        boolean success = usrUserPermissionService.updatePermissions(userId, permissions);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新权限失败");
    }

    @PreAuthorize("hasAuthority('user:permission:read')")
    @GetMapping("/keys")
    public Result<List<Map<String, Object>>> getPermissionKeys() {
        List<Map<String, Object>> keys = List.of(
                Map.of("key", "ai_dialog_enabled", "label", "AI对话功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "code_execution_enabled", "label", "代码执行功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "paid_template_access", "label", "付费模板访问", "type", "boolean", "defaultValue", "false"),
                Map.of("key", "file_upload_enabled", "label", "文件上传功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "max_projects", "label", "最大项目数", "type", "number", "defaultValue", "10"),
                Map.of("key", "max_daily_calls", "label", "每日最大调用次数", "type", "number", "defaultValue", "100"),
                Map.of("key", "max_concurrent_sessions", "label", "最大并发会话数", "type", "number", "defaultValue", "5")
        );
        return Result.success(keys);
    }
}