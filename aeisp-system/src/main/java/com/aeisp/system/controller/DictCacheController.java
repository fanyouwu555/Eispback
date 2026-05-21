package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.CacheConstants;
import com.aeisp.system.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/dict")
@RequiredArgsConstructor
public class DictCacheController {

    private final StringRedisTemplate stringRedisTemplate;

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "刷新字典缓存")
    @PostMapping("/refresh")
    public Result<Void> refresh() {
        var keys = stringRedisTemplate.keys(CacheConstants.CACHE_DICT + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        return Result.success();
    }
}
