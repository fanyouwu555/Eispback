-- 为已存在的 usr_user 表增加 api_key 和 tenant_id 字段
ALTER TABLE usr_user
    ADD COLUMN IF NOT EXISTS api_key VARCHAR(128) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) DEFAULT NULL;

COMMENT ON COLUMN usr_user.api_key IS 'API 访问密钥';
COMMENT ON COLUMN usr_user.tenant_id IS '租户 ID';
