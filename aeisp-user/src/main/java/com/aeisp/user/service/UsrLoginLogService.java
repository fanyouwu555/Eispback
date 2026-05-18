package com.aeisp.user.service;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrLoginLog;
import com.aeisp.user.request.LogQueryRequest;
import com.aeisp.user.vo.UsrLoginLogVO;

/**
 * 用户登录日志 Service 接口。
 *
 * @author AEISP Team
 */
public interface UsrLoginLogService {

    /**
     * 保存登录日志。
     *
     * @param log 日志实体
     */
    void saveLog(UsrLoginLog log);

    /**
     * 分页查询登录日志。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<UsrLoginLogVO> listLogs(LogQueryRequest request);
}
