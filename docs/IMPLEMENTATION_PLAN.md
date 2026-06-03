# 实施计划：潜在风险处理与验证

## 一、任务分解

### 任务1: 编写状态管理验证测试 (0.5天)

#### 1.1 管理员状态管理测试
**文件**: `aeisp-system/src/test/java/com/aeisp/system/service/impl/SysUserStatusTest.java`

**测试用例**:
```java
@Test
void testCreateUserIgnoresStatusField() {
    // 验证管理员创建时忽略 status 字段
    // 发送 status=2 (禁用)，验证创建后状态为 1 (正常)
}

@Test
void testUpdateUserAcceptsStatusField() {
    // 验证管理员更新时接受 status 字段
    // 更新 status=2 (禁用)，验证状态成功更新
}
```

#### 1.2 前端用户状态管理测试
**文件**: `aeisp-user/src/test/java/com/aeisp/user/service/impl/UsrUserStatusTest.java`

**测试用例**:
```java
@Test
void testCreateUserAcceptsStatusField() {
    // 验证前端用户创建时接受 status 字段
    // 发送 status=2 (禁用)，验证创建后状态为 2 (禁用)
}
```

### 任务2: 编写密码策略验证测试 (0.5天)

#### 2.1 密码默认值测试
**文件**: `aeisp-system/src/test/java/com/aeisp/system/service/impl/PasswordStrategyTest.java`

**测试用例**:
```java
@Test
void testAdminCreateUserDefaultPassword() {
    // 验证管理员创建时密码为空使用默认密码 "123456"
}

@Test
void testUserCreateUserDefaultPassword() {
    // 验证前端用户创建时密码为空使用默认密码 "123456"
}
```

#### 2.2 密码复杂度测试
**测试用例**:
```java
@Test
void testPassword123456SkipsComplexityCheck() {
    // 验证 "123456" 跳过复杂度验证
}

@Test
void testPasswordWithoutNumberFails() {
    // 验证只有字母的密码失败
}

@Test
void testPasswordWithNumberAndLetterSucceeds() {
    // 验证包含字母和数字的密码成功
}
```

### 任务3: 编写超级管理员保护验证测试 (0.5天)

#### 3.1 删除保护测试
**文件**: `aeisp-system/src/test/java/com/aeisp/system/service/impl/SuperAdminProtectionTest.java`

**测试用例**:
```java
@Test
void testDeleteLastSuperAdminThrowsException() {
    // 验证删除最后一个超级管理员抛出异常
}

@Test
void testDeleteNonLastSuperAdminSucceeds() {
    // 验证删除非最后一个超级管理员成功
}
```

#### 3.2 降级保护测试
**测试用例**:
```java
@Test
void testDemoteLastSuperAdminThrowsException() {
    // 验证降级最后一个超级管理员抛出异常
}

@Test
void testDemoteNonLastSuperAdminSucceeds() {
    // 验证降级非最后一个超级管理员成功
}
```

### 任务4: 执行验证测试 (0.5天)

#### 4.1 运行测试
```bash
# 运行系统模块测试
mvn test -pl aeisp-system

# 运行用户模块测试
mvn test -pl aeisp-user
```

#### 4.2 记录测试结果
- 记录每个测试用例的通过/失败情况
- 记录不符合预期的结果
- 分析问题原因

### 任务5: 修复问题 (1-2天)

#### 5.1 修复状态管理问题
**问题**: 管理员创建页面显示状态选择但无效

**解决方案**:
1. 移除管理员创建页面的状态选择控件
2. 或者禁用状态选择控件并显示提示

**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改**:
```vue
<!-- 移除状态选择 -->
<el-form-item label="状态" prop="status">
  <el-radio-group v-model="form.status">
    <el-radio :label="1">正常</el-radio>
    <el-radio :label="2">禁用</el-radio>  <!-- 移除这一行 -->
  </el-radio-group>
</el-form-item>
```

#### 5.2 修复超级管理员保护前端错误处理
**问题**: 前端错误处理简单，没有专门的超级管理员保护提示

**解决方案**:
1. 在前端添加超级管理员保护错误处理
2. 显示友好的错误提示

**文件**: `aeisp-admin/src/views/system/user/index.vue`

**修改**:
```javascript
async function submitForm() {
  try {
    await formRef.value.validate()
    if (form.id) {
      // 更新管理员
      const data = { ... }
      await updateSysUser(form.id, data)
      ElMessage.success('修改成功')
    } else {
      // 新增管理员
      const data = { ... }
      await addSysUser(data)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  } catch (error) {
    console.error('用户操作失败:', error)
    // 增强错误处理
    const errorMessage = error.message || error.response?.data?.message || '操作失败，请稍后重试'
    
    // 特殊处理超级管理员保护错误
    if (errorMessage.includes('超级管理员')) {
      ElMessage.error(errorMessage)
    } else {
      ElMessage.error(errorMessage)
    }
  }
}
```

### 任务6: 回归测试 (0.5天)

#### 6.1 运行所有测试
```bash
# 运行所有模块测试
mvn test

# 运行特定模块测试
mvn test -pl aeisp-system
mvn test -pl aeisp-user
```

#### 6.2 验证修复效果
- 确认状态管理逻辑正确
- 确认密码策略一致
- 确认超级管理员保护有效
- 确认前端UI一致

---

## 二、时间安排

| 阶段 | 任务 | 时间 | 负责人 |
|------|------|------|--------|
| 阶段1 | 编写验证测试 | 1天 | 开发人员 |
| 阶段2 | 执行验证测试 | 0.5天 | 开发人员 |
| 阶段3 | 修复问题 | 1-2天 | 开发人员 |
| 阶段4 | 回归测试 | 0.5天 | 开发人员 |
| **总计** | | **3-4天** | |

---

## 三、交付物

### 代码交付
1. `SysUserStatusTest.java` - 管理员状态管理测试
2. `UsrUserStatusTest.java` - 前端用户状态管理测试
3. `PasswordStrategyTest.java` - 密码策略测试
4. `SuperAdminProtectionTest.java` - 超级管理员保护测试
5. `index.vue` - 更新后的管理员管理页面

### 文档交付
1. `VERIFICATION_PLAN.md` - 验证测试计划
2. `IMPLEMENTATION_PLAN.md` - 实施计划
3. `TEST_RESULTS.md` - 测试结果报告

---

## 四、验收标准

### 功能验收
- [ ] 管理员创建时忽略 `status` 字段
- [ ] 管理员更新时接受 `status` 字段
- [ ] 前端用户创建时接受 `status` 字段
- [ ] 两个模块都支持默认密码 "123456"
- [ ] 两个模块的密码复杂度验证逻辑一致
- [ ] 不能删除最后一个超级管理员
- [ ] 不能降级最后一个超级管理员
- [ ] 前端显示友好的错误提示

### 质量验收
- [ ] 所有测试用例通过
- [ ] 代码符合项目规范
- [ ] 文档完整清晰

---

## 五、风险与应对

### 风险1: 测试用例覆盖不全
**应对**: 
- 使用代码覆盖率工具检查
- 补充边界情况测试

### 风险2: 修复引入新问题
**应对**: 
- 执行回归测试
- 使用代码审查

### 风险3: 时间延期
**应对**: 
- 并行执行测试和修复
- 优先处理高风险问题

---

## 六、后续优化

1. **统一状态管理**: 考虑统一管理员和前端用户的状态管理逻辑
2. **统一密码策略**: 考虑统一两个模块的密码策略
3. **增强错误处理**: 考虑增强前端错误处理，提供更友好的提示
4. **完善文档**: 更新相关文档，说明状态管理和密码策略
