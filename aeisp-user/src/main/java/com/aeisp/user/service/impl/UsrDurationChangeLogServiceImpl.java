package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrDurationChangeLog;
import com.aeisp.user.mapper.UsrDurationChangeLogMapper;
import com.aeisp.user.request.DurationChangeLogQueryRequest;
import com.aeisp.user.service.UsrDurationChangeLogService;
import com.aeisp.user.vo.UsrDurationChangeLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 时长变更日志 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class UsrDurationChangeLogServiceImpl implements UsrDurationChangeLogService {

    private final UsrDurationChangeLogMapper usrDurationChangeLogMapper;

    @Override
    public void saveLog(UsrDurationChangeLog log) {
        usrDurationChangeLogMapper.insert(log);
    }

    @Override
    public PageResult<UsrDurationChangeLogVO> listLogs(DurationChangeLogQueryRequest request) {
        LambdaQueryWrapper<UsrDurationChangeLog> wrapper = Wrappers.lambdaQuery();

        if (request.getUserId() != null) {
            wrapper.eq(UsrDurationChangeLog::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getOperationType())) {
            wrapper.eq(UsrDurationChangeLog::getOperationType, request.getOperationType());
        }
        if (request.getOperatorType() != null) {
            wrapper.eq(UsrDurationChangeLog::getOperatorType, request.getOperatorType());
        }
        if (request.getCreatedAtStart() != null) {
            wrapper.ge(UsrDurationChangeLog::getCreatedAt, request.getCreatedAtStart());
        }
        if (request.getCreatedAtEnd() != null) {
            wrapper.le(UsrDurationChangeLog::getCreatedAt, request.getCreatedAtEnd());
        }
        wrapper.orderByDesc(UsrDurationChangeLog::getCreatedAt);

        Page<UsrDurationChangeLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UsrDurationChangeLog> resultPage = usrDurationChangeLogMapper.selectPage(page, wrapper);

        List<UsrDurationChangeLogVO> voList = resultPage.getRecords().stream()
                .map(UsrDurationChangeLogServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    private static UsrDurationChangeLogVO convertToVO(UsrDurationChangeLog log) {
        UsrDurationChangeLogVO vo = new UsrDurationChangeLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setOperationType(log.getOperationType());
        vo.setOperationTypeLabel(resolveOperationTypeLabel(log.getOperationType()));
        vo.setChangeMinutes(log.getChangeMinutes());
        vo.setPreviousRemaining(log.getPreviousRemaining());
        vo.setCurrentRemaining(log.getCurrentRemaining());
        vo.setReason(log.getReason());
        vo.setRelatedOrderId(log.getRelatedOrderId());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorType(log.getOperatorType());
        vo.setOperatorTypeLabel(resolveOperatorTypeLabel(log.getOperatorType()));
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private static String resolveOperationTypeLabel(String operationType) {
        if (operationType == null) {
            return "未知";
        }
        return switch (operationType) {
            case "REGISTER_GRANT" -> "注册赠送";
            case "RECHARGE" -> "充值";
            case "ADMIN_ADD" -> "管理员增加";
            case "ADMIN_SUBTRACT" -> "管理员扣减";
            case "ADMIN_SET" -> "管理员设置";
            case "CONSUME" -> "消费扣减";
            case "REFUND_DEDUCT" -> "退款扣减";
            default -> operationType;
        };
    }

    private static String resolveOperatorTypeLabel(Integer operatorType) {
        if (operatorType == null) {
            return "未知";
        }
        return switch (operatorType) {
            case 1 -> "系统自动";
            case 2 -> "管理员";
            case 3 -> "用户本人";
            default -> "未知";
        };
    }
}
