package com.aeisp.recharge.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.recharge.dto.DurationConsumeVO;
import com.aeisp.recharge.dto.DurationQueryRequest;
import com.aeisp.recharge.dto.DurationStatVO;
import com.aeisp.recharge.service.DurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 时长控制器。
 *
 * <p>提供时长消耗记录查询、时长增加及统计接口。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "时长管理", description = "用户时长消耗与统计")
@RestController
@RequestMapping("/api/v1/recharge/duration")
@RequiredArgsConstructor
public class DurationController {

    private final DurationService durationService;

    /**
     * 消耗时长。
     *
     * @param userId      用户 ID
     * @param minutes     消耗分钟数
     * @param consumeType 消耗类型
     * @param projectId   关联项目 ID
     * @param description 描述信息
     * @return 操作结果
     */
    @Operation(summary = "消耗时长")
    @PostMapping("/{userId}/consume")
    public Result<Boolean> consumeDuration(@PathVariable Long userId,
                                            @RequestParam Long minutes,
                                            @RequestParam String consumeType,
                                            @RequestParam(required = false) Long projectId,
                                            @RequestParam(required = false) String description) {
        return Result.success(durationService.consumeDuration(userId, minutes, consumeType, projectId, description));
    }

    /**
     * 增加时长。
     *
     * @param userId 用户 ID
     * @param minutes 增加分钟数
     * @param reason 原因
     * @return 操作结果
     */
    @Operation(summary = "增加时长")
    @PostMapping("/{userId}/add")
    public Result<Boolean> addDuration(@PathVariable Long userId, @RequestParam Long minutes, @RequestParam String reason) {
        return Result.success(durationService.addDuration(userId, minutes, reason));
    }

    /**
     * 分页查询时长消耗记录。
     *
     * @param userId  用户 ID
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "时长消耗记录")
    @GetMapping("/{userId}/consumes")
    public Result<PageResult<DurationConsumeVO>> listConsumes(@PathVariable Long userId, DurationQueryRequest request) {
        return Result.success(durationService.listConsumes(userId, request));
    }

    /**
     * 获取用户时长统计。
     *
     * @param userId 用户 ID
     * @return 时长统计
     */
    @Operation(summary = "时长统计")
    @GetMapping("/{userId}/stats")
    public Result<DurationStatVO> getDurationStats(@PathVariable Long userId) {
        return Result.success(durationService.getDurationStats(userId));
    }
}
