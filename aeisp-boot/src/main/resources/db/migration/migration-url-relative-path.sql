-- ============================================================
-- 数据迁移：将数据库中存储的完整 URL 截断为相对路径
-- 目的：HFS 服务器端口变化时，无需修改历史数据
-- ============================================================
-- 执行前请备份数据库！
-- 执行后请重启后端服务以加载新配置
-- ============================================================

-- 1. prj_project.resource_url — 项目资源路径
UPDATE prj_project
SET resource_url = SUBSTRING(resource_url FROM LENGTH('http://192.168.50.215:4050/EISP/Users/') + 1)
WHERE resource_url LIKE 'http://192.168.50.215:4050/EISP/Users/%';

-- 兼容旧端口（无 :4050）
UPDATE prj_project
SET resource_url = SUBSTRING(resource_url FROM LENGTH('http://192.168.50.215/EISP/Users/') + 1)
WHERE resource_url LIKE 'http://192.168.50.215/EISP/Users/%';

-- 2. tpl_template.preview_image — 封面图
UPDATE tpl_template
SET preview_image = SUBSTRING(preview_image FROM LENGTH('http://192.168.50.215:4050/EISP/Resource/Template/') + 1)
WHERE preview_image LIKE 'http://192.168.50.215:4050/EISP/Resource/Template/%';

UPDATE tpl_template
SET preview_image = SUBSTRING(preview_image FROM LENGTH('http://192.168.50.215/EISP/Resource/Template/') + 1)
WHERE preview_image LIKE 'http://192.168.50.215/EISP/Resource/Template/%';

-- 3. tpl_template.thumbnail — 缩略图
UPDATE tpl_template
SET thumbnail = SUBSTRING(thumbnail FROM LENGTH('http://192.168.50.215:4050/EISP/Resource/Template/') + 1)
WHERE thumbnail LIKE 'http://192.168.50.215:4050/EISP/Resource/Template/%';

UPDATE tpl_template
SET thumbnail = SUBSTRING(thumbnail FROM LENGTH('http://192.168.50.215/EISP/Resource/Template/') + 1)
WHERE thumbnail LIKE 'http://192.168.50.215/EISP/Resource/Template/%';

-- 4. tpl_template_version.storage_url — 模板版本存储路径
UPDATE tpl_template_version
SET storage_url = SUBSTRING(storage_url FROM LENGTH('http://192.168.50.215:4050/EISP/Resource/Template/') + 1)
WHERE storage_url LIKE 'http://192.168.50.215:4050/EISP/Resource/Template/%';

UPDATE tpl_template_version
SET storage_url = SUBSTRING(storage_url FROM LENGTH('http://192.168.50.215/EISP/Resource/Template/') + 1)
WHERE storage_url LIKE 'http://192.168.50.215/EISP/Resource/Template/%';

-- 5. lib_resource_version.storage_url — 库资源版本存储路径
UPDATE lib_resource_version
SET storage_url = SUBSTRING(storage_url FROM LENGTH('http://192.168.50.215:4050/EISP/Resource/Library/') + 1)
WHERE storage_url LIKE 'http://192.168.50.215:4050/EISP/Resource/Library/%';

UPDATE lib_resource_version
SET storage_url = SUBSTRING(storage_url FROM LENGTH('http://192.168.50.215/EISP/Resource/Library/') + 1)
WHERE storage_url LIKE 'http://192.168.50.215/EISP/Resource/Library/%';

-- 6. sys_config — 库资源 baseUrl 配置（改为相对路径前缀）
UPDATE sys_config
SET config_value = 'EISP/Resource/Library/'
WHERE config_key = 'storage.library.baseUrl'
  AND config_value LIKE 'http://192.168.50.215%';

-- 7. 验证：检查是否还有残留完整 URL（应该返回 0 条）
SELECT 'prj_project.resource_url' AS table_col, COUNT(*) AS残留数量
FROM prj_project WHERE resource_url LIKE 'http://%'
UNION ALL
SELECT 'tpl_template.preview_image', COUNT(*)
FROM tpl_template WHERE preview_image LIKE 'http://%'
UNION ALL
SELECT 'tpl_template.thumbnail', COUNT(*)
FROM tpl_template WHERE thumbnail LIKE 'http://%'
UNION ALL
SELECT 'tpl_template_version.storage_url', COUNT(*)
FROM tpl_template_version WHERE storage_url LIKE 'http://%'
UNION ALL
SELECT 'lib_resource_version.storage_url', COUNT(*)
FROM lib_resource_version WHERE storage_url LIKE 'http://%'
UNION ALL
SELECT 'sys_config.storage.library.baseUrl', COUNT(*)
FROM sys_config WHERE config_key = 'storage.library.baseUrl' AND config_value LIKE 'http://%';
