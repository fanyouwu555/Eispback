# AEISP 后端管理系统 — 开发任务清单

> **用途：** 本清单是项目后续所有开发工作的唯一依据。每完成一个任务后，在对应复选框中打勾并记录完成日期。未完成所有 P1 任务前，不得启动下一迭代。
> **维护规则：** 新增需求必须转化为任务卡片并明确优先级；禁止口头约定、禁止绕过清单直接开发。
> **最后更新：** 2026-05-16

---

## 一、项目状态总览

| 模块 | 当前状态 | 阻塞问题 | 待办任务 |
|------|---------|---------|---------|
| aeisp-common | 通过 | 无 | 2 项 P2 |
| aeisp-system | 有条件通过 | 无 | 2 项 P2 |
| aeisp-user | 有条件通过 | 无 | 1 项 P1 + 1 项 P2 |
| aeisp-message | 有条件通过 | 无 | 2 项 P1 + 1 项 P2 |
| aeisp-template | 有条件通过 | 无 | 2 项 P2（建议） |
| aeisp-boot | 通过 | 无 | 3 项建议 |
| aeisp-model | 通过 | 无 | 预留接口，暂不实现 |
| aeisp-recharge | 通过 | 无 | 预留接口，暂不实现 |

**当前阶段：** 迭代 5 — 问题修复与优化（进行中）

---

## 二、迭代 5：问题修复与优化

> **目标：** 修复审查阶段遗留的 P1 问题，补充基础设施（数据库脚本、测试）。
> **准入标准：** 所有 P1 任务完成，编译通过。
> **准出标准：** 所有 P1 任务验收通过，CI（如有）编译通过。

### 任务 5.1：MsgNotificationService 全员推送性能优化（P1）

- [ ] **5.1.1 分批查询用户列表**
  - **描述：** `resolveTargetUsers` 中 `PushScopeEnum.ALL` 场景下一次性加载所有用户 ID，存在内存溢出风险。
  - **相关文件：** `aeisp-message/src/.../MsgNotificationServiceImpl.java`
  - **实现方案：** 使用 MyBatis-Plus 分页查询或 `LIMIT/OFFSET` 分批（每批 1000 条）获取用户 ID，逐批插入 `msg_user_notification`。
  - **验收标准：**
    - [ ] 能正确处理 10 万级以上用户推送，不出现 OOM。
    - [ ] 保持事务一致性：分批插入失败时回滚全部。
    - [ ] 单元测试覆盖分批逻辑。

### 任务 5.2：标记已读时自动更新 readCount（P1）

- [ ] **5.2.1 在 markAsRead 中增加 read_count 原子更新**
  - **描述：** 用户调用 `markAsRead` 后，`msg_notification.read_count` 没有增加，导致后台统计不准确。
  - **相关文件：** `aeisp-message/src/.../MsgUserNotificationServiceImpl.java`
  - **实现方案：** 在 `markAsRead` 事务中，调用 `msgNotificationMapper.updateReadCount(notificationId, +1)` 进行原子递增。
  - **验收标准：**
    - [ ] 单条标记已读后，对应消息的 `read_count` 准确 +1。
    - [ ] 批量标记全部已读时，`read_count` 更新为 `total_count`（或累加未读数）。
    - [ ] 并发标记已读时，read_count 不出现负数或重复计数。

### 任务 5.3：batchCreate 接口参数改为 DTO（P1）

- [ ] **5.3.1 创建 UserBatchCreateRequest DTO**
  - **描述：** `POST /api/v1/users/batch` 当前接收 `List<UsrUser>` Entity，存在安全风险。
  - **相关文件：**
    - `aeisp-user/src/.../dto/UserBatchCreateRequest.java`（新建）
    - `aeisp-user/src/.../controller/UserController.java`
    - `aeisp-user/src/.../service/UsrUserService.java`
    - `aeisp-user/src/.../service/impl/UsrUserServiceImpl.java`
  - **实现方案：**
    1. 新建 `UserBatchCreateRequest`，字段包含：username、password、phone、email、initialDuration、status。
    2. Controller 接收 `List<UserBatchCreateRequest>`。
    3. Service 中将 DTO 转换为 `UsrUser` Entity 后批量插入。
  - **验收标准：**
    - [ ] 接口不再直接暴露 Entity。
    - [ ] 校验逻辑保持与单条创建一致（用户名/手机号/邮箱唯一性校验）。
    - [ ] Swagger 文档中参数结构正确。

