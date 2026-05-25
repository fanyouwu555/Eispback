-- ============================================================
-- Migration: 模板系统功能对齐
-- Date: 2026-05-25
-- Description: 移除 scenario 字段（统一三级分类体系）
-- ============================================================

-- 1. 移除 scenario 字段（统一三级分类体系）
ALTER TABLE tpl_template DROP COLUMN IF EXISTS scenario;

-- 2. 补充/更新字段注释
COMMENT ON COLUMN tpl_template.difficulty IS '难度等级：1-入门, 2-初级, 3-中级, 4-高级, 5-专家';
COMMENT ON COLUMN tpl_template.template_code IS '模板编码（自动生成：TPL+yyyyMMdd+4位序号）';
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
COMMENT ON COLUMN tpl_template.status IS '状态：0-正常, 1-上架, 2-下架, 3-违规';