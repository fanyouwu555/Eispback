-- ============================================================
-- 测试用管理员账号（按角色分配）
-- 用途：验证不同角色的前端权限显示是否正确
-- 密码统一为 admin123（BCrypt 加密）
-- ============================================================

-- 创建测试管理员用户（ID 从 100 起，避免与种子数据冲突）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, created_at, updated_at)
VALUES
  (101, 'user_mgr',    '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '用户管理员',   'user_mgr@test.com',    '13900000101', 1, NOW(), NOW()),
  (102, 'model_mgr',   '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '模型管理员',   'model_mgr@test.com',   '13900000102', 1, NOW(), NOW()),
  (103, 'msg_mgr',     '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '消息管理员',   'msg_mgr@test.com',     '13900000103', 1, NOW(), NOW()),
  (104, 'tpl_mgr',     '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '模板管理员',   'tpl_mgr@test.com',     '13900000104', 1, NOW(), NOW()),
  (105, 'finance_mgr', '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '财务管理员',   'finance_mgr@test.com', '13900000105', 1, NOW(), NOW()),
  (106, 'normal_user', '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '普通用户',     'normal_user@test.com', '13900000106', 1, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
  username = EXCLUDED.username,
  password = EXCLUDED.password,
  real_name = EXCLUDED.real_name,
  status = EXCLUDED.status,
  updated_at = NOW();

-- 分配角色
INSERT INTO sys_user_role (user_id, role_id)
VALUES
  (101, 2),  -- user_mgr    → ROLE_USER_MANAGER
  (102, 3),  -- model_mgr   → ROLE_MODEL_MANAGER
  (103, 4),  -- msg_mgr     → ROLE_NOTIFICATION_MANAGER
  (104, 5),  -- tpl_mgr     → ROLE_TEMPLATE_MANAGER
  (105, 6),  -- finance_mgr → ROLE_FINANCE_MANAGER
  (106, 7)   -- normal_user → ROLE_USER
ON CONFLICT (user_id, role_id) DO NOTHING;
