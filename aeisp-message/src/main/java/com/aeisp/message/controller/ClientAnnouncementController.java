package com.aeisp.message.controller;

import com.aeisp.common.Result;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.message.service.MsgNotificationService;
import com.aeisp.message.vo.ClientAnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Unity 客户端公告 Controller。
 *
 * <p>为 Unity 客户端提供公告列表查询接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/client/announcements")
@RequiredArgsConstructor
@Tag(name = "客户端公告", description = "Unity 客户端公告查询接口")
public class ClientAnnouncementController {

    private final MsgNotificationService msgNotificationService;
    private final SysFeatureSwitchService featureSwitchService;

    /**
     * 获取当前可用的公告列表（已发送且未过期）。
     */
    @GetMapping
    @Operation(summary = "公告列表", description = "获取当前已发送且未过期的公告列表")
    public Result<List<ClientAnnouncementVO>> listAnnouncements(
            @AuthenticationPrincipal CustomUserDetails user) {
        if (!featureSwitchService.isEnabled("notify.announcement")) {
            return Result.success(List.of());
        }
        List<ClientAnnouncementVO> list = msgNotificationService.listClientAnnouncements();
        return Result.success(list);
    }
}