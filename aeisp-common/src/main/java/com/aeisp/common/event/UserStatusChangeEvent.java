package com.aeisp.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户状态变更事件。
 *
 * <p>当管理员修改前端用户账号状态时发布，供消息模块监听并发送通知。</p>
 *
 * @author AEISP Team
 */
@Getter
public class UserStatusChangeEvent extends ApplicationEvent {

    /**
     * 受影响的用户 ID。
     */
    private final Long userId;

    /**
     * 变更后的状态。
     */
    private final Integer newStatus;

    /**
     * 变更原因。
     */
    private final String reason;

    public UserStatusChangeEvent(Object source, Long userId, Integer newStatus, String reason) {
        super(source);
        this.userId = userId;
        this.newStatus = newStatus;
        this.reason = reason;
    }
}
