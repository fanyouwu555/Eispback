package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrDurationChangeLog;
import com.aeisp.user.mapper.UsrDurationChangeLogMapper;
import com.aeisp.user.request.DurationChangeLogQueryRequest;
import com.aeisp.user.vo.UsrDurationChangeLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UsrDurationChangeLogServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UsrDurationChangeLogServiceImplTest {

    @Mock
    private UsrDurationChangeLogMapper usrDurationChangeLogMapper;

    @InjectMocks
    private UsrDurationChangeLogServiceImpl usrDurationChangeLogService;

    @Test
    void testSaveLog() {
        UsrDurationChangeLog log = new UsrDurationChangeLog();
        log.setUserId(1L);
        log.setChangeMinutes(50);
        when(usrDurationChangeLogMapper.insert(log)).thenReturn(1);

        usrDurationChangeLogService.saveLog(log);
        verify(usrDurationChangeLogMapper).insert(log);
    }

    @Test
    void testListLogsWithFilters() {
        UsrDurationChangeLog log = new UsrDurationChangeLog();
        log.setId(1L);
        log.setUserId(1L);
        log.setOperationType("ADMIN_ADD");
        log.setChangeMinutes(50);
        log.setPreviousRemaining(100);
        log.setCurrentRemaining(150);
        log.setReason("测试增加");
        log.setOperatorType(2);
        log.setCreatedAt(LocalDateTime.now());

        Page<UsrDurationChangeLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(usrDurationChangeLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        DurationChangeLogQueryRequest request = new DurationChangeLogQueryRequest();
        request.setUserId(1L);
        request.setOperationType("ADMIN_ADD");
        request.setOperatorType(2);
        request.setCreatedAtStart(LocalDateTime.now().minusDays(1));
        request.setCreatedAtEnd(LocalDateTime.now());
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<UsrDurationChangeLogVO> result = usrDurationChangeLogService.listLogs(request);

        assertEquals(1, result.getTotal());
        UsrDurationChangeLogVO vo = result.getList().get(0);
        assertEquals("ADMIN_ADD", vo.getOperationType());
        assertEquals("管理员增加", vo.getOperationTypeLabel());
        assertEquals(50, vo.getChangeMinutes());
        assertEquals(100, vo.getPreviousRemaining());
        assertEquals(150, vo.getCurrentRemaining());
        assertEquals("管理员", vo.getOperatorTypeLabel());
    }

    @Test
    void testListLogsEmpty() {
        Page<UsrDurationChangeLog> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(usrDurationChangeLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        DurationChangeLogQueryRequest request = new DurationChangeLogQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<UsrDurationChangeLogVO> result = usrDurationChangeLogService.listLogs(request);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void testResolveLabels() {
        UsrDurationChangeLog log = new UsrDurationChangeLog();
        log.setId(1L);
        log.setOperationType("CONSUME");
        log.setOperatorType(1);
        log.setCreatedAt(LocalDateTime.now());

        Page<UsrDurationChangeLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(usrDurationChangeLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<UsrDurationChangeLogVO> result = usrDurationChangeLogService.listLogs(new DurationChangeLogQueryRequest());
        UsrDurationChangeLogVO vo = result.getList().get(0);
        assertEquals("消费扣减", vo.getOperationTypeLabel());
        assertEquals("系统自动", vo.getOperatorTypeLabel());
    }
}
