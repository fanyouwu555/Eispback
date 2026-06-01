-- 修复 sys_config 分类数据
-- 运行前提：migration_postgresql.sql 已执行（category 和 field_type 字段已存在）

-- 1. 更新现有数据的 category 和 field_type
UPDATE sys_config SET category = 'platform', field_type = 'text'
    WHERE config_key LIKE 'system.%'
       OR config_key IN ('official_website_url', 'contact_email', 'contact_phone');

UPDATE sys_config SET category = 'threshold', field_type = 'number'
    WHERE config_key LIKE '%threshold%'
       OR config_key LIKE '%max%'
       OR config_key LIKE '%min%'
       OR config_key IN ('default_register_duration', 'default_model_max_qps');

UPDATE sys_config SET category = 'timeout', field_type = 'number'
    WHERE config_key LIKE '%minutes%'
       OR config_key LIKE '%seconds%'
       OR config_key LIKE '%days%';

UPDATE sys_config SET category = 'general', field_type = 'text'
    WHERE category = 'general' AND config_key NOT LIKE 'system.%'
       AND config_key NOT IN ('official_website_url', 'contact_email', 'contact_phone');
