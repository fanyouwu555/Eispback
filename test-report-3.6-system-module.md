# 3.6 系统基础管理模块 - 测试报告

**测试日期**: 2026-05-20
**测试范围**: 权限管理、日志管理、官网配置
**测试方式**: API接口测试 + 前端代码审查 + 后端代码审查

---

## 一、权限管理功能测试

### 1.1 角色列表查询

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| GET /api/v1/system/roles | 返回7个预设角色 | 返回7个预设角色，数据正确 | 通过 |
| 角色字段完整性 | 包含roleName, roleCode, description, status, isSystem | 字段完整，但permissions为null | 部分通过 |

**说明**: 角色列表返回与文档3.6.2中roles表预设数据一致（超级管理员、用户管理员、模型管理员、消息管理员、模板管理员、财务管理员、普通用户）。

### 1.2 权限点列表查询

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| GET /api/v1/system/permissions | 返回权限点列表 | 返回空数组 `[]` | **不通过** |

**问题分析**:
- `docs/sql/init.sql` 中 `sys_permission` 表 **没有初始化INSERT数据**
- 文档3.6.2中定义了权限点预设数据（user:create, user:read, model:create等），但SQL脚本未插入
- 导致RBAC权限体系中权限编码为空，权限校验基础数据缺失

### 1.3 创建角色

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| POST /api/v1/system/roles | 创建成功，返回200 | 返回500 "系统繁忙" | **不通过** |

**问题分析**:
- 需要进一步排查具体异常堆栈
- 可能原因：MyBatis-Plus自动填充字段冲突、数据库字段不匹配、或事务问题

### 1.4 删除角色

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| DELETE /api/v1/system/roles/1 (系统内置角色) | 返回业务异常"系统内置角色不可删除" | 返回500 "系统繁忙" | **不通过** |

**问题分析**:
- `SysRoleServiceImpl.deleteRole()` 中确实检查了 `isSystem == 1` 并抛出 `BizException`
- 但 `GlobalExceptionHandler.handleBizException()` 应该能正确处理
- 500错误说明在到达Service逻辑前就已发生异常（可能是Mapper查询时）

### 1.5 RBAC权限校验

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| JWT中包含角色和权限 | /auth/info返回roles和permissions | roles正确，permissions为空数组 | **不通过** |
| 后端API权限拦截 | 无权限返回403 | 未实现 | **不通过** |
| 前端路由权限过滤 | 按权限过滤菜单 | 未实现 | **不通过** |

**问题分析**:
1. **后端无API权限控制**: `SecurityConfig` 中仅配置了认证，没有基于权限的URL拦截。所有Controller（包括SysRoleController, SysConfigController等）均未使用 `@PreAuthorize` 注解。`SysUserController` 注释明确说明"当前版本暂未添加 @PreAuthorize 注解"。
2. **前端路由无权限过滤**: `router/index.js` 的 `beforeEach` 守卫只检查token，没有根据 `meta.permissions` 过滤路由，所有登录用户都能看到所有菜单。
3. **前端权限指令失效**: `v-permission` 指令和 `hasPermission` 函数依赖 `userStore.permissions`，但该数组始终为空（因为数据库无权限点数据）。

### 1.6 角色权限分配

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 前端分配权限弹窗 | 选择权限后调用API保存 | 仅打印console.log，未调用API | **不通过** |

**问题代码** (`aeisp-admin/src/views/system/role/index.vue:140-145`):
```javascript
async function submitPermission() {
  const checkedKeys = treeRef.value.getCheckedKeys()
  console.log('分配权限:', currentRole.value.id, checkedKeys)
  permOpen.value = false
  ElMessage.success('权限分配成功')
}
```
- 未调用 `updateRole` API 保存权限分配结果
- 即使调用了，前端 `permissionList` 也是空数组（无权限点数据）

---

## 二、日志管理功能测试

### 2.1 系统操作日志

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 操作日志分页查询 | 返回分页数据 | 成功，分页正常 | 通过 |
| 按操作人筛选 | 支持username模糊搜索 | 成功，返回42条记录 | 通过 |
| 按时间范围筛选 | 支持startTime/endTime | 返回0条（可能时区/格式问题） | 部分通过 |
| 自动记录管理员操作 | 角色增删改、配置更新均被记录 | 仅登录操作被记录 | **不通过** |
| 记录登录失败 | 失败登录也应记录 | 未记录 | **不通过** |

