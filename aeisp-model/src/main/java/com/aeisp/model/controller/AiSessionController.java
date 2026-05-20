package com.aeisp.model.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.model.dto.AiMessageVO;
import com.aeisp.model.dto.AiSessionQueryRequest;
import com.aeisp.model.dto.AiSessionVO;
import com.aeisp.model.service.AiSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 会话控制器。
 */
@Tag(name = "AI会话", description = "AI对话会话管理")
@RestController
@RequestMapping("/api/v1/ai/sessions")
@RequiredArgsConstructor
public class AiSessionController {

    private final AiSessionService aiSessionService;

    @PreAuthorize("hasAuthority('ai:session:read')")
    @Operation(summary = "会话列表")
    @GetMapping
    public Result<PageResult<AiSessionVO>> listSessions(AiSessionQueryRequest request) {
        return Result.success(aiSessionService.listSessions(request));
    }

    @PreAuthorize("hasAuthority('ai:session:read')")
    @Operation(summary = "会话详情")
    @GetMapping("/{id}")
    public Result<AiSessionVO> getSession(@PathVariable Long id) {
        return Result.success(aiSessionService.getSession(id));
    }

    @PreAuthorize("hasAuthority('ai:session:read')")
    @Operation(summary = "消息列表")
    @GetMapping("/{id}/messages")
    public Result<List<AiMessageVO>> listMessages(@PathVariable Long id) {
        return Result.success(aiSessionService.listMessages(id));
    }

    @PreAuthorize("hasAuthority('ai:session:manage')")
    @Operation(summary = "归档会话")
    @PostMapping("/{id}/archive")
    public Result<Boolean> archiveSession(@PathVariable Long id) {
        return Result.success(aiSessionService.archiveSession(id));
    }

    @PreAuthorize("hasAuthority('ai:session:manage')")
    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSession(@PathVariable Long id) {
        return Result.success(aiSessionService.deleteSession(id));
    }
}