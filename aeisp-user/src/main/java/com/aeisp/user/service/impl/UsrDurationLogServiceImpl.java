package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrDurationLog;
import com.aeisp.user.mapper.UsrDurationLogMapper;
import com.aeisp.user.request.DurationLogQueryRequest;
import com.aeisp.user.service.UsrDurationLogService;
import com.aeisp.user.vo.UsrDurationLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户时长消耗日志 Service 实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsrDurationLogServiceImpl implements UsrDurationLogService {

    private final UsrDurationLogMapper usrDurationLogMapper;

    @Override
    public void saveLog(UsrDurationLog log) {
        usrDurationLogMapper.insert(log);
    }

    @Override
    public PageResult<UsrDurationLogVO> listLogs(DurationLogQueryRequest request) {
        LambdaQueryWrapper<UsrDurationLog> wrapper = Wrappers.lambdaQuery();

        if (request.getUserId() != null) {
            wrapper.eq(UsrDurationLog::getUserId, request.getUserId());
        }
        if (request.getConsumeType() != null) {
            wrapper.eq(UsrDurationLog::getConsumeType, request.getConsumeType());
        }
        if (request.getConsumeTimeStart() != null) {
            wrapper.ge(UsrDurationLog::getConsumeTime, request.getConsumeTimeStart());
        }
        if (request.getConsumeTimeEnd() != null) {
            wrapper.le(UsrDurationLog::getConsumeTime, request.getConsumeTimeEnd());
        }
        wrapper.orderByDesc(UsrDurationLog::getConsumeTime);

        Page<UsrDurationLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UsrDurationLog> resultPage = usrDurationLogMapper.selectPage(page, wrapper);

        List<UsrDurationLogVO> voList = resultPage.getRecords().stream()
                .map(UsrDurationLogServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    /**
     * 将实体转换为 VO。
     */
    private static UsrDurationLogVO convertToVO(UsrDurationLog log) {
        UsrDurationLogVO vo = new UsrDurationLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setConsumeTime(log.getConsumeTime());
        vo.setConsumeType(log.getConsumeType());
        vo.setConsumeDuration(log.getConsumeDuration());
        vo.setProjectId(log.getProjectId());
        vo.setDescription(log.getDescription());
        return vo;
    }
}
