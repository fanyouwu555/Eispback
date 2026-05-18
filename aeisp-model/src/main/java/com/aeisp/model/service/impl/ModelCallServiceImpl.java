package com.aeisp.model.service.impl;

import com.aeisp.model.service.ModelCallService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 大模型调用服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class ModelCallServiceImpl implements ModelCallService {

    @Override
    public String callModel(Long modelId, String input, Map<String, Object> params) {
        throw new UnsupportedOperationException("callModel 待实现");
    }

    @Override
    public boolean checkUserPermission(Long userId, Long modelId) {
        throw new UnsupportedOperationException("checkUserPermission 待实现");
    }

    @Override
    public boolean checkDailyLimit(Long userId, Long modelId) {
        throw new UnsupportedOperationException("checkDailyLimit 待实现");
    }
}
