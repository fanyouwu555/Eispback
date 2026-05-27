package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.vo.SysConfigLogVO;
import com.aeisp.system.vo.SysConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器。
 *
 * <p>提供系统配置的查询和更新接口，路径前缀 {@code /api/v1/system/configs}。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/system/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;
    private final SysConfigLogService sysConfigLogService;

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 查询所有系统配置列表。
     *
     * @return 配置列表
     */
    @PreAuthorize("hasAuthority('system:config')")
    @GetMapping
    public Result<List<SysConfigVO>> listAll() {
        List<SysConfigVO> list = sysConfigService.listAll();
        return Result.success(list);
    }

    /**
     * 根据配置键获取配置值。
     *
     * @param key 配置键
     * @return 配置值
     */
    @PreAuthorize("hasAuthority('system:config')")
    @GetMapping("/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        String value = sysConfigService.getConfigValue(key, activeProfile);
        return Result.success(value);
    }

    /**
     * 更新配置值。
     *
     * @param key   配置键
     * @param value 配置值
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:config')")
    @OperationLog(module = "系统配置", operation = "修改")
    @PutMapping("/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("configValue");
        if (value == null) {
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, "configValue 不能为空");
        }
        boolean success = sysConfigService.updateConfig(key, value, activeProfile);
        return success ? Result.success() : Result.error(CommonErrorCode.SYSTEM_ERROR, "更新配置失败，配置键不存在");
    }

    /**
     * 按分类查询系统配置。
     *
     * @param category 配置分类
     * @return 配置列表
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('system:config')")
    @OperationLog(module = "系统配置", operation = "按分类查询")
    public Result<List<SysConfigVO>> listByCategory(@PathVariable String category) {
        return Result.success(sysConfigService.listByCategory(category));
    }

    /**
     * 查看配置变更历史。
     *
     * @param configType 配置类型
     * @param refId      关联 ID
     * @return 变更历史列表
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('system:config')")
    @OperationLog(module = "系统配置", operation = "查看变更历史")
    public Result<List<SysConfigLogVO>> listLogs(
            @RequestParam String configType,
            @RequestParam Long refId) {
        return Result.success(sysConfigLogService.listByRef(configType, refId));
    }
}
