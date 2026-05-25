package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.code.RechargeErrorCode;
import com.aeisp.recharge.dto.DurationConsumeVO;
import com.aeisp.recharge.dto.DurationQueryRequest;
import com.aeisp.recharge.dto.DurationStatVO;
import com.aeisp.recharge.service.DurationService;
import com.aeisp.user.entity.UsrDurationChangeLog;
import com.aeisp.user.entity.UsrUserDuration;
import com.aeisp.user.mapper.UsrDurationChangeLogMapper;
import com.aeisp.user.mapper.UsrUserDurationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 时长服务实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DurationServiceImpl implements DurationService {

    private final UsrUserDurationMapper userDurationMapper;
    private final UsrDurationChangeLogMapper durationChangeLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeDuration(Long userId, Long minutes, String consumeType, Long projectId, String description) {
        if (minutes == null || minutes <= 0) {
            throw new BizException(RechargeErrorCode.DURATION_CONSUME_INVALID);
        }
        UsrUserDuration duration = userDurationMapper.selectByUserId(userId);
        int previousRemaining = duration != null ? duration.getRemainingMinutes() : 0;
        if (previousRemaining < minutes) {
            throw new BizException(RechargeErrorCode.DURATION_INSUFFICIENT);
        }
        int currentRemaining = (int) (previousRemaining - minutes);

        if (duration == null) {
            duration = new UsrUserDuration();
            duration.setUserId(userId);
            duration.setTotalGrantedMinutes(0);
            duration.setTotalConsumedMinutes((int) (long) minutes);
            duration.setRemainingMinutes(currentRemaining);
            userDurationMapper.insert(duration);
        } else {
            duration.setTotalConsumedMinutes(duration.getTotalConsumedMinutes() + (int) (long) minutes);
            duration.setRemainingMinutes(currentRemaining);
            duration.setLastConsumedAt(LocalDateTime.now());
            userDurationMapper.updateById(duration);
        }

        UsrDurationChangeLog changeLog = new UsrDurationChangeLog();
        changeLog.setUserId(userId);
        changeLog.setOperationType("CONSUME");
        changeLog.setChangeMinutes(-(int) (long) minutes);
        changeLog.setPreviousRemaining(previousRemaining);
        changeLog.setCurrentRemaining(currentRemaining);
        changeLog.setReason(description);
        changeLog.setRelatedOrderId(projectId != null ? projectId.toString() : null);
        changeLog.setOperatorType(1); // 系统自动
        changeLog.setCreatedAt(LocalDateTime.now());
        durationChangeLogMapper.insert(changeLog);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDuration(Long userId, Long minutes, String reason) {
        if (minutes == null || minutes <= 0) {
            throw new BizException(RechargeErrorCode.DURATION_ADD_INVALID);
        }
        UsrUserDuration duration = userDurationMapper.selectByUserId(userId);
        int previousRemaining = duration != null ? duration.getRemainingMinutes() : 0;
        int currentRemaining = previousRemaining + (int) (long) minutes;

        if (duration == null) {
            duration = new UsrUserDuration();
            duration.setUserId(userId);
            duration.setTotalGrantedMinutes((int) (long) minutes);
            duration.setTotalConsumedMinutes(0);
            duration.setRemainingMinutes(currentRemaining);
            userDurationMapper.insert(duration);
        } else {
            duration.setTotalGrantedMinutes(duration.getTotalGrantedMinutes() + (int) (long) minutes);
            duration.setRemainingMinutes(currentRemaining);
            userDurationMapper.updateById(duration);
        }

        UsrDurationChangeLog changeLog = new UsrDurationChangeLog();
        changeLog.setUserId(userId);
        changeLog.setOperationType("ADMIN_ADD");
        changeLog.setChangeMinutes((int) (long) minutes);
        changeLog.setPreviousRemaining(previousRemaining);
        changeLog.setCurrentRemaining(currentRemaining);
        changeLog.setReason(reason);
        changeLog.setOperatorType(1); // 系统自动
        changeLog.setCreatedAt(LocalDateTime.now());
        durationChangeLogMapper.insert(changeLog);

        return true;
    }

    @Override
    public PageResult<DurationConsumeVO> listConsumes(Long userId, DurationQueryRequest request) {
        // 时长消耗记录查询保持空实现，后续根据业务需要完善
        return PageResult.<DurationConsumeVO>builder()
                .list(Collections.emptyList())
                .total(0L)
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .totalPages(0L)
                .build();
    }

    @Override
    public DurationStatVO getDurationStats(Long userId) {
        UsrUserDuration duration = userDurationMapper.selectByUserId(userId);
        DurationStatVO vo = new DurationStatVO();
        vo.setUserId(userId);
        if (duration != null) {
            vo.setTotalRecharged((long) duration.getTotalGrantedMinutes());
            vo.setTotalConsumed((long) duration.getTotalConsumedMinutes());
            vo.setRemainingDuration((long) duration.getRemainingMinutes());
        } else {
            vo.setTotalRecharged(0L);
            vo.setTotalConsumed(0L);
            vo.setRemainingDuration(0L);
        }
        vo.setCurrentMonthConsumed(0L);
        vo.setTodayConsumed(0L);
        return vo;
    }
}
