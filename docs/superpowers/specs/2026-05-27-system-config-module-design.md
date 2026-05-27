# 系统配置模块设计文档

## 概述

对平台现有的系统配置模块进行全面升级，在现有 `sys_config` key-value 存储基础上，新增功能开关模块和配置变更历史日志，重构前端展示为分类标签页形式。

## 数据库设计

### sys_config 表扩展

在现有 `sys_config` 表上增加 `category` 字段，用于配置分类：

```sql
ALTER TABLE sys_config ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'general';
COMMENT ON COLUMN sys_config.category IS '分类: storage/backup/platform/timeout/threshold/general';
```

同时增加 `field_type` 字段，标识配置值的前端控件类型：

```sql
ALTER TABLE sys_config ADD COLUMN field_type VARCHAR(20) NOT NULL DEFAULT 'text';
COMMENT ON COLUMN sys_config.field_type IS '控件类型: text/boolean/number/password';
```

已有字段见现有表结构（config_key, config_value, description, environment, is_editable, 审计字段）。

### sys_config_log 表（新增）

统一记录基础配置和功能开关的变更历史。

```sql
CREATE TABLE sys_config_log (
    id BIGSERIAL PRIMARY KEY,
    config_type VARCHAR(20) NOT NULL,     -- 'config' | 'feature'
    ref_id BIGINT NOT NULL,               -- 关联 sys_config.id 或 sys_feature_switch.id
    config_key VARCHAR(64) NOT NULL,      -- 冗余方便查询
    old_value TEXT,
    new_value TEXT,
    environment VARCHAR(10),
    operated_by BIGINT,
    operated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_config_log_ref ON sys_config_log(config_type, ref_id);
CREATE INDEX idx_config_log_time ON sys_config_log(operated_at);
```

### sys_feature_switch 表（新增）

功能开关独立存储，支持布尔类型和分类管理。

```sql
CREATE TABLE sys_feature_switch (
    id BIGSERIAL PRIMARY KEY,
    feature_key VARCHAR(64) NOT NULL UNIQUE,
    feature_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,        -- business/commercial/security/message/maintenance
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT DEFAULT 0
);
```

### 预置数据

#### sys_config 5 大类配置项

| config_key | category | field_type | 说明 |
|---|---|---|---|
| storage.project | storage | text | 项目文件存储路径 |
| storage.template | storage | text | 模板资源存储路径 |
| storage.upload | storage | text | 上传文件存储路径 |
| storage.cache | storage | text | 缓存文件存储路径 |
| backup.enabled | backup | boolean | 云端自动备份开关 |
| backup.interval | backup | number | 备份周期（天） |
| backup.retention | backup | number | 保留备份份数 |
| backup.path | backup | text | 备份存储路径 |
| backup.alert | backup | boolean | 异常备份告警开关 |
| platform.website | platform | text | 官网地址 |
| platform.foredomain | platform | text | 前台域名 |
| platform.backdomain | platform | text | 后台域名 |
| platform.name | platform | text | 平台名称 |
| platform.copyright | platform | text | 版权信息 |
| platform.icp | platform | text | 备案号 |
| timeout.session | timeout | number | 会话超时（分钟） |
| timeout.file | timeout | number | 文件过期时间（天） |
| timeout.order | timeout | number | 未支付订单超时（分钟） |
| timeout.task | timeout | number | 运行任务超时（分钟） |
| threshold.balance | threshold | number | 用户余额预警阈值 |
| threshold.runtime | threshold | number | 运行时长预警阈值 |
| threshold.resource | threshold | number | 资源占用告警阈值 |

#### sys_feature_switch 预置项

| feature_key | feature_name | category |
|---|---|---|
| user.register | 用户注册 | business |
| user.login | 账号登录 | business |
| project.create | 项目创建 | business |
| code.run | 代码运行 | business |
| ai.chat | AI 对话 | business |
| file.upload | 文件上传 | business |
| recharge | 充值功能 | commercial |
| template.purchase | 付费模板购买 | commercial |
| balance.deduct | 余额扣费 | commercial |
| order.pay | 订单支付 | commercial |
| security.location | 异地登录提醒 | security |
| security.password | 密码复杂度校验 | security |
| security.lock | 登录失败锁定 | security |
| security.content | 内容风控审核 | security |
| notify.system | 系统通知推送 | message |
| notify.alert | 预警消息推送 | message |
| notify.announcement | 公告推送 | message |
| maintenance | 全站维护模式 | maintenance |

## 后端设计

### 已有代码变更

**SysConfig**
- Entity: 增加 `category` 字段
- VO: 增加 `category` 字段，并且把 `environment`、`isEditable` 加入到 VO 中（当前 VO 排除了这两个字段，但前端需要知道是否可编辑）
- Service: 增加 `listByCategory(category)` 方法
- Controller: 增加 `GET /api/v1/system/configs/category/{category}` 接口

