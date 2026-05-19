package com.aeisp.user.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.user.request.DurationChangeLogQueryRequest;
import com.aeisp.user.request.LogQueryRequest;
import com.aeisp.user.service.UsrDurationChangeLogService;
import com.aeisp.user.service.UsrLoginLogService;
import com.aeisp.user.vo.UsrDurationChangeLogVO;
import com.aeisp.user.vo.UsrLoginLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户日志查询 Controller。
 *
 * <p>提供用户登录日志和时长变更日志的分页查询。
 * 路径前缀 {@code /api/v1/users}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserLogController {

    private final UsrLoginLogService usrLoginLogService;
    private final UsrDurationChangeLogService usrDurationChangeLogService;

    /**
     * 查询指定用户的登录日志。
     */
    @GetMapping("/{userId}/login-logs")
    public Result<PageResult<UsrLoginLogVO>> listLoginLogs(@PathVariable Long userId,
                                                           LogQueryRequest request) {
        request.setUserId(userId);
        PageResult<UsrLoginLogVO> result = usrLoginLogService.listLogs(request);
        return Result.success(result);
    }

    /**
     * 查询指定用户的时长变更日志。
     */
    @GetMapping("/{userId}/duration-logs")
    public Result<PageResult<UsrDurationChangeLogVO>> listDurationLogs(@PathVariable Long userId,
                                                                       DurationChangeLogQueryRequest request) {
        request.setUserId(userId);
        PageResult<UsrDurationChangeLogVO> result = usrDurationChangeLogService.listLogs(request);
        return Result.success(result);
    }
}
