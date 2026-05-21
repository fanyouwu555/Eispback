-- PostgreSQL 数据库迁移脚本
-- 为现有表添加菜单管理字段 + 创建新表 + 迁移权限数据到树结构

-- ========================================================
-- 1. 扩展 sys_permission 表：菜单管理相关字段
-- ========================================================
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS parent_id BIGINT DEFAULT 0;
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS menu_type SMALLINT DEFAULT 0;
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS icon VARCHAR(100);
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS route_path VARCHAR(200);
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS component VARCHAR(255);
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS is_visible SMALLINT DEFAULT 1;
ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS is_cache SMALLINT DEFAULT 1;

-- 修改 permission_code 允许 NULL（目录/菜单不需要权限标识）
ALTER TABLE sys_permission
    ALTER COLUMN permission_code DROP NOT NULL;

-- 重新设置 permission_code 的默认值为 NULL
ALTER TABLE sys_permission
    ALTER COLUMN permission_code SET DEFAULT NULL;

COMMENT ON COLUMN sys_permission.parent_id IS '父菜单ID，0表示根节点';
COMMENT ON COLUMN sys_permission.menu_type IS '类型：0-目录，1-菜单，2-按钮，3-外链';
COMMENT ON COLUMN sys_permission.sort_order IS '排序号';
COMMENT ON COLUMN sys_permission.icon IS '菜单图标';
COMMENT ON COLUMN sys_permission.route_path IS '路由路径';
COMMENT ON COLUMN sys_permission.component IS '组件路径';
COMMENT ON COLUMN sys_permission.is_visible IS '是否可见：0-隐藏，1-显示';
COMMENT ON COLUMN sys_permission.is_cache IS '是否缓存：0-否，1-是';

