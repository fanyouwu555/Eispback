-- ========================================================n-- AEISP 后端管理系统 — 数据库初始化脚本（PostgreSQL 版）n-- 数据库：PostgreSQL 14+n-- 字符集：UTF8n-- ========================================================nn-- 创建数据库（请在 psql 或 pgAdmin 中执行，或连接后执行）n-- CREATE DATABASE aeisp WITH ENCODING = 'UTF8' LC_COLLATE = 'zh_CN.UTF-8' LC_CTYPE = 'zh_CN.UTF-8' TEMPLATE = template0;n-- \c aeisp;nn-- --------------------------------------------------------n-- 1. 系统基础模块 (aeisp-system)n-- --------------------------------------------------------nn-- 后台管理员表nCREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(32) DEFAULT NULL,
    email VARCHAR(64) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_email UNIQUE (email),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user (status);
COMMENT ON TABLE sys_user IS '后台管理员账号表';
COMMENT ON COLUMN sys_user.id IS '主键 ID';
COMMENT ON COLUMN sys_user.username IS '登录用户名';
COMMENT ON COLUMN sys_user.password IS 'BCrypt 加密密码';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.email IS '电子邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号码';
COMMENT ON COLUMN sys_user.status IS '状态：0-禁用，1-正常';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';
COMMENT ON COLUMN sys_user.created_by IS '创建人 ID';
COMMENT ON COLUMN sys_user.updated_by IS '更新人 ID';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除：0-未删除，1-已删除';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(32) NOT NULL,
    role_code VARCHAR(32) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    is_system SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role (status);
CREATE INDEX IF NOT EXISTS idx_sys_role_is_system ON sys_role (is_system);
COMMENT ON TABLE sys_role IS '系统角色表';
COMMENT ON COLUMN sys_role.is_system IS '是否系统内置角色：0-自定义，1-系统内置（不可删除）';

