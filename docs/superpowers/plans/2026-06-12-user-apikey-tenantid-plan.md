# 用户 APIKEY 与租户 ID 功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为前端用户（`usr_user`）新增 `api_key` 和 `tenant_id` 两个字符串字段，支持后台创建时必填、编辑时修改需二次确认，并同步更新数据库、后端 API 与前端管理页面。

**Architecture:** 直接在 `usr_user` 主表扩展两个字段，遵循现有分层：Entity → Request/VO → ServiceImpl → Controller。前端在创建/编辑弹窗和详情页增加输入与展示，编辑修改时通过 `ElMessageBox.confirm` 二次确认。

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus, PostgreSQL, Vue 3, Element Plus, JUnit 5 + Mockito

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `docs/sql/init-postgresql-complete.sql` | 新环境初始化时包含 `api_key`、`tenant_id` 两列 |
| `docs/sql/migration-add-user-apikey-tenantid.sql` | 已部署环境执行 ALTER 语句 |
| `aeisp-user/src/main/java/com/aeisp/user/entity/UsrUser.java` | 用户实体增加两个字段 |
| `aeisp-user/src/main/java/com/aeisp/user/request/UserCreateRequest.java` | 创建请求增加两个必填字段及校验 |
| `aeisp-user/src/main/java/com/aeisp/user/request/UserUpdateRequest.java` | 更新请求增加两个必填字段及校验 |
| `aeisp-user/src/main/java/com/aeisp/user/vo/UsrUserVO.java` | 用户详情/列表 VO 增加两个字段 |
| `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java` | 创建、更新、VO 转换逻辑 |
| `aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserServiceImplTest.java` | 新增/更新单元测试 |
| `aeisp-admin/src/views/user/list/index.vue` | 前端创建、编辑、详情展示 |

---

## Task 1: 数据库表结构变更

**Files:**
- Modify: `docs/sql/init-postgresql-complete.sql:360`
- Create: `docs/sql/migration-add-user-apikey-tenantid.sql`

- [ ] **Step 1: 在完整初始化脚本中新增两列**

在 `docs/sql/init-postgresql-complete.sql` 的 `usr_user` 建表语句中，找到 `is_competition` 列之后、`created_at` 列之前，插入：

```sql
    is_competition SMALLINT NOT NULL DEFAULT 0,
    api_key VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
```

在同文件的 `COMMENT ON COLUMN` 区域，新增：

```sql
COMMENT ON COLUMN usr_user.api_key IS 'API 访问密钥';
COMMENT ON COLUMN usr_user.tenant_id IS '租户 ID';
```

- [ ] **Step 2: 创建独立迁移脚本**

创建 `docs/sql/migration-add-user-apikey-tenantid.sql`，内容：

```sql
-- 为已存在的 usr_user 表增加 api_key 和 tenant_id 字段
ALTER TABLE usr_user
    ADD COLUMN IF NOT EXISTS api_key VARCHAR(128) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) DEFAULT NULL;

COMMENT ON COLUMN usr_user.api_key IS 'API 访问密钥';
COMMENT ON COLUMN usr_user.tenant_id IS '租户 ID';
```

- [ ] **Step 3: Commit**

```bash
git add docs/sql/init-postgresql-complete.sql docs/sql/migration-add-user-apikey-tenantid.sql
git commit -m "chore(db): usr_user 表增加 api_key 与 tenant_id 字段

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 2: 后端 Entity / Request / VO

**Files:**
- Modify: `aeisp-user/src/main/java/com/aeisp/user/entity/UsrUser.java:128`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/request/UserCreateRequest.java:68`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/request/UserUpdateRequest.java:50`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/vo/UsrUserVO.java:118`

- [ ] **Step 1: Entity 增加字段**

在 `UsrUser.java` 的 `isCompetition` 字段之后、`serialVersionUID` 之前添加：

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

- [ ] **Step 2: UserCreateRequest 增加字段与校验**

在 `UserCreateRequest.java` 的 `isCompetition` 字段之前添加：

```java
    /**
     * API 访问密钥。
     */
    @NotBlank(message = "APIKEY不能为空")
    @Size(max = 128, message = "APIKEY长度不能超过128位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "APIKEY只能包含字母、数字、下划线、横线")
    private String apiKey;

    /**
     * 租户 ID。
     */
    @NotBlank(message = "租户ID不能为空")
    @Size(max = 64, message = "租户ID长度不能超过64位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户ID只能包含字母、数字、下划线、横线")
    private String tenantId;
```

