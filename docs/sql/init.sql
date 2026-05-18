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
    permission_name VARCHAR(32) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code)
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
    username VARCHAR(32) DEFAULT NULL COMMENT '操作人用户名',
    module VARCHAR(32) DEFAULT NULL COMMENT '操作模块',
    operation VARCHAR(32) DEFAULT NULL COMMENT '操作类型',
    request_method VARCHAR(10) DEFAULT NULL COMMENT 'HTTP 请求方法',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求 URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数（JSON）',
    response_data TEXT DEFAULT NULL COMMENT '响应数据（JSON）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    error_msg VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    ip VARCHAR(64) DEFAULT NULL COMMENT '操作人 IP',
    duration BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_operation_log_user_id (user_id),
    KEY idx_sys_operation_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    config_key VARCHAR(64) NOT NULL COMMENT '配置键',
    config_value TEXT DEFAULT NULL COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- --------------------------------------------------------
-- 2. 用户管理模块 (aeisp-user)
-- --------------------------------------------------------

-- 前端用户表
CREATE TABLE IF NOT EXISTS usr_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常，2-冻结',
    remaining_duration BIGINT NOT NULL DEFAULT 0 COMMENT '剩余时长（分钟）',
    total_recharge BIGINT NOT NULL DEFAULT 0 COMMENT '充值总额（分）',
    last_login_time DATETIME DEFAULT NULL COMMENT '最近一次登录时间',
    register_ip VARCHAR(64) DEFAULT NULL COMMENT '注册 IP',
    register_device VARCHAR(255) DEFAULT NULL COMMENT '注册设备',
    register_time DATETIME DEFAULT NULL COMMENT '注册时间',
    role_code VARCHAR(32) DEFAULT 'frontend_user' COMMENT '角色编码，关联 sys_role.role_code',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_username (username),
    UNIQUE KEY uk_usr_user_phone (phone),
    UNIQUE KEY uk_usr_user_email (email),
    KEY idx_usr_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前端用户表';

-- 用户登录日志表
CREATE TABLE IF NOT EXISTS usr_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    username VARCHAR(32) DEFAULT NULL COMMENT '登录用户名',
    login_time DATETIME DEFAULT NULL COMMENT '登录时间',
    ip VARCHAR(64) DEFAULT NULL COMMENT '登录 IP',
    device VARCHAR(255) DEFAULT NULL COMMENT '登录设备',
    result TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果：0-失败，1-成功',
    error_msg VARCHAR(512) DEFAULT NULL COMMENT '失败原因',
    PRIMARY KEY (id),
    KEY idx_usr_login_log_user_id (user_id),
    KEY idx_usr_login_log_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- 用户时长消耗日志表
CREATE TABLE IF NOT EXISTS usr_duration_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    consume_time DATETIME DEFAULT NULL COMMENT '消耗发生时间',
    consume_type TINYINT NOT NULL COMMENT '消耗类型：1-模型调用，2-仿真运行，3-编译调试',
    consume_duration BIGINT NOT NULL COMMENT '消耗时长（分钟）',
    project_id BIGINT DEFAULT NULL COMMENT '关联项目 ID',
    description VARCHAR(255) DEFAULT NULL COMMENT '消耗描述',
    PRIMARY KEY (id),
    KEY idx_usr_duration_log_user_id (user_id),
    KEY idx_usr_duration_log_consume_time (consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户时长消耗日志表';

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
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已推送，2-已撤回，3-已归档',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-正常，1-置顶',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '已读人数',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '推送总人数',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_msg_notification_status (status),
    KEY idx_msg_notification_push_time (push_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知主表';

-- 用户消息关联表
CREATE TABLE IF NOT EXISTS msg_user_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    notification_id BIGINT NOT NULL COMMENT '消息通知 ID',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间（推送时间）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_user_notification (user_id, notification_id),
    KEY idx_msg_user_notification_notification_id (notification_id),
    KEY idx_msg_user_notification_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息关联表';

-- --------------------------------------------------------
-- 4. 模板管理模块 (aeisp-template)
-- --------------------------------------------------------

-- 模板主表
CREATE TABLE IF NOT EXISTS tpl_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_name VARCHAR(64) NOT NULL COMMENT '模板名称',
    scenario VARCHAR(32) DEFAULT NULL COMMENT '适用场景：teaching-教学, competition-竞赛, practice-实战',
    description VARCHAR(512) DEFAULT NULL COMMENT '模板简介',
    preview_image VARCHAR(255) DEFAULT NULL COMMENT '预览图片 URL',
    sort_weight INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-下线，1-上线',
    current_version_id BIGINT DEFAULT NULL COMMENT '当前版本 ID',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_tpl_template_status (status),
    KEY idx_tpl_template_scenario (scenario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板主表';

-- 模板版本表
CREATE TABLE IF NOT EXISTS tpl_template_version (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    template_id BIGINT NOT NULL COMMENT '关联模板 ID',
    version_no VARCHAR(16) NOT NULL COMMENT '版本号',
    file_path VARCHAR(255) DEFAULT NULL COMMENT 'ZIP 文件存储路径',
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
