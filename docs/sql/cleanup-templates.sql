-- 清理所有模板数据（按外键依赖顺序）
-- 执行前确保已备份重要数据

DELETE FROM tpl_template_preview_image;
DELETE FROM usr_template_usage_log;
DELETE FROM usr_user_template;
DELETE FROM tpl_template_version;
DELETE FROM tpl_template;