- [ ] **Step 3: UserUpdateRequest 增加字段与校验**

在 `UserUpdateRequest.java` 的 `avatarUrl` 字段之后、`roleIds` 字段之前添加：

```java
    /**
     * API 访问密钥。
     */
    @NotBlank(message = "APIKEY不能为空")
    @Size(max = 128, message = "APIKEY长度不能超过128位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "APIKEY只能包含字母、数字、下划线、横线")
    private String apiKey;

    /**
     * 租户 ID。
     */
    @NotBlank(message = "租户ID不能为空")
    @Size(max = 64, message = "租户ID长度不能超过64位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户ID只能包含字母、数字、下划线、横线")
    private String tenantId;
```

- [ ] **Step 4: UsrUserVO 增加字段**

在 `UsrUserVO.java` 的 `isCompetition` 字段之后、`roleCodes` 字段之前添加：

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

- [ ] **Step 5: Commit**

```bash
git add aeisp-user/src/main/java/com/aeisp/user/entity/UsrUser.java \
        aeisp-user/src/main/java/com/aeisp/user/request/UserCreateRequest.java \
        aeisp-user/src/main/java/com/aeisp/user/request/UserUpdateRequest.java \
        aeisp-user/src/main/java/com/aeisp/user/vo/UsrUserVO.java
git commit -m "feat(user): entity/request/vo 增加 api_key 与 tenant_id

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 3: Service 实现创建与更新逻辑

**Files:**
- Modify: `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java:142`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java:231`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java:937`

- [ ] **Step 1: createByAdmin 写入新字段**

在 `createByAdmin` 方法中，找到：

```java
user.setIsCompetition(request.getIsCompetition() != null ? request.getIsCompetition() : 0);
user.setDeleted(CommonConstants.DELETED_NO);
```

在其后添加：

```java
user.setApiKey(request.getApiKey());
user.setTenantId(request.getTenantId());
```

- [ ] **Step 2: updateUser 写入新字段**

在 `updateUser` 方法中，找到：

```java
UsrUser user = new UsrUser();
user.setId(request.getId());
user.setPhone(request.getPhone());
user.setEmail(request.getEmail());
user.setNickname(request.getNickname());
user.setAvatarUrl(request.getAvatarUrl());
// 不修改密码
```

修改后应为：

```java
UsrUser user = new UsrUser();
user.setId(request.getId());
user.setPhone(request.getPhone());
user.setEmail(request.getEmail());
user.setNickname(request.getNickname());
user.setAvatarUrl(request.getAvatarUrl());
user.setApiKey(request.getApiKey());
user.setTenantId(request.getTenantId());
// 不修改密码
```

- [ ] **Step 3: convertToVO 复制新字段**

在 `convertToVO` 方法中，找到：

```java
vo.setIsCompetition(user.getIsCompetition());
vo.setRoleCodes(roleCodes);
```

修改后应为：

```java
vo.setIsCompetition(user.getIsCompetition());
vo.setApiKey(user.getApiKey());
vo.setTenantId(user.getTenantId());
vo.setRoleCodes(roleCodes);
```

- [ ] **Step 4: Commit**

```bash
git add aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java
git commit -m "feat(user): service 创建/更新/VO 转换支持 api_key 与 tenant_id

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 4: 后端单元测试

**Files:**
- Modify: `aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserServiceImplTest.java:976`

- [ ] **Step 1: 增加创建成功时保存新字段的测试**

在 `UsrUserServiceImplTest.java` 末尾（最后一个 `}` 之前）添加：

