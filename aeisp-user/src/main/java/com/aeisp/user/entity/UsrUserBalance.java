package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户余额表实体。
 *
 * <p>记录每个用户的资金余额，与时长系统相互独立，支持灵活充值和时长兑换。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_user_balance")
public class UsrUserBalance {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，唯一索引。
     */
    private Long userId;

    /**
     * 当前余额，单位为分。
     */
    private Integer balanceCents;

    /**
     * 累计充值金额（分）。
     */
    private Integer totalRechargeCents;

    /**
     * 累计消费金额（分）。
     */
    private Integer totalConsumedCents;

    /**
     * 最后更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
