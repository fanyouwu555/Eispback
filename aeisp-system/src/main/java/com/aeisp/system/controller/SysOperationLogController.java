package com.aeisp.system.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.dto.LogQueryRequest;
import com.aeisp.system.service.SysOperationLogService;
import com.aeisp.system.vo.SysOperationLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志管理控制器。
 *
 * <p>提供操作日志的分页查询接口，路径前缀 {@code /api/v1/system/logs}。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/system/logs")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService sysOperationLogService;

    /**
     * 分页查询操作日志列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('admin:log:read')")
    @OperationLog(module = "操作日志", operation = "查询", recordResponse = false)
    @GetMapping
    public Result<PageResult<SysOperationLogVO>> listLogs(LogQueryRequest request) {
        PageResult<SysOperationLogVO> result = sysOperationLogService.listLogs(request);
        return Result.success(result);
    }

    @PreAuthorize("hasAuthority('admin:log:read')")
    @OperationLog(module = "操作日志", operation = "导出", recordResponse = false)
    @GetMapping("/export")
    public void exportLogs(LogQueryRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(
                "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        sysOperationLogService.exportLogs(request, response);
    }
}
