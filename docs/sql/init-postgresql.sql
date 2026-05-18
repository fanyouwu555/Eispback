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
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role (status);
COMMENT ON TABLE sys_role IS '系统角色表';

-- 权限点表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(32) NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_permission_code UNIQUE (permission_code)
);
COMMENT ON TABLE sys_permission IS '系统权限点表';

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
    username VARCHAR(32) DEFAULT NULL,
    module VARCHAR(32) DEFAULT NULL,
    operation VARCHAR(32) DEFAULT NULL,
    request_method VARCHAR(10) DEFAULT NULL,
    request_url VARCHAR(255) DEFAULT NULL,
    request_params TEXT DEFAULT NULL,
    response_data TEXT DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    error_msg VARCHAR(512) DEFAULT NULL,
    ip VARCHAR(64) DEFAULT NULL,
    duration BIGINT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_user_id ON sys_operation_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_created_at ON sys_operation_log (created_at);
COMMENT ON TABLE sys_operation_log IS '系统操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL,
    config_value TEXT DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);
COMMENT ON TABLE sys_config IS '系统配置表';

-- --------------------------------------------------------
-- 2. 用户管理模块 (aeisp-user)
-- --------------------------------------------------------

-- 前端用户表
CREATE TABLE IF NOT EXISTS usr_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(64) DEFAULT NULL,
    password VARCHAR(128) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    remaining_duration BIGINT NOT NULL DEFAULT 0,
    total_recharge BIGINT NOT NULL DEFAULT 0,
    last_login_time TIMESTAMP DEFAULT NULL,
    register_ip VARCHAR(64) DEFAULT NULL,
    register_device VARCHAR(255) DEFAULT NULL,
    register_time TIMESTAMP DEFAULT NULL,
    role_code VARCHAR(32) DEFAULT 'frontend_user',
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
COMMENT ON TABLE usr_user IS '前端用户表';

-- 用户登录日志表
CREATE TABLE IF NOT EXISTS usr_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(32) DEFAULT NULL,
    login_time TIMESTAMP DEFAULT NULL,
    ip VARCHAR(64) DEFAULT NULL,
    device VARCHAR(255) DEFAULT NULL,
    result SMALLINT NOT NULL DEFAULT 1,
    error_msg VARCHAR(512) DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_login_log_user_id ON usr_login_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_login_log_login_time ON usr_login_log (login_time);
COMMENT ON TABLE usr_login_log IS '用户登录日志表';

-- 用户时长消耗日志表
CREATE TABLE IF NOT EXISTS usr_duration_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    consume_time TIMESTAMP DEFAULT NULL,
    consume_type SMALLINT NOT NULL,
    consume_duration BIGINT NOT NULL,
    project_id BIGINT DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL
);
CREATE INDEX IF NOT EXISTS idx_usr_duration_log_user_id ON usr_duration_log (user_id);
CREATE INDEX IF NOT EXISTS idx_usr_duration_log_consume_time ON usr_duration_log (consume_time);
COMMENT ON TABLE usr_duration_log IS '用户时长消耗日志表';

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
    status SMALLINT NOT NULL DEFAULT 0,
    is_top SMALLINT NOT NULL DEFAULT 0,
    read_count BIGINT NOT NULL DEFAULT 0,
    total_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_msg_notification_status ON msg_notification (status);
CREATE INDEX IF NOT EXISTS idx_msg_notification_push_time ON msg_notification (push_time);
COMMENT ON TABLE msg_notification IS '消息通知主表';

-- 用户消息关联表
CREATE TABLE IF NOT EXISTS msg_user_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    is_read SMALLINT NOT NULL DEFAULT 0,
    read_time TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_msg_user_notification UNIQUE (user_id, notification_id)
);
CREATE INDEX IF NOT EXISTS idx_msg_user_notification_notification_id ON msg_user_notification (notification_id);
CREATE INDEX IF NOT EXISTS idx_msg_user_notification_is_read ON msg_user_notification (is_read);
COMMENT ON TABLE msg_user_notification IS '用户消息关联表';

-- --------------------------------------------------------
-- 4. 模板管理模块 (aeisp-template)
-- --------------------------------------------------------

-- 模板主表
CREATE TABLE IF NOT EXISTS tpl_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(64) NOT NULL,
    scenario VARCHAR(32) DEFAULT NULL,
    description VARCHAR(512) DEFAULT NULL,
    preview_image VARCHAR(255) DEFAULT NULL,
    sort_weight INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 0,
    current_version_id BIGINT DEFAULT NULL,
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_status ON tpl_template (status);
CREATE INDEX IF NOT EXISTS idx_tpl_template_scenario ON tpl_template (scenario);
COMMENT ON TABLE tpl_template IS '模板主表';

-- 模板版本表
CREATE TABLE IF NOT EXISTS tpl_template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_no VARCHAR(16) NOT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
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

-- --------------------------------------------------------
-- 5. 初始化数据
-- --------------------------------------------------------

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_code, description, status, created_at, updated_at) VALUES
(1, '超级管理员', 'super_admin', '系统最高权限，拥有所有操作权限', 1, NOW(), NOW()),
(2, '前端用户', 'frontend_user', '平台注册用户默认角色', 1, NOW(), NOW());

-- 初始化超级管理员账号（密码明文：admin123，BCrypt 加密后）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'admin@aeisp.com', '13800138000', 1, NOW(), NOW());

-- 关联超级管理员与角色
INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES
(1, 1, NOW());

-- 初始化常用系统配置
INSERT INTO sys_config (config_key, config_value, description, created_at, updated_at) VALUES
('system.name', 'AEISP 后端管理系统', '系统名称', NOW(), NOW()),
('system.version', '1.0.0', '系统版本号', NOW(), NOW()),
('system.copyright', '© 2026 AEISP Team', '系统版权信息', NOW(), NOW());
