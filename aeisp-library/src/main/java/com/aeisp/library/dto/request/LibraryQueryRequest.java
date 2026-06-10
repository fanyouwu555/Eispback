package com.aeisp.library.dto.request;

import lombok.Data;

@Data
public class LibraryQueryRequest {

    private String resourceName;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
