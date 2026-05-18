package com.aeisp.message.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 推送目标用户 VO。
 *
 * <p>用于展示消息推送的目标用户信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class PushTargetUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 是否已读：0-未读，1-已读。
     */
    private Integer isRead;
}
