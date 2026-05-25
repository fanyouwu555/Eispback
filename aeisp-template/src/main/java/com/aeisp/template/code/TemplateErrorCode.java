package com.aeisp.template.code;

import com.aeisp.common.code.ErrorCode;

public enum TemplateErrorCode implements ErrorCode {
    TEMPLATE_NOT_FOUND(4001, "模板不存在"),
    TEMPLATE_NOT_PUBLISHED(4002, "模板未上线"),
    TEMPLATE_VERSION_EXISTS(4003, "版本号已存在"),
    TEMPLATE_ALREADY_OWNED(4004, "您已拥有该模板，无需重复购买"),
    TEMPLATE_PRICE_INVALID(4005, "模板价格异常"),
    TEMPLATE_IS_FREE(4006, "该模板为免费模板，无需购买"),
    VERSION_NOT_FOUND(4011, "版本不存在"),
    VERSION_NOT_BELONG(4012, "版本不存在或不属于该模板"),
    VERSION_IN_USE(4013, "不能删除当前正在使用的版本"),
    VERSION_NUMBER_INVALID(4014, "非法版本号"),
    CATEGORY_NOT_FOUND(4021, "分类不存在"),
    CATEGORY_PARENT_NOT_FOUND(4022, "上级分类不存在"),
    CATEGORY_MAX_LEVEL(4023, "最多支持三级分类"),
    CATEGORY_HAS_CHILDREN(4024, "存在子分类，无法删除"),
    FILE_PATH_INVALID(4031, "非法文件路径"),
    FILE_SAVE_FAILED(4032, "存储 ZIP 文件失败"),
    FILE_READ_FAILED(4033, "读取文件失败");

    private final int code;
    private final String message;

    TemplateErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
