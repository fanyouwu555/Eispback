package com.aeisp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型调用日志实体。
 *
 * <p>记录每次模型调用的详细信息，用于用量统计和故障排查。
 * 不继承 {@link com.aeisp.common.entity.BaseEntity}，使用独立轻量结构。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("model_call_log")
public class ModelCallLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 模型 ID。
     */
    private Long modelId;

    /**
     * 请求参数（JSON）。
     */
    private String requestParams;

    /**
     * 响应摘要。
     */
    private String responseSummary;

    /**
     * 消耗 Token 数。
     */
    private Integer tokensUsed;

    /**
     * 调用耗时（毫秒）。
     */
    private Integer durationMs;

    /**
     * 费用（分）。
     */
    private Integer costCents;

    /**
     * 状态：1-成功，2-失败。
     */
    private Integer status;

    /**
     * 错误信息。
     */
    private String errorMsg;

    /**
     * 调用者 IP。
     */
    private String ipAddress;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