-- 权限点表（支持菜单树结构）
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(64) DEFAULT NULL,
    resource_type VARCHAR(20) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    parent_id BIGINT DEFAULT 0,
    menu_type SMALLINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    icon VARCHAR(100) DEFAULT NULL,
    route_path VARCHAR(200) DEFAULT NULL,
    component VARCHAR(255) DEFAULT NULL,
    is_visible SMALLINT DEFAULT 1,
    is_cache SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_permission_code UNIQUE (permission_code)
);
CREATE INDEX IF NOT EXISTS idx_sys_permission_resource_type ON sys_permission (resource_type);
CREATE INDEX IF NOT EXISTS idx_sys_permission_parent_id ON sys_permission (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_permission_menu_type ON sys_permission (menu_type);
COMMENT ON TABLE sys_permission IS '系统权限点表（菜单树）';
COMMENT ON COLUMN sys_permission.resource_type IS '资源类型：user/model/template/order/notification/system';
COMMENT ON COLUMN sys_permission.action IS '操作类型：create/read/update/delete/manage/list/send/config/test';
COMMENT ON COLUMN sys_permission.parent_id IS '父节点 ID，0 表示根节点';
COMMENT ON COLUMN sys_permission.menu_type IS '类型：0-目录，1-菜单，2-按钮';
COMMENT ON COLUMN sys_permission.sort_order IS '排序值（同级排序）';
COMMENT ON COLUMN sys_permission.icon IS '图标（仅目录/菜单）';
COMMENT ON COLUMN sys_permission.route_path IS '路由路径（仅目录/菜单）';
COMMENT ON COLUMN sys_permission.component IS '组件路径（仅菜单）';
COMMENT ON COLUMN sys_permission.is_visible IS '是否可见：0-隐藏，1-可见';
COMMENT ON COLUMN sys_permission.is_cache IS '是否缓存：0-不缓存，1-缓存';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role (role_id);
COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_permission_id ON sys_role_permission (permission_id);
COMMENT ON TABLE sys_role_permission IS '角色权限关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    operator_username VARCHAR(32) DEFAULT NULL,
    role_name VARCHAR(50) DEFAULT NULL,
    operation_type VARCHAR(30) DEFAULT NULL,
    operation_type_label VARCHAR(50) DEFAULT NULL,
    target_type VARCHAR(20) DEFAULT NULL,
    target_id VARCHAR(36) DEFAULT NULL,
    operation_detail JSONB DEFAULT NULL,
    request_method VARCHAR(10) DEFAULT NULL,
    request_url VARCHAR(255) DEFAULT NULL,
    request_params TEXT DEFAULT NULL,
    response_data TEXT DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    error_msg VARCHAR(512) DEFAULT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    duration BIGINT DEFAULT NULL,
    sensitivity SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_user_id ON sys_operation_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operation_type ON sys_operation_log (operation_type);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_target ON sys_operation_log (target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_created_at ON sys_operation_log (created_at);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_sensitivity ON sys_operation_log (sensitivity);
COMMENT ON TABLE sys_operation_log IS '系统操作日志表';
COMMENT ON COLUMN sys_operation_log.role_name IS '操作人角色名';
COMMENT ON COLUMN sys_operation_log.operation_type IS '操作类型编码，如 UPDATE_USER_STATUS';
COMMENT ON COLUMN sys_operation_log.operation_type_label IS '操作类型中文标签';
COMMENT ON COLUMN sys_operation_log.target_type IS '操作目标类型：user/model/template/order/notification/system';
COMMENT ON COLUMN sys_operation_log.target_id IS '操作目标ID';
COMMENT ON COLUMN sys_operation_log.operation_detail IS '操作详情JSON，包含变更前后的数据快照';
COMMENT ON COLUMN sys_operation_log.sensitivity IS '敏感度：1-普通，2-敏感（涉及资金/权限变更）';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL,
    config_value TEXT DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    environment VARCHAR(10) NOT NULL DEFAULT 'all',
    is_editable SMALLINT NOT NULL DEFAULT 1,
    category VARCHAR(20) NOT NULL DEFAULT 'general',
    field_type VARCHAR(20) NOT NULL DEFAULT 'text',
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);
CREATE INDEX IF NOT EXISTS idx_sys_config_environment ON sys_config (environment);
CREATE INDEX IF NOT EXISTS idx_sys_config_category ON sys_config (category);
COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.environment IS '适用环境：all/dev/test/prod';
COMMENT ON COLUMN sys_config.is_editable IS '是否可通过后台编辑：0-只读，1-可编辑';
COMMENT ON COLUMN sys_config.category IS '配置分类: storage/backup/platform/timeout/threshold/general';
COMMENT ON COLUMN sys_config.field_type IS '控件类型: text/boolean/number/password';

-- 用户行为日志表
CREATE TABLE IF NOT EXISTS sys_user_behavior_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    behavior_type VARCHAR(30) NOT NULL,
    behavior_detail JSONB DEFAULT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    device_info JSONB DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_user_behavior_log_user_id ON sys_user_behavior_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_behavior_log_behavior_type ON sys_user_behavior_log (behavior_type);
CREATE INDEX IF NOT EXISTS idx_sys_user_behavior_log_created_at ON sys_user_behavior_log (created_at);
COMMENT ON TABLE sys_user_behavior_log IS '用户行为日志表';

-- 异常报错日志表
CREATE TABLE IF NOT EXISTS sys_error_log (
    id BIGSERIAL PRIMARY KEY,
    error_type VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT DEFAULT NULL,
    request_url VARCHAR(255) DEFAULT NULL,
    request_params JSONB DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    module VARCHAR(30) DEFAULT NULL,
    severity SMALLINT NOT NULL DEFAULT 2,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_error_log_error_type ON sys_error_log (error_type);
CREATE INDEX IF NOT EXISTS idx_sys_error_log_severity ON sys_error_log (severity);
CREATE INDEX IF NOT EXISTS idx_sys_error_log_module ON sys_error_log (module);
CREATE INDEX IF NOT EXISTS idx_sys_error_log_created_at ON sys_error_log (created_at);
COMMENT ON TABLE sys_error_log IS '异常报错日志表';
COMMENT ON COLUMN sys_error_log.severity IS '严重程度：1-低，2-中，3-高，4-紧急';

-- --------------------------------------------------------
-- 2. 用户管理模块 (aeisp-user)
-- --------------------------------------------------------

-- 前端用户表
CREATE TABLE IF NOT EXISTS usr_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(64) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    status_reason VARCHAR(200) DEFAULT NULL,
    locked_until TIMESTAMP DEFAULT NULL,
    need_change_password SMALLINT NOT NULL DEFAULT 0,
    failed_login_attempts SMALLINT NOT NULL DEFAULT 0,
    last_login_time TIMESTAMP DEFAULT NULL,
    last_login_ip VARCHAR(64) DEFAULT NULL,
    register_ip VARCHAR(64) NOT NULL,
    register_device_info JSONB DEFAULT NULL,
    register_time TIMESTAMP DEFAULT NULL,
    invitation_code_used VARCHAR(10) DEFAULT NULL,
    login_count INT NOT NULL DEFAULT 0,
    abnormal_login SMALLINT NOT NULL DEFAULT 0,
    is_competition SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_usr_user_username UNIQUE (username),
    CONSTRAINT uk_usr_user_phone UNIQUE (phone),
    CONSTRAINT uk_usr_user_email UNIQUE (email)
);
CREATE INDEX IF NOT EXISTS idx_usr_user_status ON usr_user (status);
CREATE INDEX IF NOT EXISTS idx_usr_user_created_at ON usr_user (created_at);
CREATE INDEX IF NOT EXISTS idx_usr_user_last_login ON usr_user (last_login_time);
COMMENT ON TABLE usr_user IS '前端用户表';
COMMENT ON COLUMN usr_user.username IS '用户名，全局唯一，只允许字母、数字、下划线，首位必须为字母';
COMMENT ON COLUMN usr_user.phone IS '手机号，全局唯一，11位数字';
COMMENT ON COLUMN usr_user.nickname IS '用户昵称，展示用';
COMMENT ON COLUMN usr_user.avatar_url IS '头像图片URL';
COMMENT ON COLUMN usr_user.status IS '账号状态：1-正常，2-禁用，3-冻结，4-锁定';
COMMENT ON COLUMN usr_user.status_reason IS '状态变更原因说明';
COMMENT ON COLUMN usr_user.locked_until IS '账号锁定到期时间，仅在锁定状态时有效';
COMMENT ON COLUMN usr_user.need_change_password IS '下次登录是否需要修改密码：0-否，1-是';
COMMENT ON COLUMN usr_user.failed_login_attempts IS '连续登录失败次数，登录成功后清零';
COMMENT ON COLUMN usr_user.last_login_ip IS '最后登录IP地址（支持IPv6）';
COMMENT ON COLUMN usr_user.register_ip IS '注册时IP地址';
COMMENT ON COLUMN usr_user.register_device_info IS '注册设备信息JSON，包含设备类型、操作系统、浏览器、设备ID';
COMMENT ON COLUMN usr_user.invitation_code_used IS '注册时使用的邀请码';

-- 用户登录日志表
CREATE TABLE IF NOT EXISTS usr_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    login_account VARCHAR(50) NOT NULL,
    login_type SMALLINT NOT NULL,
    login_result SMALLINT NOT NULL,
    ip_address VARCHAR(64) NOT NULL,
    device_type VARCHAR(20) DEFAULT NULL,
    os_info VARCHAR(50) DEFAULT NULL,
    browser_info VARCHAR(100) DEFAULT NULL,
    device_id VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_login_log_user_id ON usr_login_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_login_log_created_at ON usr_login_log (created_at);
CREATE INDEX IF NOT EXISTS idx_usr_login_log_ip ON usr_login_log (ip_address);
COMMENT ON TABLE usr_login_log IS '用户登录日志表';
COMMENT ON COLUMN usr_login_log.login_account IS '登录时使用的账号（用户名/手机号/邮箱）';
COMMENT ON COLUMN usr_login_log.login_type IS '登录类型：1-密码登录，2-验证码登录，3-Token刷新';
COMMENT ON COLUMN usr_login_log.login_result IS '结果：1-成功，2-密码错误，3-账号不存在，4-账号禁用，5-账号冻结，6-账号锁定，7-验证码错误，8-Token过期';
COMMENT ON COLUMN usr_login_log.device_type IS '设备类型：desktop/mobile/tablet/unknown';
COMMENT ON COLUMN usr_login_log.os_info IS '操作系统信息';
COMMENT ON COLUMN usr_login_log.browser_info IS '浏览器信息';
COMMENT ON COLUMN usr_login_log.device_id IS '设备唯一标识';

-- 用户角色关联表（前端用户）
CREATE TABLE IF NOT EXISTS usr_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_by BIGINT DEFAULT NULL,
    granted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_usr_user_role UNIQUE (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_usr_user_role_role_id ON usr_user_role (role_id);
COMMENT ON TABLE usr_user_role IS '前端用户角色关联表';

-- 用户业务权限表
CREATE TABLE IF NOT EXISTS usr_user_permission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    perm_key VARCHAR(100) NOT NULL,
    perm_value VARCHAR(500) NOT NULL DEFAULT '',
    effective_at TIMESTAMP DEFAULT NULL,
    expire_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_usr_user_perm UNIQUE (user_id, perm_key)
);
CREATE INDEX IF NOT EXISTS idx_usr_user_permission_user_id ON usr_user_permission (user_id);
COMMENT ON TABLE usr_user_permission IS '用户业务权限表';

-- 用户权限变更日志表
CREATE TABLE IF NOT EXISTS usr_user_permission_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    perm_key VARCHAR(100) DEFAULT NULL,
    perm_key_label VARCHAR(100) DEFAULT NULL,
    old_value VARCHAR(500) DEFAULT NULL,
    new_value VARCHAR(500) DEFAULT NULL,
    operation_type VARCHAR(20) NOT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_name VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_user_perm_log_user_id ON usr_user_permission_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_user_perm_log_created ON usr_user_permission_log (created_at);
COMMENT ON TABLE usr_user_permission_log IS '用户权限变更日志表';

-- 模板使用记录表
CREATE TABLE IF NOT EXISTS usr_template_usage_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    template_name VARCHAR(100) DEFAULT NULL,
    version_no VARCHAR(50) DEFAULT NULL,
    action_type VARCHAR(20) NOT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_tpl_usage_user_id ON usr_template_usage_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_tpl_usage_created ON usr_template_usage_log (created_at);
COMMENT ON TABLE usr_template_usage_log IS '模板使用记录表';

-- 用户时长表
CREATE TABLE IF NOT EXISTS usr_user_duration (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_granted_minutes INT NOT NULL DEFAULT 0,
    total_consumed_minutes INT NOT NULL DEFAULT 0,
    remaining_minutes INT NOT NULL DEFAULT 0,
    expiry_date DATE DEFAULT NULL,
    warning_threshold INT NOT NULL DEFAULT 120,
    last_consumed_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_usr_user_duration_user_id UNIQUE (user_id)
);
CREATE INDEX IF NOT EXISTS idx_usr_user_duration_remaining ON usr_user_duration (remaining_minutes);
COMMENT ON TABLE usr_user_duration IS '用户时长表';

-- 用户余额表
CREATE TABLE IF NOT EXISTS usr_user_balance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance_cents INT NOT NULL DEFAULT 0,
    total_recharge_cents INT NOT NULL DEFAULT 0,
    total_consumed_cents INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_usr_user_balance_user_id UNIQUE (user_id)
);
COMMENT ON TABLE usr_user_balance IS '用户余额表';

