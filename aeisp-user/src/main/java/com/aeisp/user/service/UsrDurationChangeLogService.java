package com.aeisp.user.service;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrDurationChangeLog;
import com.aeisp.user.request.DurationChangeLogQueryRequest;
import com.aeisp.user.vo.UsrDurationChangeLogVO;

/**
 * 时长变更日志 Service 接口。
 *
 * @author AEISP Team
 */
public interface UsrDurationChangeLogService {

    /**
     * 保存日志。
     *
     * @param log 日志实体
     */
    void saveLog(UsrDurationChangeLog log);

    /**
     * 分页查询日志。
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<UsrDurationChangeLogVO> listLogs(DurationChangeLogQueryRequest request);
}
