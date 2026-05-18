package com.aeisp.recharge.service;

import com.aeisp.recharge.dto.BalanceVO;

/**
 * 余额服务接口。
 *
 * <p>提供余额查询、充值、抵扣及后台手动调整能力。</p>
 *
 * @author AEISP Team
 */
public interface BalanceService {

    /**
     * 获取用户余额。
     *
     * @param userId 用户 ID
     * @return 余额视图对象
     */
    BalanceVO getBalance(Long userId);

    /**
     * 余额充值。
     *
     * @param userId 用户 ID
     * @param amount 充值金额（分）
     * @return 是否成功
     */
    boolean rechargeBalance(Long userId, Integer amount);

    /**
     * 余额抵扣。
     *
     * @param userId 用户 ID
     * @param amount 抵扣金额（分）
     * @param reason 抵扣原因
     * @return 是否成功
     */
    boolean deductBalance(Long userId, Integer amount, String reason);

    /**
     * 后台手动调整余额。
     *
     * @param userId     用户 ID
     * @param delta      调整金额（正数增加，负数减少）
     * @param reason     调整原因
     * @param operatorId 操作人 ID
     * @return 是否成功
     */
    boolean adjustBalance(Long userId, Integer delta, String reason, Long operatorId);
}
