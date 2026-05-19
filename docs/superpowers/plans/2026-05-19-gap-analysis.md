# AEISP 系统 vs 文档详细补充v2 — 差距分析

> 基于 `文档详细补充v2.txt` 对现有代码库的全面审查

---

## 一、用户管理模块 (aeisp-user)

### 1.1 前端用户表 (`usr_user` vs `users`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `user_id` VARCHAR(36), 格式 `u_{timestamp}_{6位随机串}` | `id` BIGINT 自增 | **主键策略需改** — 影响所有外键关联 |
| `username` 只允许字母/数字/下划线，首位字母 | `username` VARCHAR(32) | 需加格式校验 |
| `nickname` VARCHAR(50) | **缺失** | 需新增字段 |
| `avatar_url` | **缺失** | 需新增字段 |
| `account_status` TINYINT: 1=正常,2=禁用,3=冻结,4=锁定 | `status` TINYINT: 0=禁用,1=正常,2=冻结 | **枚举值重定义** + 需新增"锁定"状态 |
| `status_reason` VARCHAR(200) | **缺失** | 需新增字段 |
| `locked_until` DATETIME | **缺失** | 需新增字段 |
| `need_change_password` TINYINT | **缺失** | 需新增字段 |
| `failed_login_attempts` TINYINT | **缺失** | 需新增字段 |
| `last_login_ip` VARCHAR(40) | **缺失** | 需新增字段 |
| `register_device_info` JSON | `register_device` VARCHAR(255) | **类型需改 JSON** |
| `invitation_code_used` VARCHAR(10) | **缺失** | 需新增字段 |
| 角色通过 `user_roles` 多对多关联 | `role_code` VARCHAR 单角色内联 | **需拆分为独立关联表** |

### 1.2 用户角色关联 (`user_roles`)

- **当前**: 无。`usr_user` 内联 `role_code` 字段，只能持有一个角色。
- **文档要求**: 独立的 `user_roles` 表，多对多关联 `users` 和 `roles`。
- **动作**: 新建 `user_roles` 表，迁移现有 `role_code` 数据，修改用户创建/更新逻辑。

### 1.3 用户时长表 (`user_duration`)

- **当前**: `usr_user.remaining_duration` 内联字段。
- **文档要求**: 独立的 `user_duration` 表，含 `total_granted_minutes`, `total_consumed_minutes`, `remaining_minutes`, `expiry_date`, `warning_threshold`。
- **动作**: 新建表，迁移数据，修改时长调整逻辑。

### 1.4 用户余额表 (`user_balance`)

- **当前**: `usr_user.total_recharge` 内联字段。
- **文档要求**: 独立的 `user_balance` 表，含 `balance_cents`, `total_recharge_cents`, `total_consumed_cents`。
- **动作**: 新建表。

### 1.5 时长变更日志 (`duration_change_logs`)

- **当前**: `usr_duration_log` 仅记录消耗（consume_type: 1=模型调用, 2=仿真运行, 3=编译调试）。
- **文档要求**: `duration_change_logs` 记录所有时长变更（注册赠送、充值、管理员增/减/设定、使用消耗、退款扣减），含变更前后余额、操作人、原因。
- **动作**: 新建表，替换/扩展现有日志逻辑。

### 1.6 登录日志 (`login_logs`)

- **当前**: `usr_login_log` — 字段较少，result 只有 0/1。
- **文档要求**: `login_logs` — 含 `login_account`, `login_type`(1=密码,2=验证码,3=Token刷新), `login_result`(1=成功,2=密码错误,3=账号不存在,4=禁用,5=冻结,6=锁定,7=验证码错误,8=Token过期), `device_type`, `os_info`, `browser_info`, `device_id`。
- **动作**: 扩展字段，修改登录逻辑以记录更详细信息。

### 1.7 验证码记录 (`verification_records`)

- **当前**: **缺失**。
- **文档要求**: 记录验证码发送历史，含 `target`, `send_type`, `scene`, `verification_code`, `verify_result`, `ip_address`, `expired_at`。
- **动作**: 新建表，保留接口（短信/邮件发送暂不实装）。

### 1.8 用户统计接口

- **当前**: `UserStatisticsController` 为 stub（抛出 `UnsupportedOperationException`）。
- **文档要求**: 总用户数、状态分布、新增用户（日/周/月）、DAU/WAU/MAU、趋势折线数据。
- **动作**: 实装统计查询逻辑。

