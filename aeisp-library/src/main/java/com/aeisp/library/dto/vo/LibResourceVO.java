package com.aeisp.library.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LibResourceVO {

    private Long id;
    private String resourceName;
    private String description;
    private LocalDateTime createdAt;
}
