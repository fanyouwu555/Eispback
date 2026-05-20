package com.aeisp.model.service;

import com.aeisp.common.PageResult;
import com.aeisp.model.dto.AiMessageVO;
import com.aeisp.model.dto.AiSessionQueryRequest;
import com.aeisp.model.dto.AiSessionVO;

import java.util.List;

/**
 * AI 会话服务接口。
 */
public interface AiSessionService {

    PageResult<AiSessionVO> listSessions(AiSessionQueryRequest request);

    AiSessionVO getSession(Long id);

    List<AiMessageVO> listMessages(Long sessionId);

    Boolean deleteSession(Long id);

    Boolean archiveSession(Long id);
}