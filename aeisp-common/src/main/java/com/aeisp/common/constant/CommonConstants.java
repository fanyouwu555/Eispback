package com.aeisp.common.constant;

/**
 * 通用常量类。
 *
 * <p>定义项目内通用的常量值，如分页默认值、状态标志等。</p>
 *
 * @author AEISP Team
 */
public final class CommonConstants {

    private CommonConstants() {
        // 工具类禁止实例化
    }

    /**
     * 默认页码。
     */
    public static final long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页大小。
     */
    public static final long DEFAULT_PAGE_SIZE = 20L;

    /**
     * 最大每页大小。
     */
    public static final long MAX_PAGE_SIZE = 500L;

    /**
     * 通用启用状态：0-禁用，1-启用。
     */
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    /**
     * 前端用户账号状态：1-正常，2-禁用，3-冻结，4-锁定。
     */
    public static final int USER_STATUS_NORMAL = 1;
    public static final int USER_STATUS_DISABLED = 2;
    public static final int USER_STATUS_FROZEN = 3;
    public static final int USER_STATUS_LOCKED = 4;

    /**
     * 逻辑删除标志：0-未删除，1-已删除。
     */
    public static final int DELETED_NO = 0;
    public static final int DELETED_YES = 1;

    /**
     * 默认编码。
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * 默认日期时间格式。
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式。
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 前端用户默认角色编码。
     */
    public static final String DEFAULT_FRONTEND_ROLE_CODE = "frontend_user";
}
