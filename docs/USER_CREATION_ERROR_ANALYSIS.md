# 用户创建页面"数据库操作失败"问题分析

## 问题描述

在前端用户创建页面填完信息点击确认后，提示"数据库操作失败"。

## 问题分析

### 错误日志

```
ERROR: null value in column "register_ip" of relation "usr_user" violates not-null constraint
```

### 根本原因

在 `UsrUserServiceImpl.createByAdmin()` 方法中创建用户时，没有设置 `register_ip` 字段，但数据库表 `usr_user` 的 `register_ip` 字段是 NOT NULL 约束。

### 代码位置

**文件**: `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java`

**问题代码** (第130-141行):
```java
UsrUser user = new UsrUser();
user.setUsername(request.getUsername());
user.setPhone(request.getPhone());
user.setEmail(request.getEmail());
user.setNickname(request.getNickname());
user.setPassword(passwordEncoder.encode(rawPassword));
user.setStatus(request.getStatus() != null ? request.getStatus() : CommonConstants.USER_STATUS_NORMAL);
user.setNeedChangePassword(1);
user.setFailedLoginAttempts(0);
// 缺少: user.setRegisterIp(...);
user.setRegisterTime(LocalDateTime.now());
user.setIsCompetition(request.getIsCompetition() != null ? request.getIsCompetition() : 0);
user.setDeleted(CommonConstants.DELETED_NO);
```

## 修复方案

在创建用户时设置 `register_ip` 字段为 "system"，表示是管理员创建的用户。

### 修复代码

```java
UsrUser user = new UsrUser();
user.setUsername(request.getUsername());
user.setPhone(request.getPhone());
user.setEmail(request.getEmail());
user.setNickname(request.getNickname());
user.setPassword(passwordEncoder.encode(rawPassword));
user.setStatus(request.getStatus() != null ? request.getStatus() : CommonConstants.USER_STATUS_NORMAL);
user.setNeedChangePassword(1);
user.setFailedLoginAttempts(0);
user.setRegisterIp("system"); // 新增：设置注册IP为 system
user.setRegisterTime(LocalDateTime.now());
user.setIsCompetition(request.getIsCompetition() != null ? request.getIsCompetition() : 0);
user.setDeleted(CommonConstants.DELETED_NO);
```

## 验证测试

### 新增测试类

**文件**: `aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserRegisterIpTest.java`

**测试用例**:
1. `testCreateUserSetsRegisterIpToSystem()` - 验证 register_ip 字段设置为 "system"
2. `testCreateUserRegisterIpIsNotNull()` - 验证 register_ip 字段不为 null

### 测试结果

```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 修复验证

### 后端服务状态

- ✅ 后端服务已重启
- ✅ 修复代码已部署
- ✅ 测试用例已通过

### 前端页面

- ✅ 管理员创建页面状态选择已移除（创建时不显示）
- ✅ 密码输入框提示"留空默认为123456"

## 后续建议

1. **IP地址记录**: 考虑记录真实的客户端IP地址而不是固定值 "system"
2. **审计日志**: 添加管理员操作的详细审计日志
3. **权限验证**: 确保只有有权限的管理员才能创建用户

## 相关文件

- `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java` - 已修复
- `aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserRegisterIpTest.java` - 新增测试
- `aeisp-admin/src/views/system/user/index.vue` - 前端页面已优化
