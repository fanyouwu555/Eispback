package com.aeisp.system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志 Excel 导出视图对象。
 *
 * @author AEISP Team
 */
@Data
public class SysOperationLogExcelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("日志ID")
    private Long id;

    @ExcelProperty("操作人")
    private String operatorUsername;

    @ExcelProperty("操作模块")
    private String operationType;

    @ExcelProperty("操作类型")
    private String operationTypeLabel;

    @ExcelProperty("请求方式")
    private String requestMethod;

    @ExcelProperty("请求URL")
    private String requestUrl;

    @ExcelProperty("IP地址")
    private String ipAddress;

    @ExcelProperty("状态")
    private String statusText;

    @ExcelProperty("执行时长(ms)")
    private Long duration;

    @ExcelProperty("敏感度")
    private String sensitivityText;

    @ExcelProperty("创建时间")
    private String createdAt;

    @ExcelProperty("错误信息")
    private String errorMsg;
}