```java
    @Test
    void testCreateByAdminSavesApiKeyAndTenantId() {
        when(usrUserMapper.selectByUsername("apiuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13900139004")).thenReturn(null);
        when(usrUserMapper.selectByEmail("api@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("apiuser");
        request.setPhone("13900139004");
        request.setEmail("api@example.com");
        request.setPassword("password123");
        request.setApiKey("sk-v1-tenant_r-1781257647568-glOIks8MJKvY");
        request.setTenantId("tenant_req_cc6d9abf7d074b2dae1f7151456219a4");

        String password = usrUserService.createByAdmin(request);
        assertEquals("password123", password);
        verify(usrUserMapper).insert(argThat((UsrUser u) ->
                "sk-v1-tenant_r-1781257647568-glOIks8MJKvY".equals(u.getApiKey())
                        && "tenant_req_cc6d9abf7d074b2dae1f7151456219a4".equals(u.getTenantId())));
    }

    @Test
    void testUpdateUserSavesApiKeyAndTenantId() {
        UsrUser existing = new UsrUser();
        existing.setId(1L);
        existing.setPhone("13800138000");
        existing.setEmail("old@example.com");
        existing.setApiKey("old-api-key");
        existing.setTenantId("old-tenant-id");
        when(usrUserMapper.selectById(1L)).thenReturn(existing);
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setApiKey("sk-v1-new");
        request.setTenantId("tenant_new");

        assertTrue(usrUserService.updateUser(request));
        verify(usrUserMapper).updateById(argThat((UsrUser u) ->
                "sk-v1-new".equals(u.getApiKey()) && "tenant_new".equals(u.getTenantId())));
    }
```

- [ ] **Step 2: 运行 aeisp-user 模块测试**

```bash
mvn test -pl aeisp-user
```

Expected: 所有测试通过，包括新增的两个测试。

- [ ] **Step 3: Commit**

```bash
git add aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserServiceImplTest.java
git commit -m "test(user): 增加 api_key 与 tenant_id 创建/更新单元测试

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 5: 前端创建用户弹窗

**Files:**
- Modify: `aeisp-admin/src/views/user/list/index.vue:480`
- Modify: `aeisp-admin/src/views/user/list/index.vue:669`
- Modify: `aeisp-admin/src/views/user/list/index.vue:681`

- [ ] **Step 1: 扩展 createForm 初始化数据**

找到：

```js
const createForm = reactive({
  username: '', password: '123456', phone: '', email: '', nickname: '',
  roleIds: [], remainingMinutes: 0, isCompetition: 0
})
```

修改为：

```js
const createForm = reactive({
  username: '', password: '123456', phone: '', email: '', nickname: '',
  roleIds: [], remainingMinutes: 0, isCompetition: 0,
  apiKey: '', tenantId: ''
})
```

- [ ] **Step 2: 在创建弹窗中增加输入项**

找到创建弹窗中的“比赛用户”表单项：

```vue
        <el-form-item label="比赛用户">
          <el-select v-model="createForm.isCompetition" style="width:100px">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
        </el-form-item>
```

在其之前插入：

```vue
        <el-form-item label="租户ID">
          <el-input v-model="createForm.tenantId" placeholder="请输入租户ID" />
        </el-form-item>
        <el-form-item label="APIKEY">
          <el-input v-model="createForm.apiKey" placeholder="请输入APIKEY" />
        </el-form-item>
```

- [ ] **Step 3: 重置与提交时携带新字段**

找到 `handleCreate` 函数：

```js
function handleCreate() {
  createForm.username = ''; createForm.password = '123456'; createForm.phone = ''
  createForm.email = ''; createForm.nickname = ''
  createForm.roleIds = []; createForm.remainingMinutes = 0; createForm.isCompetition = 0
  createOpen.value = true
}
```

修改为：

```js
function handleCreate() {
  createForm.username = ''; createForm.password = '123456'; createForm.phone = ''
  createForm.email = ''; createForm.nickname = ''
  createForm.roleIds = []; createForm.remainingMinutes = 0; createForm.isCompetition = 0
  createForm.apiKey = ''; createForm.tenantId = ''
  createOpen.value = true
}
```

找到 `submitCreate` 函数中调用 `createUser` 的代码：

```js
    const password = await createUser({
      username: createForm.username,
      password: createForm.password,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined,
      nickname: createForm.nickname || undefined,
      roleIds: createForm.roleIds.length ? createForm.roleIds : undefined,
      remainingMinutes: createForm.remainingMinutes || undefined,
      isCompetition: createForm.isCompetition
    })
```

修改为：

```js
    const password = await createUser({
      username: createForm.username,
      password: createForm.password,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined,
      nickname: createForm.nickname || undefined,
      roleIds: createForm.roleIds.length ? createForm.roleIds : undefined,
      remainingMinutes: createForm.remainingMinutes || undefined,
      isCompetition: createForm.isCompetition,
      apiKey: createForm.apiKey,
      tenantId: createForm.tenantId
    })
```

- [ ] **Step 4: Commit**

```bash
git add aeisp-admin/src/views/user/list/index.vue
git commit -m "feat(admin): 创建用户弹窗增加 apiKey 与 tenantId

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 6: 前端编辑用户弹窗（含二次确认）

