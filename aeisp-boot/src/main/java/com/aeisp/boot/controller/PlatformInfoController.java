package com.aeisp.boot.controller;

import com.aeisp.common.Result;
import com.aeisp.boot.vo.PlatformInfoVO;
import com.aeisp.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台信息控制器。
 *
 * <p>提供前端获取平台名称、版权、备案等公开信息。</p>
 */
@Tag(name = "平台信息", description = "公开平台配置信息")
@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformInfoController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "平台信息", description = "获取平台名称、版权、备案等公开信息")
    @GetMapping("/info")
    public Result<PlatformInfoVO> getPlatformInfo() {
        PlatformInfoVO vo = PlatformInfoVO.builder()
                .name(getConfig("platform.name", "AEisp"))
                .website(getConfig("platform.website", ""))
                .foreDomain(getConfig("platform.foredomain", ""))
                .backDomain(getConfig("platform.backdomain", ""))
                .copyright(getConfig("platform.copyright", "Copyright 2026 AEisp"))
                .icp(getConfig("platform.icp", ""))
                .build();
        return Result.success(vo);
    }

    private String getConfig(String key, String defaultValue) {
        String value = sysConfigService.getConfigValue(key, "all");
        return value != null ? value : defaultValue;
    }
}
