# 系统公告模块修复与增强方案

## 概述

对 aeisp-message 模块的系统公告功能进行完整修复与功能补齐，覆盖管理端（Vue）和 Unity 客户端两种场景。

## 1. 状态值修复

### 问题
后端状态常量 `1-草稿/2-已发送/3-已撤回/4-定时发送/5-已归档`，但前端 SQL 字典配置为 `0-草稿/1-已推送/2-已撤回/3-已归档`，导致发布、撤回、归档按钮条件始终不触发。

### 方案
更新 SQL 字典数据，使前端 dict 与后端常量对齐，并增加"已过期"状态：

| 字典值 | 标签 | 排序 |
|--------|------|------|
| 1 | 草稿 | 1 |
| 2 | 已发送 | 2 |
| 3 | 已撤回 | 3 |
| 4 | 定时发送 | 4 |
| 5 | 已归档 | 5 |
| 6 | 已过期 | 6 |

前端 `index.vue` 中的按钮条件同步更新。

### 涉及文件
- `docs/sql/init-postgresql.sql` — 更新字典数据
- `aeisp-admin/src/views/message/notification/index.vue` — 更新条件判断
- 新增 `migration_2026_05_26_notification_status.sql`

## 2. 编辑公告接口

### 端点
```
PUT /api/v1/messages/{id}
```

### 规则
- 仅草稿（status=1）状态可编辑
- 复用 `CreateNotificationRequest` 请求体结构
- 更新 `updatedBy` 为当前管理员

### 涉及文件
- `MsgNotificationController.java` — 新增 `update()` 端点
- `MsgNotificationService.java` — 新增 `updateNotification()` 接口方法
- `MsgNotificationServiceImpl.java` — 实现更新逻辑
- `CreateNotificationRequest.java` — 添加 `id` 字段或新建 `UpdateNotificationRequest`（选后者，更清晰）

## 3. 列表增加发布时间 + 发布人

### 发布时间
- VO 已包含 `pushTime` 字段（`MsgNotificationVO.java`）
- 推送成功时记录 `sentAt`
- 前端列表增加"推送时间"列（显示 `pushTime` 或 `sentAt`）

### 发布人
- `MsgNotificationVO.java` 增加 `publisherName` 字段
- `convertToVO()` 中通过 `createdBy` 查询 `sys_user` 获取用户名
- 前端列表增加"发布人"列

### 涉及文件
- `MsgNotificationVO.java`
- `MsgNotificationServiceImpl.java` — convertToVO 增加发布人查询
- `index.vue` — 新增两列

## 4. 图片上传

### 存储路径
- 本地 NFS：`D:\Users\admin\Desktop\EISP\Notification\{uuid}-{filename}`
- 对外 URL：`http://192.168.50.215/EISP/Notification/{uuid}-{filename}`

### 端点
```
POST /api/v1/messages/upload-image
Content-Type: multipart/form-data
File field: file
```

### 返回
```json
{
  "code": 200,
  "data": "http://192.168.50.215/EISP/Notification/xxx-cover.png"
}
```

### 实现方式
- 新增 `NotificationImageController.java`
- 简单文件写入（不通过 ResourceServerService 抽象，因用户指定为临时方案）
- 允许的图片类型：jpg、png、gif、webp，最大 5MB
- 前端在编辑框下方添加"上传图片"按钮，点击后插入 `[图片:URL]` 占位符

### 涉及文件
- 新增 `aeisp-message/.../controller/NotificationImageController.java`
- `aeisp-message/pom.xml` — 确认文件处理依赖
- `index.vue` — 添加图片上传按钮

## 5. 增加"简介"字段

### DB
```sql
ALTER TABLE msg_notification ADD COLUMN summary VARCHAR(256) DEFAULT NULL;
```

### 影响范围
- Entity: `MsgNotification.java` — 新增 `summary` 字段
- VO: `MsgNotificationVO.java` + `MsgNotificationDetailVO.java` — 新增 `summary` 字段
- Request: `CreateNotificationRequest.java` + 新建 `UpdateNotificationRequest.java` — 新增 `summary` 字段
- Service impl: `convertToVO()` / `createNotification()` / `updateNotification()` — 处理 summary

## 6. 已过期状态 + 自动过期调度

### DB
状态 6 = 已过期（见第 1 节）

### 定时任务
新建 `ExpiredNotificationScheduler.java`，每分钟扫描：
```sql
UPDATE msg_notification
SET status = 6
WHERE status = 2 AND expire_time IS NOT NULL AND expire_time < NOW()
```

