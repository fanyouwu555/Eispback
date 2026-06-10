package com.aeisp.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件树节点 VO。
 *
 * <p>通用文件树结构，用于模板/库资源等模块展示 ZIP 解压后的目录结构。</p>
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
