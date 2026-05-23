-- 用户模板权限表：记录用户已购买/已授权的模板
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
COMMENT ON COLUMN usr_user_template.expire_at IS '授权过期时间，NULL 表示永久有效';
