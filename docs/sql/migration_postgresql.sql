-- PostgreSQL 数据库迁移脚本
-- 为现有表添加菜单管理字段 + 创建新表

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

COMMENT ON COLUMN sys_permission.parent_id IS '父菜单ID，0表示根节点';
COMMENT ON COLUMN sys_permission.menu_type IS '类型：0-目录，1-菜单，2-按钮，3-外链';
COMMENT ON COLUMN sys_permission.sort_order IS '排序号';
COMMENT ON COLUMN sys_permission.icon IS '菜单图标';
COMMENT ON COLUMN sys_permission.route_path IS '路由路径';
COMMENT ON COLUMN sys_permission.component IS '组件路径';
COMMENT ON COLUMN sys_permission.is_visible IS '是否可见：0-隐藏，1-显示';
COMMENT ON COLUMN sys_permission.is_cache IS '是否缓存：0-否，1-是';

-- ========================================================
-- 2. 字典类型表
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
-- 3. 字典数据表
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
-- 4. 用户业务权限表
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