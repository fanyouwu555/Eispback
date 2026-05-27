package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体。
 *
 * <p>对应数据库表 {@code sys_config}，存储系统运行时的动态配置项，支持热更新。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键，全局唯一，如 {@code official.website.url}。
     */
    private String configKey;

    /**
     * 配置值。
     */
    private String configValue;

    /**
     * 配置描述。
     */
    private String description;

    /**
     * 适用环境：all/dev/test/prod。
     */
    private String environment;

    /**
     * 是否可通过后台编辑：0-只读，1-可编辑。
     */
    private Integer isEditable;

    /**
     * 配置分类: storage/backup/platform/timeout/threshold/general
     */
    private String category;

    /**
     * 控件类型: text/boolean/number/password
     */
    private String fieldType;
}
