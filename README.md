# AEISP Server

AEISP 后端管理系统 —— 基于 Spring Boot 3.x 的模块化 Java 后端服务。

## 项目结构

```
aeisp-server/
├── aeisp-common/          # 公共模块：工具类、异常、响应模型、JWT、Redis
├── aeisp-system/          # 系统基础模块：RBAC 权限、操作日志、系统配置
├── aeisp-user/            # 用户模块：注册、登录、管理、日志、统计
├── aeisp-message/         # 通知消息模块：消息推送、自动消息、状态管控
├── aeisp-template/        # 模板管理模块：上传、版本、上下线、统计
├── aeisp-model/           # 【预留接口】大模型对接
├── aeisp-recharge/        # 【预留接口】时长与充值
└── aeisp-boot/            # 启动模块：Spring Boot 入口 + Security + JWT + OpenAPI
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| Java | 17 | 运行环境 |
| PostgreSQL | 14+ | 关系型数据库 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Redis | 6+ | 缓存与会话 |
| Spring Security | 6.x | 安全认证与授权 |
| JWT | 0.12.3 | 无状态 Token |
| SpringDoc OpenAPI | 2.3.0 | API 文档（Swagger UI） |
| Maven | 3.8+ | 构建工具 |
| Hutool | 5.8.23 | 工具库 |
| Lombok | 1.18.30 | 代码简化 |

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+

### 2. 数据库初始化

创建数据库：

```sql
CREATE DATABASE aeisp WITH ENCODING = 'UTF8';
```

执行初始化 SQL：

```bash
psql -U postgres -d aeisp -f docs/sql/init-postgresql-complete.sql
```

### 3. 修改配置

编辑 `aeisp-boot/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aeisp
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 4. 启动基础设施

```bash
# PostgreSQL 5432 + Redis 6379
docker compose up -d
```

### 5. 编译运行后端

```bash
# 安装全部模块到本地仓库（必须先 install，否则跨模块依赖会失败）
mvn clean install -DskipTests

# 运行后端服务
mvn spring-boot:run -pl aeisp-boot
```

### 6. 启动前端

```bash
cd aeisp-admin
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173/`，并通过 Vite 代理把 `/api` 转发到后端 `http://localhost:8080/`。

### 7. 访问接口文档

启动后访问：

```
http://localhost:8080/swagger-ui.html
```

## 模块说明

### aeisp-common

公共模块，被所有其他模块依赖。提供：

- `Result<T>`：统一 REST 响应包装
- `BaseEntity`：数据库实体基类（含审计字段、逻辑删除）
- `PageResult<T>`：分页结果封装
- `JwtUtil`：JWT Token 生成与解析
- `RedisUtil`：Redis 操作工具
- 异常体系：`BizException`、`SysException`、`GlobalExceptionHandler`
- 常量类：`CommonConstants`、`CacheConstants`

### aeisp-system

系统基础模块。提供：

- **RBAC 权限管理**：用户-角色-权限三级模型
- **操作日志**：自动记录关键操作
- **系统配置**：动态配置（如官网 URL）

核心表：
- `sys_user`：后台管理员
- `sys_role`：角色
- `sys_permission`：权限点
- `sys_role_permission`：角色权限关联
- `sys_user_role`：用户角色关联
- `sys_operation_log`：操作日志
- `sys_config`：系统配置

### aeisp-user

用户管理模块。提供：

- **自主注册**：用户名/手机号/邮箱注册，密码复杂度校验，验证码机制预留
- **后台开户**：管理员手动创建、批量导入（Excel 预留）
- **用户管理**：启用/禁用/冻结、重置密码、调整时长
- **日志记录**：登录日志、操作日志、时长消耗日志
- **数据统计**：总用户数、新增用户趋势、活跃用户 DAU/WAU/MAU

核心表：
- `usr_user`：前端用户主表
- `usr_login_log`：登录日志
- `usr_duration_log`：时长消耗日志

### aeisp-message

通知消息模块。提供：

