package com.aeisp.system.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.system.dto.LogQueryRequest;
import com.aeisp.system.entity.SysOperationLog;
import com.aeisp.system.mapper.SysOperationLogMapper;
import com.aeisp.system.service.SysOperationLogService;
import com.aeisp.system.vo.SysOperationLogExcelVO;
import com.aeisp.system.vo.SysOperationLogVO;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public void saveLog(SysOperationLog log) {
        sysOperationLogMapper.insert(log);
    }

    @Override
    public PageResult<SysOperationLogVO> listLogs(LogQueryRequest request) {
        LambdaQueryWrapper<SysOperationLog> wrapper = buildWrapper(request);
        Page<SysOperationLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<SysOperationLog> resultPage = sysOperationLogMapper.selectPage(page, wrapper);

        List<SysOperationLogVO> voList = resultPage.getRecords().stream()
                .map(SysOperationLogServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    @Override
    public void exportLogs(LogQueryRequest request, HttpServletResponse response) {
        try {
            LambdaQueryWrapper<SysOperationLog> wrapper = buildWrapper(request);
            wrapper.last("LIMIT 5000");
            List<SysOperationLog> records = sysOperationLogMapper.selectList(wrapper);
            List<SysOperationLogExcelVO> excelList = records.stream()
                    .map(SysOperationLogServiceImpl::convertToExcelVO)
                    .toList();
            EasyExcel.write(response.getOutputStream(), SysOperationLogExcelVO.class)
                    .sheet("操作日志")
                    .doWrite(excelList);
        } catch (IOException e) {
            throw new RuntimeException("导出操作日志失败", e);
        }
    }

    private LambdaQueryWrapper<SysOperationLog> buildWrapper(LogQueryRequest request) {
        LambdaQueryWrapper<SysOperationLog> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysOperationLog::getOperatorUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getModule())) {
            wrapper.like(SysOperationLog::getOperationType, request.getModule());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysOperationLog::getStatus, request.getStatus());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(SysOperationLog::getCreatedAt, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(SysOperationLog::getCreatedAt, request.getEndTime());
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedAt);
        return wrapper;
    }

    private static SysOperationLogVO convertToVO(SysOperationLog log) {
        SysOperationLogVO vo = new SysOperationLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setOperatorUsername(log.getOperatorUsername());
        vo.setOperationType(log.getOperationType());
        vo.setOperationTypeLabel(log.getOperationTypeLabel());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setOperationDetail(log.getOperationDetail());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setRequestParams(log.getRequestParams());
        vo.setResponseData(log.getResponseData());
        vo.setStatus(log.getStatus());
        vo.setErrorMsg(log.getErrorMsg());
        vo.setIpAddress(log.getIpAddress());
        vo.setDuration(log.getDuration());
        vo.setSensitivity(log.getSensitivity());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private static SysOperationLogExcelVO convertToExcelVO(SysOperationLog log) {
        SysOperationLogExcelVO vo = new SysOperationLogExcelVO();
        vo.setId(log.getId());
        vo.setOperatorUsername(log.getOperatorUsername());
        vo.setOperationType(log.getOperationType());
        vo.setOperationTypeLabel(log.getOperationTypeLabel());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setIpAddress(log.getIpAddress());
        vo.setStatusText(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
        vo.setDuration(log.getDuration());
        vo.setSensitivityText(log.getSensitivity() != null && log.getSensitivity() == 2 ? "敏感" : "普通");
        vo.setCreatedAt(log.getCreatedAt() != null
                ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null);
        vo.setErrorMsg(log.getErrorMsg());
        return vo;
    }
}
