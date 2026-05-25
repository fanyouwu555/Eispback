package com.aeisp.template.service.impl;

import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.exception.BizException;
import com.aeisp.template.code.TemplateErrorCode;
import com.aeisp.template.entity.TplTemplate;
import com.aeisp.template.entity.UserTemplate;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.template.mapper.UserTemplateMapper;
import com.aeisp.template.service.UserTemplateService;
import com.aeisp.user.entity.UsrUserBalance;
import com.aeisp.user.mapper.UsrUserBalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户模板权限 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class UserTemplateServiceImpl implements UserTemplateService {

    private final UserTemplateMapper userTemplateMapper;
    private final TplTemplateMapper templateMapper;
    private final UsrUserBalanceMapper userBalanceMapper;

    @Override
    public boolean hasAccess(Long userId, Long templateId) {
        if (userId == null || templateId == null) {
            return false;
        }
        UserTemplate record = userTemplateMapper.selectValidByUserAndTemplate(userId, templateId);
        return record != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchaseTemplate(Long userId, Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getIsPaid() == null || template.getIsPaid() != 1) {
            throw new BizException(TemplateErrorCode.TEMPLATE_IS_FREE);
        }
        if (template.getPrice() == null || template.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BizException(TemplateErrorCode.TEMPLATE_PRICE_INVALID);
        }

        // 检查是否已购买
        if (hasAccess(userId, templateId)) {
            throw new BizException(TemplateErrorCode.TEMPLATE_ALREADY_OWNED);
        }

        // 价格转分
        int priceCents = template.getPrice().multiply(new java.math.BigDecimal("100")).intValue();

        // 扣减余额
        UsrUserBalance balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null || balance.getBalanceCents() == null || balance.getBalanceCents() < priceCents) {
            throw new BizException(TemplateErrorCode.TEMPLATE_PRICE_INVALID);
        }

        balance.setBalanceCents(balance.getBalanceCents() - priceCents);
        balance.setTotalConsumedCents(balance.getTotalConsumedCents() + priceCents);
        userBalanceMapper.updateById(balance);

        // 记录购买授权
        UserTemplate ut = new UserTemplate();
        ut.setUserId(userId);
        ut.setTemplateId(templateId);
        ut.setAccessType(2);
        ut.setCreatedAt(LocalDateTime.now());
        ut.setUpdatedAt(LocalDateTime.now());
        userTemplateMapper.insert(ut);

        return true;
    }

    @Override
    public void grantFreeAccess(Long userId, Long templateId) {
        if (hasAccess(userId, templateId)) {
            return;
        }
        UserTemplate ut = new UserTemplate();
        ut.setUserId(userId);
        ut.setTemplateId(templateId);
        ut.setAccessType(1);
        ut.setCreatedAt(LocalDateTime.now());
        ut.setUpdatedAt(LocalDateTime.now());
        userTemplateMapper.insert(ut);
    }
}
