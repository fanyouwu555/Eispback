package com.aeisp.system.service;

import com.aeisp.common.PageResult;
import com.aeisp.system.dto.LogQueryRequest;
import com.aeisp.system.entity.SysOperationLog;
import com.aeisp.system.vo.SysOperationLogVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 操作日志 Service 接口。
 *
 * @author AEISP Team
 */
public interface SysOperationLogService {

    /**
     * 保存操作日志。
     *
     * @param log 日志实体
     */
    void saveLog(SysOperationLog log);

    /**
     * 分页查询操作日志列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<SysOperationLogVO> listLogs(LogQueryRequest request);

    /**
     * 导出操作日志为 Excel。
     *
     * @param request  查询条件
     * @param response HTTP 响应
     */
    void exportLogs(LogQueryRequest request, HttpServletResponse response);
}
