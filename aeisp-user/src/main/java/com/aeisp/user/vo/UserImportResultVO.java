package com.aeisp.user.vo;

import lombok.Data;

import java.util.List;

/**
 * Excel 批量导入用户结果。
 *
 * @author AEISP Team
 */
@Data
public class UserImportResultVO {

    /**
     * 总记录数。
     */
    private int total;

    /**
     * 成功数。
     */
    private int successCount;

    /**
     * 失败数。
     */
    private int failCount;

    /**
     * 失败明细。
     */
    private List<FailItem> failList;

    /**
     * 失败行明细。
     */
    @Data
    public static class FailItem {

        /**
         * Excel 行号（从 1 开始）。
         */
        private int rowNum;

        /**
         * 用户名。
         */
        private String username;

        /**
         * 失败原因。
         */
        private String reason;
    }
}