**Files:**
- Modify: `aeisp-admin/src/views/user/list/index.vue:494`
- Modify: `aeisp-admin/src/views/user/list/index.vue:547`
- Modify: `aeisp-admin/src/views/user/list/index.vue:555`
- Modify: `aeisp-admin/src/views/user/list/index.vue:112`

- [ ] **Step 1: 扩展 form 数据与原始值记录**

找到：

```js
const form = reactive({
  id: undefined, username: '', nickname: '', phone: '', email: '',
  status: 1, originalStatus: 1, adminPassword: ''
})
```

修改为：

```js
const form = reactive({
  id: undefined, username: '', nickname: '', phone: '', email: '',
  status: 1, originalStatus: 1, adminPassword: '',
  apiKey: '', tenantId: '', originalApiKey: '', originalTenantId: ''
})
```

- [ ] **Step 2: 在编辑弹窗中增加输入项**

找到编辑弹窗中的“状态”表单项：

```vue
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option
              v-for="item in userStatusOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
```

在其之前插入：

```vue
        <el-form-item label="租户ID">
          <el-input v-model="form.tenantId" placeholder="请输入租户ID" />
        </el-form-item>
        <el-form-item label="APIKEY">
          <el-input v-model="form.apiKey" placeholder="请输入APIKEY" />
        </el-form-item>
```

- [ ] **Step 3: handleUpdate 读取原始值与新字段**

找到：

```js
function handleUpdate(row) {
  Object.assign(form, {
    id: row.id, username: row.username, nickname: row.nickname,
    phone: row.phone, email: row.email, status: row.status,
    originalStatus: row.status, adminPassword: ''
  })
  open.value = true
}
```

修改为：

```js
function handleUpdate(row) {
  Object.assign(form, {
    id: row.id, username: row.username, nickname: row.nickname,
    phone: row.phone, email: row.email, status: row.status,
    originalStatus: row.status, adminPassword: '',
    apiKey: row.apiKey || '', tenantId: row.tenantId || '',
    originalApiKey: row.apiKey || '', originalTenantId: row.tenantId || ''
  })
  open.value = true
}
```

- [ ] **Step 4: submitUpdate 增加校验与二次确认**

找到 `submitUpdate` 函数：

```js
async function submitUpdate() {
  const baseData = { phone: form.phone, email: form.email, nickname: form.nickname }
  if (form.status !== form.originalStatus) {
    if (!form.adminPassword) {
      ElMessage.warning('状态已变更，请输入管理员密码')
      return
    }
    await updateUserStatus(form.id, {
      status: form.status,
      reason: '管理员手动修改',
      adminPassword: form.adminPassword
    })
    ElMessage.success('状态修改成功')
  }
  await updateUser(form.id, baseData)
  ElMessage.success('修改成功')
  open.value = false
  getList()
}
```

整体替换为：

```js
async function submitUpdate() {
  if (!form.apiKey || !form.tenantId) {
    ElMessage.warning('APIKEY 和 租户ID 不能为空')
    return
  }

  const apiKeyChanged = form.apiKey !== form.originalApiKey
  const tenantIdChanged = form.tenantId !== form.originalTenantId
  if (apiKeyChanged || tenantIdChanged) {
    const changedFields = []
    if (apiKeyChanged) changedFields.push('APIKEY')
    if (tenantIdChanged) changedFields.push('租户ID')
    try {
      await ElMessageBox.confirm(
        `${changedFields.join('、')} 已修改，确认保存？`,
        '确认修改',
        { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return
    }
  }

  const baseData = {
    phone: form.phone,
    email: form.email,
    nickname: form.nickname,
    apiKey: form.apiKey,
    tenantId: form.tenantId
  }
  if (form.status !== form.originalStatus) {
    if (!form.adminPassword) {
      ElMessage.warning('状态已变更，请输入管理员密码')
      return
    }
    await updateUserStatus(form.id, {
      status: form.status,
      reason: '管理员手动修改',
      adminPassword: form.adminPassword
    })
    ElMessage.success('状态修改成功')
  }
  await updateUser(form.id, baseData)
  ElMessage.success('修改成功')
  open.value = false
  getList()
}
```

注意：需要在 `<script setup>` 顶部确认已导入 `ElMessageBox`。当前文件顶部已有 `import { ElMessage } from 'element-plus'`，需改为：

```js
import { ElMessage, ElMessageBox } from 'element-plus'
```

