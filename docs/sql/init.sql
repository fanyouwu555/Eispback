-- ========================================================
-- AEISP 后端管理系统 — 数据库初始化脚本
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- ========================================================

CREATE DATABASE IF NOT EXISTS aeisp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aeisp;

-- --------------------------------------------------------
-- 1. 系统基础模块 (aeisp-system)
-- --------------------------------------------------------

-- 后台管理员表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    username VARCHAR(32) NOT NULL COMMENT '登录用户名',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码',
    real_name VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(64) DEFAULT NULL COMMENT '电子邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员账号表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    role_name VARCHAR(32) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(32) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code),
    KEY idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 权限点表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：user/model/template/order/notification/system',
    action VARCHAR(20) NOT NULL COMMENT '操作类型：create/read/update/delete/manage',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code),
    KEY idx_sys_permission_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限点表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    permission_id BIGINT NOT NULL COMMENT '权限 ID',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作人 ID',
    operator_username VARCHAR(32) DEFAULT NULL COMMENT '操作人用户名（冗余存储）',
    operation_type VARCHAR(30) DEFAULT NULL COMMENT '操作类型编码，如 UPDATE_USER_STATUS',
    operation_type_label VARCHAR(50) DEFAULT NULL COMMENT '操作类型中文标签',
    target_type VARCHAR(20) DEFAULT NULL COMMENT '操作目标类型：user/model/template/order/notification/system',
    target_id VARCHAR(36) DEFAULT NULL COMMENT '操作目标ID',
    operation_detail JSON DEFAULT NULL COMMENT '操作详情JSON，包含变更前后的数据快照',
    request_method VARCHAR(10) DEFAULT NULL COMMENT 'HTTP 请求方法',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求 URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数（JSON）',
    response_data TEXT DEFAULT NULL COMMENT '响应数据（JSON）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    error_msg VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT '操作人IP地址',
    duration BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    sensitivity TINYINT NOT NULL DEFAULT 1 COMMENT '敏感度：1-普通，2-敏感（涉及资金/权限变更）',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_operation_log_user_id (user_id),
    KEY idx_sys_operation_log_operation_type (operation_type),
    KEY idx_sys_operation_log_target (target_type, target_id),
    KEY idx_sys_operation_log_created_at (created_at),
    KEY idx_sys_operation_log_sensitivity (sensitivity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    config_key VARCHAR(64) NOT NULL COMMENT '配置键',
    config_value TEXT DEFAULT NULL COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    environment VARCHAR(10) NOT NULL DEFAULT 'all' COMMENT '适用环境：all/dev/test/prod',
    is_editable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可通过后台编辑：0-只读，1-可编辑',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_key (config_key),
    KEY idx_sys_config_environment (environment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 用户行为日志表
CREATE TABLE IF NOT EXISTS sys_user_behavior_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    behavior_type VARCHAR(30) NOT NULL COMMENT '行为类型：LOGIN/LOGOUT/CREATE_PROJECT/MODEL_CALL/RECHARGE/等',
    behavior_detail JSON DEFAULT NULL COMMENT '行为详情JSON',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT '用户IP',
    device_info JSON DEFAULT NULL COMMENT '设备信息',
    created_at DATETIME DEFAULT NULL COMMENT '行为时间',
    PRIMARY KEY (id),
    KEY idx_sys_user_behavior_log_user_id (user_id),
    KEY idx_sys_user_behavior_log_behavior_type (behavior_type),
    KEY idx_sys_user_behavior_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';

-- 异常报错日志表
CREATE TABLE IF NOT EXISTS sys_error_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    error_type VARCHAR(50) NOT NULL COMMENT '异常类型',
    error_message TEXT NOT NULL COMMENT '异常信息',
    stack_trace TEXT DEFAULT NULL COMMENT '错误堆栈',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    request_params JSON DEFAULT NULL COMMENT '请求参数',
    user_id BIGINT DEFAULT NULL COMMENT '受影响用户ID',
    module VARCHAR(30) DEFAULT NULL COMMENT '发生异常的模块',
    severity TINYINT NOT NULL DEFAULT 2 COMMENT '严重程度：1-低，2-中，3-高，4-紧急',
    created_at DATETIME DEFAULT NULL COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_sys_error_log_error_type (error_type),
    KEY idx_sys_error_log_severity (severity),
    KEY idx_sys_error_log_module (module),
    KEY idx_sys_error_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常报错日志表';

-- --------------------------------------------------------
-- 2. 用户管理模块 (aeisp-user)
-- --------------------------------------------------------

-- 前端用户表
CREATE TABLE IF NOT EXISTS usr_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名，全局唯一，只允许字母、数字、下划线，首位必须为字母',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号，全局唯一，11位数字',
    email VARCHAR(64) DEFAULT NULL COMMENT '邮箱，全局唯一',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '用户昵称，展示用',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像图片URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常，2-禁用，3-冻结，4-锁定',
    status_reason VARCHAR(200) DEFAULT NULL COMMENT '状态变更原因说明',
    locked_until DATETIME DEFAULT NULL COMMENT '账号锁定到期时间，仅在锁定状态时有效',
    need_change_password TINYINT NOT NULL DEFAULT 0 COMMENT '下次登录是否需要修改密码：0-否，1-是',
    failed_login_attempts TINYINT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数，登录成功后清零',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP地址（支持IPv6）',
    register_ip VARCHAR(64) NOT NULL COMMENT '注册时IP地址',
    register_device_info JSON DEFAULT NULL COMMENT '注册设备信息JSON，包含设备类型、操作系统、浏览器、设备ID',
    register_time DATETIME DEFAULT NULL COMMENT '注册时间',
    invitation_code_used VARCHAR(10) DEFAULT NULL COMMENT '注册时使用的邀请码',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_username (username),
    UNIQUE KEY uk_usr_user_phone (phone),
    UNIQUE KEY uk_usr_user_email (email),
    KEY idx_usr_user_status (status),
    KEY idx_usr_user_created_at (created_at),
    KEY idx_usr_user_last_login (last_login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前端用户表';

-- 用户登录日志表
CREATE TABLE IF NOT EXISTS usr_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID，登录失败时可能为NULL',
    login_account VARCHAR(50) NOT NULL COMMENT '登录时使用的账号（用户名/手机号/邮箱）',
    login_type TINYINT NOT NULL COMMENT '登录类型：1-密码登录，2-验证码登录，3-Token刷新',
    login_result TINYINT NOT NULL COMMENT '结果：1-成功，2-密码错误，3-账号不存在，4-账号禁用，5-账号冻结，6-账号锁定，7-验证码错误，8-Token过期',
    ip_address VARCHAR(64) NOT NULL COMMENT '登录IP',
    device_type VARCHAR(20) DEFAULT NULL COMMENT '设备类型：desktop/mobile/tablet/unknown',
    os_info VARCHAR(50) DEFAULT NULL COMMENT '操作系统信息',
    browser_info VARCHAR(100) DEFAULT NULL COMMENT '浏览器信息',
    device_id VARCHAR(64) DEFAULT NULL COMMENT '设备唯一标识',
    created_at DATETIME DEFAULT NULL COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_usr_login_log_user_id (user_id),
    KEY idx_usr_login_log_created_at (created_at),
    KEY idx_usr_login_log_ip (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- 用户角色关联表（前端用户）
CREATE TABLE IF NOT EXISTS usr_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID，关联 sys_role.id',
    granted_by BIGINT DEFAULT NULL COMMENT '授权人管理员ID',
    granted_at DATETIME DEFAULT NULL COMMENT '授权时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_role (user_id, role_id),
    KEY idx_usr_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前端用户角色关联表';

-- 用户时长表
CREATE TABLE IF NOT EXISTS usr_user_duration (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户ID，外键关联 usr_user.id，唯一索引',
    total_granted_minutes INT NOT NULL DEFAULT 0 COMMENT '累计获得时长（分钟），历史累计不减少',
    total_consumed_minutes INT NOT NULL DEFAULT 0 COMMENT '累计消耗时长（分钟）',
    remaining_minutes INT NOT NULL DEFAULT 0 COMMENT '当前剩余可用时长（分钟）',
    expiry_date DATE DEFAULT NULL COMMENT '当前批次时长的过期日期，NULL表示无固定过期时间',
    warning_threshold INT NOT NULL DEFAULT 120 COMMENT '时长预警阈值（分钟），低于此值触发预警通知',
    last_consumed_at DATETIME DEFAULT NULL COMMENT '最后一次消耗时间',
    updated_at DATETIME DEFAULT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_duration_user_id (user_id),
    KEY idx_usr_user_duration_remaining (remaining_minutes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户时长表';

-- 用户余额表
CREATE TABLE IF NOT EXISTS usr_user_balance (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户ID，唯一索引',
    balance_cents INT NOT NULL DEFAULT 0 COMMENT '当前余额，单位为分',
    total_recharge_cents INT NOT NULL DEFAULT 0 COMMENT '累计充值金额（分）',
    total_consumed_cents INT NOT NULL DEFAULT 0 COMMENT '累计消费金额（分）',
    updated_at DATETIME DEFAULT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_balance_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户余额表';

-- 时长变更日志表
CREATE TABLE IF NOT EXISTS usr_duration_change_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '关联用户',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：REGISTER_GRANT(注册赠送), RECHARGE(充值), ADMIN_ADD(管理员增加), ADMIN_SUBTRACT(管理员扣减), ADMIN_SET(管理员设定), CONSUME(使用消耗), REFUND_DEDUCT(退款扣减)',
    change_minutes INT NOT NULL COMMENT '变更时长（分钟），正数表示增加，负数表示扣减',
    previous_remaining INT NOT NULL COMMENT '变更前剩余时长',
    current_remaining INT NOT NULL COMMENT '变更后剩余时长',
    reason VARCHAR(200) NOT NULL COMMENT '变更原因说明',
    related_order_id VARCHAR(36) DEFAULT NULL COMMENT '关联订单ID，充值类操作时填写',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID，系统自动操作时NULL',
    operator_type TINYINT NOT NULL COMMENT '操作者类型：1-系统自动，2-管理员，3-用户本人',
    created_at DATETIME DEFAULT NULL COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_usr_duration_change_log_user_id (user_id),
    KEY idx_usr_duration_change_log_operation_type (operation_type),
    KEY idx_usr_duration_change_log_created_at (created_at),
    KEY idx_usr_duration_change_log_order_id (related_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时长变更日志表';

-- 验证码发送记录表
CREATE TABLE IF NOT EXISTS usr_verification_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    target VARCHAR(100) NOT NULL COMMENT '发送目标（手机号或邮箱）',
    send_type TINYINT NOT NULL COMMENT '类型：1-短信，2-邮件',
    scene VARCHAR(20) NOT NULL COMMENT '场景：register/reset_password/bind/login',
    verification_code VARCHAR(10) NOT NULL COMMENT '发送的验证码内容（明文存储，短期有效）',
    verify_result TINYINT DEFAULT NULL COMMENT '验证结果：1-验证成功，2-验证失败，NULL-未验证',
    ip_address VARCHAR(64) NOT NULL COMMENT '请求IP',
    expired_at DATETIME NOT NULL COMMENT '验证码过期时间',
    created_at DATETIME DEFAULT NULL COMMENT '发送时间',
    PRIMARY KEY (id),
    KEY idx_usr_verification_record_target_scene (target, scene),
    KEY idx_usr_verification_record_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码发送记录表';

-- --------------------------------------------------------
-- 3. 通知消息模块 (aeisp-message)
-- --------------------------------------------------------

-- 消息通知主表
CREATE TABLE IF NOT EXISTS msg_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    title VARCHAR(128) NOT NULL COMMENT '消息标题',
    content TEXT DEFAULT NULL COMMENT '消息内容（富文本）',
    msg_type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型',
    push_scope TINYINT NOT NULL DEFAULT 1 COMMENT '推送范围：1-全员，2-指定用户，3-指定角色',
    push_target TEXT DEFAULT NULL COMMENT '推送目标（JSON 数组）',
    push_type TINYINT NOT NULL DEFAULT 1 COMMENT '推送方式：1-立即，2-定时',
    push_time DATETIME DEFAULT NULL COMMENT '定时推送时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-草稿，2-已发送，3-已撤回，4-定时发送',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-正常，1-置顶',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '已读人数',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '推送总人数',
    priority TINYINT DEFAULT 2 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    channels JSON DEFAULT NULL COMMENT '推送渠道（JSON 数组：site/email/sms/push）',
    target_user_ids JSON DEFAULT NULL COMMENT '目标用户 ID 列表（JSON 数组）',
    target_group_ids JSON DEFAULT NULL COMMENT '目标分组 ID 列表（JSON 数组）',
    need_read_confirmation TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要阅读确认：0-否，1-是',
    target_count INT DEFAULT 0 COMMENT '目标人数',
    success_count INT DEFAULT 0 COMMENT '发送成功人数',
    failed_count INT DEFAULT 0 COMMENT '发送失败人数',
    sent_at DATETIME DEFAULT NULL COMMENT '发送时间',
    revoked_at DATETIME DEFAULT NULL COMMENT '撤回时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_msg_notification_status (status),
    KEY idx_msg_notification_push_time (push_time),
    KEY idx_msg_notification_priority (priority),
    KEY idx_msg_notification_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知主表';

-- 用户消息关联表
CREATE TABLE IF NOT EXISTS msg_user_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    notification_id BIGINT NOT NULL COMMENT '消息通知 ID',
    read_status TINYINT NOT NULL DEFAULT 1 COMMENT '阅读状态：1-未读，2-已读，3-已撤回',
    channel_received JSON DEFAULT NULL COMMENT '已接收渠道（JSON 数组：site/email/sms/push）',
    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间（推送时间）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_user_notification (user_id, notification_id),
    KEY idx_msg_user_notification_notification_id (notification_id),
    KEY idx_msg_user_notification_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息关联表';

-- 通知发送日志表
CREATE TABLE IF NOT EXISTS msg_notification_send_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    notification_id BIGINT NOT NULL COMMENT '消息通知 ID',
    user_id BIGINT NOT NULL COMMENT '目标用户 ID',
    channel VARCHAR(20) NOT NULL COMMENT '发送渠道：site-站内，email-邮件，sms-短信，push-推送',
    status TINYINT NOT NULL DEFAULT 3 COMMENT '发送状态：1-成功，2-失败，3-待发送',
    request_content TEXT DEFAULT NULL COMMENT '发送请求内容（JSON）',
    response_content TEXT DEFAULT NULL COMMENT '发送响应内容（JSON）',
    error_msg VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    sent_at DATETIME DEFAULT NULL COMMENT '发送时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_msg_notification_send_log_notification_id (notification_id),
    KEY idx_msg_notification_send_log_user_id (user_id),
    KEY idx_msg_notification_send_log_channel (channel),
    KEY idx_msg_notification_send_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送日志表';

-- 通知模板表
CREATE TABLE IF NOT EXISTS msg_notification_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码，全局唯一',
    template_name VARCHAR(64) NOT NULL COMMENT '模板名称',
    msg_type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型',
    title VARCHAR(128) DEFAULT NULL COMMENT '模板标题（支持变量占位符）',
    content TEXT DEFAULT NULL COMMENT '模板内容（支持变量占位符）',
    channels JSON DEFAULT NULL COMMENT '适用渠道（JSON 数组：site/email/sms/push）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_notification_template_code (template_code),
    KEY idx_msg_notification_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- --------------------------------------------------------
-- 4. 模板管理模块 (aeisp-template)
-- --------------------------------------------------------

-- 模板主表
CREATE TABLE IF NOT EXISTS tpl_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_code VARCHAR(30) DEFAULT NULL COMMENT '模板唯一编码',
    template_name VARCHAR(64) NOT NULL COMMENT '模板名称',
    scenario VARCHAR(32) DEFAULT NULL COMMENT '适用场景：teaching-教学, competition-竞赛, practice-实战',
    description VARCHAR(512) DEFAULT NULL COMMENT '模板简介',
    preview_image VARCHAR(255) DEFAULT NULL COMMENT '预览图片 URL',
    sort_weight INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-上架，2-下架',
    current_version_id BIGINT DEFAULT NULL COMMENT '当前版本 ID',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
    project_count INT NOT NULL DEFAULT 0 COMMENT '累计项目数',
    optimization_flag TINYINT NOT NULL DEFAULT 0 COMMENT '优化标记：0-正常，1-需要优化',
    storage_path VARCHAR(255) DEFAULT NULL COMMENT '存储路径',
    difficulty INT DEFAULT NULL COMMENT '难度系数',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tpl_template_code (template_code),
    KEY idx_tpl_template_status (status),
    KEY idx_tpl_template_scenario (scenario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板主表';

-- 模板版本表
CREATE TABLE IF NOT EXISTS tpl_template_version (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_id BIGINT NOT NULL COMMENT '关联模板 ID',
    version_no VARCHAR(16) NOT NULL COMMENT '版本号',
    file_path VARCHAR(255) DEFAULT NULL COMMENT 'ZIP 文件存储路径',
    storage_url VARCHAR(255) DEFAULT NULL COMMENT '存储 URL',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    file_hash VARCHAR(64) DEFAULT NULL COMMENT '文件哈希值',
    is_major_update TINYINT NOT NULL DEFAULT 0 COMMENT '是否重大更新：0-否，1-是',
    config_content TEXT DEFAULT NULL COMMENT '配置文件内容（JSON）',
    changelog TEXT DEFAULT NULL COMMENT '更新日志',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tpl_template_version (template_id, version_no),
    KEY idx_tpl_template_version_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板版本表';

-- 模板预览图片表
CREATE TABLE IF NOT EXISTS tpl_template_preview_image (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_id BIGINT NOT NULL COMMENT '关联模板 ID',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '预览图片 URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_tpl_template_preview_image_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板预览图片表';

-- --------------------------------------------------------
-- 4. 模型管理模块 (aeisp-model)
-- --------------------------------------------------------

-- AI 模型配置表
CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    model_name VARCHAR(50) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(30) NOT NULL COMMENT '模型类型',
    api_endpoint VARCHAR(255) NOT NULL COMMENT '接口地址',
    api_key VARCHAR(255) NOT NULL COMMENT '访问密钥（AES 加密存储）',
    weight INT NOT NULL DEFAULT 1 COMMENT '调用权重',
    max_qps INT NOT NULL DEFAULT 10 COMMENT '最大 QPS 限制',
    scenario_tags JSON DEFAULT NULL COMMENT '适配场景标签（JSON 数组）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '优先级排序',
    maintain_window VARCHAR(50) DEFAULT NULL COMMENT '维护时间窗口',
    default_params JSON DEFAULT NULL COMMENT '默认参数（JSON）',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '累计调用次数',
    failure_rate INT NOT NULL DEFAULT 0 COMMENT '失败率（%）',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_ai_model_status (status),
    KEY idx_ai_model_type (model_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 大模型配置表';

-- 模型调用日志表
CREATE TABLE IF NOT EXISTS model_call_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    model_id BIGINT NOT NULL COMMENT '模型 ID',
    request_params JSON DEFAULT NULL COMMENT '请求参数（JSON）',
    response_summary VARCHAR(500) DEFAULT NULL COMMENT '响应摘要',
    tokens_used INT DEFAULT NULL COMMENT '消耗 Token 数',
    duration_ms INT DEFAULT NULL COMMENT '调用耗时（毫秒）',
    cost_cents INT DEFAULT NULL COMMENT '费用（分）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-成功，2-失败',
    error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    ip_address VARCHAR(40) DEFAULT NULL COMMENT '调用者 IP',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_model_call_log_user_id (user_id),
    KEY idx_model_call_log_model_id (model_id),
    KEY idx_model_call_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型调用日志表';

-- --------------------------------------------------------
-- 5. 充值模块 (aeisp-recharge)
-- --------------------------------------------------------

-- 时长套餐表
CREATE TABLE IF NOT EXISTS duration_package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    package_name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    price_cents INT NOT NULL DEFAULT 0 COMMENT '价格（分）',
    duration_minutes BIGINT NOT NULL DEFAULT 0 COMMENT '对应时长（分钟）',
    valid_days INT DEFAULT NULL COMMENT '有效期（天）',
    promotion VARCHAR(255) DEFAULT NULL COMMENT '优惠活动描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-上架，2-下架',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_duration_package_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时长套餐表';

-- 充值订单表
CREATE TABLE IF NOT EXISTS recharge_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    package_id BIGINT DEFAULT NULL COMMENT '关联套餐 ID',
    amount_cents INT NOT NULL DEFAULT 0 COMMENT '充值金额（分）',
    duration_minutes BIGINT NOT NULL DEFAULT 0 COMMENT '到账时长（分钟）',
    pay_type VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待支付，2-已支付，3-已退款，4-已取消',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    refund_reason VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recharge_order_no (order_no),
    KEY idx_recharge_order_user_id (user_id),
    KEY idx_recharge_order_status (status),
    KEY idx_recharge_order_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值订单表';

-- 余额变更日志表
CREATE TABLE IF NOT EXISTS usr_balance_change_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    change_type TINYINT NOT NULL COMMENT '变更类型：1-充值，2-消费，3-退款，4-管理员调整',
    change_cents INT NOT NULL COMMENT '变更金额（分），正数增加，负数扣减',
    previous_balance INT NOT NULL COMMENT '变更前余额（分）',
    current_balance INT NOT NULL COMMENT '变更后余额（分）',
    related_order_id BIGINT DEFAULT NULL COMMENT '关联订单 ID',
    reason VARCHAR(200) DEFAULT NULL COMMENT '变更原因',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人 ID',
    operator_type TINYINT NOT NULL DEFAULT 1 COMMENT '操作者类型：1-系统自动，2-管理员，3-用户本人',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_usr_balance_change_log_user_id (user_id),
    KEY idx_usr_balance_change_log_type (change_type),
    KEY idx_usr_balance_change_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额变更日志表';

-- --------------------------------------------------------
-- 6. 初始化数据
-- --------------------------------------------------------

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_code, description, status, created_at, updated_at) VALUES
(1, '超级管理员', 'ROLE_SUPER_ADMIN', '系统最高权限，拥有所有操作权限', 1, NOW(), NOW()),
(2, '用户管理员', 'ROLE_USER_MANAGER', '负责用户账号的全生命周期管理', 1, NOW(), NOW()),
(3, '模型管理员', 'ROLE_MODEL_MANAGER', '负责AI模型的接入、配置、测试、状态管理', 1, NOW(), NOW()),
(4, '消息管理员', 'ROLE_NOTIFICATION_MANAGER', '负责平台消息的内容编辑、推送管理、撤回和效果监控', 1, NOW(), NOW()),
(5, '模板管理员', 'ROLE_TEMPLATE_MANAGER', '负责编程模板的上传、版本管理、上下线控制和统计分析', 1, NOW(), NOW()),
(6, '财务管理员', 'ROLE_FINANCE_MANAGER', '负责充值套餐配置、订单管理、退款审核和余额调整', 1, NOW(), NOW()),
(7, '普通用户', 'ROLE_USER', '平台注册用户默认角色', 1, NOW(), NOW());

-- 初始化超级管理员账号（密码明文：admin123，BCrypt 加密后）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'admin@aeisp.com', '13800138000', 1, NOW(), NOW());

-- 关联超级管理员与角色
INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES
(1, 1, NOW());

-- 初始化常用系统配置
INSERT INTO sys_config (config_key, config_value, environment, description, created_at, updated_at) VALUES
('system.name', 'AEISP 后端管理系统', 'all', '系统名称', NOW(), NOW()),
('system.version', '1.0.0', 'all', '系统版本号', NOW(), NOW()),
('system.copyright', '© 2026 AEISP Team', 'all', '系统版权信息', NOW(), NOW()),
('official_website_url', 'https://www.example.com', 'prod', '生产环境官网地址', NOW(), NOW()),
('official_website_url', 'https://test.example.com', 'test', '测试环境官网地址', NOW(), NOW()),
('contact_email', 'contact@example.com', 'all', '联系邮箱', NOW(), NOW()),
('contact_phone', '400-123-4567', 'all', '客服电话', NOW(), NOW()),
('default_register_duration', '120', 'all', '注册默认赠送时长（分钟）', NOW(), NOW()),
('default_duration_expiry_days', '90', 'all', '赠送时长有效期（天）', NOW(), NOW()),
('duration_warning_threshold', '120', 'all', '时长预警阈值（分钟）', NOW(), NOW()),
('login_max_fail_attempts', '5', 'all', '登录最大失败次数', NOW(), NOW()),
('login_lockout_minutes', '30', 'all', '登录锁定持续时间（分钟）', NOW(), NOW()),
('password_min_length', '8', 'all', '密码最小长度', NOW(), NOW()),
('password_max_length', '20', 'all', '密码最大长度', NOW(), NOW()),
('verify_code_ttl_seconds', '300', 'all', '验证码有效期（秒）', NOW(), NOW()),
('verify_code_cooldown_seconds', '60', 'all', '验证码发送冷却时间（秒）', NOW(), NOW()),
('default_model_max_qps', '10', 'all', '默认模型QPS限制', NOW(), NOW()),
('model_failure_rate_threshold', '10', 'all', '模型失败率告警阈值（%）', NOW(), NOW()),
('notification_archive_days', '90', 'all', '消息归档天数', NOW(), NOW()),
('recharge_order_timeout_minutes', '30', 'all', '充值订单超时时间（分钟）', NOW(), NOW());
