-- ========================================================
-- 补全缺失的表
-- ========================================================

-- 1. AI会话表
CREATE TABLE IF NOT EXISTS ai_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    model_id BIGINT DEFAULT NULL,
    session_title VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    message_count INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP DEFAULT NULL,
    ended_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ai_session_user_id ON ai_session (user_id);
CREATE INDEX IF NOT EXISTS idx_ai_session_status ON ai_session (status);

COMMENT ON TABLE ai_session IS 'AI会话表';
COMMENT ON COLUMN ai_session.status IS '会话状态：1-进行中，2-已归档';

-- 2. AI消息表
CREATE TABLE IF NOT EXISTS ai_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    tokens_used INT DEFAULT NULL,
    model_id BIGINT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_ai_message_session_id ON ai_message (session_id);

COMMENT ON TABLE ai_message IS 'AI消息表';
COMMENT ON COLUMN ai_message.role IS '角色：user-用户，assistant-助手';
COMMENT ON COLUMN ai_message.content IS '消息内容';
COMMENT ON COLUMN ai_message.tokens_used IS '消耗Token数';

-- 3. 库资源主表
CREATE TABLE IF NOT EXISTS lib_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_code VARCHAR(30) DEFAULT NULL,
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
COMMENT ON COLUMN lib_resource.status IS '状态：0-正常，1-上架，2-下架，3-违规';

-- 4. 库资源版本表
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

-- 5. 模板与库资源关联表
CREATE TABLE IF NOT EXISTS tpl_template_library (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_tpl_template_library UNIQUE (template_id, library_id)
);

CREATE INDEX IF NOT EXISTS idx_tpl_template_library_template_id ON tpl_template_library (template_id);

COMMENT ON TABLE tpl_template_library IS '模板与库资源关联表';

-- 6. 用户模板权限表
CREATE TABLE IF NOT EXISTS usr_user_template (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    access_type SMALLINT NOT NULL DEFAULT 1,
    expire_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_template UNIQUE (user_id, template_id)
);

CREATE INDEX IF NOT EXISTS idx_user_template_user_id ON usr_user_template (user_id);
CREATE INDEX IF NOT EXISTS idx_user_template_template_id ON usr_user_template (template_id);

COMMENT ON TABLE usr_user_template IS '用户模板权限表：记录用户对各模板的购买/授权关系';
COMMENT ON COLUMN usr_user_template.access_type IS '授权类型：1-免费授权，2-一次性购买';
COMMENT ON COLUMN usr_user_template.expire_at IS '授权过期时间，NULL表示永久有效';

-- ========================================================
-- 添加库资源相关的系统配置（如果不存在）
-- ========================================================

INSERT INTO sys_config (config_key, config_value, description, environment, is_editable, category, field_type, created_at, updated_at) VALUES
('storage.library.path', 'D:/Users/admin/Desktop/EISP/Resource/Library/', '库资源本地存储路径', 'all', 1, 'storage', 'text', NOW(), NOW()),
('storage.library.baseUrl', 'http://192.168.50.215/EISP/Resource/Library/', '库资源访问基础URL', 'all', 1, 'storage', 'text', NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;

-- ========================================================
-- 完成提示
-- ========================================================

DO $$ BEGIN
    RAISE NOTICE '✅ 缺失的表已添加完成！';
END $$;
