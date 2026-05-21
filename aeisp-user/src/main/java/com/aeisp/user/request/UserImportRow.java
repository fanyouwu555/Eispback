package com.aeisp.user.request;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Excel 导入用户行数据。
 *
 * @author AEISP Team
 */
@Data
public class UserImportRow {

    /**
     * 用户名。
     */
    @ExcelProperty("用户名")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$", message = "用户名格式不正确")
    private String username;

    /**
     * 初始密码。
     */
    @ExcelProperty("初始密码")
    @NotBlank(message = "初始密码不能为空")
    private String password;

    /**
     * 角色名称（可选，多个用逗号分隔）。
     */
    @ExcelProperty("角色")
    private String roleNames;

    /**
     * 是否比赛用户：0-否，1-是。
     */
    @ExcelProperty("比赛用户")
    private Integer isCompetition;

    /**
     * 备注（可选）。
     */
    @ExcelProperty("备注")
    private String remark;
}
