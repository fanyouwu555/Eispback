-- 扩展模板主表字段
ALTER TABLE tpl_template
    ADD COLUMN IF NOT EXISTS top_category_id BIGINT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS first_category_id BIGINT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS second_category_id BIGINT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS is_paid SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS fee_type VARCHAR(32) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS price DECIMAL(10,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS online_time TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS valid_time TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS creator VARCHAR(64) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS produce_date DATE DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS download_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS favorite_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS visit_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS detail_desc TEXT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS violation_reason VARCHAR(255) DEFAULT NULL;

COMMENT ON COLUMN tpl_template.top_category_id IS '顶级分类 ID';
COMMENT ON COLUMN tpl_template.first_category_id IS '一级分类 ID';
COMMENT ON COLUMN tpl_template.second_category_id IS '二级分类 ID';
COMMENT ON COLUMN tpl_template.is_paid IS '是否付费：0-免费，1-付费';
COMMENT ON COLUMN tpl_template.fee_type IS '费用类型（字典 template_fee_type）';
COMMENT ON COLUMN tpl_template.price IS '价格（元）';
COMMENT ON COLUMN tpl_template.online_time IS '上线时间';
COMMENT ON COLUMN tpl_template.valid_time IS '有效截止时间';
COMMENT ON COLUMN tpl_template.creator IS '创作者名称';
COMMENT ON COLUMN tpl_template.produce_date IS '创作时间';
COMMENT ON COLUMN tpl_template.download_count IS '下载次数';
COMMENT ON COLUMN tpl_template.favorite_count IS '收藏数';
COMMENT ON COLUMN tpl_template.visit_count IS '访问量';
COMMENT ON COLUMN tpl_template.detail_desc IS '详细描述';
COMMENT ON COLUMN tpl_template.violation_reason IS '违规原因';

-- 字典类型：模板状态（扩展后）
INSERT INTO sys_dict_type (dict_name, dict_code, description, status, is_system)
VALUES ('模板状态', 'template_status', '模板上下架及违规状态', 1, 1)
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, item_label, item_value, sort_order, status, color, is_default) VALUES
('template_status', '正常', '0', 1, 1, 'info', 1),
('template_status', '上架', '1', 2, 1, 'success', 0),
('template_status', '下架', '2', 3, 1, 'danger', 0),
('template_status', '违规', '3', 4, 1, 'warning', 0)
ON CONFLICT DO NOTHING;

-- 字典类型：模板费用类型
INSERT INTO sys_dict_type (dict_name, dict_code, description, status, is_system)
VALUES ('模板费用类型', 'template_fee_type', '模板付费计费方式', 1, 1)
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, item_label, item_value, sort_order, status, color, is_default) VALUES
('template_fee_type', '免费', 'free', 1, 1, 'success', 1),
('template_fee_type', '一次性付费', 'onetime', 2, 1, 'primary', 0),
('template_fee_type', '订阅制', 'subscription', 3, 1, 'warning', 0)
ON CONFLICT DO NOTHING;
