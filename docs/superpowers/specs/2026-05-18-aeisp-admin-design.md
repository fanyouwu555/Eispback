# AEISP 后台管理前端设计方案

## 1. 项目背景

AEISP Server 是基于 Spring Boot 3.x 的模块化 Java 后端服务，已提供完整的 REST API 和 Swagger 文档。当前无前端代码，需要建设一套后台管理页面，优先对接 `aeisp-system`（系统基础）和 `aeisp-user`（用户管理）模块。

## 2. 设计目标

- 采用 Vue 3 + Element Plus 轻量自建框架，代码完全可控
- 逐个功能模块实现，便于实时调整优先级和细节
- 精确对接后端 `/api/v1/` 接口规范和 JWT 认证流程
- 支持 RBAC 权限控制、多标签页、暗黑模式

## 3. 技术栈

| 层 | 技术 | 版本 | 说明 |
|---|---|---|---|
| 构建工具 | Vite | 5.x | 快速热更新，开发体验最优 |
| 框架 | Vue | 3.4+ | 组合式 API (`<script setup>`) |
| UI 组件库 | Element Plus | 2.7+ | 中后台场景组件最丰富 |
| 路由 | Vue Router | 4.x | 动态路由 + 路由守卫 |
| 状态管理 | Pinia | 2.x | 替代 Vuex，TypeScript 友好 |
| HTTP 客户端 | Axios | 1.x | 拦截器处理 JWT 和统一错误 |
| 工具库 | VueUse | 10.x | useDark、useStorage 等组合式函数 |
| 图表 | ECharts | 5.x | 用户统计页面趋势图 |

## 4. 项目结构

```
aeisp-admin/
├── public/
├── src/
│   ├── api/              # 按模块封装的 HTTP 接口
│   │   ├── system.js     # 用户管理、角色管理、权限、日志、配置
│   │   └── user.js       # 用户列表、登录日志、时长日志、统计
│   ├── assets/           # 图片、样式变量
│   ├── components/       # 全局通用组件
│   │   ├── SvgIcon.vue
│   │   ├── Pagination.vue
│   │   └── RightToolbar.vue
│   ├── views/            # 页面视图
│   │   ├── login/        # 登录页
│   │   ├── system/       # System 模块页面
│   │   │   ├── user/
│   │   │   ├── role/
│   │   │   ├── permission/
│   │   │   ├── log/
│   │   │   └── config/
│   │   └── user/         # User 模块页面
│   │       ├── list/
│   │       ├── login-log/
│   │       ├── duration-log/
│   │       └── statistics/
│   ├── router/           # 路由配置
│   │   ├── index.js      # 路由入口 + 守卫
│   │   └── routes.js     # 业务路由表
│   ├── stores/           # Pinia 状态
│   │   ├── app.js        # 侧边栏折叠、暗黑模式
│   │   ├── user.js       # 登录态、用户信息、权限码
│   │   └── tagsView.js   # 多标签页状态
│   ├── utils/
│   │   ├── request.js    # Axios 封装
│   │   ├── auth.js       # Token 存取 (localStorage)
│   │   └── permission.js # 权限判断工具
│   ├── directives/       # 自定义指令
│   │   └── permission.js # v-permission
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
├── package.json
├── .env
└── .env.production
```

## 5. 核心框架设计

### 5.1 登录页

- 布局：居中卡片，深色背景 + 品牌 Logo
- 字段：用户名、密码、验证码（图形验证码预留位）
- 流程：
  1. 前端校验非空
  2. 调用 `/api/v1/auth/login`
  3. 后端返回 `accessToken`（2小时）+ `refreshToken`（7天）
  4. 存入 `localStorage`，跳转首页或原目标页
- 记住密码：可选，存入 `localStorage`（明文仅用户名）

### 5.2 主布局（Layout）

```
┌─────────────────────────────────────┐
│  Logo    折叠按钮    面包屑    用户头像  │  <-- Header
├──────────┬──────────────────────────┤
│          │  [Tab1] [Tab2] [Tab3] x  │  <-- TagsView
│  侧边栏   ├──────────────────────────┤
│  Menu    │                          │
│          │      页面内容区域         │  <-- Main (router-view + keep-alive)
│          │                          │
└──────────┴──────────────────────────┘
```

- **侧边栏**：`el-menu`，与路由配置联动；支持一级/二级菜单；可折叠（`Collapse`）
- **Header**：面包屑（自动根据路由生成）、全屏按钮、暗黑模式切换、用户下拉（个人中心/退出登录）
- **TagsView**：点击菜单自动打开标签；右键菜单（刷新、关闭、关闭其他、关闭全部）；标签状态绑定路由，刷新页面后恢复；`keep-alive` 缓存页面状态
- **内容区**：`router-view` 包裹 `keep-alive`

### 5.3 路由与权限

- **常量路由**：登录页 (`/login`)、404 (`/404`)，无需权限
- **动态路由**：业务页面，根据后端 `/api/v1/auth/info` 返回的 `permissions` 和 `menus` 动态生成
- **路由守卫逻辑**：
  1. 有 Token 且访问登录页 → 跳转首页
  2. 无 Token 且访问受保护页 → 跳转登录页，携带 `redirect` 参数
  3. 有 Token 但无用户信息 → 调用 `auth/info` 获取权限并生成动态路由
  4. `accessToken` 过期 → 用 `refreshToken` 静默刷新；刷新失败跳转登录

### 5.4 Axios 封装

