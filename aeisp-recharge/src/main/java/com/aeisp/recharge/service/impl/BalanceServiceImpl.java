package com.aeisp.recharge.service.impl;

import com.aeisp.recharge.dto.BalanceVO;
import com.aeisp.recharge.service.BalanceService;
import org.springframework.stereotype.Service;

/**
 * 余额服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class BalanceServiceImpl implements BalanceService {

    @Override
    public BalanceVO getBalance(Long userId) {
        throw new UnsupportedOperationException("getBalance 待实现");
    }

    @Override
    public boolean rechargeBalance(Long userId, Integer amount) {
        throw new UnsupportedOperationException("rechargeBalance 待实现");
    }

    @Override
    public boolean deductBalance(Long userId, Integer amount, String reason) {
        throw new UnsupportedOperationException("deductBalance 待实现");
    }

    @Override
    public boolean adjustBalance(Long userId, Integer delta, String reason, Long operatorId) {
        throw new UnsupportedOperationException("adjustBalance 待实现");
    }
}
