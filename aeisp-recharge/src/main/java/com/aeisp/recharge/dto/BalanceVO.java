package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 余额视图对象。
 *
 * <p>用于展示用户当前余额及剩余时长。</p>
 *
 * @author AEISP Team
 */
@Data
public class BalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 余额（分）。
     */
    private Integer balance;

    /**
     * 剩余时长（分钟）。
     */
    private Long remainingDuration;
}