---

## 二、通知消息模块 (aeisp-message)

### 2.1 消息通知主表 (`msg_notification` vs `notifications`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `notification_type` VARCHAR(30) | `msg_type` TINYINT | **类型改 String** |
| `priority` TINYINT (1=低,2=中,3=高,4=紧急) | **缺失** | 需新增 |
| `target_user_ids` JSON | `push_target` TEXT (通用JSON) | 需明确区分用户ID和组ID |
| `target_group_ids` JSON | **缺失** | 需新增 |
| `channels` JSON (site/email/sms/push) | **缺失** | 需新增 |
| `schedule_time` DATETIME | `push_time` | 字段名可保留 |
| `expiry_time` DATETIME | `expire_time` | 字段名可保留 |
| `need_read_confirmation` TINYINT | **缺失** | 需新增 |
| `target_count` / `success_count` / `failed_count` | `total_count` + `read_count` | 需补 success/failed count |
| `sent_at` / `revoked_at` | **缺失** | 需新增 |
| `status`: 1=草稿,2=已发送,3=已撤回,4=定时中 | 0=草稿,1=已推送,2=已撤回,3=已归档 | **枚举值重定义** |

### 2.2 用户消息关联 (`msg_user_notification` vs `user_notifications`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `read_status`: 1=未读,2=已读,3=已撤回 | `is_read`: 0=未读,1=已读 | **枚举重定义** + 新增"已撤回" |
| `read_at` DATETIME | `read_time` | 字段名可保留 |
| `channel_received` JSON | **缺失** | 需新增 |

### 2.3 消息发送日志 (`notification_send_logs`)

- **当前**: **缺失**。
- **文档要求**: 记录每条消息在每个渠道的发送详情（成功/失败、错误信息）。
- **动作**: 新建表。

### 2.4 消息模板 (`notification_templates`)

- **当前**: **缺失**。
- **文档要求**: 预定义各类型消息的推送模板（标题模板、内容模板、渠道）。
- **动作**: 新建表，保留接口（暂不强制使用模板引擎）。

---

## 三、模板管理模块 (aeisp-template)

### 3.1 模板主表 (`tpl_template` vs `templates`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `template_code` VARCHAR(30), 唯一编码 | **缺失** | 需新增 |
| `scenario` TINYINT | `scenario` VARCHAR(32) | **类型改 TINYINT** |
| `status`: 1=活跃, 2=下线 | `status`: 0=下线, 1=上线 | **枚举值微调** |
| `sort_order` INT | `sort_weight` INT | 字段名可保留 |
| `use_count` INT | `usage_count` BIGINT | 类型调整 |
| `project_count` INT | **缺失** | 需新增 |
| `optimization_flag` TINYINT | **缺失** | 需新增 |
| `storage_path` VARCHAR(255) | **缺失** | 需新增 |
| `Easy_at` (难度系数) | **缺失** | 需新增 |

### 3.2 模板版本表 (`tpl_template_version` vs `template_versions`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `version` VARCHAR(15) | `version_no` VARCHAR(16) | 字段名可保留 |
| `file_size` BIGINT | **缺失** | 需新增 |
| `file_hash` VARCHAR(64) | **缺失** | 需新增 |
| `storage_url` VARCHAR(255) | `file_path` VARCHAR(255) | 字段名可保留 |
| `config_json` JSON | `config_content` TEXT | 字段名可保留 |
| `is_major_update` TINYINT | **缺失** | 需新增 |

### 3.3 模板预览图片 (`template_preview_images`)

- **当前**: `tpl_template.preview_image` 单图片 VARCHAR。
- **文档要求**: 独立的 `template_preview_images` 表，支持多图 + 排序。
- **动作**: 新建表，迁移现有单图数据。

---

## 四、系统基础管理模块 (aeisp-system)

### 4.1 权限点表 (`sys_permission` vs `permissions`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `resource_type` VARCHAR(20) | **缺失** | 需新增 |
| `action` VARCHAR(20) | **缺失** | 需新增 |

