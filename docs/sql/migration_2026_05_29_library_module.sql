-- ========================================================
-- AEISP 库资源模块数据库迁移脚本（PostgreSQL）
-- Date: 2026-05-29
-- ========================================================

-- 1. 库资源主表
CREATE TABLE IF NOT EXISTS lib_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_code VARCHAR(30) NOT NULL,
    resource_name VARCHAR(64) NOT NULL,
    description VARCHAR(512) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    current_version_id BIGINT DEFAULT NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    violation_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_code UNIQUE (resource_code)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_status ON lib_resource (status);
COMMENT ON TABLE lib_resource IS '库资源主表';
COMMENT ON COLUMN lib_resource.resource_code IS '资源编码（自动生成：LIB+yyyyMMdd+4位序号）';
COMMENT ON COLUMN lib_resource.status IS '状态：0-正常, 1-上架, 2-下架, 3-违规';

-- 2. 库资源版本表
CREATE TABLE IF NOT EXISTS lib_resource_version (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    version_no VARCHAR(16) NOT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
    storage_url VARCHAR(255) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_hash VARCHAR(64) DEFAULT NULL,
    changelog TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_version UNIQUE (resource_id, version_no)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_version_resource_id ON lib_resource_version (resource_id);
COMMENT ON TABLE lib_resource_version IS '库资源版本表';
COMMENT ON COLUMN lib_resource_version.file_size IS '文件大小（字节）';
COMMENT ON COLUMN lib_resource_version.file_hash IS '文件哈希值（MD5）';

-- 3. 模板与库资源关联表
CREATE TABLE IF NOT EXISTS tpl_template_library (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_tpl_template_library UNIQUE (template_id, library_id)
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_library_template_id ON tpl_template_library (template_id);
COMMENT ON TABLE tpl_template_library IS '模板与库资源关联表';

-- 4. 系统配置
INSERT INTO sys_config (config_key, config_value, description, environment, is_editable)
VALUES ('storage.library.path', 'D:/Users/admin/Desktop/EISP/Resource/Library/', '库资源本地存储路径', 'all', 1)
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO sys_config (config_key, config_value, description, environment, is_editable)
VALUES ('storage.library.baseUrl', 'http://192.168.50.215/EISP/Resource/Library/', '库资源访问基础URL', 'all', 1)
ON CONFLICT (config_key) DO NOTHING;

-- 5. 权限菜单
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible)
VALUES ('库资源管理', 'library:manage', 'library', 'manage', 0, 0, 40, 'Collection', '/library', NULL, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible)
VALUES ('库资源列表', 'library:list', 'library', 'list', (SELECT id FROM sys_permission WHERE permission_code = 'library:manage'), 1, 1, 'List', 'list', 'library/list/index', 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('新增库资源', 'library:create', 'library', 'create', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 1, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('编辑库资源', 'library:update', 'library', 'update', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 2, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('删除库资源', 'library:delete', 'library', 'delete', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 3, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('查询库资源', 'library:read', 'library', 'read', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 4, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('版本管理', 'library:version:manage', 'library', 'manage', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 5, 1)
ON CONFLICT (permission_code) DO NOTHING;