### 任务 5.4：数据库初始化 SQL 脚本（P1）

- [ ] **5.4.1 编写 DDL 脚本**
  - **描述：** 当前所有模块只有 Entity，没有建表 SQL，新项目部署时无法自动建表。
  - **相关文件：** `docs/sql/init-schema.sql`（新建）
  - **实现方案：**
    1. 按模块顺序编写所有表的 `CREATE TABLE` 语句。
    2. 包含主键、索引、外键（可选）、COMMENT 注释。
    3. 编写 `docs/sql/init-data.sql` 插入默认数据（如超级管理员、默认角色、初始权限）。
  - **验收标准：**
    - [ ] 在全新 PostgreSQL 数据库中执行脚本后，所有表创建成功。
    - [ ] MyBatis-Plus 逻辑删除字段 `deleted` 默认值为 0。
    - [ ] 默认数据包含：一个超级管理员账号（admin/123456，密码需 BCrypt 加密后插入）。

### 任务 5.5：基础单元测试覆盖（P2）

- [ ] **5.5.1 为各模块 Service 编写单元测试**
  - **描述：** 当前项目无任何测试代码。
  - **相关文件：** 各模块 `src/test/java/...`
  - **实现方案：**
    1. 使用 JUnit 5 + Mockito。
    2. 每个 Service 至少覆盖核心业务逻辑（如注册、推送、创建用户）。
    3. 不得连接真实数据库，Mapper 全部 Mock。
  - **验收标准：**
    - [ ] aeisp-system：SysUserService、SysRoleService 核心方法有测试。
    - [ ] aeisp-user：UsrUserService 注册、创建、更新有测试。
    - [ ] aeisp-message：MsgNotificationService 创建、推送、撤回有测试。
    - [ ] 运行 `mvn test` 全部通过。

---

## 三、迭代 6：功能增强

> **目标：** 实现审查阶段标记为 P2 的功能增强，以及需求文档中明确但尚未实现的能力。
> **准入标准：** 迭代 5 全部完成。
> **准出标准：** 所有功能自测通过，Swagger 文档正常展示。

### 任务 6.1：GlobalExceptionHandler 增强（P2）

- [ ] **6.1.1 添加 ConstraintViolationException 处理**
  - **描述：** 方法参数使用 `@Validated` 分组校验时抛出的异常未被捕获。
  - **相关文件：** `aeisp-common/src/.../GlobalExceptionHandler.java`
  - **验收标准：**
    - [ ] `@Validated` 校验失败时返回 400 业务码，而非 500。
    - [ ] 错误信息中包含具体字段和校验失败原因。

### 任务 6.2：操作日志 AOP 自动记录（P2）

- [ ] **6.2.1 实现 @LogOperation 注解 + 切面**
  - **描述：** 当前操作日志需要手动调用 `saveLog()`，容易遗漏。
  - **相关文件：**
    - `aeisp-common/src/.../annotation/LogOperation.java`（新建）
    - `aeisp-system/src/.../aspect/OperationLogAspect.java`（新建）
  - **实现方案：**
    1. 定义 `@LogOperation(value = "删除用户")` 注解，可标注在 Controller 方法上。
    2. 定义 `@Aspect` 切面，拦截所有标注了该注解的方法。
    3. 切面中自动获取当前登录用户、请求参数、执行结果、耗时，调用 `SysOperationLogService.saveLog()`。
  - **验收标准：**
    - [ ] 在 `SysUserController.deleteUser` 上添加注解后，删除操作自动记录到 `sys_operation_log`。
    - [ ] 支持记录操作结果（成功/失败）和异常信息。