-- 添加新索引
CREATE INDEX IF NOT EXISTS idx_sys_permission_parent_id ON sys_permission (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_permission_menu_type ON sys_permission (menu_type);

-- ========================================================
-- 2. 替换权限种子数据（从扁平 33 条替换为树形 78 条）
-- ========================================================

-- 2a. 清空旧的关联数据
DELETE FROM sys_role_permission;

-- 2b. 清空旧的权限数据
DELETE FROM sys_permission;

-- 2c. 重置序列
ALTER SEQUENCE sys_permission_id_seq RESTART WITH 1;

-- 2d. 插入新的树形权限数据
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
(9, '运营统计模块', 'operations', 'system', 'manage', NULL, 0, 0, 9, 'DataBoard', '/operations', NULL, 1, 1, NOW(), NOW(), 1, 1),
(10, '系统配置模块', 'sysconfig', 'system', 'manage', NULL, 0, 0, 10, 'Operation', '/sysconfig', NULL, 1, 1, NOW(), NOW(), 1, 1);

-- 仪表盘 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(11, '数据大盘', 'dashboard:overview', 'dashboard', 'read', NULL, 1, 1, 1, 'DataLine', '/dashboard', 'dashboard/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 系统管理 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(12, '菜单管理', 'system:menu', 'system', 'read', NULL, 2, 1, 1, NULL, '/system/menu', 'system/menu/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(13, '角色管理', 'system:role', 'system', 'read', NULL, 2, 1, 2, NULL, '/system/role', 'system/role/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(14, '管理员管理', 'system:user', 'system', 'read', NULL, 2, 1, 3, NULL, '/system/user', 'system/user/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(15, '操作日志', 'system:log', 'system', 'read', NULL, 2, 1, 4, NULL, '/system/log', 'system/log/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(16, '登录日志', 'system:login-log', 'system', 'read', NULL, 2, 1, 5, NULL, '/system/login-log', 'system/login-log/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(17, '数据字典', 'system:dict', 'system', 'read', NULL, 2, 1, 6, NULL, '/system/dict', 'system/dict/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(18, '系统配置', 'system:config', 'system', 'read', NULL, 2, 1, 7, NULL, '/system/config', 'system/config/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 用户与权限 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(19, '用户管理', 'user:list', 'user', 'read', NULL, 3, 1, 1, NULL, '/user/list', 'user/list/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(20, '权限分配', 'user:permission', 'user', 'read', NULL, 3, 1, 2, NULL, '/user/permission', 'user/permission/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 资产与计费 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(21, '用户余额', 'finance:balance', 'finance', 'read', NULL, 4, 1, 1, NULL, '/finance/balance', 'recharge/balance/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(22, '充值记录', 'finance:order', 'finance', 'read', NULL, 4, 1, 2, NULL, '/finance/order', 'recharge/order/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(23, '扣费记录', 'finance:deduction', 'finance', 'read', NULL, 4, 1, 3, NULL, '/finance/deduction', 'recharge/deduction/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(24, '模型购买', 'finance:purchase', 'finance', 'read', NULL, 4, 1, 4, NULL, '/finance/purchase', 'recharge/purchase/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(25, '套餐管理', 'finance:package', 'finance', 'read', NULL, 4, 1, 5, NULL, '/finance/package', 'recharge/package/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 消息管理 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(26, '系统通告', 'notification:list', 'notification', 'read', NULL, 5, 1, 1, NULL, '/message/notification', 'message/notification/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 项目管理 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(27, '项目列表', 'project:list-page', 'project', 'read', NULL, 6, 1, 1, NULL, '/project/list', 'project/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 模板资源 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(28, '模板分类', 'template:category', 'template', 'read', NULL, 7, 1, 1, NULL, '/template/category', 'template/category/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(29, '模板管理', 'template:list', 'template', 'read', NULL, 7, 1, 2, NULL, '/template/list', 'template/list/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- AI对话模块 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(30, '会话管理', 'ai:session', 'ai', 'read', NULL, 8, 1, 1, NULL, '/ai/session', 'ai/session/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(31, 'AI配置', 'ai:config', 'ai', 'read', NULL, 8, 1, 2, NULL, '/ai/config', 'model/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(32, 'AI统计', 'ai:stats', 'ai', 'read', NULL, 8, 1, 3, NULL, '/ai/stats', 'model/stats/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 运营统计模块 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(33, '数据大盘', 'operations:dashboard', 'operations', 'read', NULL, 9, 1, 1, NULL, '/operations/dashboard', 'operations/dashboard/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(34, '系统监控', 'operations:monitor', 'operations', 'read', NULL, 9, 1, 2, NULL, '/operations/monitor', 'operations/monitor/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 系统配置模块 -> 菜单
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
(35, '基础配置', 'sysconfig:base', 'sysconfig', 'read', NULL, 10, 1, 1, NULL, '/sysconfig/base', 'system/config/index.vue', 1, 1, NOW(), NOW(), 1, 1),
(36, '功能开关', 'sysconfig:feature', 'sysconfig', 'read', NULL, 10, 1, 2, NULL, '/sysconfig/feature', 'system/feature/index.vue', 1, 1, NOW(), NOW(), 1, 1);

-- 按钮权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action, description, parent_id, menu_type, sort_order, icon, route_path, component, is_visible, is_cache, created_at, updated_at, created_by, updated_by) VALUES
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
-- 系统管理 -> 系统配置 -> 按钮
(44, '系统配置操作', 'system:config:update', 'system', 'update', NULL, 18, 2, 1, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1),
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

-- 2e. 插入新的角色-权限映射
-- 超级管理员：拥有全部 78 个权限
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
(1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), (1, 4, NOW()), (1, 5, NOW()), (1, 6, NOW()), (1, 7, NOW()), (1, 8, NOW()), (1, 9, NOW()), (1, 10, NOW()),
(1, 11, NOW()), (1, 12, NOW()), (1, 13, NOW()), (1, 14, NOW()), (1, 15, NOW()), (1, 16, NOW()), (1, 17, NOW()), (1, 18, NOW()), (1, 19, NOW()), (1, 20, NOW()),
(1, 21, NOW()), (1, 22, NOW()), (1, 23, NOW()), (1, 24, NOW()), (1, 25, NOW()), (1, 26, NOW()), (1, 27, NOW()), (1, 28, NOW()), (1, 29, NOW()), (1, 30, NOW()),
(1, 31, NOW()), (1, 32, NOW()), (1, 33, NOW()), (1, 34, NOW()), (1, 35, NOW()), (1, 36, NOW()), (1, 37, NOW()), (1, 38, NOW()), (1, 39, NOW()), (1, 40, NOW()),
(1, 41, NOW()), (1, 42, NOW()), (1, 43, NOW()), (1, 44, NOW()), (1, 45, NOW()), (1, 46, NOW()), (1, 47, NOW()), (1, 48, NOW()), (1, 49, NOW()), (1, 50, NOW()),
(1, 51, NOW()), (1, 52, NOW()), (1, 53, NOW()), (1, 54, NOW()), (1, 55, NOW()), (1, 56, NOW()), (1, 57, NOW()), (1, 58, NOW()), (1, 59, NOW()), (1, 60, NOW()),
(1, 61, NOW()), (1, 62, NOW()), (1, 63, NOW()), (1, 64, NOW()), (1, 65, NOW()), (1, 66, NOW()), (1, 67, NOW()), (1, 68, NOW()), (1, 69, NOW()), (1, 70, NOW()),
(1, 71, NOW()), (1, 72, NOW()), (1, 73, NOW()), (1, 74, NOW()), (1, 75, NOW()), (1, 76, NOW()), (1, 77, NOW()), (1, 78, NOW());

-- 用户管理员：用户管理相关按钮 + 目录/菜单可见性
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 目录可见性
(2, 3, NOW()),
-- 菜单可见性
(2, 19, NOW()), (2, 20, NOW()),
-- 按钮权限
(2, 45, NOW()), (2, 46, NOW()), (2, 47, NOW()), (2, 48, NOW()), (2, 49, NOW()), (2, 50, NOW()), (2, 51, NOW()),
(2, 52, NOW()), (2, 53, NOW());

-- 模型管理员：AI配置相关按钮 + AI菜单可见性
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 目录可见性
(3, 8, NOW()),
-- 菜单可见性
(3, 30, NOW()), (3, 31, NOW()), (3, 32, NOW()),
-- 按钮权限
(3, 71, NOW()), (3, 72, NOW()),
(3, 73, NOW()), (3, 74, NOW()), (3, 75, NOW()), (3, 76, NOW()), (3, 77, NOW()), (3, 78, NOW());

-- 消息管理员：系统通告相关按钮 + 消息目录可见性
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 目录可见性
(4, 5, NOW()),
-- 菜单可见性
(4, 26, NOW()),
-- 按钮权限
(4, 58, NOW()), (4, 59, NOW()), (4, 60, NOW()), (4, 61, NOW()), (4, 62, NOW());

-- 模板管理员：模板管理相关按钮 + 模板目录可见性
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 目录可见性
(5, 7, NOW()),
-- 菜单可见性
(5, 28, NOW()), (5, 29, NOW()),
-- 按钮权限
(5, 65, NOW()),
(5, 66, NOW()), (5, 67, NOW()), (5, 68, NOW()), (5, 69, NOW()), (5, 70, NOW());

-- 财务管理员：资产计费相关按钮 + 资产目录可见性
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
-- 目录可见性
(6, 4, NOW()),
-- 菜单可见性
(6, 21, NOW()), (6, 22, NOW()), (6, 23, NOW()), (6, 24, NOW()), (6, 25, NOW()),
-- 按钮权限
(6, 54, NOW()), (6, 55, NOW()), (6, 56, NOW()), (6, 57, NOW());

-- 2f. 更新序列
SELECT setval('sys_permission_id_seq', 78);
SELECT setval('sys_role_permission_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_role_permission));

-- ========================================================
-- 3. 字典类型表
-- ========================================================
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGSERIAL PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_code VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    is_system SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_dict_type_code UNIQUE (dict_code)
);

CREATE INDEX IF NOT EXISTS idx_sys_dict_type_status ON sys_dict_type(status);

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称（如"用户状态"）';
COMMENT ON COLUMN sys_dict_type.dict_code IS '字典标识（如 user_status）';
COMMENT ON COLUMN sys_dict_type.description IS '备注';
COMMENT ON COLUMN sys_dict_type.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN sys_dict_type.is_system IS '是否系统内置：0-非系统，1-系统（禁止删除）';

-- ========================================================
-- 4. 字典数据表
-- ========================================================
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGSERIAL PRIMARY KEY,
    dict_code VARCHAR(100) NOT NULL,
    item_label VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    color VARCHAR(50),
    is_default SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sys_dict_data_dict_code ON sys_dict_data(dict_code);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_sort_order ON sys_dict_data(sort_order);

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_code IS '关联 sys_dict_type.dict_code';
COMMENT ON COLUMN sys_dict_data.item_label IS '展示标签';
COMMENT ON COLUMN sys_dict_data.item_value IS '实际值';
COMMENT ON COLUMN sys_dict_data.sort_order IS '排序';
COMMENT ON COLUMN sys_dict_data.status IS '状态：0-停用，1-启用';
COMMENT ON COLUMN sys_dict_data.color IS '标签颜色（如 success/warning/info/danger）';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认：0-否，1-是';

-- ========================================================
-- 5. 用户业务权限表
-- ========================================================
CREATE TABLE IF NOT EXISTS usr_user_permission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    perm_key VARCHAR(100) NOT NULL,
    perm_value VARCHAR(500) NOT NULL DEFAULT '',
    effective_at TIMESTAMP,
    expire_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_usr_user_perm UNIQUE (user_id, perm_key)
);

CREATE INDEX IF NOT EXISTS idx_usr_user_permission_user_id ON usr_user_permission(user_id);

COMMENT ON TABLE usr_user_permission IS '用户业务权限表';
COMMENT ON COLUMN usr_user_permission.user_id IS '用户ID';
COMMENT ON COLUMN usr_user_permission.perm_key IS '权限键（如 ai_dialog_enabled）';
COMMENT ON COLUMN usr_user_permission.perm_value IS '权限值（如 true/10/配额值）';
COMMENT ON COLUMN usr_user_permission.effective_at IS '生效时间';
COMMENT ON COLUMN usr_user_permission.expire_at IS '过期时间';