-- 时长变更日志表
CREATE TABLE IF NOT EXISTS usr_duration_change_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    change_minutes INT NOT NULL,
    previous_remaining INT NOT NULL,
    current_remaining INT NOT NULL,
    reason VARCHAR(200) NOT NULL,
    related_order_id VARCHAR(36) DEFAULT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_type SMALLINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_duration_change_log_user_id ON usr_duration_change_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_duration_change_log_operation_type ON usr_duration_change_log (operation_type);
CREATE INDEX IF NOT EXISTS idx_usr_duration_change_log_created_at ON usr_duration_change_log (created_at);
CREATE INDEX IF NOT EXISTS idx_usr_duration_change_log_order_id ON usr_duration_change_log (related_order_id);
COMMENT ON TABLE usr_duration_change_log IS '时长变更日志表';

-- 验证码发送记录表
CREATE TABLE IF NOT EXISTS usr_verification_record (
    id BIGSERIAL PRIMARY KEY,
    target VARCHAR(100) NOT NULL,
    send_type SMALLINT NOT NULL,
    scene VARCHAR(20) NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    verify_result SMALLINT DEFAULT NULL,
    ip_address VARCHAR(64) NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_verification_record_target_scene ON usr_verification_record (target, scene);
CREATE INDEX IF NOT EXISTS idx_usr_verification_record_created_at ON usr_verification_record (created_at);
COMMENT ON TABLE usr_verification_record IS '验证码发送记录表';

-- --------------------------------------------------------
-- 3. 通知消息模块 (aeisp-message)
-- --------------------------------------------------------

-- 消息通知主表
CREATE TABLE IF NOT EXISTS msg_notification (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    content TEXT DEFAULT NULL,
    msg_type SMALLINT NOT NULL DEFAULT 1,
    push_scope SMALLINT NOT NULL DEFAULT 1,
    push_target TEXT DEFAULT NULL,
    push_type SMALLINT NOT NULL DEFAULT 1,
    push_time TIMESTAMP DEFAULT NULL,
    expire_time TIMESTAMP DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    is_top SMALLINT NOT NULL DEFAULT 0,
    read_count BIGINT NOT NULL DEFAULT 0,
    total_count BIGINT NOT NULL DEFAULT 0,
    priority SMALLINT DEFAULT 2,
    channels JSONB DEFAULT NULL,
    target_user_ids JSONB DEFAULT NULL,
    target_group_ids JSONB DEFAULT NULL,
    need_read_confirmation SMALLINT NOT NULL DEFAULT 0,
    target_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    sent_at TIMESTAMP DEFAULT NULL,
    revoked_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_msg_notification_status ON msg_notification (status);
CREATE INDEX IF NOT EXISTS idx_msg_notification_push_time ON msg_notification (push_time);
CREATE INDEX IF NOT EXISTS idx_msg_notification_priority ON msg_notification (priority);
CREATE INDEX IF NOT EXISTS idx_msg_notification_sent_at ON msg_notification (sent_at);
COMMENT ON TABLE msg_notification IS '消息通知主表';
COMMENT ON COLUMN msg_notification.status IS '状态：1-草稿，2-已发送，3-已撤回，4-定时发送';
COMMENT ON COLUMN msg_notification.priority IS '优先级：1-低，2-中，3-高，4-紧急';
COMMENT ON COLUMN msg_notification.channels IS '推送渠道（JSON 数组：site/email/sms/push）';
COMMENT ON COLUMN msg_notification.target_user_ids IS '目标用户 ID 列表（JSON 数组）';
COMMENT ON COLUMN msg_notification.target_group_ids IS '目标分组 ID 列表（JSON 数组）';
COMMENT ON COLUMN msg_notification.need_read_confirmation IS '是否需要阅读确认：0-否，1-是';
COMMENT ON COLUMN msg_notification.target_count IS '目标人数';
COMMENT ON COLUMN msg_notification.success_count IS '发送成功人数';
COMMENT ON COLUMN msg_notification.failed_count IS '发送失败人数';
COMMENT ON COLUMN msg_notification.sent_at IS '发送时间';
COMMENT ON COLUMN msg_notification.revoked_at IS '撤回时间';

-- 用户消息关联表
CREATE TABLE IF NOT EXISTS msg_user_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    read_status SMALLINT NOT NULL DEFAULT 1,
    channel_received JSONB DEFAULT NULL,
    read_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_msg_user_notification UNIQUE (user_id, notification_id)
);
CREATE INDEX IF NOT EXISTS idx_msg_user_notification_notification_id ON msg_user_notification (notification_id);
CREATE INDEX IF NOT EXISTS idx_msg_user_notification_read_status ON msg_user_notification (read_status);
COMMENT ON TABLE msg_user_notification IS '用户消息关联表';
COMMENT ON COLUMN msg_user_notification.read_status IS '阅读状态：1-未读，2-已读，3-已撤回';
COMMENT ON COLUMN msg_user_notification.channel_received IS '已接收渠道（JSON 数组：site/email/sms/push）';
COMMENT ON COLUMN msg_user_notification.read_at IS '阅读时间';

-- 通知发送日志表
CREATE TABLE IF NOT EXISTS msg_notification_send_log (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 3,
    request_content TEXT DEFAULT NULL,
    response_content TEXT DEFAULT NULL,
    error_msg VARCHAR(512) DEFAULT NULL,
    sent_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_msg_notification_send_log_notification_id ON msg_notification_send_log (notification_id);
CREATE INDEX IF NOT EXISTS idx_msg_notification_send_log_user_id ON msg_notification_send_log (user_id);
CREATE INDEX IF NOT EXISTS idx_msg_notification_send_log_channel ON msg_notification_send_log (channel);
CREATE INDEX IF NOT EXISTS idx_msg_notification_send_log_status ON msg_notification_send_log (status);
COMMENT ON TABLE msg_notification_send_log IS '通知发送日志表';
COMMENT ON COLUMN msg_notification_send_log.notification_id IS '消息通知 ID';
COMMENT ON COLUMN msg_notification_send_log.user_id IS '目标用户 ID';
COMMENT ON COLUMN msg_notification_send_log.channel IS '发送渠道：site-站内，email-邮件，sms-短信，push-推送';
COMMENT ON COLUMN msg_notification_send_log.status IS '发送状态：1-成功，2-失败，3-待发送';
COMMENT ON COLUMN msg_notification_send_log.request_content IS '发送请求内容（JSON）';
COMMENT ON COLUMN msg_notification_send_log.response_content IS '发送响应内容（JSON）';
COMMENT ON COLUMN msg_notification_send_log.error_msg IS '错误信息';
COMMENT ON COLUMN msg_notification_send_log.sent_at IS '发送时间';

-- 通知模板表
CREATE TABLE IF NOT EXISTS msg_notification_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(64) NOT NULL,
    msg_type SMALLINT NOT NULL DEFAULT 1,
    title VARCHAR(128) DEFAULT NULL,
    content TEXT DEFAULT NULL,
    channels JSONB DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_msg_notification_template_code UNIQUE (template_code)
);
CREATE INDEX IF NOT EXISTS idx_msg_notification_template_status ON msg_notification_template (status);
COMMENT ON TABLE msg_notification_template IS '通知模板表';
COMMENT ON COLUMN msg_notification_template.template_code IS '模板编码，全局唯一';
COMMENT ON COLUMN msg_notification_template.template_name IS '模板名称';
COMMENT ON COLUMN msg_notification_template.msg_type IS '消息类型';
COMMENT ON COLUMN msg_notification_template.title IS '模板标题（支持变量占位符）';
COMMENT ON COLUMN msg_notification_template.content IS '模板内容（支持变量占位符）';
COMMENT ON COLUMN msg_notification_template.channels IS '适用渠道（JSON 数组：site/email/sms/push）';
COMMENT ON COLUMN msg_notification_template.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN msg_notification_template.remark IS '备注说明';

-- --------------------------------------------------------
-- 4. 模板管理模块 (aeisp-template)
-- --------------------------------------------------------

-- 模板主表
CREATE TABLE IF NOT EXISTS tpl_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(30) DEFAULT NULL,
    template_name VARCHAR(64) NOT NULL,
    description VARCHAR(512) DEFAULT NULL,
    preview_image VARCHAR(255) DEFAULT NULL,
    sort_weight INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    current_version_id BIGINT DEFAULT NULL,
    usage_count BIGINT NOT NULL DEFAULT 0,
    project_count INT NOT NULL DEFAULT 0,
    optimization_flag SMALLINT NOT NULL DEFAULT 0,
    storage_path VARCHAR(255) DEFAULT NULL,
    difficulty INT DEFAULT NULL,
    top_category_id BIGINT DEFAULT NULL,
    first_category_id BIGINT DEFAULT NULL,
    second_category_id BIGINT DEFAULT NULL,
    is_paid SMALLINT NOT NULL DEFAULT 0,
    fee_type VARCHAR(32) DEFAULT NULL,
    price DECIMAL(10,2) DEFAULT NULL,
    online_time TIMESTAMP DEFAULT NULL,
    valid_time TIMESTAMP DEFAULT NULL,
    creator VARCHAR(64) DEFAULT NULL,
    produce_date DATE DEFAULT NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    visit_count BIGINT NOT NULL DEFAULT 0,
    detail_desc TEXT DEFAULT NULL,
    violation_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_tpl_template_code UNIQUE (template_code)
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_status ON tpl_template (status);
COMMENT ON TABLE tpl_template IS '模板主表';
COMMENT ON COLUMN tpl_template.template_code IS '模板编码（自动生成：TPL+yyyyMMdd+4位序号）';
COMMENT ON COLUMN tpl_template.status IS '状态：0-正常, 1-上架, 2-下架, 3-违规';
COMMENT ON COLUMN tpl_template.project_count IS '累计项目数';
COMMENT ON COLUMN tpl_template.optimization_flag IS '优化标记：0-正常，1-需要优化';
COMMENT ON COLUMN tpl_template.storage_path IS '存储路径';
COMMENT ON COLUMN tpl_template.difficulty IS '难度等级：1-入门, 2-初级, 3-中级, 4-高级, 5-专家';
COMMENT ON COLUMN tpl_template.top_category_id IS '顶级分类 ID';
COMMENT ON COLUMN tpl_template.first_category_id IS '一级分类 ID';
COMMENT ON COLUMN tpl_template.second_category_id IS '二级分类 ID';
COMMENT ON COLUMN tpl_template.is_paid IS '是否付费：0-免费，1-付费';
COMMENT ON COLUMN tpl_template.fee_type IS '费用类型（字典 template_fee_type）';
COMMENT ON COLUMN tpl_template.price IS '价格（元）';
COMMENT ON COLUMN tpl_template.online_time IS '上线时间';
COMMENT ON COLUMN tpl_template.valid_time IS '有效截止时间';
COMMENT ON COLUMN tpl_template.creator IS '创作者名称';
COMMENT ON COLUMN tpl_template.produce_date IS '创作时间';
COMMENT ON COLUMN tpl_template.download_count IS '下载次数';
COMMENT ON COLUMN tpl_template.favorite_count IS '收藏数';
COMMENT ON COLUMN tpl_template.visit_count IS '访问量';
COMMENT ON COLUMN tpl_template.detail_desc IS '详细描述';
COMMENT ON COLUMN tpl_template.violation_reason IS '违规原因';

-- 模板版本表
CREATE TABLE IF NOT EXISTS tpl_template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_no VARCHAR(16) NOT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
    storage_url VARCHAR(255) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_hash VARCHAR(64) DEFAULT NULL,
    is_major_update SMALLINT NOT NULL DEFAULT 0,
    config_content TEXT DEFAULT NULL,
    changelog TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_tpl_template_version UNIQUE (template_id, version_no)
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_version_template_id ON tpl_template_version (template_id);
COMMENT ON TABLE tpl_template_version IS '模板版本表';
COMMENT ON COLUMN tpl_template_version.storage_url IS '存储 URL';
COMMENT ON COLUMN tpl_template_version.file_size IS '文件大小（字节）';
COMMENT ON COLUMN tpl_template_version.file_hash IS '文件哈希值';
COMMENT ON COLUMN tpl_template_version.is_major_update IS '是否重大更新：0-否，1-是';

-- 模板三级分类表
CREATE TABLE IF NOT EXISTS tpl_template_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    level INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
COMMENT ON TABLE tpl_template_category IS '模板三级分类表';
COMMENT ON COLUMN tpl_template_category.name IS '分类名称';
COMMENT ON COLUMN tpl_template_category.parent_id IS '上级分类 ID';
COMMENT ON COLUMN tpl_template_category.level IS '层级：0-顶级, 1-一级, 2-二级';
COMMENT ON COLUMN tpl_template_category.sort_order IS '排序顺序';

-- 模板预览图片表
CREATE TABLE IF NOT EXISTS tpl_template_preview_image (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_preview_image_template_id ON tpl_template_preview_image (template_id);
COMMENT ON TABLE tpl_template_preview_image IS '模板预览图片表';
COMMENT ON COLUMN tpl_template_preview_image.template_id IS '关联模板 ID';
COMMENT ON COLUMN tpl_template_preview_image.image_url IS '预览图片 URL';
COMMENT ON COLUMN tpl_template_preview_image.sort_order IS '排序顺序';

-- --------------------------------------------------------
-- 4. 模型管理模块 (aeisp-model)
-- --------------------------------------------------------

-- AI 模型配置表
CREATE TABLE IF NOT EXISTS ai_model (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(50) NOT NULL,
    model_type VARCHAR(30) NOT NULL,
    api_endpoint VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    weight INT NOT NULL DEFAULT 1,
    max_qps INT NOT NULL DEFAULT 10,
    scenario_tags JSONB DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    maintain_window VARCHAR(50) DEFAULT NULL,
    default_params JSONB DEFAULT NULL,
    usage_count BIGINT NOT NULL DEFAULT 0,
    failure_rate INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_ai_model_status ON ai_model (status);
CREATE INDEX IF NOT EXISTS idx_ai_model_type ON ai_model (model_type);
COMMENT ON TABLE ai_model IS 'AI 大模型配置表';
COMMENT ON COLUMN ai_model.model_name IS '模型名称';
COMMENT ON COLUMN ai_model.model_type IS '模型类型';
COMMENT ON COLUMN ai_model.api_endpoint IS '接口地址';
COMMENT ON COLUMN ai_model.api_key IS '访问密钥（AES 加密存储）';
COMMENT ON COLUMN ai_model.weight IS '调用权重';
COMMENT ON COLUMN ai_model.max_qps IS '最大 QPS 限制';
COMMENT ON COLUMN ai_model.scenario_tags IS '适配场景标签（JSON 数组）';
COMMENT ON COLUMN ai_model.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN ai_model.sort_order IS '优先级排序';
COMMENT ON COLUMN ai_model.maintain_window IS '维护时间窗口';
COMMENT ON COLUMN ai_model.default_params IS '默认参数（JSON）';
COMMENT ON COLUMN ai_model.usage_count IS '累计调用次数';
COMMENT ON COLUMN ai_model.failure_rate IS '失败率（%）';

-- 模型调用日志表
CREATE TABLE IF NOT EXISTS model_call_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    request_params JSONB DEFAULT NULL,
    response_summary VARCHAR(500) DEFAULT NULL,
    tokens_used INT DEFAULT NULL,
    duration_ms INT DEFAULT NULL,
    cost_cents INT DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    error_msg VARCHAR(500) DEFAULT NULL,
    ip_address VARCHAR(40) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_model_call_log_user_id ON model_call_log (user_id);
CREATE INDEX IF NOT EXISTS idx_model_call_log_model_id ON model_call_log (model_id);
CREATE INDEX IF NOT EXISTS idx_model_call_log_created_at ON model_call_log (created_at);
COMMENT ON TABLE model_call_log IS '模型调用日志表';
COMMENT ON COLUMN model_call_log.user_id IS '用户 ID';
COMMENT ON COLUMN model_call_log.model_id IS '模型 ID';
COMMENT ON COLUMN model_call_log.request_params IS '请求参数（JSON）';
COMMENT ON COLUMN model_call_log.response_summary IS '响应摘要';
COMMENT ON COLUMN model_call_log.tokens_used IS '消耗 Token 数';
COMMENT ON COLUMN model_call_log.duration_ms IS '调用耗时（毫秒）';
COMMENT ON COLUMN model_call_log.cost_cents IS '费用（分）';
COMMENT ON COLUMN model_call_log.status IS '状态：1-成功，2-失败';
COMMENT ON COLUMN model_call_log.error_msg IS '错误信息';
COMMENT ON COLUMN model_call_log.ip_address IS '调用者 IP';

-- --------------------------------------------------------
-- 5. 充值模块 (aeisp-recharge)
-- --------------------------------------------------------

-- 时长套餐表
CREATE TABLE IF NOT EXISTS duration_package (
    id BIGSERIAL PRIMARY KEY,
    package_name VARCHAR(50) NOT NULL,
    price_cents INT NOT NULL DEFAULT 0,
    duration_minutes BIGINT NOT NULL DEFAULT 0,
    valid_days INT DEFAULT NULL,
    promotion VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_duration_package_status ON duration_package (status);
COMMENT ON TABLE duration_package IS '时长套餐表';
COMMENT ON COLUMN duration_package.package_name IS '套餐名称';
COMMENT ON COLUMN duration_package.price_cents IS '价格（分）';
COMMENT ON COLUMN duration_package.duration_minutes IS '对应时长（分钟）';
COMMENT ON COLUMN duration_package.valid_days IS '有效期（天）';
COMMENT ON COLUMN duration_package.promotion IS '优惠活动描述';
COMMENT ON COLUMN duration_package.status IS '状态：1-上架，2-下架';
COMMENT ON COLUMN duration_package.sort_order IS '排序值';

-- 充值订单表
CREATE TABLE IF NOT EXISTS recharge_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    package_id BIGINT DEFAULT NULL,
    amount_cents INT NOT NULL DEFAULT 0,
    duration_minutes BIGINT NOT NULL DEFAULT 0,
    pay_type VARCHAR(20) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    pay_time TIMESTAMP DEFAULT NULL,
    refund_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_recharge_order_no UNIQUE (order_no)
);
CREATE INDEX IF NOT EXISTS idx_recharge_order_user_id ON recharge_order (user_id);
CREATE INDEX IF NOT EXISTS idx_recharge_order_status ON recharge_order (status);
CREATE INDEX IF NOT EXISTS idx_recharge_order_created_at ON recharge_order (created_at);
COMMENT ON TABLE recharge_order IS '充值订单表';
COMMENT ON COLUMN recharge_order.order_no IS '订单号';
COMMENT ON COLUMN recharge_order.user_id IS '用户 ID';
COMMENT ON COLUMN recharge_order.package_id IS '关联套餐 ID';
COMMENT ON COLUMN recharge_order.amount_cents IS '充值金额（分）';
COMMENT ON COLUMN recharge_order.duration_minutes IS '到账时长（分钟）';
COMMENT ON COLUMN recharge_order.pay_type IS '支付方式';
COMMENT ON COLUMN recharge_order.status IS '状态：1-待支付，2-已支付，3-已退款，4-已取消';
COMMENT ON COLUMN recharge_order.pay_time IS '支付时间';
COMMENT ON COLUMN recharge_order.refund_reason IS '退款原因';

-- 余额变更日志表
CREATE TABLE IF NOT EXISTS usr_balance_change_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_type SMALLINT NOT NULL,
    change_cents INT NOT NULL,
    previous_balance INT NOT NULL,
    current_balance INT NOT NULL,
    related_order_id BIGINT DEFAULT NULL,
    reason VARCHAR(200) DEFAULT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_type SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_balance_change_log_user_id ON usr_balance_change_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_balance_change_log_type ON usr_balance_change_log (change_type);
CREATE INDEX IF NOT EXISTS idx_usr_balance_change_log_created_at ON usr_balance_change_log (created_at);
COMMENT ON TABLE usr_balance_change_log IS '余额变更日志表';
COMMENT ON COLUMN usr_balance_change_log.user_id IS '用户 ID';
COMMENT ON COLUMN usr_balance_change_log.change_type IS '变更类型：1-充值，2-消费，3-退款，4-管理员调整';
COMMENT ON COLUMN usr_balance_change_log.change_cents IS '变更金额（分），正数增加，负数扣减';
COMMENT ON COLUMN usr_balance_change_log.previous_balance IS '变更前余额（分）';
COMMENT ON COLUMN usr_balance_change_log.current_balance IS '变更后余额（分）';
COMMENT ON COLUMN usr_balance_change_log.related_order_id IS '关联订单 ID';
COMMENT ON COLUMN usr_balance_change_log.reason IS '变更原因';
COMMENT ON COLUMN usr_balance_change_log.operator_id IS '操作人 ID';
COMMENT ON COLUMN usr_balance_change_log.operator_type IS '操作者类型：1-系统自动，2-管理员，3-用户本人';

-- --------------------------------------------------------
-- 6. 初始化数据
-- --------------------------------------------------------

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_code, description, status, is_system, created_at, updated_at) VALUES
(1, '超级管理员', 'ROLE_SUPER_ADMIN', '系统最高权限，拥有所有操作权限', 1, 1, NOW(), NOW()),
(2, '用户管理员', 'ROLE_USER_MANAGER', '负责用户账号的全生命周期管理', 1, 1, NOW(), NOW()),
(3, '模型管理员', 'ROLE_MODEL_MANAGER', '负责AI模型的接入、配置、测试、状态管理', 1, 1, NOW(), NOW()),
(4, '消息管理员', 'ROLE_NOTIFICATION_MANAGER', '负责平台消息的内容编辑、推送管理、撤回和效果监控', 1, 1, NOW(), NOW()),
(5, '模板管理员', 'ROLE_TEMPLATE_MANAGER', '负责编程模板的上传、版本管理、上下线控制和统计分析', 1, 1, NOW(), NOW()),
(6, '财务管理员', 'ROLE_FINANCE_MANAGER', '负责充值套餐配置、订单管理、退款审核和余额调整', 1, 1, NOW(), NOW()),
(7, '普通用户', 'ROLE_USER', '平台注册用户默认角色', 1, 1, NOW(), NOW());

-- 初始化权限点（菜单树结构）
-- menu_type: 0-目录, 1-菜单, 2-按钮
-- 目录（顶级导航）
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(1, '仪表盘', 'dashboard', 'system', 'manage', NULL, 0, 0, 1, 'DataAnalysis', '/', NULL, 1, 1, NOW(), NOW(), 1, 1),
(2, '系统管理', 'system', 'system', 'manage', NULL, 0, 0, 2, 'Setting', '/system', NULL, 1, 1, NOW(), NOW(), 1, 1),
(3, '用户与权限', 'user', 'user', 'manage', NULL, 0, 0, 3, 'UserFilled', '/user', NULL, 1, 1, NOW(), NOW(), 1, 1),
(4, '资产与计费', 'finance', 'finance', 'manage', NULL, 0, 0, 4, 'Coin', '/finance', NULL, 1, 1, NOW(), NOW(), 1, 1),
(5, '消息管理', 'message', 'notification', 'manage', NULL, 0, 0, 5, 'Bell', '/message', NULL, 1, 1, NOW(), NOW(), 1, 1),
(6, '项目管理', 'project', 'project', 'manage', NULL, 0, 0, 6, 'Management', '/project', NULL, 1, 1, NOW(), NOW(), 1, 1),
(7, '模板资源', 'template', 'template', 'manage', NULL, 0, 0, 7, 'Files', '/template', NULL, 1, 1, NOW(), NOW(), 1, 1),
(8, 'AI对话模块', 'ai', 'model', 'manage', NULL, 0, 0, 8, 'Cpu', '/ai', NULL, 1, 1, NOW(), NOW(), 1, 1),
(9, '运营统计模块', 'operations', 'system', 'manage', NULL, 0, 0, 10, 'DataBoard', '/operations', NULL, 1, 1, NOW(), NOW(), 1, 1),
(10, '系统配置模块', 'sysconfig', 'system', 'manage', NULL, 0, 0, 9, 'Operation', '/sysconfig', NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 仪表盘 -> 菜单
(11, '数据大盘', 'dashboard:overview', 'dashboard', 'read', NULL, 1, 1, 1, 'DataLine', '/dashboard', 'dashboard/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 菜单
(12, '菜单管理', 'system:menu', 'system', 'read', NULL, 2, 1, 1, NULL, '/system/menu', 'system/menu/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(13, '角色管理', 'system:role', 'system', 'read', NULL, 2, 1, 2, NULL, '/system/role', 'system/role/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(14, '管理员管理', 'system:user', 'system', 'read', NULL, 2, 1, 3, NULL, '/system/user', 'system/user/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(15, '操作日志', 'system:log', 'system', 'read', NULL, 2, 1, 4, NULL, '/system/log', 'system/log/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(16, '登录日志', 'system:login-log', 'system', 'read', NULL, 2, 1, 5, NULL, '/system/login-log', 'system/login-log/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(17, '数据字典', 'system:dict', 'system', 'read', NULL, 2, 1, 6, NULL, '/system/dict', 'system/dict/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 用户与权限 -> 菜单
(19, '用户管理', 'user:list', 'user', 'read', NULL, 3, 1, 1, NULL, '/user/list', 'user/list/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(20, '权限分配', 'user:permission', 'user', 'read', NULL, 3, 1, 2, NULL, '/user/permission', 'user/permission/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 资产与计费 -> 菜单
(21, '用户余额', 'finance:balance', 'finance', 'read', NULL, 4, 1, 1, NULL, '/finance/balance', 'recharge/balance/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(22, '充值记录', 'finance:order', 'finance', 'read', NULL, 4, 1, 2, NULL, '/finance/order', 'recharge/order/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(23, '扣费记录', 'finance:deduction', 'finance', 'read', NULL, 4, 1, 3, NULL, '/finance/deduction', 'recharge/deduction/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(24, '模型购买', 'finance:purchase', 'finance', 'read', NULL, 4, 1, 4, NULL, '/finance/purchase', 'recharge/purchase/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(25, '套餐管理', 'finance:package', 'finance', 'read', NULL, 4, 1, 5, NULL, '/finance/package', 'recharge/package/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 消息管理 -> 菜单
(26, '系统通告', 'notification:list', 'notification', 'read', NULL, 5, 1, 1, NULL, '/message/notification', 'message/notification/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 项目管理 -> 菜单
(27, '项目列表', 'project:list-page', 'project', 'read', NULL, 6, 1, 1, NULL, '/project/list', 'project/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 模板资源 -> 菜单
(28, '模板分类', 'template:category', 'template', 'read', NULL, 7, 1, 1, NULL, '/template/category', 'template/category/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(29, '模板管理', 'template:list', 'template', 'read', NULL, 7, 1, 2, NULL, '/template/list', 'template/list/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- AI对话模块 -> 菜单
(30, '会话管理', 'ai:session', 'ai', 'read', NULL, 8, 1, 1, NULL, '/ai/session', 'ai/session/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(31, 'AI配置', 'ai:config', 'ai', 'read', NULL, 8, 1, 2, NULL, '/ai/config', 'model/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(32, 'AI统计', 'ai:stats', 'ai', 'read', NULL, 8, 1, 3, NULL, '/ai/stats', 'model/stats/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 运营统计模块 -> 菜单
(33, '数据大盘', 'operations:dashboard', 'operations', 'read', NULL, 9, 1, 1, NULL, '/operations/dashboard', 'operations/dashboard/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(34, '系统监控', 'operations:monitor', 'operations', 'read', NULL, 9, 1, 2, NULL, '/operations/monitor', 'operations/monitor/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 系统配置模块 -> 菜单
(35, '基础配置', 'system:config', 'sysconfig', 'read', NULL, 10, 1, 1, NULL, '/sysconfig/base', 'system/config/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(36, '功能开关', 'system:feature:view', 'sysconfig', 'read', NULL, 10, 1, 2, NULL, '/sysconfig/feature', 'system/feature/index.vue', 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 菜单管理 -> 按钮
(37, '菜单列表', 'system:menu:list', 'system', 'list', NULL, 12, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(38, '菜单管理操作', 'system:menu:manage', 'system', 'manage', NULL, 12, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 角色管理 -> 按钮
(39, '角色管理操作', 'system:role:manage', 'system', 'manage', NULL, 13, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 管理员管理 -> 按钮
(40, '管理员管理操作', 'system:user:manage', 'system', 'manage', NULL, 14, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 操作日志 -> 按钮
(41, '查看操作日志', 'admin:log:read', 'admin', 'read', NULL, 15, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 系统管理 -> 数据字典 -> 按钮
(42, '字典列表', 'system:dict:list', 'system', 'list', NULL, 17, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(43, '字典管理操作', 'system:dict:manage', 'system', 'manage', NULL, 17, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 用户与权限 -> 用户管理 -> 按钮
(45, '创建用户', 'user:create', 'user', 'create', NULL, 19, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(46, '查看用户', 'user:read', 'user', 'read', NULL, 19, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(47, '修改用户', 'user:update', 'user', 'update', NULL, 19, 2, 3, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(48, '删除用户', 'user:delete', 'user', 'delete', NULL, 19, 2, 4, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(49, '修改用户状态', 'user:status:update', 'user', 'update', NULL, 19, 2, 5, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(50, '重置用户密码', 'user:password:reset', 'user', 'update', NULL, 19, 2, 6, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(51, '调整用户时长', 'user:duration:adjust', 'user', 'update', NULL, 19, 2, 7, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 用户与权限 -> 权限分配 -> 按钮
(52, '查看权限分配', 'user:permission:read', 'user', 'read', NULL, 20, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(53, '修改权限分配', 'user:permission:update', 'user', 'update', NULL, 20, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 资产与计费 -> 用户余额 -> 按钮
(54, '调整余额', 'finance:balance:adjust', 'finance', 'adjust', NULL, 21, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 资产与计费 -> 充值记录 -> 按钮
(55, '管理订单', 'order:manage', 'order', 'manage', NULL, 22, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(56, '订单退款', 'order:refund', 'order', 'refund', NULL, 22, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 资产与计费 -> 套餐管理 -> 按钮
(57, '套餐管理操作', 'finance:package:manage', 'finance', 'manage', NULL, 25, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 消息管理 -> 系统通告 -> 按钮
(58, '创建消息', 'notification:create', 'notification', 'create', NULL, 26, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(59, '查看消息', 'notification:read', 'notification', 'read', NULL, 26, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(60, '修改消息', 'notification:update', 'notification', 'update', NULL, 26, 2, 3, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(61, '删除消息', 'notification:delete', 'notification', 'delete', NULL, 26, 2, 4, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(62, '发送消息', 'notification:send', 'notification', 'send', NULL, 26, 2, 5, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 项目管理 -> 项目列表 -> 按钮
(63, '项目列表查看', 'project:list', 'project', 'list', NULL, 27, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(64, '项目管理操作', 'project:manage', 'project', 'manage', NULL, 27, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 模板资源 -> 模板分类 -> 按钮
(65, '模板分类管理', 'template:category:manage', 'template', 'manage', NULL, 28, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- 模板资源 -> 模板管理 -> 按钮
(66, '创建模板', 'template:create', 'template', 'create', NULL, 29, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(67, '查看模板', 'template:read', 'template', 'read', NULL, 29, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(68, '修改模板', 'template:update', 'template', 'update', NULL, 29, 2, 3, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(69, '删除模板', 'template:delete', 'template', 'delete', NULL, 29, 2, 4, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(70, '模板版本管理', 'template:version:manage', 'template', 'manage', NULL, 29, 2, 5, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- AI对话模块 -> 会话管理 -> 按钮
(71, '查看会话', 'ai:session:read', 'ai', 'read', NULL, 30, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(72, '管理会话', 'ai:session:manage', 'ai', 'manage', NULL, 30, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
-- AI对话模块 -> AI配置 -> 按钮
(73, '创建模型', 'model:create', 'model', 'create', NULL, 31, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(74, '查看模型', 'model:read', 'model', 'read', NULL, 31, 2, 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(75, '修改模型', 'model:update', 'model', 'update', NULL, 31, 2, 3, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(76, '删除模型', 'model:delete', 'model', 'delete', NULL, 31, 2, 4, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(77, '配置模型', 'model:config', 'model', 'config', NULL, 31, 2, 5, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
(78, '测试模型', 'model:test', 'model', 'test', NULL, 31, 2, 6, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1);

-- 初始化角色权限映射
-- 超级管理员：拥有全部 78 个权限
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
(1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), (1, 4, NOW()), (1, 5, NOW()), (1, 6, NOW()), (1, 7, NOW()), (1, 8, NOW()), (1, 9, NOW()), (1, 10, NOW()),
(1, 11, NOW()), (1, 12, NOW()), (1, 13, NOW()), (1, 14, NOW()), (1, 15, NOW()), (1, 16, NOW()), (1, 17, NOW()), (1, 19, NOW()), (1, 20, NOW()),
(1, 21, NOW()), (1, 22, NOW()), (1, 23, NOW()), (1, 24, NOW()), (1, 25, NOW()), (1, 26, NOW()), (1, 27, NOW()), (1, 28, NOW()), (1, 29, NOW()), (1, 30, NOW()),
(1, 31, NOW()), (1, 32, NOW()), (1, 33, NOW()), (1, 34, NOW()), (1, 35, NOW()), (1, 36, NOW()), (1, 37, NOW()), (1, 38, NOW()), (1, 39, NOW()), (1, 40, NOW()),
(1, 41, NOW()), (1, 42, NOW()), (1, 43, NOW()), (1, 45, NOW()), (1, 46, NOW()), (1, 47, NOW()), (1, 48, NOW()), (1, 49, NOW()), (1, 50, NOW()),
(1, 51, NOW()), (1, 52, NOW()), (1, 53, NOW()), (1, 54, NOW()), (1, 55, NOW()), (1, 56, NOW()), (1, 57, NOW()), (1, 58, NOW()), (1, 59, NOW()), (1, 60, NOW()),
(1, 61, NOW()), (1, 62, NOW()), (1, 63, NOW()), (1, 64, NOW()), (1, 65, NOW()), (1, 66, NOW()), (1, 67, NOW()), (1, 68, NOW()), (1, 69, NOW()), (1, 70, NOW()),
(1, 71, NOW()), (1, 72, NOW()), (1, 73, NOW()), (1, 74, NOW()), (1, 75, NOW()), (1, 76, NOW()), (1, 77, NOW()), (1, 78, NOW()),
-- 用户管理员：用户管理相关按钮
(2, 45, NOW()), (2, 46, NOW()), (2, 47, NOW()), (2, 48, NOW()), (2, 49, NOW()), (2, 50, NOW()), (2, 51, NOW()),
-- 模型管理员：AI配置相关按钮
(3, 73, NOW()), (3, 74, NOW()), (3, 75, NOW()), (3, 76, NOW()), (3, 77, NOW()), (3, 78, NOW()),
-- 消息管理员：系统通告相关按钮
(4, 58, NOW()), (4, 59, NOW()), (4, 60, NOW()), (4, 61, NOW()), (4, 62, NOW()),
-- 模板管理员：模板管理相关按钮
(5, 66, NOW()), (5, 67, NOW()), (5, 68, NOW()), (5, 69, NOW()), (5, 70, NOW()),
-- 财务管理员：资产计费相关按钮
(6, 54, NOW()), (6, 55, NOW()), (6, 56, NOW()), (6, 57, NOW());

-- 更新序列
SELECT setval('sys_permission_id_seq', 78);
SELECT setval('sys_role_permission_id_seq', 105);

-- 初始化超级管理员账号（密码明文：admin123，BCrypt 加密后）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, created_at, updated_at) VALUES
(1, 'admin', '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '系统管理员', 'admin@aeisp.com', '13800138000', 1, NOW(), NOW());

-- 关联超级管理员与角色
INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES
(1, 1, NOW());

-- 初始化常用系统配置
INSERT INTO sys_config (config_key, config_value, environment, description, category, field_type, created_at, updated_at) VALUES
('system.name', 'AEISP 后端管理系统', 'all', '系统名称', 'platform', 'text', NOW(), NOW()),
('system.version', '1.0.0', 'all', '系统版本号', 'platform', 'text', NOW(), NOW()),
('system.copyright', '© 2026 AEISP Team', 'all', '系统版权信息', 'platform', 'text', NOW(), NOW()),
('official_website_url', 'https://www.example.com', 'prod', '生产环境官网地址', 'platform', 'text', NOW(), NOW()),
('official_website_url', 'https://test.example.com', 'test', '测试环境官网地址', 'platform', 'text', NOW(), NOW()),
('official_website_url', 'https://dev.example.com', 'dev', '开发环境官网地址', 'platform', 'text', NOW(), NOW()),
('contact_email', 'contact@example.com', 'all', '联系邮箱', 'platform', 'text', NOW(), NOW()),
('contact_phone', '400-123-4567', 'all', '客服电话', 'platform', 'text', NOW(), NOW()),
('default_register_duration', '120', 'all', '注册默认赠送时长（分钟）', 'threshold', 'number', NOW(), NOW()),
('default_duration_expiry_days', '90', 'all', '赠送时长有效期（天）', 'timeout', 'number', NOW(), NOW()),
('duration_warning_threshold', '120', 'all', '时长预警阈值（分钟）', 'threshold', 'number', NOW(), NOW()),
('login_max_fail_attempts', '5', 'all', '登录最大失败次数', 'threshold', 'number', NOW(), NOW()),
('login_lockout_minutes', '30', 'all', '登录锁定持续时间（分钟）', 'timeout', 'number', NOW(), NOW()),
('password_min_length', '8', 'all', '密码最小长度', 'threshold', 'number', NOW(), NOW()),
('password_max_length', '20', 'all', '密码最大长度', 'threshold', 'number', NOW(), NOW()),
('verify_code_ttl_seconds', '300', 'all', '验证码有效期（秒）', 'timeout', 'number', NOW(), NOW()),
('verify_code_cooldown_seconds', '60', 'all', '验证码发送冷却时间（秒）', 'timeout', 'number', NOW(), NOW()),
('default_model_max_qps', '10', 'all', '默认模型QPS限制', 'threshold', 'number', NOW(), NOW()),
('model_failure_rate_threshold', '10', 'all', '模型失败率告警阈值（%）', 'threshold', 'number', NOW(), NOW()),
('notification_archive_days', '90', 'all', '消息归档天数', 'timeout', 'number', NOW(), NOW()),
('recharge_order_timeout_minutes', '30', 'all', '充值订单超时时间（分钟）', 'timeout', 'number', NOW(), NOW());

-- --------------------------------------------------------
-- 字典类型种子数据
-- --------------------------------------------------------
INSERT INTO sys_dict_type (dict_name, dict_code, description, status, is_system) VALUES
('模型类型', 'model_type', 'AI模型分类', 1, 1),
('用户状态', 'user_status', '前端用户账号状态', 1, 1),
('订单状态', 'order_status', '充值订单状态', 1, 1),
('套餐状态', 'package_status', '充值套餐上下架状态', 1, 1),
('消耗类型', 'deduction_type', '用户时长消耗类型', 1, 1),
('会话状态', 'session_status', 'AI会话状态', 1, 1),
('项目状态', 'project_status', '用户项目状态', 1, 1),
('登录类型', 'login_type', '用户登录方式', 1, 1),
('登录结果', 'login_result', '用户登录结果', 1, 1),
('通告状态', 'notification_status', '消息通告状态', 1, 1),
('消息类型', 'msg_type', '通告消息类型', 1, 1),
('推送范围', 'push_scope', '通告推送范围', 1, 1),
('推送方式', 'push_type', '通告推送方式', 1, 1),
('余额操作类型', 'balance_op_type', '管理员调整余额/时长操作类型', 1, 1);

-- --------------------------------------------------------
-- 字典数据种子数据
-- --------------------------------------------------------
INSERT INTO sys_dict_data (dict_code, item_label, item_value, sort_order, status, color, is_default) VALUES
('model_type', '通用', 'general', 1, 1, '', 1),
('model_type', '教学', 'teaching', 2, 1, 'success', 0),
('model_type', '竞赛', 'competition', 3, 1, 'warning', 0),
('user_status', '正常', '1', 1, 1, 'success', 1),
('user_status', '禁用', '2', 2, 1, 'danger', 0),
('user_status', '冻结', '3', 3, 1, 'warning', 0),
('user_status', '锁定', '4', 4, 1, 'info', 0),
('order_status', '待支付', '0', 1, 1, 'warning', 1),
('order_status', '已支付', '1', 2, 1, 'success', 0),
('order_status', '已退款', '2', 3, 1, 'danger', 0),
('order_status', '已取消', '3', 4, 1, 'info', 0),
('package_status', '上架', '1', 1, 1, 'success', 1),
('package_status', '下架', '0', 2, 1, 'danger', 0),
('deduction_type', '模型调用', 'model_call', 1, 1, 'primary', 1),
('deduction_type', '仿真运行', 'sim_run', 2, 1, 'success', 0),
('deduction_type', '编译调试', 'debug', 3, 1, 'warning', 0),
('session_status', '进行中', '1', 1, 1, 'primary', 1),
('session_status', '已归档', '2', 2, 1, 'info', 0),
('project_status', '进行中', '0', 1, 1, 'primary', 1),
('project_status', '已完成', '1', 2, 1, 'success', 0),
('project_status', '已归档', '2', 3, 1, 'info', 0),
('login_type', '密码登录', '1', 1, 1, '', 1),
('login_type', '验证码登录', '2', 2, 1, 'success', 0),
('login_type', 'Token刷新', '3', 3, 1, 'info', 0),
('login_result', '成功', '1', 1, 1, 'success', 1),
('login_result', '密码错误', '2', 2, 1, 'danger', 0),
('login_result', '账号不存在', '3', 3, 1, 'warning', 0),
('login_result', '账号禁用', '4', 4, 1, 'warning', 0),
('login_result', '账号冻结', '5', 5, 1, 'warning', 0),
('login_result', '账号锁定', '6', 6, 1, 'warning', 0),
('login_result', '验证码错误', '7', 7, 1, 'danger', 0),
('login_result', 'Token过期', '8', 8, 1, 'info', 0),
('notification_status', '草稿', '1', 1, 1, 'info', 1),
('notification_status', '已发送', '2', 2, 1, 'success', 0),
('notification_status', '已撤回', '3', 3, 1, 'warning', 0),
('notification_status', '定时发送', '4', 4, 1, 'primary', 0),
('notification_status', '已归档', '5', 5, 1, '', 0),
('notification_status', '已过期', '6', 6, 1, 'danger', 0),
('msg_type', '系统公告', '1', 1, 1, '', 1),
('msg_type', '活动通知', '2', 2, 1, 'success', 0),
('msg_type', '维护通知', '3', 3, 1, 'warning', 0),
('msg_type', '更新通知', '4', 4, 1, 'info', 0),
('push_scope', '全员', '1', 1, 1, '', 1),
('push_scope', '指定用户', '2', 2, 1, 'primary', 0),
('push_scope', '指定角色', '3', 3, 1, 'warning', 0),
('push_type', '立即推送', '1', 1, 1, 'success', 1),
('push_type', '定时推送', '2', 2, 1, 'warning', 0),
('balance_op_type', '增加', '1', 1, 1, 'success', 1),
('balance_op_type', '扣减', '2', 2, 1, 'danger', 0),
('balance_op_type', '设定', '3', 3, 1, 'primary', 0);
