package com.aeisp.message.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.message.request.CreateNotificationRequest;
import com.aeisp.message.request.UpdateNotificationRequest;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.service.MsgNotificationService;
import com.aeisp.message.vo.MsgNotificationDetailVO;
import com.aeisp.message.vo.MsgNotificationVO;
import com.aeisp.system.service.SysFeatureSwitchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知管理端 Controller。
 *
 * <p>提供后台管理接口，包括消息的创建、推送、撤回、归档、置顶及查询。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "消息通知管理", description = "后台消息通知管理接口")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MsgNotificationController {

    private final MsgNotificationService msgNotificationService;
    private final SysFeatureSwitchService featureSwitchService;

    /**
     * 创建消息通知（草稿状态）。
     *
     * @param request 创建请求
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:create')")
    @Operation(summary = "创建消息", description = "创建新的消息通知，初始状态为草稿")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody CreateNotificationRequest request) {
        boolean success = msgNotificationService.createNotification(request);
        return Result.success(success);
    }

    /**
     * 执行消息推送。
     *
     * @param id 消息 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:send')")
    @Operation(summary = "执行推送", description = "对草稿状态的消息执行推送")
    @PostMapping("/{id}/push")
    public Result<Boolean> push(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id) {
        if (!featureSwitchService.isEnabled("notify.system")) {
            return Result.error(503, "系统通知推送功能已关闭");
        }
        boolean success = msgNotificationService.pushNotification(id);
        return Result.success(success);
    }

    /**
     * 撤回消息通知。
     *
     * @param id 消息 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:update')")
    @Operation(summary = "撤回消息", description = "撤回已推送的消息")
    @PostMapping("/{id}/revoke")
    public Result<Boolean> revoke(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id) {
        boolean success = msgNotificationService.revokeNotification(id);
        return Result.success(success);
    }

    /**
     * 归档消息通知。
     *
     * @param id 消息 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:update')")
    @Operation(summary = "归档消息", description = "将消息归档")
    @PostMapping("/{id}/archive")
    public Result<Boolean> archive(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id) {
        boolean success = msgNotificationService.archiveNotification(id);
        return Result.success(success);
    }

    /**
     * 置顶或取消置顶消息。
     *
     * @param id    消息 ID
     * @param isTop 是否置顶：0-正常，1-置顶
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:update')")
    @Operation(summary = "置顶/取消置顶", description = "设置消息置顶状态")
    @PostMapping("/{id}/top")
    public Result<Boolean> toggleTop(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id,
            @Parameter(description = "是否置顶：0-正常，1-置顶") @RequestParam("isTop") Integer isTop) {
        boolean success = msgNotificationService.toggleTop(id, isTop);
        return Result.success(success);
    }

    /**
     * 更新消息通知（仅草稿状态可编辑）。
     *
     * @param id      消息 ID
     * @param request 更新请求
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('notification:update')")
    @Operation(summary = "更新消息", description = "更新草稿状态的消息通知")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateNotificationRequest request) {
        boolean success = msgNotificationService.updateNotification(id, request);
        return Result.success(success);
    }

    /**
     * 分页查询消息通知列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "消息列表", description = "分页查询消息通知列表")
    @GetMapping
    public Result<PageResult<MsgNotificationVO>> list(NotificationQueryRequest request) {
        PageResult<MsgNotificationVO> result = msgNotificationService.listNotifications(request);
        return Result.success(result);
    }

    /**
     * 获取消息通知详情。
     *
     * @param id 消息 ID
     * @return 消息详情
     */
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "消息详情", description = "获取消息通知详情")
    @GetMapping("/{id}")
    public Result<MsgNotificationDetailVO> getDetail(
            @Parameter(description = "消息 ID") @PathVariable("id") Long id) {
        MsgNotificationDetailVO detail = msgNotificationService.getDetail(id);
        return Result.success(detail);
    }
}
