# 超级管理员信息

## 默认超级管理员账号

项目初始化时会自动创建一个超级管理员账号：

### 账号信息
- **用户名**: admin
- **密码**: admin123（BCrypt加密存储）
- **真实姓名**: 系统管理员
- **邮箱**: admin@aeisp.com
- **手机号**: 13800138000
- **状态**: 正常（1）

### 角色信息
- **角色ID**: 1
- **角色名**: 超级管理员
- **角色编码**: ROLE_SUPER_ADMIN
- **数据范围**: ALL（全部数据）
- **系统内置**: 是（不可删除）

## 超级管理员保护机制

### 1. 删除保护
代码中实现了防止删除最后一个超级管理员的保护机制：

```java
// SysUserServiceImpl.java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean deleteUser(Long userId) {
    // 防止删除最后一个超级管理员
    if (hasSuperAdminRole(userId) && isLastSuperAdmin(userId)) {
        throw new BizException(SystemErrorCode.USER_CANNOT_DELETE_LAST_SUPER);
    }
    int rows = sysUserMapper.deleteById(userId);
    if (rows > 0) {
        sysUserRoleMapper.deleteByUserId(userId);
    }
    return rows > 0;
}

private boolean hasSuperAdminRole(Long userId) {
    List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
        new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, userId));
    return userRoles.stream().anyMatch(ur -> SUPER_ADMIN_ROLE_ID.equals(ur.getRoleId()));
}

private boolean isLastSuperAdmin(Long userId) {
    long count = sysUserRoleMapper.countUsersByRoleId(SUPER_ADMIN_ROLE_ID);
    return count <= 1;
}
```

### 2. 保护逻辑说明
1. **检查用户角色**: `hasSuperAdminRole()` 方法检查用户是否拥有超级管理员角色（ROLE_SUPER_ADMIN）
2. **检查是否最后一个**: `isLastSuperAdmin()` 方法检查是否是最后一个拥有超级管理员角色的用户
3. **抛出异常**: 如果是最后一个超级管理员，删除时会抛出 `USER_CANNOT_DELETE_LAST_SUPER` 异常

### 3. 测试用例
新增了3个测试用例验证保护机制：

1. **testDeleteLastSuperAdminThrowsException**: 测试不能删除最后一个超级管理员
2. **testDeleteNonSuperAdminSuccess**: 测试可以删除非超级管理员用户
3. **testDeleteSuperAdminNotLastSuccess**: 测试可以删除有超级管理员角色但不是最后一个的用户

## 数据库初始化

超级管理员账号在数据库初始化脚本中创建：

```sql
-- 初始化超级管理员账号（密码明文：admin123，BCrypt加密后）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, created_at, updated_at) VALUES
(1, 'admin', '$2b$12$3iUnfkqO7aNwxbd///FNUOiWsdKd33d74KiUSL5dYjcu3yWSfsNz2', '系统管理员', 'admin@aeisp.com', '13800138000', 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 关联超级管理员与角色
INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES
(1, 1, NOW())
ON CONFLICT DO NOTHING;
```

## 使用建议

1. **首次登录**: 使用 `admin / admin123` 登录系统
2. **修改密码**: 首次登录后建议立即修改密码
3. **创建其他管理员**: 可以创建其他管理员账号并分配超级管理员角色
4. **删除保护**: 只有当存在多个超级管理员时，才能删除其中一个

## 常见问题

### Q: 如何创建新的超级管理员？
A: 在"管理员管理"页面创建新管理员时，选择"超级管理员"角色即可。

### Q: 如何删除超级管理员？
A: 只有当存在多个超级管理员时，才能删除其中一个。系统会阻止删除最后一个超级管理员。

### Q: 如果忘记了超级管理员密码怎么办？
A: 需要通过数据库直接修改密码，或者使用其他有权限的管理员账号重置密码。

### Q: 超级管理员有什么特殊权限？
A: 超级管理员拥有系统所有权限，可以访问所有菜单和功能，不受任何权限限制。
