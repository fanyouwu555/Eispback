package com.aeisp.model.service;

import java.util.Map;

/**
 * 大模型调用服务接口。
 *
 * <p>提供模型调用、用户权限校验及日调用上限检查能力。</p>
 *
 * @author AEISP Team
 */
public interface ModelCallService {

    /**
     * 调用指定模型。
     *
     * @param modelId 模型 ID
     * @param input   输入内容
     * @param params  扩展参数
     * @return 模型返回结果（JSON 字符串）
     */
    String callModel(Long modelId, String input, Map<String, Object> params);

    /**
     * 检查用户是否具备调用指定模型的权限。
     *
     * @param userId  用户 ID
     * @param modelId 模型 ID
     * @return 是否有权限
     */
    boolean checkUserPermission(Long userId, Long modelId);

    /**
     * 检查用户当日调用是否已达上限。
     *
     * @param userId  用户 ID
     * @param modelId 模型 ID
     * @return 是否未达上限
     */
    boolean checkDailyLimit(Long userId, Long modelId);
}
