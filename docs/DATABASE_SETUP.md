# 数据库初始化指南

## 问题回顾

登录失败的根本原因是缺少 `sys_feature_switch`（功能开关表）和其他一些表。AuthController 在登录时需要检查功能开关，但这些表在初始化脚本中缺失。

## 快速开始

### 方式一：使用 Docker 命令（推荐）

如果数据库已经运行，直接执行完整初始化脚本：

```bash
docker exec -i aeisp-postgres psql -U postgres -d aeisp < docs/sql/init-postgresql-complete.sql
```

### 方式二：使用 DbInitTest

1. 确保后端配置文件中的数据库连接正确
2. 运行 `DbInitTest.initDatabase()` 测试方法

### 方式三：分步操作

如果数据库已经有数据，只需要添加缺失的表：

```bash
docker exec -i aeisp-postgres psql -U postgres -d aeisp < docs/sql/migration-postgresql.sql
```

## SQL 文件说明

| 文件 | 说明 | 适用场景 |
|------|------|----------|
| `init-postgresql.sql` | 基础初始化脚本 | 历史遗留，不推荐新环境使用 |
| `init-postgresql-complete.sql` | **完整初始化脚本** | **新环境推荐使用** |
| `migration-postgresql.sql` | 迁移脚本 | 给已存在的数据库添加缺失的表和字段 |
| `migration_*.sql` | 增量迁移文件 | 单独的功能模块迁移 |

## 数据库表清单

### 系统模块 (aeisp-system)

| 表名 | 说明 |
|------|------|
| `sys_user` | 后台管理员账号 |
| `sys_role` | 系统角色 |
| `sys_permission` | 系统权限点（菜单树） |
| `sys_user_role` | 用户-角色关联 |
| `sys_role_permission` | 角色-权限关联 |
| `sys_operation_log` | 操作日志 |
| `sys_config` | 系统配置 |
| `sys_config_log` | 配置变更历史日志 |
| `sys_feature_switch` | **功能开关（登录必需）** |
| `sys_user_behavior_log` | 用户行为日志 |
| `sys_error_log` | 异常报错日志 |
| `sys_dict_type` | 字典类型 |
| `sys_dict_data` | 字典数据 |

### 用户模块 (aeisp-user)

| 表名 | 说明 |
|------|------|
| `usr_user` | 前端用户 |
| `usr_user_role` | 前端用户-角色关联 |
| `usr_user_permission` | 用户业务权限 |
| `usr_user_permission_log` | 用户权限变更日志 |
| `usr_user_template` | 用户模板权限 |
| `usr_login_log` | 用户登录日志 |
| `usr_template_usage_log` | 模板使用日志 |
| `usr_user_duration` | 用户时长 |
| `usr_duration_change_log` | 时长变更日志 |
| `usr_user_balance` | 用户余额 |
| `usr_balance_change_log` | 余额变更日志 |
| `usr_verification_record` | 验证码发送记录 |

### 消息模块 (aeisp-message)

| 表名 | 说明 |
|------|------|
| `msg_notification` | 消息通知主表 |
| `msg_user_notification` | 用户-消息关联 |
| `msg_notification_send_log` | 通知发送日志 |
| `msg_notification_template` | 通知模板 |

### 模板模块 (aeisp-template)

| 表名 | 说明 |
|------|------|
| `tpl_template` | 模板主表 |
| `tpl_template_version` | 模板版本 |
| `tpl_template_category` | 模板分类 |
| `tpl_template_preview_image` | 模板预览图 |
| `tpl_template_library` | 模板-库资源关联 |

### 库资源模块 (aeisp-library)

| 表名 | 说明 |
|------|------|
| `lib_resource` | 库资源主表 |
| `lib_resource_version` | 库资源版本 |

### 项目模块 (aeisp-project)

| 表名 | 说明 |
|------|------|
| `prj_project` | 项目表 |

### AI模型模块 (aeisp-model)

| 表名 | 说明 |
|------|------|
| `ai_model` | AI模型配置 |
| `model_call_log` | 模型调用日志 |
| `ai_session` | AI会话 |
| `ai_message` | AI消息 |

### 充值模块 (aeisp-recharge)

| 表名 | 说明 |
|------|------|
| `duration_package` | 时长套餐 |
| `recharge_order` | 充值订单 |

## 默认账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| `admin` | `admin123` | 超级管理员 |

## 常见问题

### Q: 登录时提示"登录失败，请稍后重试"？

A: 检查数据库是否包含 `sys_feature_switch` 表。如果不存在，执行迁移脚本：

```bash
docker exec -i aeisp-postgres psql -U postgres -d aeisp < docs/sql/migration-postgresql.sql
```

### Q: 如何完全重置数据库？

A:

```bash
# 1. 停止并删除容器
docker-compose down -v

# 2. 重新启动容器
docker-compose up -d

# 3. 等待数据库启动，然后执行完整初始化
docker exec -i aeisp-postgres psql -U postgres -d aeisp < docs/sql/init-postgresql-complete.sql
```

### Q: 哪些表是登录必需的？

A: 登录功能依赖以下表：
- `sys_user` - 管理员账号
- `sys_role` - 角色
- `sys_permission` - 权限
- `sys_user_role` - 用户角色关联
- `sys_role_permission` - 角色权限关联
- `sys_feature_switch` - **功能开关（必需）**
- `sys_config` - 系统配置
- `sys_operation_log` - 操作日志（用于记录登录）
- `sys_user_behavior_log` - 用户行为日志

### Q: 如何验证数据库是否完整？

A: 执行以下查询检查表是否存在：

```sql
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
```

应该能看到上面列出的所有表（37+ 张表）。

## 完整初始化后的检查表

执行以下 SQL 验证关键数据是否存在：

```sql
-- 检查管理员用户
SELECT id, username, status FROM sys_user WHERE username = 'admin';

-- 检查角色
SELECT id, role_name, role_code FROM sys_role ORDER BY id;

-- 检查功能开关
SELECT feature_key, feature_name, enabled FROM sys_feature_switch ORDER BY sort_order;

-- 检查权限数量
SELECT COUNT(*) FROM sys_permission WHERE deleted = 0;
```

## 迁移规范

### 添加新表时

1. 在 `migration-postgresql.sql` 中添加 `CREATE TABLE IF NOT EXISTS` 语句
2. 同时在 `init-postgresql-complete.sql` 中添加对应的表定义（保持同步）
3. 在本文件的"数据库表清单"中添加说明

### 修改现有表时

1. 使用 `ALTER TABLE IF NOT EXISTS ... ADD COLUMN IF NOT EXISTS` 语法
2. 添加相应的注释
3. 在 `init-postgresql-complete.sql` 中更新表定义

### 添加初始数据时

1. 使用 `INSERT INTO ... ON CONFLICT DO NOTHING` 语法
2. 确保可以重复执行而不会报错
