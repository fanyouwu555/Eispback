package com.aeisp.model.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.model.code.ModelErrorCode;
import com.aeisp.model.dto.AiMessageVO;
import com.aeisp.model.dto.AiSessionQueryRequest;
import com.aeisp.model.dto.AiSessionVO;
import com.aeisp.model.entity.AiMessage;
import com.aeisp.model.entity.AiSession;
import com.aeisp.model.mapper.AiMessageMapper;
import com.aeisp.model.mapper.AiSessionMapper;
import com.aeisp.model.service.AiSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 会话服务实现。
 */
@Service
@RequiredArgsConstructor
public class AiSessionServiceImpl implements AiSessionService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;

    @Override
    public PageResult<AiSessionVO> listSessions(AiSessionQueryRequest request) {
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getDeleted, 0);
        if (request.getUserId() != null) {
            wrapper.eq(AiSession::getUserId, request.getUserId());
        }
        if (request.getModelId() != null) {
            wrapper.eq(AiSession::getModelId, request.getModelId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(AiSession::getStatus, request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.like(AiSession::getSessionTitle, request.getKeyword());
        }
        wrapper.orderByDesc(AiSession::getCreatedAt);

        Page<AiSession> page = aiSessionMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);

        List<AiSessionVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page, list);
    }

    @Override
    public AiSessionVO getSession(Long id) {
        AiSession entity = aiSessionMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BizException(ModelErrorCode.SESSION_NOT_FOUND);
        }
        return toVO(entity);
    }

    @Override
    public List<AiMessageVO> listMessages(Long sessionId) {
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getCreatedAt);
        return aiMessageMapper.selectList(wrapper).stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSession(Long id) {
        AiSession entity = aiSessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ModelErrorCode.SESSION_NOT_FOUND);
        }
        entity.setDeleted(1);
        aiSessionMapper.updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean archiveSession(Long id) {
        AiSession entity = aiSessionMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ModelErrorCode.SESSION_NOT_FOUND);
        }
        entity.setStatus(2);
        aiSessionMapper.updateById(entity);
        return true;
    }

    private AiSessionVO toVO(AiSession entity) {
        AiSessionVO vo = new AiSessionVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusLabel(entity.getStatus() != null && entity.getStatus() == 1 ? "进行中" : "已归档");
        return vo;
    }

    private AiMessageVO toMessageVO(AiMessage entity) {
        AiMessageVO vo = new AiMessageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}