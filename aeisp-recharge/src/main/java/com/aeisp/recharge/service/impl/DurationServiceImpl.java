package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.DurationConsumeVO;
import com.aeisp.recharge.dto.DurationQueryRequest;
import com.aeisp.recharge.dto.DurationStatVO;
import com.aeisp.recharge.service.DurationService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 时长服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class DurationServiceImpl implements DurationService {

    @Override
    public boolean consumeDuration(Long userId, Long minutes, String consumeType, Long projectId, String description) {
        throw new UnsupportedOperationException("consumeDuration 待实现");
    }

    @Override
    public boolean addDuration(Long userId, Long minutes, String reason) {
        throw new UnsupportedOperationException("addDuration 待实现");
    }

    @Override
    public PageResult<DurationConsumeVO> listConsumes(Long userId, DurationQueryRequest request) {
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
        throw new UnsupportedOperationException("getDurationStats 待实现");
    }
}
