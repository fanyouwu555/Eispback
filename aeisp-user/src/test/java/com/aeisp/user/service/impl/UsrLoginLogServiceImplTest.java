package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.user.entity.UsrLoginLog;
import com.aeisp.user.mapper.UsrLoginLogMapper;
import com.aeisp.user.request.LogQueryRequest;
import com.aeisp.user.vo.UsrLoginLogVO;
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
 * UsrLoginLogServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UsrLoginLogServiceImplTest {

    @Mock
    private UsrLoginLogMapper usrLoginLogMapper;

    @InjectMocks
    private UsrLoginLogServiceImpl usrLoginLogService;

    @Test
    void testSaveLog() {
        UsrLoginLog log = new UsrLoginLog();
        log.setUserId(1L);
        log.setLoginAccount("test");
        when(usrLoginLogMapper.insert(log)).thenReturn(1);

        usrLoginLogService.saveLog(log);
        verify(usrLoginLogMapper).insert(log);
    }

    @Test
    void testListLogsWithAllConditions() {
        UsrLoginLog log = new UsrLoginLog();
        log.setId(1L);
        log.setUserId(1L);
        log.setLoginAccount("test");
        log.setLoginType(1);
        log.setLoginResult(1);
        log.setIpAddress("127.0.0.1");
        log.setDeviceType("desktop");
        log.setOsInfo("Windows 10");
        log.setBrowserInfo("Chrome");
        log.setCreatedAt(LocalDateTime.now());

        Page<UsrLoginLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(usrLoginLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        LogQueryRequest request = new LogQueryRequest();
        request.setUserId(1L);
        request.setLoginAccount("test");
        request.setLoginType(1);
        request.setLoginResult(1);
        request.setCreatedAtStart(LocalDateTime.now().minusDays(1));
        request.setCreatedAtEnd(LocalDateTime.now());
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<UsrLoginLogVO> result = usrLoginLogService.listLogs(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("test", result.getList().get(0).getLoginAccount());
        assertEquals(1, result.getList().get(0).getLoginType());
        assertEquals("127.0.0.1", result.getList().get(0).getIpAddress());
    }

    @Test
    void testListLogsEmptyResult() {
        Page<UsrLoginLog> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(usrLoginLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        LogQueryRequest request = new LogQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);

        PageResult<UsrLoginLogVO> result = usrLoginLogService.listLogs(request);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
