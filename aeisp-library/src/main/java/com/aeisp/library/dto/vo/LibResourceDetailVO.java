package com.aeisp.library.dto.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LibResourceDetailVO extends LibResourceVO {

    private List<LibResourceVersionVO> versionList;
    private List<String> fileTree;
}
