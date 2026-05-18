package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置 VO。
 *
 * <p>用于返回系统配置的详细信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class SysConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID。
     */
    private Long id;

    /**
     * 配置键。
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
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
