package com.aeisp.system.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.system.dto.LogQueryRequest;
import com.aeisp.system.entity.SysOperationLog;
import com.aeisp.system.mapper.SysOperationLogMapper;
import com.aeisp.system.vo.SysOperationLogVO;
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
 * SysOperationLogServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SysOperationLogServiceImplTest {

    @Mock
    private SysOperationLogMapper sysOperationLogMapper;

    @InjectMocks
    private SysOperationLogServiceImpl sysOperationLogService;

    @Test
    void testSaveLog() {
        SysOperationLog log = new SysOperationLog();
        log.setUserId(1L);
        log.setOperationType("UPDATE_USER_STATUS");
        when(sysOperationLogMapper.insert(log)).thenReturn(1);

        sysOperationLogService.saveLog(log);
        verify(sysOperationLogMapper).insert(log);
    }

    @Test
    void testListLogsWithAllConditions() {
        SysOperationLog log = new SysOperationLog();
        log.setId(1L);
        log.setUserId(1L);
        log.setOperatorUsername("admin");
        log.setOperationType("认证管理");
        log.setOperationTypeLabel("登录");
        log.setTargetType("user");
        log.setTargetId("1");
        log.setRequestMethod("POST");
        log.setRequestUrl("/api/v1/auth/login");
        log.setStatus(1);
        log.setIpAddress("127.0.0.1");
        log.setDuration(120L);
        log.setSensitivity(1);
        log.setCreatedAt(LocalDateTime.now());

        Page<SysOperationLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(sysOperationLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        LogQueryRequest request = new LogQueryRequest();
        request.setUsername("admin");
        request.setModule("认证管理");
        request.setStatus(1);
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now());
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<SysOperationLogVO> result = sysOperationLogService.listLogs(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        SysOperationLogVO vo = result.getList().get(0);
        assertEquals("admin", vo.getOperatorUsername());
        assertEquals("认证管理", vo.getOperationType());
        assertEquals("登录", vo.getOperationTypeLabel());
        assertEquals("127.0.0.1", vo.getIpAddress());
        assertEquals(120L, vo.getDuration());
    }

    @Test
    void testListLogsEmptyResult() {
        Page<SysOperationLog> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(sysOperationLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        LogQueryRequest request = new LogQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<SysOperationLogVO> result = sysOperationLogService.listLogs(request);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