- **消息类型**：系统公告、版本更新、模板更新、任务结果、时长预警、充值通知、违规提醒
- **推送管理**：全员推送、指定用户推送、指定角色推送
- **推送方式**：立即推送、定时推送
- **状态管控**：已读/未读、撤回、置顶、归档
- **自动消息**：时长预警、任务完成通知（预留触发器）

核心表：
- `msg_notification`：消息主表
- `msg_user_notification`：用户消息关联表

### aeisp-template

模板管理模块。提供：

- **模板上传**：ZIP 资源包上传，自动解压
- **资源管理**：在线预览、下载、编辑、上下线
- **版本管理**：多版本共存、版本回滚
- **使用统计**：模板使用次数排行

核心表：
- `tpl_template`：模板主表
- `tpl_template_version`：模板版本表

### aeisp-model / aeisp-recharge

**预留接口模块**。已定义完整的 Service 接口、DTO/VO 和 Controller，提供 Stub 实现返回模拟数据。待后续迭代补充具体业务逻辑。

## 安全设计

### 认证方式

- **JWT 无状态认证**：Access Token（2 小时）+ Refresh Token（7 天）
- **Token 刷新**：使用 Refresh Token 换取新的 Access Token
- **Token 黑名单**：登出时存入 Redis（预留）

### 授权方式

- **RBAC**：用户 → 角色 → 权限点
- **接口注解控制**：`@PreAuthorize("hasAuthority('user:create')")`
- **超级管理员**：拥有全部权限

### 防护措施

- **密码加密**：BCrypt 存储
- **接口限流**：基于 Redis 的令牌桶算法（预留）
- **验证码**：登录/注册图形验证码（预留）
- **审计日志**：关键操作记录完整上下文

## API 规范

### 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1715731200000
}
```

### 错误码

| 错误码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 权限不足 |
| 500 | 系统内部错误 |

### 版本控制

所有接口以 `/api/v1/` 开头。

## 开发规范

1. **Controller 只做三件事**：接收参数、调用 Service、返回响应
2. **Service 必须接口化**：依赖接口而非实现
3. **Entity 不外泄**：禁止直接返回 entity，使用 VO/DTO
4. **Mapper 只负责单表 CRUD**：复杂查询允许手写 XML
5. **禁止硬编码**：配置项放入 `application.yml`
6. **禁止魔法值**：使用枚举或常量类
7. **不要重复造轮子**：优先使用 Hutool、MyBatis-Plus 自带工具

## 模块依赖关系

```
aeisp-boot
├─ aeisp-common（所有模块依赖）
├─ aeisp-system
├─ aeisp-user
│   └─ 依赖 system
├─ aeisp-message
│   └─ 依赖 system, user
├─ aeisp-template
│   └─ 依赖 system
├─ aeisp-model
│   └─ 依赖 system, user
└─ aeisp-recharge
    └─ 依赖 system, user
```

## 数据库表前缀

| 前缀 | 模块 |
|------|------|
| `sys_` | 系统基础 |
| `usr_` | 用户管理 |
| `msg_` | 通知消息 |
| `tpl_` | 模板管理 |
| `mdl_` | 大模型对接（预留） |
| `rcg_` | 时长充值（预留） |

## 迭代计划

| 迭代 | 模块 | 状态 |
|------|------|------|
| 1 | 系统基础 + 用户管理 | 已完成 |
| 2 | 通知消息 | 已完成 |
| 3 | 模板管理 | 已完成 |
| 4 | 大模型对接 + 时长充值 | 接口预留 |
| 5 | 联调优化 | 待进行 |

## 常见问题

### Q: 启动报错 "Cannot resolve dependency"？

确保先执行 `mvn clean install` 安装所有模块到本地仓库，再运行 `aeisp-boot`。

### Q: Swagger UI 无法访问？

检查 `aeisp-boot` 是否正确依赖了所有业务模块，以及 `OpenApiConfig` 是否被扫描到。

### Q: Mapper 接口找不到？

检查 `application.yml` 中的 `mybatis-plus.mapper-locations` 配置，确保值为 `classpath*:/mapper/**/*.xml`。

## License

MIT