- 基地址：`/api/v1`（开发代理到 `http://localhost:8080`）
- 请求拦截器：自动注入 `Authorization: Bearer <accessToken>`
- 响应拦截器：
  - `code === 200`：返回 `response.data.data`
  - `code === 401`：清除 Token，跳转登录
  - `code === 403`：`ElMessage.error('权限不足')`
  - 其他：`ElMessage.error(message || '系统错误')`

### 5.5 暗黑模式

- `vueuse/useDark` 监听系统偏好或手动切换
- Element Plus 通过 `html.dark` class 自动响应
- 主题色变量定义在 `assets/styles/variables.scss`

### 5.6 按钮级权限

- 自定义指令 `v-permission="['system:user:create']"`
- 无权限时组件不渲染（`remove()`）
- 权限码来源：后端 `auth/info` 接口返回的权限列表

## 6. System 模块页面

### 6.1 用户管理 (`/system/user`)

- **筛选区**：用户名、手机号、状态（`el-select`）、创建时间范围（`el-date-picker`）
- **表格字段**：用户名、昵称、手机号、邮箱、状态（`el-switch` 快速切换）、创建时间、操作
- **操作按钮**：新增、编辑、删除（二次确认）、重置密码（弹窗）、分配角色（弹窗多选）
- **批量操作**：批量删除、批量禁用
- **新增/编辑弹窗**：表单含用户名、密码（新增时必填）、昵称、手机号、邮箱、状态、角色选择（`el-select` 多选）

### 6.2 角色管理 (`/system/role`)

- **表格字段**：角色名称、角色编码、描述、创建时间、操作
- **操作**：新增、编辑、删除、分配权限（核心）
- **分配权限弹窗**：`el-tree` 展示权限点树，父子联动勾选，保存提交选中 ID 列表

### 6.3 权限管理 (`/system/permission`)

- **表格字段**：权限名称、权限编码、类型（菜单/按钮/接口）、排序、操作
- **操作**：新增、编辑、删除
- **树形展示**：支持层级父子结构

### 6.4 操作日志 (`/system/log`)

- **筛选区**：操作人、操作模块、操作类型、时间范围
- **表格字段**：操作人、IP 地址、操作模块、操作类型、结果（成功/失败）、耗时、操作时间
- **详情**：抽屉（`el-drawer`）展示完整请求参数和响应结果
- **只读页面**，无增删改

### 6.5 系统配置 (`/system/config`)

- **表格字段**：配置名称、配置 Key、配置值、备注、操作
- **操作**：编辑（弹窗修改值）
- 修改后后端实时生效

## 7. User 模块页面

### 7.1 用户列表 (`/user/list`)

- **筛选区**：用户名、手机号、邮箱、状态（正常/禁用/冻结）、注册时间范围
- **表格字段**：用户名、手机号、邮箱、状态标签（`el-tag`）、剩余时长、充值总额、最后登录时间、注册时间、操作
- **操作按钮**：编辑、重置密码、调整时长（弹窗：增加/减少数值 + 原因）、启用/禁用/冻结
- **批量操作**：批量禁用、批量冻结
- **数据导出**：导出当前筛选结果 Excel

### 7.2 登录日志 (`/user/login-log`)

- **筛选区**：用户名、登录结果（成功/失败）、IP、时间范围
- **表格字段**：用户名、登录 IP、登录设备、结果、登录时间
- **只读页面**

### 7.3 时长日志 (`/user/duration-log`)

- **筛选区**：用户名、消耗类型（模型调用/仿真运行/编译调试）、时间范围
- **表格字段**：用户名、消耗时间、消耗类型、消耗时长、关联项目、备注
- **顶部汇总卡**：总消耗时长、今日消耗

### 7.4 数据统计 (`/user/statistics`)

- **顶部指标卡**：总用户数、今日新增、今日活跃、总时长消耗
- **趋势图表（ECharts）**：
  - 新增用户趋势：日/周/月折线图
  - 活跃用户趋势：DAU/WAU/MAU 折线图
- **排行表格**：热门用户排行（按消费时长/充值金额）

## 8. 开发规范

- **页面组件**：统一放在 `views/模块/页面名/index.vue`
- **列表页统一模式**：`<script setup>` + `ref` 定义查询参数/表格数据/弹窗显隐 + `getList()` 方法
- **API 封装**：按后端模块拆分 `api/system.js` 和 `api/user.js`，函数命名 `listXxx`、`getXxx`、`addXxx`、`updateXxx`、`deleteXxx`
- **表单校验**：使用 `el-form` 的 `rules`，提交前统一 `await formRef.value.validate()`
- **加载状态**：表格 `v-loading="loading"`，提交按钮 `:loading="submitLoading"`
- **敏感操作**：删除、重置密码、调整时长必须 `ElMessageBox.confirm`

## 9. 与后端集成

- 开发环境：Vite 配置 `server.proxy`，将 `/api` 代理到 `http://localhost:8080`
- 生产环境：打包为静态文件，可由 Spring Boot `src/main/resources/static` 托管，或独立 Nginx 部署
- 接口版本：统一使用 `/api/v1/` 前缀

## 10. 迭代计划

按以下顺序逐个实现，每个功能可独立交付和验证：

1. **项目脚手架搭建**：Vite + Vue 3 + Element Plus + Vue Router + Pinia
2. **Axios 封装 + 登录页**：完成 JWT 认证流程
3. **主框架**：Layout + 侧边栏 + Header + 路由守卫
4. **多标签页 + 暗黑模式**
5. **System - 用户管理**
6. **System - 角色管理 + 权限管理**
7. **System - 操作日志 + 系统配置**
8. **User - 用户列表**
9. **User - 登录日志 + 时长日志**
10. **User - 数据统计（ECharts）**

---

*设计日期：2026-05-18*
