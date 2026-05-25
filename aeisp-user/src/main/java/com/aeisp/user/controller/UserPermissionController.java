package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.user.entity.UsrUserPermission;
import com.aeisp.user.entity.UsrUserPermissionLog;
import com.aeisp.user.mapper.UsrUserPermissionLogMapper;
import com.aeisp.user.request.UserPermissionUpdateRequest;
import com.aeisp.user.service.UsrUserPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UsrUserPermissionService usrUserPermissionService;
    private final UsrUserPermissionLogMapper usrUserPermissionLogMapper;

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
        // 查询变更前的权限值
        List<UsrUserPermission> oldPerms = usrUserPermissionService.getByUserId(userId);
        Map<String, String> oldValueMap = oldPerms.stream()
                .collect(Collectors.toMap(UsrUserPermission::getPermKey, UsrUserPermission::getPermValue));

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

        // 记录变更日志
        if (success) {
            String operatorName = getCurrentOperatorName();
            for (UserPermissionUpdateRequest.PermissionItem item : request.getPermissions()) {
                String oldVal = oldValueMap.get(item.getPermKey());
                if (oldVal == null || !oldVal.equals(item.getPermValue())) {
                    UsrUserPermissionLog log = new UsrUserPermissionLog();
                    log.setUserId(userId);
                    log.setPermKey(item.getPermKey());
                    log.setOldValue(oldVal);
                    log.setNewValue(item.getPermValue());
                    log.setOperationType("UPDATE");
                    log.setOperatorName(operatorName);
                    log.setCreatedAt(LocalDateTime.now());
                    usrUserPermissionLogMapper.insert(log);
                }
            }
        }

        return success ? Result.success() : Result.error(CommonErrorCode.SYSTEM_ERROR, "更新权限失败");
    }

    @PreAuthorize("hasAuthority('user:permission:update')")
    @OperationLog(module = "用户管理", operation = "重置用户权限", sensitivity = 2)
    @DeleteMapping("/{userId}")
    public Result<Void> resetPermissions(@PathVariable Long userId) {
        boolean success = usrUserPermissionService.clearPermissions(userId);
        if (success) {
            UsrUserPermissionLog log = new UsrUserPermissionLog();
            log.setUserId(userId);
            log.setOperationType("RESET");
            log.setOperatorName(getCurrentOperatorName());
            log.setCreatedAt(LocalDateTime.now());
            usrUserPermissionLogMapper.insert(log);
        }
        return success ? Result.success() : Result.error(CommonErrorCode.SYSTEM_ERROR, "重置权限失败");
    }

    @PreAuthorize("hasAuthority('user:permission:read')")
    @GetMapping("/{userId}/logs")
    public Result<List<UsrUserPermissionLog>> getPermissionLogs(@PathVariable Long userId) {
        return Result.success(usrUserPermissionLogMapper.selectByUserId(userId));
    }

    @PreAuthorize("hasAuthority('user:permission:read')")
    @GetMapping("/keys")
    public Result<List<Map<String, Object>>> getPermissionKeys() {
        List<Map<String, Object>> keys = List.of(
                Map.of("key", "ai_dialog_enabled", "label", "AI对话功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "code_execution_enabled", "label", "代码执行功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "paid_template_access", "label", "付费模板访问", "type", "boolean", "defaultValue", "false"),
                Map.of("key", "file_upload_enabled", "label", "文件上传功能", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "file_upload_max_size_mb", "label", "文件上传大小限制(MB)", "type", "number", "defaultValue", "50"),
                Map.of("key", "project_create_enabled", "label", "允许创建项目", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "max_projects", "label", "最大项目数", "type", "number", "defaultValue", "10"),
                Map.of("key", "max_starred_projects", "label", "最大星标项目数", "type", "number", "defaultValue", "5"),
                Map.of("key", "max_daily_calls", "label", "每日最大AI调用次数", "type", "number", "defaultValue", "100"),
                Map.of("key", "max_daily_executions", "label", "每日最大运行次数", "type", "number", "defaultValue", "50"),
                Map.of("key", "max_concurrent_sessions", "label", "最大并发会话数", "type", "number", "defaultValue", "5"),
                Map.of("key", "template_download_enabled", "label", "模板下载权限", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "resource_file_access", "label", "资源文件使用权限", "type", "boolean", "defaultValue", "true"),
                Map.of("key", "cloud_backup_enabled", "label", "云端备份权限", "type", "boolean", "defaultValue", "true")
        );
        return Result.success(keys);
    }

    private String getCurrentOperatorName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}