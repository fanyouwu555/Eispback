-- 2026-05-21 用户与权限模块增量迁移

-- 1. usr_user 表新增字段
ALTER TABLE usr_user ADD COLUMN IF NOT EXISTS login_count INT NOT NULL DEFAULT 0;
ALTER TABLE usr_user ADD COLUMN IF NOT EXISTS abnormal_login SMALLINT NOT NULL DEFAULT 0;

-- 2. 用户业务权限表（如缺失则创建）
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

-- 3. 用户权限变更日志表
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

-- 4. 模板使用记录表
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

-- 注释
COMMENT ON TABLE usr_user_permission IS '用户业务权限表';
COMMENT ON TABLE usr_user_permission_log IS '用户权限变更日志表';
COMMENT ON TABLE usr_template_usage_log IS '模板使用记录表';
COMMENT ON COLUMN usr_user.login_count IS '累计登录次数';
COMMENT ON COLUMN usr_user.abnormal_login IS '是否异地登录：0-否，1-是';
