package com.aeisp.library.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class LibraryQueryRequest {

    private String resourceName;

    private Integer status;

    private List<Long> ids;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
