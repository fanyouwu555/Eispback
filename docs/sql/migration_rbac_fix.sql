-- =====================================================
-- RBAC 模块审查问题修复迁移脚本
-- 适用场景：已部署环境的增量迁移
-- 执行方式：psql -U postgres -d aeisp -f migration_rbac_fix.sql
-- =====================================================

-- S1: sys_role 添加 data_scope 列
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS data_scope VARCHAR(20) NOT NULL DEFAULT 'ALL';
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围：ALL-全部数据，SELF-仅本人数据';

-- S2: sys_user 添加 last_login_ip 和 last_login_at 列
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64) DEFAULT NULL;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP DEFAULT NULL;
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录 IP 地址';
COMMENT ON COLUMN sys_user.last_login_at IS '最后登录时间';

-- S3: 添加 system:permission:read 权限码
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'system:permission:read') THEN
        INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, action,
            description, parent_id, menu_type, sort_order, icon, route_path, component,
            is_visible, is_cache, created_at, updated_at, created_by, updated_by)
        VALUES (79, '查看权限点列表', 'system:permission:read', 'system', 'read',
            NULL, 2, 2, 7, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 1, 1);

        -- 超级管理员获得该权限
        INSERT INTO sys_role_permission (role_id, permission_id, created_at)
        VALUES (1, 79, NOW());

        -- 更新序列（确保后续插入不冲突）
        PERFORM setval('sys_permission_id_seq', GREATEST((SELECT MAX(id) FROM sys_permission), 79));
    END IF;
END $$;

-- M6: sys_role 添加 role_name 唯一约束（需先确认无重复数据）
-- 注意：执行前先运行以下查询确认无重复数据：
-- SELECT role_name, COUNT(*) FROM sys_role GROUP BY role_name HAVING COUNT(*) > 1;
-- 如果有重复数据，需要先清理后再执行
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_sys_role_name') THEN
        -- 检查是否有重复数据
        IF EXISTS (SELECT role_name FROM sys_role GROUP BY role_name HAVING COUNT(*) > 1) THEN
            RAISE WARNING '存在重复的 role_name，请先清理数据后再添加唯一约束';
        ELSE
            ALTER TABLE sys_role ADD CONSTRAINT uk_sys_role_name UNIQUE (role_name);
            RAISE NOTICE '已添加 uk_sys_role_name 唯一约束';
        END IF;
    ELSE
        RAISE NOTICE 'uk_sys_role_name 约束已存在，跳过';
    END IF;
END $$;

-- L4: 统一 menu_type 注释
COMMENT ON COLUMN sys_permission.menu_type IS '类型：0-目录，1-菜单，2-按钮，3-外链';

-- 完成提示
DO $$
BEGIN
    RAISE NOTICE 'RBAC 模块修复迁移完成！';
END $$;
