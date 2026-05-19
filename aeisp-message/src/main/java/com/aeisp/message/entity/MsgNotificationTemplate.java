package com.aeisp.message.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体。
 *
 * <p>对应数据库表 {@code msg_notification_template}，存储可复用的消息通知模板，
 * 支持变量占位符替换。继承 {@link com.aeisp.common.entity.BaseEntity} 获得统一的审计字段。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_notification_template")
public class MsgNotificationTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板编码，全局唯一。
     */
    private String templateCode;

    /**
     * 模板名称。
     */
    private String templateName;

    /**
     * 消息类型枚举值。
     *
     * @see com.aeisp.message.enums.MsgTypeEnum
     */
    private Integer msgType;

    /**
     * 模板标题（支持变量占位符）。
     */
    private String title;

    /**
     * 模板内容（支持变量占位符）。
     */
    private String content;

    /**
     * 适用渠道（JSON 数组：site/email/sms/push）。
     */
    private String channels;

    /**
     * 状态：0-禁用，1-启用。
     */
    private Integer status;

    /**
     * 备注说明。
     */
    private String remark;
}