### 任务 6.3：消息定时推送机制（P2）

- [ ] **6.3.1 实现 Spring @Scheduled 定时轮询推送**
  - **描述：** `PushTypeEnum.SCHEDULED` 类型消息当前只保存了 `pushTime`，没有实际定时执行。
  - **相关文件：**
    - `aeisp-message/src/.../scheduler/NotificationPushScheduler.java`（新建）
    - `aeisp-message/src/.../MsgNotificationService.java`
  - **实现方案：**
    1. 添加 `@Scheduled(fixedRate = 60000)` 每分钟轮询一次。
    2. 查询 `status=0`（草稿）且 `pushType=1`（定时）且 `pushTime <= now` 的消息。
    3. 调用 `msgNotificationService.pushNotification(id)` 执行推送。
  - **验收标准：**
    - [ ] 创建定时消息并设定未来时间，到达时间后自动推送。
    - [ ] 已推送的定时消息不会重复推送。
    - [ ] 轮询任务支持集群环境（需分布式锁，可选，至少单机正确）。

### 任务 6.4：注册验证码校验（P2）

- [ ] **6.4.1 在注册接口中校验验证码**
  - **描述：** `CacheConstants` 中已有验证码 Redis Key，但注册接口未实际校验。
  - **相关文件：** `aeisp-user/src/.../controller/AuthController.java`
  - **验收标准：**
    - [ ] 注册请求携带 `captchaKey` 和 `captchaCode`。
    - [ ] 从 Redis 取出验证码进行比对，错误时返回明确的错误信息。
    - [ ] 验证码校验成功后从 Redis 删除，防止重复使用。

### 任务 6.5：Token 黑名单与刷新安全（P2）

- [ ] **6.5.1 登出时将 Token 加入 Redis 黑名单**
  - **描述：** 当前登出后 Refresh Token 仍然有效，可继续换取 Access Token。
  - **相关文件：**
    - `aeisp-boot/src/.../AuthController.java`
    - `aeisp-boot/src/.../JwtAuthenticationFilter.java`
  - **实现方案：**
    1. 登出时解析 Token 的 `jti` 和 `exp`，存入 Redis Set 或 String（TTL = 剩余过期时间）。
    2. `refresh` 接口先检查 Token 是否在黑名单中。
    3. `JwtAuthenticationFilter` 校验 Access Token 时也检查黑名单。
  - **验收标准：**
    - [ ] 登出后同一 Refresh Token 无法再次刷新。
    - [ ] 黑名单中的 Access Token 被拒绝访问。
    - [ ] Redis 中黑名单记录随 Token 过期自动清理。

### 任务 6.6：接口限流（P2）

- [ ] **6.6.1 基于 Redis 实现接口限流**
  - **描述：** 需求文档要求基于 Redis + 令牌桶实现限流，防止暴力破解和恶意刷量。
  - **相关文件：**
    - `aeisp-common/src/.../ratelimit/RateLimiter.java`（新建）
    - `aeisp-common/src/.../annotation/RateLimit.java`（新建）
    - `aeisp-common/src/.../aspect/RateLimitAspect.java`（新建）
  - **实现方案：**
    1. 使用 Redis + Lua 脚本实现令牌桶算法，或简化为固定窗口计数器。
    2. 提供 `@RateLimit(key = "login", limit = 5, period = 60)` 注解。
    3. 在登录、注册等高敏感接口上标注。
  - **验收标准：**
    - [ ] 60 秒内同一 IP 登录失败超过 5 次，返回 429 Too Many Requests。
    - [ ] 限流计数正确存储在 Redis 中，有过期时间。

### 任务 6.7：引入 MapStruct 减少样板代码（P2）

