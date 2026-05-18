package com.aeisp.user.service;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrDurationLog;
import com.aeisp.user.request.DurationLogQueryRequest;
import com.aeisp.user.vo.UsrDurationLogVO;

/**
 * 用户时长消耗日志 Service 接口。
 *
 * @author AEISP Team
 */
public interface UsrDurationLogService {

    /**
     * 保存时长消耗日志。
     *
     * @param log 日志实体
     */
    void saveLog(UsrDurationLog log);

    /**
     * 分页查询时长消耗日志。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<UsrDurationLogVO> listLogs(DurationLogQueryRequest request);
}
