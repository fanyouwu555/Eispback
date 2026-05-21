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
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置角色：0-自定义，1-系统内置（不可删除）',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code),
    KEY idx_sys_role_status (status),
    KEY idx_sys_role_is_system (is_system)
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

-- 扩展权限表：菜单管理相关字段
ALTER TABLE sys_permission
    ADD COLUMN parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示根节点' AFTER description,
    ADD COLUMN menu_type TINYINT DEFAULT 0 COMMENT '类型：0-目录，1-菜单，2-按钮，3-外链' AFTER parent_id,
    ADD COLUMN sort_order INT DEFAULT 0 COMMENT '排序号' AFTER menu_type,
    ADD COLUMN icon VARCHAR(100) DEFAULT NULL COMMENT '菜单图标' AFTER sort_order,
    ADD COLUMN route_path VARCHAR(200) DEFAULT NULL COMMENT '路由路径' AFTER icon,
    ADD COLUMN component VARCHAR(255) DEFAULT NULL COMMENT '组件路径' AFTER route_path,
    ADD COLUMN is_visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示' AFTER component,
    ADD COLUMN is_cache TINYINT DEFAULT 1 COMMENT '是否缓存：0-否，1-是' AFTER is_visible;

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
    role_name VARCHAR(50) DEFAULT NULL COMMENT '操作人角色名',
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

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称（如"用户状态"）',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典标识（如 user_status）',
    description VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置：0-非系统，1-系统（禁止删除）',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_type_code (dict_code),
    KEY idx_sys_dict_type_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    dict_code VARCHAR(100) NOT NULL COMMENT '关联 sys_dict_type.dict_code',
    item_label VARCHAR(100) NOT NULL COMMENT '展示标签',
    item_value VARCHAR(100) NOT NULL COMMENT '实际值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    color VARCHAR(50) DEFAULT NULL COMMENT '标签颜色（如 success/warning/info/danger）',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_sys_dict_data_dict_code (dict_code),
    KEY idx_sys_dict_data_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 字典类型种子数据
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

-- 字典数据种子数据
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
('notification_status', '草稿', '0', 1, 1, 'info', 1),
('notification_status', '已推送', '1', 2, 1, 'success', 0),
('notification_status', '已撤回', '2', 3, 1, 'warning', 0),
('notification_status', '已归档', '3', 4, 1, '', 0),
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
    is_competition TINYINT NOT NULL DEFAULT 0 COMMENT '是否比赛用户：0-否，1-是',
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

-- 用户业务权限表
CREATE TABLE IF NOT EXISTS usr_user_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    perm_key VARCHAR(100) NOT NULL COMMENT '权限键（如 ai_dialog_enabled）',
    perm_value VARCHAR(500) NOT NULL DEFAULT '' COMMENT '权限值（如 true/10/配额值）',
    effective_at DATETIME DEFAULT NULL COMMENT '生效时间',
    expire_at DATETIME DEFAULT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人 ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_usr_user_perm (user_id, perm_key),
    KEY idx_usr_user_permission_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户业务权限表';

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
INSERT INTO sys_role (id, role_name, role_code, description, status, is_system, created_at, updated_at) VALUES
(1, '超级管理员', 'ROLE_SUPER_ADMIN', '系统最高权限，拥有所有操作权限', 1, 1, NOW(), NOW()),
(2, '用户管理员', 'ROLE_USER_MANAGER', '负责用户账号的全生命周期管理', 1, 1, NOW(), NOW()),
(3, '模型管理员', 'ROLE_MODEL_MANAGER', '负责AI模型的接入、配置、测试、状态管理', 1, 1, NOW(), NOW()),
(4, '消息管理员', 'ROLE_NOTIFICATION_MANAGER', '负责平台消息的内容编辑、推送管理、撤回和效果监控', 1, 1, NOW(), NOW()),
(5, '模板管理员', 'ROLE_TEMPLATE_MANAGER', '负责编程模板的上传、版本管理、上下线控制和统计分析', 1, 1, NOW(), NOW()),
(6, '财务管理员', 'ROLE_FINANCE_MANAGER', '负责充值套餐配置、订单管理、退款审核和余额调整', 1, 1, NOW(), NOW()),
(7, '普通用户', 'ROLE_USER', '平台注册用户默认角色', 1, 1, NOW(), NOW());

