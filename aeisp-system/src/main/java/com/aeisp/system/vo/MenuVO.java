package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String permissionName;

    private String permissionCode;

    private String resourceType;

    private String action;

    private String description;

    private Long parentId;

    private Integer menuType;

    private Integer sortOrder;

    private String icon;

    private String routePath;

    private String component;

    private Integer isVisible;

    private Integer isCache;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<MenuVO> children;
}