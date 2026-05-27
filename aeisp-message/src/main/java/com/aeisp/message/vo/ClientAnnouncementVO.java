package com.aeisp.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Unity 客户端公告列表项 VO。
 *
 * <p>返回给 Unity 客户端的公告信息，包含标题、内容、图片 URL 等。</p>
 *
 * @author AEISP Team
 */
@Data
public class ClientAnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    /**
     * 公告内容（纯文本，含 [图片:URL] 占位符）。
     */
    private String content;

    /**
     * 公告摘要/简介。
     */
    private String summary;

    /**
     * 从 content 中提取的图片 URL 列表。
     */
    private List<String> imageUrls;

    /**
     * 推送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}