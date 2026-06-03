# 用户和管理员创建功能修复

## 问题描述
用户在用户创建页和管理员创建页填完信息后确认，直接提示"系统繁忙"。

## 问题分析

### 1. 管理员管理页面 API 错误
**问题**: `aeisp-admin/src/views/system/user/index.vue` 页面错误地导入了 `@/api/user`（前端用户API），而不是 `@/api/system`（后台管理员API）。

**影响**: 管理员创建、更新、删除操作都调用了错误的API端点。

### 2. 管理员表单字段不匹配
**问题**: 管理员管理页面使用 `nickname` 字段，但后端 `SysUser` 实体使用 `realName` 字段。

**影响**: 真实姓名无法正确保存。

### 3. 创建管理员时发送无效字段
**问题**: 管理员创建时发送 `status` 字段，但后端 `CreateUserRequest` 不包含此字段（后端总是设置为 `STATUS_ENABLED`）。

**影响**: 可能导致验证错误或字段被忽略。

## 修复内容

### 1. 修复管理员管理页面 API 导入
**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改前**:
```javascript
import { listUsers, getUser, createUser, updateUser, resetUserPassword, adjustDuration } from '@/api/user'
```

**修改后**:
```javascript
import { listSysUsers, getSysUser, addSysUser, updateSysUser, deleteSysUser } from '@/api/system'
```

### 2. 修复表单字段名称
**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改前**:
- 表单字段: `nickname`
- 表格列: "昵称" (prop="nickname")

**修改后**:
- 表单字段: `realName`
- 表格列: "真实姓名" (prop="realName")

### 3. 修复提交逻辑
**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改前**:
```javascript
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
  await createUser(data)
}
```

**修改后**:
```javascript
if (form.id) {
  // 更新管理员
  const data = {
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    status: form.status,
    roleIds: form.roleIds.length ? form.roleIds : undefined
  }
  await updateSysUser(form.id, data)
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
  await addSysUser(data)
}
```

### 4. 修复删除功能
**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改前**:
```javascript
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
```

**修改后**:
```javascript
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

### 5. 添加单元测试
**文件**: `aeisp-system/src/test/java/com/aeisp/system/service/impl/SysUserServiceImplTest.java`

创建了完整的单元测试覆盖管理员创建、更新、删除功能：
- `testCreateUserSuccess`: 测试创建管理员成功
- `testCreateUserWithoutPassword`: 测试不传密码的情况
- `testCreateUserWithEmptyPassword`: 测试空密码的情况
- `testCreateUserWithoutRoles`: 测试不分配角色的情况
- `testCreateUserWithEmptyRoles`: 测试分配空角色列表的情况
- `testCreateUserFailure`: 测试创建失败的情况
- `testCreateUserSetsStatusEnabled`: 测试状态设置为启用
- `testUpdateUserSuccess`: 测试更新管理员成功
- `testUpdateUserWithPassword`: 测试更新时包含密码
- `testUpdateUserWithoutPassword`: 测试更新时不包含密码
- `testDeleteUserSuccess`: 测试删除管理员成功
- `testDeleteUserFailure`: 测试删除失败的情况

## 测试结果

### 系统模块测试
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 用户模块测试
```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 验证步骤

1. **管理员创建测试**:
   - 登录后台管理系统
   - 进入"管理员管理"页面
   - 点击"新增"按钮
   - 填写用户名、密码、真实姓名、邮箱、手机号
   - 选择角色
   - 点击"确定"
   - 验证提示"新增成功"，且管理员列表刷新

2. **管理员更新测试**:
   - 在管理员列表中找到一个管理员
   - 点击"编辑"按钮
   - 修改真实姓名、邮箱、手机号
   - 点击"确定"
   - 验证提示"修改成功"，且信息已更新

3. **管理员删除测试**:
   - 在管理员列表中找到一个管理员（非最后一个超级管理员）
   - 点击"删除"按钮
   - 确认删除
   - 验证提示"删除成功"，且管理员从列表中移除

4. **用户创建测试**:
   - 登录后台管理系统
   - 进入"用户管理"页面（前端用户）
   - 点击"创建"按钮
   - 填写用户名、密码、手机号、邮箱、昵称
   - 选择角色和初始化时长
   - 点击"确定"
   - 验证提示"创建成功"，且显示生成的密码

## 注意事项

1. **权限要求**: 管理员操作需要 `system:user:manage` 权限
2. **超级管理员保护**: 不能删除最后一个超级管理员
3. **密码策略**: 管理员密码长度至少6位，开启安全开关时需包含字母和数字
4. **状态管理**: 创建管理员时状态默认为"正常"，不能通过API修改