### 4.2 操作日志 (`sys_operation_log` vs `admin_operation_logs`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `operation_type` VARCHAR(30) 编码 | `operation` VARCHAR(32) | 字段语义需调整 |
| `operation_type_label` VARCHAR(50) | **缺失** | 需新增 |
| `target_type` / `target_id` | **缺失** | 需新增 |
| `operation_detail` JSON (变更前后快照) | `request_params` / `response_data` | 需改为业务快照 |
| `sensitivity` TINYINT (1=普通,2=敏感) | **缺失** | 需新增 |

### 4.3 用户行为日志 (`user_behavior_logs`)

- **当前**: **缺失**。
- **文档要求**: 记录用户前端关键行为（LOGIN/LOGOUT/CREATE_PROJECT/MODEL_CALL/RECHARGE等）。
- **动作**: 新建表，保留接口（暂不全面接入）。

### 4.4 异常报错日志 (`error_logs`)

- **当前**: **缺失**。
- **文档要求**: 记录系统异常，含 `error_type`, `error_message`, `stack_trace`, `request_url`, `request_params`, `severity`。
- **动作**: 新建表，保留接口（可先接入全局异常处理器）。

### 4.5 系统配置 (`sys_config` vs `system_settings`)

| 文档要求 | 当前状态 | 差距 |
|---------|---------|------|
| `setting_key` VARCHAR(50) PK | `config_key` VARCHAR(64) | 字段名可保留 |
| `environment` VARCHAR(10) (all/dev/test/prod) | **缺失** | 需新增 |
| `is_editable` TINYINT | **缺失** | 需新增 |
| 大量预设配置项 | 仅3条系统配置 | **需补充文档中所有预设项** |

---

## 五、预留模块（Stub）

以下模块在文档中有数据结构定义，但当前为 stub 实现（Controller + DTO 存在，Service 抛出 `UnsupportedOperationException`）。本次审查建议**保留接口不变**，但如有字段定义差异需同步调整 DTO。

- `aeisp-model` — AI 模型管理
- `aeisp-recharge` — 充值套餐、订单、余额、时长消费

---

## 六、全局规范差距

| 规范 | 当前状态 | 差距 |
|------|---------|------|
| 主键格式: `{前缀}_{timestamp}_{6位随机串}` | 全部使用 BIGINT 自增 | **需全局改造** |
| 枚举值用 TINYINT | 部分用 String (如 scenario) | **需统一** |
| JSON 字段规范 | 部分用 VARCHAR/TEXT 存 JSON | **需改字段类型** |
| 金额以分为单位 INT | `usr_user.total_recharge` 为 BIGINT | 类型可接受 |
| 逻辑删除 | 所有表均有 `deleted` | 符合 |
| 时区统一东八区 | 使用 `LocalDateTime` | 需确认 JDBC 时区配置 |

---

## 七、修改工作量评估

| 优先级 | 模块 | 工作量 | 说明 |
|--------|------|--------|------|
| P0 | 用户表字段扩展 + 状态枚举改造 | 大 | 影响登录、用户管理、前端 |
| P0 | 新建 `user_duration` / `user_balance` / `duration_change_logs` | 大 | 核心数据结构变更 |
| P1 | 登录日志扩展 + 验证码记录表 | 中 | 安全审计相关 |
| P1 | 消息模块字段补充 + 发送日志 | 中 | 影响消息推送逻辑 |
| P1 | 模板字段补充 + 预览图片表 | 中 | 影响模板上传/展示 |
| P2 | 系统配置扩展 + 预设数据 | 中 | 管理后台配置 |
| P2 | 操作日志扩展 + 行为/异常日志 | 中 | 日志体系完善 |
| P2 | 权限点扩展字段 | 小 | 仅增字段 |
| P3 | 主键格式全局改造 | 极大 | 影响所有表和关联，建议单独迭代 |

---

## 八、关键决策点

1. **主键策略**: 文档要求业务主键 `u_{timestamp}_{random}`，但当前全库使用 BIGINT 自增。是否在本次改造？
2. **用户角色**: 当前 `usr_user.role_code` 单角色内联，改为多对多需同步修改前端用户列表/详情/创建/编辑页面。
3. **消息状态枚举重定义**: 当前消息状态 0=草稿,1=已推送,2=已撤回,3=已归档；文档要求 1=草稿,2=已发送,3=已撤回,4=定时中。是否直接替换？
4. **用户状态枚举重定义**: 当前 0=禁用,1=正常,2=冻结；文档要求 1=正常,2=禁用,3=冻结,4=锁定。是否直接替换？
