package com.aeisp.message.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 执行推送请求。
 *
 * <p>用于后台管理端对草稿状态的消息执行推送操作。</p>
 *
 * @author AEISP Team
 */
@Data
public class PushNotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息通知 ID。
     */
    @NotNull(message = "消息 ID 不能为空")
    private Long notificationId;
}