**SysConfigController**
- `PUT /{key}` 更新成功后，自动写入 sys_config_log
- 增加 `GET /logs?configType={type}&refId={id}&page={}&size={}` 日志查询接口

### 新增代码

**SysFeatureSwitch**
- Entity → Mapper → Service(interface) → ServiceImpl → VO → Controller
- Controller: `GET /` 列表、`PUT /{id}/toggle` 切换开关
- 切换时写入 sys_config_log

**SysFeatureSwitchController 接口**
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/v1/system/features | 列表（按 category 排序） |
| PUT | /api/v1/system/features/{id}/toggle | 切换单个开关 |
| PUT | /api/v1/system/features/maintenance | 维护模式特殊处理 |

### 变更日志写入机制

在 `SysConfigServiceImpl.updateConfig()` 和 `SysFeatureSwitchServiceImpl.toggle()` 中：
1. 读取旧值
2. 执行更新
3. 写入一条 sys_config_log 记录（config_type、ref_id、config_key、old_value、new_value、operated_by、operated_at）
4. 操作人从 SecurityUtils 获取当前用户

### 维护模式特殊逻辑

`PUT /api/v1/system/features/maintenance`
- 入参: `{enabled: true/false}`
- **开启时**:
  - 读取当前所有 business 类别开关的 enabled 状态，保存为 JSON 快照（存到 Redis 或 sys_config 的 `maintenance.snapshot` key）
  - 将所有 business 开关设为 false
  - 将 maintenance 开关设为 true
  - 记录受影响的开关到 sys_config_log
- **关闭时**:
  - 从快照中恢复各 business 开关的原有状态
  - 将 maintenance 开关设为 false
  - 清理快照
  - 记录到 sys_config_log

## 前端设计

### 路由结构（已有）

```
/system/config     → 系统管理 -> 系统配置（被 dynamic-routes.js 过滤掉）
/sysconfig/base    → 系统配置模块 -> 基础配置
/sysconfig/feature → 系统配置模块 -> 功能开关
```

### API 调用

在 `aeisp-admin/src/api/system.js` 中增加：

```js
// 基础配置
listConfigsByCategory(category)  // GET /system/configs/category/{category}
listConfigLogs(params)           // GET /system/configs/logs

// 功能开关
listFeatures()                   // GET /system/features
toggleFeature(id)                // PUT /system/features/{id}/toggle
toggleMaintenance(enabled)       // PUT /system/features/maintenance
```

### 基础配置页 (views/system/config/index.vue)

重构现有页面：

```
el-tabs(v-model="activeTab")
  el-tab-pane(label="存储路径" name="storage")
    el-table: 显示该分类下所有配置
      列: 配置项(config.description) → 输入框/路径选择器 → 操作(保存/历史)
      
  el-tab-pane(label="云端备份" name="backup")
    el-table: 备份开关用 switch, 周期/份数为数字输入, 路径为输入框

  el-tab-pane(label="平台信息" name="platform")
    el-table: 普通文本输入框

  el-tab-pane(label="超时参数" name="timeout")
    el-table: el-input-number(带单位后缀)

  el-tab-pane(label="阈值预警" name="threshold")
    el-table: el-input-number(带单位后缀)
```

每行操作列：
- **编辑**: 行内编辑或弹出 dialog 编辑 configValue（按类型使用对应控件）
- **历史**: 打开 dialog，显示该配置的 sys_config_log 列表

### 功能开关页 (views/system/feature/index.vue)

重构占位页面：

```
顶部: 维护模式卡片
  el-switch + 醒目样式 (红色背景)
  点击确认弹窗: "开启维护模式将禁用所有核心业务功能，是否继续？"

按分类分 el-card 展示:
  核心业务开关卡片: 注册/登录/项目创建/代码运行/AI对话/文件上传
  商业化功能卡片: 充值/模板购买/余额扣费/订单支付
  安全机制卡片: 异地登录提醒/密码复杂度/登录锁定/内容风控
  消息推送卡片: 系统通知/预警消息/公告推送

每项: feature_name + description + el-switch
```

## 权限设计

| 权限标识 | 说明 |
|---|---|
| system:config | 基础配置菜单（已有） |
| system:config:update | 基础配置更新（已有） |
| system:config_log | 配置历史查询（新增） |
| system:feature | 功能开关菜单（新增） |
| system:feature:toggle | 功能开关切换（新增） |
| system:feature:maintenance | 维护模式操作（新增） |

## 实施顺序

1. 数据库变更（ALTER TABLE + 新表创建 + 预置数据）
2. SysConfig 增强（加 category 字段、扩展 VO、增加分类查询接口）
3. SysConfigLog 表 + 后端 CRUD + 日志写入逻辑
4. SysFeatureSwitch 全栈 CRUD
5. 维护模式特殊逻辑
6. 前端基础配置页重构（Tabs + 控件类型适配 + 历史查看）
7. 前端功能开关页重写（卡片布局 + switch）
8. 权限注册 + 动态路由验证
9. 测试 + 验收