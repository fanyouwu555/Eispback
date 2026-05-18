package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.vo.SysConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 查询所有系统配置列表。
     *
     * @return 配置列表
     */
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
    @GetMapping("/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        String value = sysConfigService.getConfigValue(key);
        return Result.success(value);
    }

    /**
     * 更新配置值。
     *
     * @param key   配置键
     * @param value 配置值
     * @return 操作结果
     */
    @PutMapping("/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestParam String value) {
        boolean success = sysConfigService.updateConfig(key, value);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新配置失败，配置键不存在");
    }
}