- [ ] **Step 5: Commit**

```bash
git add aeisp-admin/src/views/user/list/index.vue
git commit -m "feat(admin): 编辑用户弹窗增加 apiKey/tenantId 修改与二次确认

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 7: 前端用户详情展示

**Files:**
- Modify: `aeisp-admin/src/views/user/list/index.vue:313`

- [ ] **Step 1: 在详情描述列表中增加两列**

找到详情弹窗“基本信息”中的：

```vue
            <el-descriptions-item label="注册IP">{{ detailUser?.registerIp || '-' }}</el-descriptions-item>
            <el-descriptions-item label="比赛用户">{{ detailUser?.isCompetition === 1 ? '是' : '否' }}</el-descriptions-item>
```

在其之前插入：

```vue
            <el-descriptions-item label="租户ID">{{ detailUser?.tenantId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="APIKEY">{{ detailUser?.apiKey || '-' }}</el-descriptions-item>
```

- [ ] **Step 2: Commit**

```bash
git add aeisp-admin/src/views/user/list/index.vue
git commit -m "feat(admin): 用户详情展示 apiKey 与 tenantId

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Task 8: 后端编译与全量测试

- [ ] **Step 1: 编译 aeisp-user 模块**

```bash
mvn clean compile -pl aeisp-user -am
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 运行 aeisp-user 测试**

```bash
mvn test -pl aeisp-user
```

Expected: Tests run: [number], Failures: 0, Errors: 0

- [ ] **Step 3: 全量编译后端**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

---

## Task 9: 集成验证

- [ ] **Step 1: 确认本地基础设施已启动**

```bash
docker compose up -d
```

- [ ] **Step 2: 执行数据库迁移（如针对已存在的数据库）**

```bash
psql -U postgres -d aeisp -f docs/sql/migration-add-user-apikey-tenantid.sql
```

- [ ] **Step 3: 启动后端**

```bash
mvn spring-boot:run -pl aeisp-boot
```

- [ ] **Step 4: 启动前端**

```bash
cd aeisp-admin && npm run dev
```

- [ ] **Step 5: 手动验证**

1. 登录后台，进入“用户管理 → 用户列表”。
2. 点击“创建”，填写用户名、APIKEY、租户ID 后提交，确认创建成功。
3. 点击用户行“详情”，确认 APIKEY 和 租户ID 显示正确。
4. 点击“编辑”，修改 APIKEY 或 租户ID，点击确定，确认弹出二次确认框。
5. 取消确认，确认值未保存；再次编辑并确认，确认值已更新。
6. 编辑时将 APIKEY 或 租户ID 清空，点击确定，确认提示“不能为空”。

---

## Task 10: 最终提交

- [ ] **Step 1: 检查 git 状态**

```bash
git status
```

Expected: 所有改动均已提交，工作区干净。

- [ ] **Step 2: 如无未提交改动，任务完成**

如果验证过程中有临时改动未提交，先补充提交：

```bash
git add -A
git commit -m "feat(user): 用户数据支持 api_key 与 tenant_id

- 数据库 usr_user 表增加 api_key、tenant_id 字段
- 后端创建/更新/详情接口支持新字段及校验
- 前端创建/编辑弹窗与详情页支持新字段
- 编辑修改 APIKEY/租户ID 时增加二次确认

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Self-Review 检查清单

1. **Spec coverage**
   - [x] 数据库新增 `api_key`、`tenant_id` 两列 → Task 1
   - [x] 创建时必填并校验格式 → Task 2 (UserCreateRequest) + Task 5
   - [x] 编辑时必填并校验格式 → Task 2 (UserUpdateRequest) + Task 6
   - [x] 编辑修改时二次确认 → Task 6
   - [x] 后端 Service 创建/更新/VO 转换 → Task 3
   - [x] 前端创建/编辑/详情展示 → Task 5/6/7
   - [x] 单元测试覆盖 → Task 4

2. **Placeholder scan**
   - [x] 无 TBD/TODO/"implement later"
   - [x] 所有代码步骤包含完整代码片段
   - [x] 所有测试步骤包含可运行命令

3. **Type consistency**
   - [x] 后端字段名统一使用 `apiKey` / `tenantId`（Java camelCase）
   - [x] 数据库列名统一使用 `api_key` / `tenant_id`（PostgreSQL snake_case）
   - [x] 前端表单字段统一使用 `apiKey` / `tenantId`
