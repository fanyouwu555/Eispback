package com.aeisp.recharge.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.dto.BalanceVO;
import com.aeisp.recharge.entity.UsrBalanceChangeLog;
import com.aeisp.recharge.mapper.UsrBalanceChangeLogMapper;
import com.aeisp.user.entity.UsrUserBalance;
import com.aeisp.user.mapper.UsrUserBalanceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * BalanceServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private UsrUserBalanceMapper userBalanceMapper;

    @Mock
    private UsrBalanceChangeLogMapper balanceChangeLogMapper;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Test
    void testGetBalanceExisting() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(5000);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);

        BalanceVO vo = balanceService.getBalance(1L);
        assertEquals(1L, vo.getUserId());
        assertEquals(5000, vo.getBalance());
    }

    @Test
    void testGetBalanceNotFound() {
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(null);
        BalanceVO vo = balanceService.getBalance(1L);
        assertEquals(0, vo.getBalance());
    }

    @Test
    void testRechargeBalanceSuccess() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(1000);
        balance.setTotalRechargeCents(1000);
        balance.setTotalConsumedCents(0);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);
        when(userBalanceMapper.updateById(any(UsrUserBalance.class))).thenReturn(1);
        when(balanceChangeLogMapper.insert(any(UsrBalanceChangeLog.class))).thenReturn(1);

        assertTrue(balanceService.rechargeBalance(1L, 2000));
        verify(userBalanceMapper).updateById(argThat((UsrUserBalance b) -> b.getBalanceCents() == 3000));
        verify(balanceChangeLogMapper).insert(argThat((UsrBalanceChangeLog l) ->
                l.getChangeType() == 1 && l.getChangeCents() == 2000
                        && l.getPreviousBalance() == 1000
                        && l.getCurrentBalance() == 3000));
    }

    @Test
    void testRechargeBalanceNewUser() {
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(null);
        when(userBalanceMapper.insert(any(UsrUserBalance.class))).thenReturn(1);
        when(balanceChangeLogMapper.insert(any(UsrBalanceChangeLog.class))).thenReturn(1);

        assertTrue(balanceService.rechargeBalance(1L, 5000));
        verify(userBalanceMapper).insert(argThat((UsrUserBalance b) -> b.getBalanceCents() == 5000));
    }

    @Test
    void testRechargeBalanceInvalidAmount() {
        BizException ex = assertThrows(BizException.class,
                () -> balanceService.rechargeBalance(1L, 0));
        assertEquals("充值金额必须大于0", ex.getMessage());
    }

    @Test
    void testDeductBalanceSuccess() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(5000);
        balance.setTotalConsumedCents(0);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);
        when(userBalanceMapper.updateById(any(UsrUserBalance.class))).thenReturn(1);
        when(balanceChangeLogMapper.insert(any(UsrBalanceChangeLog.class))).thenReturn(1);

        assertTrue(balanceService.deductBalance(1L, 2000, "消费扣减"));
        verify(userBalanceMapper).updateById(argThat((UsrUserBalance b) -> b.getBalanceCents() == 3000));
        verify(balanceChangeLogMapper).insert(argThat((UsrBalanceChangeLog l) ->
                l.getChangeType() == 2 && l.getChangeCents() == -2000));
    }

    @Test
    void testDeductBalanceInsufficient() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(100);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);

        BizException ex = assertThrows(BizException.class,
                () -> balanceService.deductBalance(1L, 200, "消费"));
        assertEquals("余额不足", ex.getMessage());
    }

    @Test
    void testDeductBalanceExactlyAll() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(2000);
        balance.setTotalConsumedCents(0);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);
        when(userBalanceMapper.updateById(any(UsrUserBalance.class))).thenReturn(1);
        when(balanceChangeLogMapper.insert(any(UsrBalanceChangeLog.class))).thenReturn(1);

        assertTrue(balanceService.deductBalance(1L, 2000, "全部消费"));
        verify(userBalanceMapper).updateById(argThat((UsrUserBalance b) -> b.getBalanceCents() == 0));
    }

    @Test
    void testAdjustBalanceSuccess() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(1000);
        balance.setTotalRechargeCents(1000);
        balance.setTotalConsumedCents(0);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);
        when(userBalanceMapper.updateById(any(UsrUserBalance.class))).thenReturn(1);
        when(balanceChangeLogMapper.insert(any(UsrBalanceChangeLog.class))).thenReturn(1);

        assertTrue(balanceService.adjustBalance(1L, -500, "调整", 2L));
        verify(userBalanceMapper).updateById(argThat((UsrUserBalance b) -> b.getBalanceCents() == 500));
        verify(balanceChangeLogMapper).insert(argThat((UsrBalanceChangeLog l) ->
                l.getChangeType() == 4 && l.getChangeCents() == -500
                        && l.getOperatorId() == 2L));
    }

    @Test
    void testAdjustBalanceInvalidDelta() {
        BizException ex = assertThrows(BizException.class,
                () -> balanceService.adjustBalance(1L, 0, "reason", 1L));
        assertEquals("调整金额不能为0", ex.getMessage());
    }

    @Test
    void testAdjustBalanceNegativeResult() {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(100);
        when(userBalanceMapper.selectByUserId(1L)).thenReturn(balance);

        BizException ex = assertThrows(BizException.class,
                () -> balanceService.adjustBalance(1L, -200, "reason", 1L));
        assertEquals("调整后余额不能为负数", ex.getMessage());
    }
}