### 前端
列表状态标签增加"已过期"样式（type=danger）

### 涉及文件
- 新增 `aeisp-message/.../scheduler/ExpiredNotificationScheduler.java`
- `index.vue` — 更新状态标签颜色映射

## 7. 修复重复定时器

### 问题
两个相同的定时推送扫描任务：
- `NotificationPushScheduler.java` — 错误的 status=4 条件（Bug）
- `ScheduledNotificationTask.java` — 正确的 status=1 条件

### 方案
删除 `NotificationPushScheduler.java`，保留 `ScheduledNotificationTask.java`

## 8. Unity 客户端公告接口

### 端点
```
GET /api/v1/client/announcements
```

### 鉴权
参照现有 `ClientProjectController` 模式（Unity access key 鉴权）

### 返回格式
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "系统维护通知",
      "content": "系统将于...",
      "summary": "系统将于...",
      "imageUrls": ["http://..."],
      "pushTime": "2026-05-26 10:00:00",
      "createdAt": "2026-05-25 10:00:00"
    }
  ]
}
```

### 查询条件
- 已发送（status=2）且未过期（expire_time > NOW() 或为 NULL）
- 按 isTop DESC、pushTime DESC 排序

### 图片 URL 提取规则
Unity 端 `imageUrls` 字段通过正则解析 `content` 中的 `[图片:URL]` 占位符提取 URL 列表：
- 正则：`/\[图片:(https?:\/\/[^\]]+)\]/g`
- 如无匹配则返回空数组

### 涉及文件
- 新增 `aeisp-message/.../controller/ClientAnnouncementController.java`
- 新增 `aeisp-message/.../vo/ClientAnnouncementVO.java`
- 新增 `aeisp-message/.../service/ClientAnnouncementService.java`
- 新增 `aeisp-message/.../service/impl/ClientAnnouncementServiceImpl.java`
- `SecurityConfig.java` — 添加白名单 `/api/v1/client/announcements`

## 文件变更汇总

### 新增文件
| 文件 | 用途 |
|------|------|
| `docs/sql/migration_2026_05_26_notification_status.sql` | 字典数据更新 + summary 字段 |
| `aeisp-message/.../controller/NotificationImageController.java` | 图片上传 |
| `aeisp-message/.../request/UpdateNotificationRequest.java` | 编辑请求 DTO |
| `aeisp-message/.../scheduler/ExpiredNotificationScheduler.java` | 自动过期调度 |
| `aeisp-message/.../controller/ClientAnnouncementController.java` | Unity 公告接口 |
| `aeisp-message/.../service/ClientAnnouncementService.java` | Unity 公告 Service 接口 |
| `aeisp-message/.../service/impl/ClientAnnouncementServiceImpl.java` | Unity 公告 Service 实现 |
| `aeisp-message/.../vo/ClientAnnouncementVO.java` | Unity 公告 VO |

### 修改文件
| 文件 | 变更内容 |
|------|----------|
| `msg_notification` (DB) | 新增 `summary` 字段 |
| `MsgNotification.java` | 新增 `summary` 字段 |
| `MsgNotificationVO.java` | 新增 `summary`、`publisherName` |
| `MsgNotificationDetailVO.java` | 新增 `summary` |
| `CreateNotificationRequest.java` | 新增 `summary` 字段 |
| `MsgNotificationController.java` | 新增 `PUT /{id}` 端点 |
| `MsgNotificationService.java` | 新增 `updateNotification()` |
| `MsgNotificationServiceImpl.java` | 实现更新、发布人查询、summary 处理 |
| `index.vue` | 状态条件、新增列、图片上传按钮 |
| `NotificationPushScheduler.java` | **删除** 此文件 |
| `SecurityConfig.java` | 添加 Unity 公告接口白名单 |
| `init-postgresql.sql` | 更新 dict 数据 |

## 测试计划

1. **状态修复测试** — 创建草稿 → 列表显示"草稿"标签 → 发布 → 显示"已发送" → 撤回 → 显示"已撤回" → 归档 → 显示"已归档"
2. **编辑测试** — 草稿可编辑、已发送不可编辑
3. **图片上传测试** — 上传合法图片 → 返回 URL → 前端插入占位符
4. **过期调度测试** — 设置过期时间 → 触发调度 → 状态变为"已过期"
5. **Unity 接口测试** — curl 调用返回格式正确、过期公告不返回
6. **前端回归** — 搜索、分页、详情弹窗正常