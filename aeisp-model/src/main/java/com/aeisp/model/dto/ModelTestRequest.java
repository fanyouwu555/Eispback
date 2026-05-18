package com.aeisp.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 模型在线测试请求。
 *
 * <p>用于对指定模型发起在线测试调用。</p>
 *
 * @author AEISP Team
 */
@Data
public class ModelTestRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型 ID。
     */
    private Long modelId;

    /**
     * 测试输入内容。
     */
    private String testInput;
}