**问题分析**:
1. **@OperationLog注解未被使用**: 全局搜索项目中没有任何Controller方法使用 `@OperationLog` 注解。`OperationLogAspect` 虽然存在，但永远不会被触发。
2. **仅登录被记录**: `AuthController` 中手动调用 `sysOperationLogService.saveLog` 记录登录，但其他操作（创建角色、更新配置等）均无日志记录。
3. **登录失败未记录**: `AuthController.login()` 的 `catch (BadCredentialsException)` 分支没有调用 `recordLoginLog`，导致失败登录无审计记录。
4. **日志字段缺失**: 操作日志VO中缺少 `sensitivity` 标记的实际业务逻辑（哪些操作属于敏感操作）。

### 2.2 用户行为日志

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 表结构 | 存在sys_user_behavior_log表 | 表存在，字段符合文档 | 通过 |
| 写入逻辑 | 用户关键行为被记录 | **无任何写入逻辑** | **不通过** |
| 查询接口 | 支持按用户/行为类型/时间筛选 | **无Controller/Service** | **不通过** |

**问题分析**:
- 仅有 `SysUserBehaviorLog` Entity 和 `SysUserBehaviorLogMapper`，无Service、无Controller、无写入触发点
- 文档3.6.1要求记录"登录登出、创建项目、发起模型调用、充值"等行为，均未实现

### 2.3 异常报错日志

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 表结构 | 存在sys_error_log表 | 表存在，字段符合文档 | 通过 |
| 写入逻辑 | 系统异常被记录 | **无任何写入逻辑** | **不通过** |
| 查询接口 | 支持按类型/模块/严重程度筛选 | **无Controller/Service** | **不通过** |

**问题分析**:
- 仅有 `SysErrorLog` Entity 和 `SysErrorLogMapper`，无Service、无Controller
- `GlobalExceptionHandler` 中未调用error log保存逻辑
- 文档3.6.1要求"记录系统运行过程中捕获的所有异常"，未实现

---

## 三、官网配置功能测试

### 3.1 配置列表查询

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| GET /api/v1/system/configs | 返回所有配置 | 返回18条配置，数据正确 | 通过 |
| 多环境配置显示 | official_website_url应有多条（prod/test） | 列表中只返回1条（可能是LIMIT 1导致） | **不通过** |

### 3.2 根据Key获取配置值

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| GET /api/v1/system/configs/official_website_url | 返回对应环境的官网地址 | 返回 `null` | **不通过** |
| GET /api/v1/system/configs/contact_email | 返回邮箱 | 返回正确值 | 通过 |

**问题分析**:
- `SysConfigMapper.selectByConfigKey` 使用 `LIMIT 1` 查询，但 `official_website_url` 在数据库中有两条记录（prod和test环境）
- SQL未指定ORDER BY，返回结果不确定
- **更严重的问题**: 未实现按当前环境筛选配置的逻辑。文档3.6.1要求"可分别为开发环境（dev）、测试环境（test）和生产环境（prod）设置不同的官网地址"，但代码中没有任何环境判断逻辑

### 3.3 更新配置值

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| PUT /api/v1/system/configs/{key}?value=xxx | 更新成功 | 更新成功 | 通过 |
| 前端调用兼容性 | 前端传递JSON body，后端正确接收 | 前端JSON body未生效 | **不通过** |

**问题分析**:
- 前端 `updateConfig(key, { configValue: value })` 发送JSON body
- 后端 `updateConfig(@PathVariable String key, @RequestParam String value)` 期望query parameter
- 测试结果证实：JSON body调用返回200但值未改变；query param调用才生效
- 这是一个**前端与后端接口契约不匹配**的问题

### 3.4 配置变更记录操作日志

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 配置变更后记录操作日志 | 审计追踪 | 未记录 | **不通过** |

---

## 四、前端管理页面代码审查

