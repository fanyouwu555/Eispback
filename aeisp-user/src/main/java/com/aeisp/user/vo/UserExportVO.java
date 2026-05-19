package com.aeisp.user.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 用户列表导出 VO。
 *
 * @author AEISP Team
 */
@Data
public class UserExportVO {

    @ExcelProperty("用户名")
    @ColumnWidth(20)
    private String username;

    @ExcelProperty("昵称")
    @ColumnWidth(20)
    private String nickname;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("注册时间")
    @ColumnWidth(20)
    private String registerTime;

    @ExcelProperty("账号状态")
    @ColumnWidth(12)
    private String statusLabel;

    @ExcelProperty("剩余时长(分钟)")
    @ColumnWidth(16)
    private Integer remainingMinutes;

    @ExcelProperty("累计充值(分)")
    @ColumnWidth(16)
    private Integer totalRechargeCents;

    @ExcelProperty("最后登录时间")
    @ColumnWidth(20)
    private String lastLoginTime;
}