-- 初始化权限点
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, created_at, updated_at, created_by, updated_by) VALUES
(1, '创建用户', 'user:create', 'user', 'create', NULL, NOW(), NOW(), 1, 1),
(2, '查看用户', 'user:read', 'user', 'read', NULL, NOW(), NOW(), 1, 1),
(3, '修改用户', 'user:update', 'user', 'update', NULL, NOW(), NOW(), 1, 1),
(4, '删除用户', 'user:delete', 'user', 'delete', NULL, NOW(), NOW(), 1, 1),
(5, '修改用户状态', 'user:status:update', 'user', 'update', NULL, NOW(), NOW(), 1, 1),
(6, '重置用户密码', 'user:password:reset', 'user', 'update', NULL, NOW(), NOW(), 1, 1),
(7, '调整用户时长', 'user:duration:adjust', 'user', 'update', NULL, NOW(), NOW(), 1, 1),
(8, '接入模型', 'model:create', 'model', 'create', NULL, NOW(), NOW(), 1, 1),
(9, '查看模型', 'model:read', 'model', 'read', NULL, NOW(), NOW(), 1, 1),
(10, '修改模型', 'model:update', 'model', 'update', NULL, NOW(), NOW(), 1, 1),
(11, '删除模型', 'model:delete', 'model', 'delete', NULL, NOW(), NOW(), 1, 1),
(12, '配置模型', 'model:config', 'model', 'update', NULL, NOW(), NOW(), 1, 1),
(13, '测试模型', 'model:test', 'model', 'read', NULL, NOW(), NOW(), 1, 1),
(14, '管理订单', 'order:manage', 'order', 'manage', NULL, NOW(), NOW(), 1, 1),
(15, '订单退款', 'order:refund', 'order', 'update', NULL, NOW(), NOW(), 1, 1),
(16, '创建消息', 'notification:create', 'notification', 'create', NULL, NOW(), NOW(), 1, 1),
(17, '查看消息', 'notification:read', 'notification', 'read', NULL, NOW(), NOW(), 1, 1),
(18, '修改消息', 'notification:update', 'notification', 'update', NULL, NOW(), NOW(), 1, 1),
(19, '删除消息', 'notification:delete', 'notification', 'delete', NULL, NOW(), NOW(), 1, 1),
(20, '发送消息', 'notification:send', 'notification', 'create', NULL, NOW(), NOW(), 1, 1),
(21, '创建模板', 'template:create', 'template', 'create', NULL, NOW(), NOW(), 1, 1),
(22, '查看模板', 'template:read', 'template', 'read', NULL, NOW(), NOW(), 1, 1),
(23, '修改模板', 'template:update', 'template', 'update', NULL, NOW(), NOW(), 1, 1),
(24, '删除模板', 'template:delete', 'template', 'delete', NULL, NOW(), NOW(), 1, 1),
(25, '管理模板版本', 'template:version:manage', 'template', 'update', NULL, NOW(), NOW(), 1, 1),
(26, '系统配置', 'system:config', 'system', 'update', NULL, NOW(), NOW(), 1, 1),
(27, '角色管理', 'system:role:manage', 'system', 'manage', NULL, NOW(), NOW(), 1, 1),
(28, '用户管理', 'system:user:manage', 'system', 'manage', NULL, NOW(), NOW(), 1, 1),
(29, '查看权限点', 'system:permission:read', 'system', 'read', NULL, NOW(), NOW(), 1, 1),
(30, '查看操作日志', 'admin:log:read', 'system', 'read', NULL, NOW(), NOW(), 1, 1),
(31, '管理充值套餐', 'finance:package:manage', 'finance', 'manage', NULL, NOW(), NOW(), 1, 1),
(32, '管理财务订单', 'finance:order:manage', 'finance', 'manage', NULL, NOW(), NOW(), 1, 1),
(33, '调整余额', 'finance:balance:adjust', 'finance', 'update', NULL, NOW(), NOW(), 1, 1);

-- 初始化角色权限映射
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 超级管理员
(1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), (1, 4, NOW()), (1, 5, NOW()), (1, 6, NOW()), (1, 7, NOW()),
(1, 8, NOW()), (1, 9, NOW()), (1, 10, NOW()), (1, 11, NOW()), (1, 12, NOW()), (1, 13, NOW()),
(1, 14, NOW()), (1, 15, NOW()),
(1, 16, NOW()), (1, 17, NOW()), (1, 18, NOW()), (1, 19, NOW()), (1, 20, NOW()),
(1, 21, NOW()), (1, 22, NOW()), (1, 23, NOW()), (1, 24, NOW()), (1, 25, NOW()),
(1, 26, NOW()), (1, 27, NOW()), (1, 28, NOW()), (1, 29, NOW()), (1, 30, NOW()),
(1, 31, NOW()), (1, 32, NOW()), (1, 33, NOW()),
-- 用户管理员
(2, 1, NOW()), (2, 2, NOW()), (2, 3, NOW()), (2, 4, NOW()), (2, 5, NOW()), (2, 6, NOW()), (2, 7, NOW()),
-- 模型管理员
(3, 8, NOW()), (3, 9, NOW()), (3, 10, NOW()), (3, 11, NOW()), (3, 12, NOW()), (3, 13, NOW()),
-- 消息管理员
(4, 16, NOW()), (4, 17, NOW()), (4, 18, NOW()), (4, 19, NOW()), (4, 20, NOW()),
-- 模板管理员
(5, 21, NOW()), (5, 22, NOW()), (5, 23, NOW()), (5, 24, NOW()), (5, 25, NOW()),
-- 财务管理员
(6, 14, NOW()), (6, 15, NOW()), (6, 31, NOW()), (6, 32, NOW()), (6, 33, NOW());

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
('official_website_url', 'https://dev.example.com', 'dev', '开发环境官网地址', NOW(), NOW()),
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
