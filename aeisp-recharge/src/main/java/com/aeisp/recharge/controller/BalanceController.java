package com.aeisp.recharge.controller;

import com.aeisp.common.Result;
import com.aeisp.recharge.dto.BalanceVO;
import com.aeisp.recharge.service.BalanceService;
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
 * 余额控制器。
 *
 * <p>提供余额查询、充值、抵扣及后台手动调整接口。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "余额管理", description = "用户余额查询与调整")
@RestController
@RequestMapping("/api/v1/recharge/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * 获取用户余额。
     *
     * @param userId 用户 ID
     * @return 余额信息
     */
    @Operation(summary = "查询余额")
    @GetMapping("/{userId}")
    public Result<BalanceVO> getBalance(@PathVariable Long userId) {
        return Result.success(balanceService.getBalance(userId));
    }

    /**
     * 余额充值。
     *
     * @param userId 用户 ID
     * @param amount 充值金额（分）
     * @return 操作结果
     */
    @Operation(summary = "余额充值")
    @PostMapping("/{userId}/recharge")
    public Result<Boolean> rechargeBalance(@PathVariable Long userId, @RequestParam Integer amount) {
        return Result.success(balanceService.rechargeBalance(userId, amount));
    }

    /**
     * 余额抵扣。
     *
     * @param userId 用户 ID
     * @param amount 抵扣金额（分）
     * @param reason 抵扣原因
     * @return 操作结果
     */
    @Operation(summary = "余额抵扣")
    @PostMapping("/{userId}/deduct")
    public Result<Boolean> deductBalance(@PathVariable Long userId, @RequestParam Integer amount, @RequestParam String reason) {
        return Result.success(balanceService.deductBalance(userId, amount, reason));
    }

    /**
     * 后台手动调整余额。
     *
     * @param userId     用户 ID
     * @param delta      调整金额（正数增加，负数减少）
     * @param reason     调整原因
     * @param operatorId 操作人 ID
     * @return 操作结果
     */
    @Operation(summary = "手动调整余额")
    @PostMapping("/{userId}/adjust")
    public Result<Boolean> adjustBalance(@PathVariable Long userId,
                                          @RequestParam Integer delta,
                                          @RequestParam String reason,
                                          @RequestParam Long operatorId) {
        return Result.success(balanceService.adjustBalance(userId, delta, reason, operatorId));
    }
}
