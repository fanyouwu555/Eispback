package com.aeisp.recharge.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.dto.DurationStatVO;
import com.aeisp.user.entity.UsrDurationChangeLog;
import com.aeisp.user.entity.UsrUserDuration;
import com.aeisp.user.mapper.UsrDurationChangeLogMapper;
import com.aeisp.user.mapper.UsrUserDurationMapper;
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
 * DurationServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class DurationServiceImplTest {

    @Mock
    private UsrUserDurationMapper userDurationMapper;

    @Mock
    private UsrDurationChangeLogMapper durationChangeLogMapper;

    @InjectMocks
    private DurationServiceImpl durationService;

    @Test
    void testConsumeDurationSuccess() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(100);
        duration.setTotalConsumedMinutes(50);
        when(userDurationMapper.selectByUserId(1L)).thenReturn(duration);
        when(userDurationMapper.updateById(any(UsrUserDuration.class))).thenReturn(1);
        when(durationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        assertTrue(durationService.consumeDuration(1L, 30L, "model_call", 10L, "test"));
        verify(userDurationMapper).updateById(argThat((UsrUserDuration d) ->
                d.getRemainingMinutes() == 70 && d.getTotalConsumedMinutes() == 80));
        verify(durationChangeLogMapper).insert(argThat((UsrDurationChangeLog l) ->
                l.getChangeMinutes() == -30 && "CONSUME".equals(l.getOperationType())));
    }

    @Test
    void testConsumeDurationInsufficient() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(10);
        when(userDurationMapper.selectByUserId(1L)).thenReturn(duration);

        BizException ex = assertThrows(BizException.class,
                () -> durationService.consumeDuration(1L, 30L, "model_call", 10L, "test"));
        assertEquals("剩余时长不足", ex.getMessage());
    }

    @Test
    void testConsumeDurationExactlyAll() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(30);
        duration.setTotalConsumedMinutes(50);
        when(userDurationMapper.selectByUserId(1L)).thenReturn(duration);
        when(userDurationMapper.updateById(any(UsrUserDuration.class))).thenReturn(1);
        when(durationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        assertTrue(durationService.consumeDuration(1L, 30L, "model_call", 10L, "test"));
        verify(userDurationMapper).updateById(argThat((UsrUserDuration d) ->
                d.getRemainingMinutes() == 0 && d.getTotalConsumedMinutes() == 80));
    }

    @Test
    void testConsumeDurationInvalidMinutes() {
        BizException ex = assertThrows(BizException.class,
                () -> durationService.consumeDuration(1L, 0L, "model_call", 10L, "test"));
        assertEquals("消耗时长必须大于0", ex.getMessage());
    }

    @Test
    void testAddDurationSuccess() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(100);
        duration.setTotalGrantedMinutes(200);
        when(userDurationMapper.selectByUserId(1L)).thenReturn(duration);
        when(userDurationMapper.updateById(any(UsrUserDuration.class))).thenReturn(1);
        when(durationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        assertTrue(durationService.addDuration(1L, 50L, "赠送时长"));
        verify(userDurationMapper).updateById(argThat((UsrUserDuration d) ->
                d.getRemainingMinutes() == 150 && d.getTotalGrantedMinutes() == 250));
        verify(durationChangeLogMapper).insert(argThat((UsrDurationChangeLog l) ->
                l.getChangeMinutes() == 50 && "ADMIN_ADD".equals(l.getOperationType())));
    }

    @Test
    void testAddDurationNewUser() {
        when(userDurationMapper.selectByUserId(1L)).thenReturn(null);
        when(userDurationMapper.insert(any(UsrUserDuration.class))).thenReturn(1);
        when(durationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        assertTrue(durationService.addDuration(1L, 60L, "新用户赠送"));
        verify(userDurationMapper).insert(argThat((UsrUserDuration d) ->
                d.getRemainingMinutes() == 60 && d.getTotalGrantedMinutes() == 60));
    }

    @Test
    void testAddDurationInvalidMinutes() {
        BizException ex = assertThrows(BizException.class,
                () -> durationService.addDuration(1L, 0L, "reason"));
        assertEquals("增加时长必须大于0", ex.getMessage());
    }

    @Test
    void testGetDurationStatsExisting() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setTotalGrantedMinutes(500);
        duration.setTotalConsumedMinutes(200);
        duration.setRemainingMinutes(300);
        when(userDurationMapper.selectByUserId(1L)).thenReturn(duration);

        DurationStatVO vo = durationService.getDurationStats(1L);
        assertEquals(1L, vo.getUserId());
        assertEquals(500L, vo.getTotalRecharged());
        assertEquals(200L, vo.getTotalConsumed());
        assertEquals(300L, vo.getRemainingDuration());
    }

    @Test
    void testGetDurationStatsNotFound() {
        when(userDurationMapper.selectByUserId(1L)).thenReturn(null);
        DurationStatVO vo = durationService.getDurationStats(1L);
        assertEquals(0L, vo.getTotalRecharged());
        assertEquals(0L, vo.getTotalConsumed());
        assertEquals(0L, vo.getRemainingDuration());
    }
}
