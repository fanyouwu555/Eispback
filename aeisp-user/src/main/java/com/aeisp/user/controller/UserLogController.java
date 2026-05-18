package com.aeisp.user.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.user.request.DurationLogQueryRequest;
import com.aeisp.user.request.LogQueryRequest;
import com.aeisp.user.service.UsrDurationLogService;
import com.aeisp.user.service.UsrLoginLogService;
import com.aeisp.user.vo.UsrDurationLogVO;
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
 * <p>提供用户登录日志和时长消耗日志的分页查询。
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
    private final UsrDurationLogService usrDurationLogService;

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
     * 查询指定用户的时长消耗日志。
     */
    @GetMapping("/{userId}/duration-logs")
    public Result<PageResult<UsrDurationLogVO>> listDurationLogs(@PathVariable Long userId,
                                                                 DurationLogQueryRequest request) {
        request.setUserId(userId);
        PageResult<UsrDurationLogVO> result = usrDurationLogService.listLogs(request);
        return Result.success(result);
    }
}