### 4.1 角色管理页面 (`views/system/role/index.vue`)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 角色列表展示 | 通过 | 正常显示，区分系统内置/自定义标签 |
| 新增/编辑角色 | 通过 | 表单验证正常（roleName, roleCode必填） |
| 删除角色 | 通过 | 系统内置角色不显示删除按钮（前端层面） |
| **分配权限** | **不通过** | 未调用API保存权限分配，仅弹窗展示 |

### 4.2 权限管理页面 (`views/system/permission/index.vue`)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 权限列表展示 | 部分通过 | 使用了`row.type`和`row.sort`，但Entity中无此字段 |
| 权限树形结构 | 不通过 | 返回的是平级列表，但前端无树形转换逻辑 |

### 4.3 操作日志页面 (`views/system/log/index.vue`)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 日志列表展示 | 通过 | 分页、状态标签正常 |
| 按操作人筛选 | 通过 | 输入框正常 |
| 按时间范围筛选 | 通过 | 日期选择器正常 |
| 按模块/状态筛选 | 不通过 | 前端无模块和状态筛选UI |

### 4.4 系统配置页面 (`views/system/config/index.vue`)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 配置列表展示 | 通过 | 正常显示 |
| 编辑配置 | **不通过** | 弹窗中configName字段显示为undefined（API未返回该字段） |
| 更新配置 | **不通过** | 调用参数格式与后端不匹配（JSON body vs query param） |

**说明**: `SysConfigVO` 中只有 `configKey`, `configValue`, `description`, `createdAt`, `updatedAt` 字段，但前端使用了 `configName` 和 `remark`，导致编辑弹窗中配置名称为空。

---

## 五、文档与实现对照

### 5.1 已实现的功能

- [x] 角色表结构和7个预设角色
- [x] 权限点表结构
- [x] 角色CRUD接口（部分可用）
- [x] 操作日志表结构和分页查询
- [x] 登录日志记录（手动实现）
- [x] 系统配置表结构和CRUD
- [x] JWT认证和角色提取
- [x] 前端角色管理页面（基础功能）
- [x] 前端操作日志查询页面
- [x] 前端系统配置页面（基础展示）

### 5.2 未实现/有缺陷的功能

- [ ] **权限点初始化数据**: SQL脚本缺少INSERT语句
- [ ] **后端API权限拦截**: 无@PreAuthorize，无URL级别的权限控制
- [ ] **前端路由权限过滤**: 所有用户都能看到所有菜单
- [ ] **操作日志AOP自动记录**: @OperationLog注解存在但未被使用
- [ ] **用户行为日志写入**: 有表但无任何业务逻辑
- [ ] **异常日志写入**: 有表但无任何业务逻辑
- [ ] **官网配置多环境隔离**: 未实现按环境筛选配置
- [ ] **角色权限分配前端**: 未调用API保存
- [ ] **创建角色API**: 500错误
- [ ] **删除角色API**: 500错误（系统内置角色保护逻辑无法触发）
- [ ] **前后端接口契约不一致**: updateConfig参数格式不匹配
- [ ] **前端字段不匹配**: configName/remark vs description

---

## 六、测试结论

### 6.1 总体评估

| 功能模块 | 完成度 | 主要问题 |
|----------|--------|----------|
| 权限管理（RBAC） | 40% | 权限点数据缺失、API无权限拦截、角色增删500错误、权限分配未实现 |
| 日志管理 | 30% | 仅登录日志被记录，行为日志和异常日志完全未实现，AOP日志未启用 |
| 官网配置 | 50% | 多环境隔离未实现，前后端接口不匹配，配置变更无审计 |

### 6.2 优先级建议

**P0（阻塞性问题）**:
1. 修复创建角色和删除角色的500错误
2. 补充权限点初始化数据到SQL脚本
3. 实现后端API权限拦截（@PreAuthorize或Security配置）

**P1（重要缺陷）**:
4. 启用@OperationLog注解或手动在关键操作处记录日志
5. 实现用户行为日志和异常日志的写入逻辑
6. 修复前端updateConfig调用格式与后端匹配
7. 实现角色权限分配的前端API调用

**P2（体验优化）**:
8. 实现官网配置的多环境隔离（传入environment参数）
9. 前端路由根据权限动态过滤
10. 统一前后端字段命名（configName/description, remark/description）
