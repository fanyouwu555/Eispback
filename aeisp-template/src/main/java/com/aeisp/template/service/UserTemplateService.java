package com.aeisp.template.service;

/**
 * 用户模板权限 Service 接口。
 */
public interface UserTemplateService {

    /**
     * 检查用户是否有权下载指定模板。
     *
     * @param userId     用户 ID
     * @param templateId 模板 ID
     * @return true 表示有权下载
     */
    boolean hasAccess(Long userId, Long templateId);

    /**
     * 用户购买模板（一次性付费）。
     *
     * @param userId     用户 ID
     * @param templateId 模板 ID
     * @return 购买结果
     */
    boolean purchaseTemplate(Long userId, Long templateId);

    /**
     * 为用户免费授权模板（首次下载免费模板时自动调用）。
     *
     * @param userId     用户 ID
     * @param templateId 模板 ID
     */
    void grantFreeAccess(Long userId, Long templateId);
}
