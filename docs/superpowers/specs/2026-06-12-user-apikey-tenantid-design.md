# 用户数据增加 APIKEY 与租户 ID 设计文档

## 1. 背景与目标

为前端用户（`usr_user`）增加两个字符串字段：

- `api_key`：API 访问密钥
- `tenant_id`：租户 ID

这两个字段需要在后台创建用户时填写，并持久化到数据库；同时支持在编辑用户时修改。

## 2. 需求确认

| 项目 | 确认结果 |
|------|----------|
| 作用对象 | 前端用户（`usr_user`），非后台管理员 |
| 字段类型 | 字符串 |
| 创建时是否必填 | 是 |
| 是否校验唯一性 | 否 |
| 租户 ID 格式 | `^[a-zA-Z0-9_-]+$`，最大长度 64 |
| APIKEY 格式 | `^[a-zA-Z0-9_-]+$`，最大长度 128 |
| 编辑时是否必填 | 否；留空表示保留原值，不覆盖 |
| 是否加入 Excel 导入导出 | 否（方案 A，最小化改动） |
| 是否在用户列表展示 | 否，仅在详情/创建/编辑弹窗展示 |

## 3. 数据库变更

在 `usr_user` 表中新增两列：

```sql
ALTER TABLE usr_user
    ADD COLUMN IF NOT EXISTS api_key VARCHAR(128) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) DEFAULT NULL;

COMMENT ON COLUMN usr_user.api_key IS 'API 访问密钥';
COMMENT ON COLUMN usr_user.tenant_id IS '租户 ID';
```

同时更新 `docs/sql/init-postgresql-complete.sql` 中的建表语句，在 `is_competition` 列之后、审计字段之前加入这两列，确保新环境一次性初始化完整。

## 4. 后端设计

### 4.1 Entity

`aeisp-user/src/main/java/com/aeisp/user/entity/UsrUser.java` 增加：

```java
/**
 * API 访问密钥。
 */
private String apiKey;

/**
 * 租户 ID。
 */
private String tenantId;
```

### 4.2 Request

`UserCreateRequest.java` 增加字段并加校验：

```java
@NotBlank(message = "租户ID不能为空")
@Size(max = 64, message = "租户ID长度不能超过64位")
@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户ID只能包含字母、数字、下划线、横线")
private String tenantId;

@NotBlank(message = "APIKEY不能为空")
@Size(max = 128, message = "APIKEY长度不能超过128位")
@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "APIKEY只能包含字母、数字、下划线、横线")
private String apiKey;
```

`UserUpdateRequest.java` 增加同名字段，但**不加 `@NotBlank`**：

```java
@Size(max = 64, message = "租户ID长度不能超过64位")
@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户ID只能包含字母、数字、下划线、横线")
private String tenantId;

@Size(max = 128, message = "APIKEY长度不能超过128位")
@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "APIKEY只能包含字母、数字、下划线、横线")
private String apiKey;
```

> 编辑时如果前端传空字符串或 `null`，Service 层忽略该字段，保留原值。

### 4.3 VO

`UsrUserVO.java` 增加：

```java
private String apiKey;
private String tenantId;
```

### 4.4 Service

`UsrUserServiceImpl.createByAdmin`：

```java
user.setApiKey(request.getApiKey());
user.setTenantId(request.getTenantId());
```

`UsrUserServiceImpl.updateUser`：

```java
if (StringUtils.hasText(request.getApiKey())) {
    user.setApiKey(request.getApiKey());
}
if (StringUtils.hasText(request.getTenantId())) {
    user.setTenantId(request.getTenantId());
}
```

`convertToVO`：

```java
vo.setApiKey(user.getApiKey());
vo.setTenantId(user.getTenantId());
```

### 4.5 Controller

无需新增接口。现有接口通过 Request 对象自动承载新字段：

- `POST /api/v1/users`：创建用户
- `PUT /api/v1/users/{id}`：更新用户
- `GET /api/v1/users/{id}`：用户详情
- `GET /api/v1/users`：用户列表

## 5. 前端设计

修改文件：`aeisp-admin/src/views/user/list/index.vue`

### 5.1 创建弹窗

在“比赛用户”表单项之前增加：

```vue
<el-form-item label="租户ID">
  <el-input v-model="createForm.tenantId" placeholder="请输入租户ID" />
</el-form-item>
<el-form-item label="APIKEY">
  <el-input v-model="createForm.apiKey" placeholder="请输入APIKEY" />
</el-form-item>
```

`createForm` 初始化增加：

```js
apiKey: '', tenantId: ''
```

`submitCreate` 请求体增加：

```js
apiKey: createForm.apiKey,
tenantId: createForm.tenantId
```

### 5.2 编辑弹窗

在邮箱/状态之间增加两个输入框，并绑定到 `form.apiKey`、`form.tenantId`。`handleUpdate` 从 `row` 中读取这两个字段。`submitUpdate` 的 `baseData` 中携带它们。

### 5.3 用户详情

在“基本信息”描述列表中增加：

```vue
<el-descriptions-item label="租户ID">{{ detailUser?.tenantId || '-' }}</el-descriptions-item>
<el-descriptions-item label="APIKEY">{{ detailUser?.apiKey || '-' }}</el-descriptions-item>
```

### 5.4 用户列表

不在表格主列中新增字段，避免列表过宽；通过详情查看。

## 6. 测试计划

在 `aeisp-user` 模块增加/更新测试：

1. **创建校验测试**
   - `apiKey` 为空 → 400
   - `tenantId` 为空 → 400
   - `apiKey` 包含非法字符 → 400
   - `tenantId` 超长 → 400

2. **创建成功测试**
   - 创建用户后，通过 `getUserDetail` 返回的 VO 包含正确的 `apiKey` 和 `tenantId`

3. **更新测试**
   - 更新 `apiKey`、`tenantId` 后数据库值变更
   - 更新时传空字符串，原值保持不变

## 7. 不在本次范围内的内容

- Excel 批量导入/导出不增加这两个字段。
- `UserBatchCreateRequest` 及其批量创建逻辑不增加这两个字段。
- 不对 APIKEY/租户 ID 做加密或脱敏处理。
- 不基于租户 ID 做数据隔离或权限过滤。

## 8. 风险与注意事项

1. 数据库迁移需要在已部署环境执行 ALTER 语句；新环境通过更新后的 `init-postgresql-complete.sql` 自动包含。
2. 前端编辑弹窗留空时，由于 Service 层忽略空字符串，因此不会清空已有值；若业务上后续需要支持清空，需要单独提供“清除”按钮或显式传空标记。
3. 该字段目前为明文存储，若后续涉及敏感密钥，建议评估加密方案。
