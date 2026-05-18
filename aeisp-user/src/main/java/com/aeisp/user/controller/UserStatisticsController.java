package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.user.vo.UserStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户数据统计 Controller。
 *
 * <p>提供平台用户统计数据及增长趋势查询。
 * 路径前缀 {@code /api/v1/users/statistics}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/statistics")
@RequiredArgsConstructor
public class UserStatisticsController {

    /**
     * 获取平台用户核心统计数据。
     */
    @GetMapping
    public Result<UserStatisticsVO> getStatistics() {
        throw new UnsupportedOperationException("用户统计功能待实现");
    }

    /**
     * 用户增长趋势。
     *
     * @param period 周期：day / week / month
     * @return 趋势数据列表
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "day") String period) {
        throw new UnsupportedOperationException("用户增长趋势功能待实现");
    }
}