- [ ] **6.7.1 使用 MapStruct 替换手动字段复制**
  - **描述：** pom.xml 中已引入 MapStruct，但代码中仍大量手动复制字段。
  - **相关文件：** 各模块 Controller、Service
  - **实现方案：**
    1. 为常用转换创建 Mapper 接口（如 `SysUserMapperStruct`、`MsgNotificationMapperStruct`）。
    2. 逐步替换 Controller 中的手动 `setXxx` 代码。
  - **验收标准：**
    - [ ] 至少替换 3 个 Controller 中的手动转换逻辑。
    - [ ] 编译后 `target/generated-sources` 中生成 MapStruct 实现类。

---

## 四、迭代 7：预留模块实现

> **目标：** 将 aeisp-model 和 aeisp-recharge 从 Stub 实现替换为真实业务逻辑。
> **准入标准：** 迭代 6 全部完成，需求文档中对应模块需求已确认。
> **准出标准：** 接口可正常调用，核心业务逻辑有单元测试。

### 任务 7.1：aeisp-model 大模型对接模块

- [ ] **7.1.1 实现模型配置管理**
  - [ ] 设计 `mdl_model_config` 表结构（模型名称、API 地址、密钥、状态）。
  - [ ] 实现模型 CRUD 接口。
- [ ] **7.1.2 实现模型调用代理**
  - [ ] 封装 HTTP 客户端调用第三方大模型 API。
  - [ ] 实现请求/响应日志记录。
  - [ ] 实现超时和重试机制。
- [ ] **7.1.3 实现用量统计**
  - [ ] 每次调用后记录 token 消耗、响应时间、调用结果。
  - [ ] 提供按用户/按模型/按日期的用量统计接口。

### 任务 7.2：aeisp-recharge 时长与充值模块

- [ ] **7.2.1 实现套餐管理**
  - [ ] 设计 `rcg_package` 表结构（套餐名称、时长、价格、状态）。
  - [ ] 实现套餐 CRUD 接口。
- [ ] **7.2.2 实现订单管理**
  - [ ] 设计 `rcg_order` 表结构（订单号、用户、套餐、金额、状态、支付时间）。
  - [ ] 实现订单创建、查询、状态流转（待支付 -> 已支付 -> 已完成）。
- [ ] **7.2.3 实现余额与时长管理**
  - [ ] 支付成功后自动增加用户时长。
  - [ ] 提供时长消耗记录查询。
  - [ ] 实现时长不足预警（预留接口，可与消息模块联动）。

---

## 五、迭代 8：工程化与质量保障

> **目标：** 提升项目工程化水平，确保长期可维护。
> **准入标准：** 核心业务功能全部实现。
> **准出标准：** 代码规范检查通过，测试覆盖率达标。

### 任务 8.1：数据库连接池优化（P2）

- [ ] **8.1.1 补充 HikariCP 配置**
  - **描述：** `application.yml` 中缺少连接池参数，生产环境可能性能不足。
  - **相关文件：** `aeisp-boot/src/main/resources/application.yml`
  - **验收标准：**
    - [ ] 配置 `maximum-pool-size`、`minimum-idle`、`connection-timeout`、`idle-timeout`、`max-lifetime`。
    - [ ] 参数值根据部署环境提供 `application-prod.yml` 覆盖。

### 任务 8.2：代码规范与静态检查（P2）

- [ ] **8.2.1 配置 Checkstyle / SpotBugs**
  - **描述：** 统一代码风格，自动发现潜在 Bug。
  - **相关文件：** `checkstyle.xml`、`pom.xml`
  - **验收标准：**
    - [ ] `mvn checkstyle:check` 通过。
    - [ ] `mvn spotbugs:check` 无高危问题。

### 任务 8.3：测试覆盖率提升（P2）

- [ ] **8.3.1 集成测试覆盖核心流程**
  - **描述：** 单元测试仅覆盖单一层级，需要集成测试验证完整流程。
  - **相关文件：** 各模块 `src/test/java/...`
  - **验收标准：**
    - [ ] 覆盖注册 -> 登录 -> 访问受保护接口的完整流程。
    - [ ] 覆盖创建消息 -> 推送 -> 标记已读的完整流程。
    - [ ] 使用 `@SpringBootTest` + Testcontainers（PostgreSQL/Redis）。

