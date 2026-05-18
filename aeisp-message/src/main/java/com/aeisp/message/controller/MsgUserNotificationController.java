package com.aeisp.message.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.service.MsgUserNotificationService;
import com.aeisp.message.vo.UserNotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户消息通知 Controller。
 *
 * <p>提供前端用户视角的消息接口，包括消息列表、未读统计、已读标记等。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "用户消息", description = "前端用户消息通知接口")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MsgUserNotificationController {

    private final MsgUserNotificationService msgUserNotificationService;

    /**
     * 获取当前登录用户 ID。
     *
     * <p>从 SecurityContext 中提取用户 ID，若未登录则返回 null。</p>
     *
     * @return 用户 ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            try {
                java.lang.reflect.Method getUserIdMethod = principal.getClass().getMethod("getUserId");
                Object userId = getUserIdMethod.invoke(principal);
                return userId instanceof Long ? (Long) userId : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 查询当前用户的消息列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "我的消息列表", description = "查询当前登录用户的消息列表")
    @GetMapping("/my")
    public Result<PageResult<UserNotificationVO>> listMyNotifications(NotificationQueryRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        PageResult<UserNotificationVO> result = msgUserNotificationService.listUserNotifications(userId, request);
        return Result.success(result);
    }

    /**
     * 获取当前用户未读消息数量。
     *
     * @return 未读消息数
     */
    @Operation(summary = "未读消息数", description = "获取当前登录用户的未读消息数量")
    @GetMapping("/my/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        Long count = msgUserNotificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条消息为已读。
     *
     * @param id 消息通知 ID
     * @return 操作结果
     */
    @Operation(summary = "标记已读", description = "将单条消息标记为已读")
    @PostMapping("/my/{id}/read")
    public Result<Boolean> markAsRead(
            @Parameter(description = "消息通知 ID") @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        boolean success = msgUserNotificationService.markAsRead(userId, id);
        return Result.success(success);
    }

    /**
     * 标记所有消息为已读。
     *
     * @return 操作结果
     */
    @Operation(summary = "全部已读", description = "将当前用户所有消息标记为已读")
    @PostMapping("/my/read-all")
    public Result<Boolean> markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        boolean success = msgUserNotificationService.markAllAsRead(userId);
        return Result.success(success);
    }
}
