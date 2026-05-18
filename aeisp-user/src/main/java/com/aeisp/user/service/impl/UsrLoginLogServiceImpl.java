package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrLoginLog;
import com.aeisp.user.mapper.UsrLoginLogMapper;
import com.aeisp.user.request.LogQueryRequest;
import com.aeisp.user.service.UsrLoginLogService;
import com.aeisp.user.vo.UsrLoginLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户登录日志 Service 实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsrLoginLogServiceImpl implements UsrLoginLogService {

    private final UsrLoginLogMapper usrLoginLogMapper;

    @Override
    public void saveLog(UsrLoginLog log) {
        usrLoginLogMapper.insert(log);
    }

    @Override
    public PageResult<UsrLoginLogVO> listLogs(LogQueryRequest request) {
        LambdaQueryWrapper<UsrLoginLog> wrapper = Wrappers.lambdaQuery();

        if (request.getUserId() != null) {
            wrapper.eq(UsrLoginLog::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(UsrLoginLog::getUsername, request.getUsername());
        }
        if (request.getResult() != null) {
            wrapper.eq(UsrLoginLog::getResult, request.getResult());
        }
        if (request.getLoginTimeStart() != null) {
            wrapper.ge(UsrLoginLog::getLoginTime, request.getLoginTimeStart());
        }
        if (request.getLoginTimeEnd() != null) {
            wrapper.le(UsrLoginLog::getLoginTime, request.getLoginTimeEnd());
        }
        wrapper.orderByDesc(UsrLoginLog::getLoginTime);

        Page<UsrLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UsrLoginLog> resultPage = usrLoginLogMapper.selectPage(page, wrapper);

        List<UsrLoginLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    /**
     * 将实体转换为 VO。
     */
    private UsrLoginLogVO convertToVO(UsrLoginLog log) {
        UsrLoginLogVO vo = new UsrLoginLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setLoginTime(log.getLoginTime());
        vo.setIp(log.getIp());
        vo.setDevice(log.getDevice());
        vo.setResult(log.getResult());
        vo.setErrorMsg(log.getErrorMsg());
        return vo;
    }
}
