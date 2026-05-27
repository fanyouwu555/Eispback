-- ============================================================
-- 系统公告模块修复与增强 — DB 迁移
-- 对应 spec: docs/superpowers/specs/2026-05-26-system-announcement-enhancement.md
-- ============================================================

-- 1）修复 notification_status 字典：对齐后端常量 1=草稿/2=已发送/3=已撤回/4=定时发送/5=已归档
--    并新增 6=已过期
DELETE FROM sys_dict_data WHERE dict_code = 'notification_status';

INSERT INTO sys_dict_data (dict_code, item_label, item_value, sort_order, status, color, is_default) VALUES
('notification_status', '草稿', '1', 1, 1, 'info', 1),
('notification_status', '已发送', '2', 2, 1, 'success', 0),
('notification_status', '已撤回', '3', 3, 1, 'warning', 0),
('notification_status', '定时发送', '4', 4, 1, 'primary', 0),
('notification_status', '已归档', '5', 5, 1, '', 0),
('notification_status', '已过期', '6', 6, 1, 'danger', 0);

-- 2）msg_notification 新增摘要字段
ALTER TABLE msg_notification ADD COLUMN IF NOT EXISTS summary VARCHAR(256) DEFAULT NULL;
COMMENT ON COLUMN msg_notification.summary IS '消息摘要/简介';