### 任务 8.4：Swagger 文档完善（P2）

- [ ] **8.4.1 补充接口描述和示例值**
  - **描述：** 当前部分接口缺少 `@Operation` 说明，参数缺少 `@Schema` 描述。
  - **相关文件：** 所有 Controller
  - **验收标准：**
    - [ ] 所有公开接口有 `@Operation(summary = "...")`。
    - [ ] 所有请求参数 DTO 的字段有 `@Schema(description = "...")`。
    - [ ] Swagger UI 中所有枚举字段显示为下拉选项而非纯数字。

---

## 六、任务状态看板

### 迭代 5（进行中）

| 任务 | 优先级 | 状态 | 负责人 | 完成日期 | 备注 |
|------|--------|------|--------|---------|------|
| 5.1 全员推送性能优化 | P1 | 待开始 | - | - | - |
| 5.2 readCount 自动更新 | P1 | 待开始 | - | - | - |
| 5.3 batchCreate DTO | P1 | 待开始 | - | - | - |
| 5.4 数据库初始化 SQL | P1 | 待开始 | - | - | - |
| 5.5 基础单元测试 | P2 | 待开始 | - | - | - |

### 迭代 6（待启动）

| 任务 | 优先级 | 状态 | 负责人 | 完成日期 | 备注 |
|------|--------|------|--------|---------|------|
| 6.1 ConstraintViolationException 处理 | P2 | 待启动 | - | - | - |
| 6.2 操作日志 AOP | P2 | 待启动 | - | - | - |
| 6.3 定时推送机制 | P2 | 待启动 | - | - | - |
| 6.4 注册验证码 | P2 | 待启动 | - | - | - |
| 6.5 Token 黑名单 | P2 | 待启动 | - | - | - |
| 6.6 接口限流 | P2 | 待启动 | - | - | - |
| 6.7 MapStruct 引入 | P2 | 待启动 | - | - | - |

### 迭代 7（待启动）

| 任务 | 优先级 | 状态 | 负责人 | 完成日期 | 备注 |
|------|--------|------|--------|---------|------|
| 7.1 aeisp-model 实现 | P1 | 待启动 | - | - | 依赖需求确认 |
| 7.2 aeisp-recharge 实现 | P1 | 待启动 | - | - | 依赖需求确认 |

### 迭代 8（待启动）

| 任务 | 优先级 | 状态 | 负责人 | 完成日期 | 备注 |
|------|--------|------|--------|---------|------|
| 8.1 HikariCP 配置 | P2 | 待启动 | - | - | - |
| 8.2 静态检查工具 | P2 | 待启动 | - | - | - |
| 8.3 测试覆盖率 | P2 | 待启动 | - | - | - |
| 8.4 Swagger 完善 | P2 | 待启动 | - | - | - |

---

## 七、变更记录

| 日期 | 版本 | 变更内容 | 变更人 |
|------|------|---------|--------|
| 2026-05-16 | v1.0 | 初始版本，基于开发审查文档整理 | Claude |

---

## 八、附录：快速参考

### 优先级定义

- **P0（阻塞）：** 存在严重 Bug、安全漏洞或功能缺失，必须立即修复，否则影响系统正常运行。
- **P1（重要）：** 影响功能完整性或用户体验，应在当前迭代内完成。
- **P2（建议）：** 代码质量、性能优化或工程化改进，可排入后续迭代。

### 提测流程

1. 开发者在本地完成自测（单元测试 + Swagger 接口调用）。
2. 更新本清单中对应任务的复选框和完成日期。
3. 提交代码变更（Git commit + push）。
4. 通知审查人进行代码审查和功能验收。
5. 审查通过后，审查人在清单中确认验收标准全部达标。

### 紧急需求插入规则

- **P0 紧急 Bug：** 可立即插入当前迭代，暂停其他 P1 任务。
- **新增 P1 需求：** 需在当前迭代剩余容量允许的情况下插入，否则排入下一迭代。
- **新增 P2 需求：** 一律排入下一迭代，不得打断当前迭代节奏。
