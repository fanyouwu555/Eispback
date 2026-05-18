package com.aeisp.message.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 推送目标 DTO。
 *
 * <p>用于解析 {@code msg_notification.push_target} 字段中的 JSON 数组内容。</p>
 *
 * @author AEISP Team
 */
@Data
public class PushTargetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标 ID 列表。
     */
    private List<Long> ids;
}
