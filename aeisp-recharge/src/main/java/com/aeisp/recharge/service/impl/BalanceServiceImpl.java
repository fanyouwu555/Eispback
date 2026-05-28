package com.aeisp.recharge.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.code.RechargeErrorCode;
import com.aeisp.recharge.dto.BalanceVO;
import com.aeisp.recharge.entity.UsrBalanceChangeLog;
import com.aeisp.recharge.mapper.UsrBalanceChangeLogMapper;
import com.aeisp.recharge.service.BalanceService;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.user.entity.UsrUserBalance;
import com.aeisp.user.mapper.UsrUserBalanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 余额服务实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final UsrUserBalanceMapper userBalanceMapper;
    private final UsrBalanceChangeLogMapper balanceChangeLogMapper;
    private final SysConfigService sysConfigService;

    @Override
    public BalanceVO getBalance(Long userId) {
        UsrUserBalance balance = userBalanceMapper.selectByUserId(userId);
        BalanceVO vo = new BalanceVO();
        vo.setUserId(userId);
        vo.setBalance(balance != null ? balance.getBalanceCents() : 0);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeBalance(Long userId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BizException(RechargeErrorCode.BALANCE_RECHARGE_AMOUNT_INVALID);
        }
        UsrUserBalance balance = userBalanceMapper.selectByUserId(userId);
        int previousBalance = balance != null ? balance.getBalanceCents() : 0;
        int currentBalance = previousBalance + amount;

        if (balance == null) {
            balance = new UsrUserBalance();
            balance.setUserId(userId);
            balance.setBalanceCents(currentBalance);
            balance.setTotalRechargeCents(amount);
            balance.setTotalConsumedCents(0);
            userBalanceMapper.insert(balance);
        } else {
            balance.setBalanceCents(currentBalance);
            balance.setTotalRechargeCents(balance.getTotalRechargeCents() + amount);
            userBalanceMapper.updateById(balance);
        }

        UsrBalanceChangeLog changeLog = new UsrBalanceChangeLog();
        changeLog.setUserId(userId);
        changeLog.setChangeType(1); // 充值
        changeLog.setChangeCents(amount);
        changeLog.setPreviousBalance(previousBalance);
        changeLog.setCurrentBalance(currentBalance);
        changeLog.setReason("余额充值");
        changeLog.setOperatorType(1); // 系统自动
        changeLog.setCreatedAt(LocalDateTime.now());
        balanceChangeLogMapper.insert(changeLog);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, Integer amount, String reason) {
        if (amount == null || amount <= 0) {
            throw new BizException(RechargeErrorCode.BALANCE_DEDUCT_AMOUNT_INVALID);
        }
        UsrUserBalance balance = userBalanceMapper.selectByUserId(userId);
        int previousBalance = balance != null ? balance.getBalanceCents() : 0;
        if (previousBalance < amount) {
            throw new BizException(RechargeErrorCode.BALANCE_INSUFFICIENT);
        }
        int currentBalance = previousBalance - amount;

        if (balance == null) {
            balance = new UsrUserBalance();
            balance.setUserId(userId);
            balance.setBalanceCents(currentBalance);
            balance.setTotalRechargeCents(0);
            balance.setTotalConsumedCents(amount);
            userBalanceMapper.insert(balance);
        } else {
            balance.setBalanceCents(currentBalance);
            balance.setTotalConsumedCents(balance.getTotalConsumedCents() + amount);
            userBalanceMapper.updateById(balance);
        }

        UsrBalanceChangeLog changeLog = new UsrBalanceChangeLog();
        changeLog.setUserId(userId);
        changeLog.setChangeType(2); // 消费
        changeLog.setChangeCents(-amount);
        changeLog.setPreviousBalance(previousBalance);
        changeLog.setCurrentBalance(currentBalance);
        changeLog.setReason(reason);
        changeLog.setOperatorType(1); // 系统自动
        changeLog.setCreatedAt(LocalDateTime.now());
        balanceChangeLogMapper.insert(changeLog);
        checkBalanceThreshold(userId, currentBalance);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustBalance(Long userId, Integer delta, String reason, Long operatorId) {
        if (delta == null || delta == 0) {
            throw new BizException(RechargeErrorCode.BALANCE_ADJUST_ZERO);
        }
        UsrUserBalance balance = userBalanceMapper.selectByUserId(userId);
        int previousBalance = balance != null ? balance.getBalanceCents() : 0;
        int currentBalance = previousBalance + delta;
        if (currentBalance < 0) {
            throw new BizException(RechargeErrorCode.BALANCE_ADJUST_NEGATIVE);
        }

        if (balance == null) {
            balance = new UsrUserBalance();
            balance.setUserId(userId);
            balance.setBalanceCents(currentBalance);
            balance.setTotalRechargeCents(delta > 0 ? delta : 0);
            balance.setTotalConsumedCents(delta < 0 ? -delta : 0);
            userBalanceMapper.insert(balance);
        } else {
            balance.setBalanceCents(currentBalance);
            if (delta > 0) {
                balance.setTotalRechargeCents(balance.getTotalRechargeCents() + delta);
            } else {
                balance.setTotalConsumedCents(balance.getTotalConsumedCents() - delta);
            }
            userBalanceMapper.updateById(balance);
        }

        UsrBalanceChangeLog changeLog = new UsrBalanceChangeLog();
        changeLog.setUserId(userId);
        changeLog.setChangeType(4); // 管理员调整
        changeLog.setChangeCents(delta);
        changeLog.setPreviousBalance(previousBalance);
        changeLog.setCurrentBalance(currentBalance);
        changeLog.setReason(reason);
        changeLog.setOperatorId(operatorId);
        changeLog.setOperatorType(2); // 管理员
        changeLog.setCreatedAt(LocalDateTime.now());
        balanceChangeLogMapper.insert(changeLog);

        return true;
    }

    /**
     * 检查余额是否低于预警阈值，低于时记录告警日志。
     */
    private void checkBalanceThreshold(Long userId, int currentBalance) {
        String thresholdStr = sysConfigService.getConfigValue("threshold.balance", "all");
        if (!StringUtils.hasText(thresholdStr)) {
            return;
        }
        try {
            int threshold = Integer.parseInt(thresholdStr.trim());
            if (currentBalance < threshold) {
                log.warn("用户余额低于预警阈值: userId={}, currentBalance={}分, threshold={}分",
                        userId, currentBalance, threshold);
            }
        } catch (NumberFormatException e) {
            log.warn("threshold.balance 配置值 '{}' 无法解析为数字", thresholdStr);
        }
    }
}
