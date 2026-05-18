package com.aeisp.template.dto.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件树节点 VO。
 *
 * @author AEISP Team
 */
@Data
public class FileNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点名称。
     */
    private String name;

    /**
     * 节点类型：file-文件, dir-目录。
     */
    private String type;

    /**
     * 子节点列表。
     */
    private List<FileNodeVO> children = new ArrayList<>();
}
