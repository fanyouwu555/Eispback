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
        if (StringUtils.hasText(request.getLoginAccount())) {
            wrapper.like(UsrLoginLog::getLoginAccount, request.getLoginAccount());
        }
        if (request.getLoginType() != null) {
            wrapper.eq(UsrLoginLog::getLoginType, request.getLoginType());
        }
        if (request.getLoginResult() != null) {
            wrapper.eq(UsrLoginLog::getLoginResult, request.getLoginResult());
        }
        if (request.getCreatedAtStart() != null) {
            wrapper.ge(UsrLoginLog::getCreatedAt, request.getCreatedAtStart());
        }
        if (request.getCreatedAtEnd() != null) {
            wrapper.le(UsrLoginLog::getCreatedAt, request.getCreatedAtEnd());
        }
        wrapper.orderByDesc(UsrLoginLog::getCreatedAt);

        Page<UsrLoginLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UsrLoginLog> resultPage = usrLoginLogMapper.selectPage(page, wrapper);

        List<UsrLoginLogVO> voList = resultPage.getRecords().stream()
                .map(UsrLoginLogServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    /**
     * 将实体转换为 VO。
     */
    private static UsrLoginLogVO convertToVO(UsrLoginLog log) {
        UsrLoginLogVO vo = new UsrLoginLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setLoginAccount(log.getLoginAccount());
        vo.setLoginType(log.getLoginType());
        vo.setLoginResult(log.getLoginResult());
        vo.setIpAddress(log.getIpAddress());
        vo.setDeviceType(log.getDeviceType());
        vo.setOsInfo(log.getOsInfo());
        vo.setBrowserInfo(log.getBrowserInfo());
        vo.setDeviceId(log.getDeviceId());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
