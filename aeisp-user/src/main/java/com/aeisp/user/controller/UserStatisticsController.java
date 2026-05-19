package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrLoginLogMapper;
import com.aeisp.user.mapper.UsrUserMapper;
import com.aeisp.user.vo.UserStatisticsVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final UsrUserMapper usrUserMapper;
    private final UsrLoginLogMapper usrLoginLogMapper;

    /**
     * 获取平台用户核心统计数据。
     */
    @GetMapping
    public Result<UserStatisticsVO> getStatistics() {
        UserStatisticsVO vo = new UserStatisticsVO();

        long total = usrUserMapper.selectCount(Wrappers.lambdaQuery());
        vo.setTotalUsers(total);

        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        long todayNew = usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery().ge(UsrUser::getRegisterTime, todayStart));
        vo.setNewUsersToday(todayNew);

        LocalDateTime weekStart = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.MIN);
        long weekNew = usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery().ge(UsrUser::getRegisterTime, weekStart));
        vo.setNewUsersWeek(weekNew);

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        long monthNew = usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery().ge(UsrUser::getRegisterTime, monthStart));
        vo.setNewUsersMonth(monthNew);

        // 状态分布
        vo.setNormalCount(countByStatus(1));
        vo.setDisabledCount(countByStatus(2));
        vo.setFrozenCount(countByStatus(3));
        vo.setLockedCount(countByStatus(4));

        // DAU / WAU / MAU
        LocalDateTime now = LocalDateTime.now();
        vo.setDau(usrLoginLogMapper.countActiveUsers(todayStart, now));
        vo.setWau(usrLoginLogMapper.countActiveUsers(weekStart, now));
        vo.setMau(usrLoginLogMapper.countActiveUsers(monthStart, now));

        return Result.success(vo);
    }

    private Long countByStatus(Integer status) {
        return usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery().eq(UsrUser::getStatus, status));
    }

    /**
     * 用户增长趋势。
     *
     * @param period 周期：day / week / month
     * @return 趋势数据列表
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "day") String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        int points = switch (period) {
            case "week" -> 7;
            case "month" -> 30;
            default -> 7;
        };

        for (int i = points - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).with(LocalTime.MIN);
            LocalDateTime dayEnd = now.minusDays(i).with(LocalTime.MAX);
            long count = usrUserMapper.selectCount(
                    Wrappers.<UsrUser>lambdaQuery()
                            .ge(UsrUser::getRegisterTime, dayStart)
                            .le(UsrUser::getRegisterTime, dayEnd));

            Map<String, Object> item = new HashMap<>();
            item.put("date", dayStart.toLocalDate().toString());
            item.put("count", count);
            result.add(item);
        }

        return Result.success(result);
    }
}
