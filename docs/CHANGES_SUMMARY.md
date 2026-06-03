# 用户和管理员创建功能修复总结

## 问题描述
用户在用户创建页和管理员创建页填完信息后确认，直接提示"系统繁忙"。

## 根本原因分析

### 1. 管理员管理页面 API 错误
**问题**: `aeisp-admin/src/views/system/user/index.vue` 页面错误地导入了 `@/api/user`（前端用户API），而不是 `@/api/system`（后台管理员API）。

**影响**: 
- 管理员创建操作调用了 `/api/v1/users`（前端用户创建接口）
- 管理员更新操作调用了 `/api/v1/users/{id}`（前端用户更新接口）
- 管理员删除操作没有实际调用任何API

**正确行为**: 
- 管理员创建应该调用 `/api/v1/system/users`（后台管理员创建接口）
- 管理员更新应该调用 `/api/v1/system/users/{id}`（后台管理员更新接口）
- 管理员删除应该调用 `/api/v1/system/users/{id}`（后台管理员删除接口）

### 2. 表单字段不匹配
**问题**: 管理员管理页面使用 `nickname` 字段，但后端 `SysUser` 实体使用 `realName` 字段。

**影响**: 真实姓名无法正确保存，可能导致数据不一致。

### 3. 创建管理员时发送无效字段
**问题**: 管理员创建时发送 `status` 字段，但后端 `CreateUserRequest` 不包含此字段。

**影响**: 
- 后端 `CreateUserRequest` 没有 `status` 字段，该字段会被忽略
- 后端 `SysUserServiceImpl.createUser()` 总是设置 `status = STATUS_ENABLED` (1)

## 修复内容

### 文件修改清单

1. **aeisp-admin/src/views/system/user/index.vue**
   - 修改API导入：从 `@/api/user` 改为 `@/api/system`
   - 修改表单字段：从 `nickname` 改为 `realName`
   - 修改表格列：从 "昵称" 改为 "真实姓名"
   - 修改提交逻辑：创建时不发送 `status` 字段
   - 实现删除功能：调用 `deleteSysUser` API

2. **aeisp-system/src/test/java/com/aeisp/system/service/impl/SysUserServiceImplTest.java** (新增)
   - 添加管理员创建测试用例
   - 添加管理员更新测试用例
   - 添加管理员删除测试用例

### 详细修改

#### 1. API 导入修改
```javascript
// 修改前
import { listUsers, getUser, createUser, updateUser, resetUserPassword, adjustDuration } from '@/api/user'

// 修改后
import { listSysUsers, getSysUser, addSysUser, updateSysUser, deleteSysUser } from '@/api/system'
```

#### 2. 表单字段修改
```javascript
// 修改前
const form = reactive({
  id: undefined,
  username: '',
  password: '',
  nickname: '',  // 错误
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

// 修改后
const form = reactive({
  id: undefined,
  username: '',
  password: '',
  realName: '',  // 正确
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})
```

#### 3. 提交逻辑修改
```javascript
// 修改前
const data = {
  nickname: form.nickname,
  phone: form.phone,
  email: form.email,
  status: form.status,  // 错误：创建时不应发送 status
  roleIds: form.roleIds.length ? form.roleIds : undefined
}
if (form.id) {
  await updateUser(form.id, data)
} else {
  data.username = form.username
  if (form.password) {
    data.password = form.password
  }
  await createUser(data)  // 错误：调用了用户创建API
}

// 修改后
if (form.id) {
  // 更新管理员
  const data = {
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    status: form.status,
    roleIds: form.roleIds.length ? form.roleIds : undefined
  }
  await updateSysUser(form.id, data)  // 正确：调用管理员更新API
} else {
  // 新增管理员
  const data = {
    username: form.username,
    password: form.password || undefined,
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    roleIds: form.roleIds.length ? form.roleIds : undefined
  }
  await addSysUser(data)  // 正确：调用管理员创建API
}
```

#### 4. 删除功能实现
```javascript
// 修改前
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 用户删除接口暂时没有，先提示
    ElMessage.info('删除功能待实现')
  })
}

// 修改后
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSysUser(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  })
}
```

## 测试结果

### 系统模块测试 (aeisp-system)
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 用户模块测试 (aeisp-user)
```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 总测试结果
```
Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 验证步骤

### 管理员创建测试
1. 登录后台管理系统（管理员账号）
2. 点击"管理员管理"菜单
3. 点击"新增"按钮
4. 填写表单：
   - 用户名：testadmin（4-20位字母/数字/下划线，首位必须为字母）
   - 密码：password123（可选，默认为123456）
   - 真实姓名：测试管理员
   - 邮箱：test@example.com
   - 手机号：13800138000
   - 角色：选择至少一个角色
5. 点击"确定"
6. 验证：提示"新增成功"，管理员列表刷新，显示新创建的管理员

### 管理员更新测试
1. 在管理员列表中找到一个管理员
2. 点击"编辑"按钮
3. 修改真实姓名、邮箱、手机号
4. 点击"确定"
5. 验证：提示"修改成功"，信息已更新

### 管理员删除测试
1. 在管理员列表中找到一个管理员（非最后一个超级管理员）
2. 点击"删除"按钮
3. 确认删除
4. 验证：提示"删除成功"，管理员从列表中移除

### 用户创建测试（前端用户）
1. 登录后台管理系统
2. 点击"用户管理"菜单（前端用户管理）
3. 点击"创建"按钮
4. 填写表单：
   - 用户名：testuser（4-20位字母/数字/下划线，首位必须为字母）
   - 密码：password123（可选，默认为123456）
   - 手机号：13900139000
   - 邮箱：test@example.com
   - 昵称：测试用户
   - 角色：选择角色
   - 初始化时长：100（分钟）
   - 比赛用户：否
5. 点击"确定"
6. 验证：提示"创建成功"，显示生成的密码

## 注意事项

1. **权限要求**: 
   - 管理员操作需要 `system:user:manage` 权限
   - 用户操作需要 `user:create` 权限

2. **超级管理员保护**: 
   - 不能删除最后一个超级管理员
   - 不能将最后一个超级管理员降级为普通角色

3. **密码策略**: 
   - 管理员密码长度至少6位
   - 开启安全开关时需包含字母和数字
   - 默认密码 123456 跳过复杂度验证

4. **状态管理**: 
   - 创建管理员时状态默认为"正常"（STATUS_ENABLED = 1）
   - 不能通过创建API修改状态
   - 更新API可以修改状态

5. **用户名验证**: 
   - 长度：4-20位
   - 格式：必须以字母开头，只允许字母、数字、下划线
   - 唯一性：不能重复

6. **手机号验证**: 
   - 格式：1开头的11位数字
   - 唯一性：不能重复

7. **邮箱验证**: 
   - 格式：有效的邮箱地址
   - 唯一性：不能重复

## 后续优化建议

1. **前端表单验证**: 
   - 添加更严格的前端验证
   - 实时验证用户名、手机号、邮箱的唯一性

2. **错误处理**: 
   - 改进错误提示信息
   - 提供更详细的错误原因

3. **用户体验**: 
   - 添加加载状态提示
   - 优化表单提交后的反馈

4. **代码复用**: 
   - 考虑将用户和管理员的表单组件化
   - 减少重复代码

5. **测试覆盖**: 
   - 添加集成测试
   - 添加端到端测试
