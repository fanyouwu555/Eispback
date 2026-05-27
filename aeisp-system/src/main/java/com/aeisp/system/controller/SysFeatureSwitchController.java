package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.vo.SysFeatureSwitchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/features")
@RequiredArgsConstructor
public class SysFeatureSwitchController {

    private final SysFeatureSwitchService sysFeatureSwitchService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:feature:view')")
    public Result<List<SysFeatureSwitchVO>> list() {
        return Result.success(sysFeatureSwitchService.listAll());
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('system:feature:toggle')")
    public Result<Void> toggle(@PathVariable Long id) {
        sysFeatureSwitchService.toggle(id);
        return Result.success();
    }

    @PutMapping("/maintenance")
    @PreAuthorize("hasAuthority('system:feature:maintenance')")
    public Result<Void> toggleMaintenance(@RequestBody Map<String, Boolean> body) {
        sysFeatureSwitchService.toggleMaintenance(body.get("enabled"));
        return Result.success();
    }
}