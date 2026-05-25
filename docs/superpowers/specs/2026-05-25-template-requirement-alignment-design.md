# 模板系统功能对齐与改造设计

## 概述

基于 `模板需求.txt` 对当前 aeisp-template 模块进行全面审查，将所有功能点对齐为统一需求标准。当前系统已有大部分功能，本次改造聚焦于差异点补齐、字段完善和架构统一。

## 1. 统一三级分类体系（替换 scenario）

### 现状
- 模板既有 `scenario` 字段（teaching/competition/practice），又有 `top_category_id/first_category_id/second_category_id` 三级分类
- 二者功能重叠：scenario 的 teaching→教学、competition→竞赛、practice→实训 与三级分类的 常用模板/比赛模板/特殊类模板 高度对应

### 改造
- 移除 `tpl_template.scenario` 列
- 移除 `TplTemplate` 实体、所有 DTO、VO 中的 scenario 字段
- 移除前端所有场景选择器、标签渲染
- 统一以三级分类（顶级→一级→二级）作为唯一分类方式

### 数据库
```sql
ALTER TABLE tpl_template DROP COLUMN scenario;
```

## 2. template_code 自动生成

### 现状
- `tpl_template.template_code` 列为 NULL，创建时未填充

### 改造
- 格式：`TPL` + yyyyMMdd + 4位序号，如 `TPL202605250001`
- 在同日内自增，使用 Redis 或数据库 select max 实现序列
- 使用 `synchronized` 代码块 + 数据库查询当前最大序号（简化方案，无需引入 Redis 依赖）

### 生成逻辑
```
prefix = "TPL" + yyyyMMdd          // e.g. "TPL20260525"
maxCode = SELECT MAX(template_code) FROM tpl_template WHERE template_code LIKE 'prefix%'
seq = (maxCode != null) ? 提取后4位+1 : 1
template_code = prefix + String.format("%04d", seq)
```

## 3. 版本编辑页扩展字段

### 现状
- `POST /{id}/versions` 仅接收 `zipFile` + `versionNo` + `changelog`
- 前端版本弹窗只有版本号和更新日志输入

### 改造
- 扩展 `uploadNewVersion` 方法参数（可选字段）：
  - `versionDesc` — 版本描述（与 changelog 合并或作为补充）
  - `onlineTime` — 上线时间（空则沿用模板当前值）
  - `validTime` — 有效截止时间
  - `difficulty` — 难度等级
  - `isPaid` / `feeType` / `price` — 付费设置
- 当新版本上传时，如果提供了以上字段，同步更新模板主表对应字段
- 如果未提供，保留模板主表原有值（"默认使用上一次填的数据"）

### 前端版本弹窗新增字段
- 版本描述（必填 — 客户端检测更新用）
- 上线时间 / 有效截止时间
- 难度等级
- 是否付费 / 费用类型 / 价格

## 4. 封面图上传迁移到资源服务器

### 现状
- `POST /{id}/cover` 调用 `TemplateStorageServiceImpl.storeCoverImage()` 存入本地文件系统
- 资源服务器抽象层 `ResourceServerService` 已经存在

### 改造
- `storeCoverImage` 改为调用 `resourceServerService.uploadFile()`
- 封面图路径：`{template_id}/cover/{uuid}.{ext}`
- 返回资源服务器 URL
- 删除旧封面时清理资源服务器上的旧文件

## 5. 有效时间过滤（客户端 API）

### 现状
- `selectOnlineList` 只检查 `status = 1`
- 过期模板仍被返回给 Unity 客户端

### 改造
- 添加 `AND (valid_time IS NULL OR valid_time > NOW())` 条件
- Unity 客户端仅收到有效期内上架的模板

## 6. 前端模板列表增加"所属分类"列

### 现状
- 列表列中缺少分类展示

### 改造
- `TplTemplateVO` 增加 `categoryPath` 字段（字符串："顶级名称-一级名称-二级名称"）
- 后端在 `convertToVO` 中通过 categoryService 查询并拼接路径
- 前端列表增加列

## 7. difficulty 难度等级字段补全

### 现状
- 实体有 `difficulty` 字段
- DTO、VO、前端表单均未使用

### 改造
- `CreateTemplateRequest` 增加 `difficulty` 字段
- `UpdateTemplateRequest` 增加 `difficulty` 字段
- 前端创建/编辑表单增加难度等级选择器
- 详情展示增加难度等级

## 8. DB SQL 文档补充

### 改造
- 将 `tpl_template_category` 表定义补充到 `init-postgresql.sql`
- 将所有新增字段合入 `init-postgresql.sql` 中的 `tpl_template` 表定义
- 生成本次变更的迁移 SQL 文件

## 影响范围

| 变更 | 后端文件数 | 前端文件数 | DB |
|------|-----------|-----------|----|
| 移除 scenario | 8 | 2 | 1 DDL |
| template_code 生成 | 1 | 0 | 无需 DDL |
| 版本编辑扩展 | 2 | 1 | 无需 DDL |
| 封面图迁移 NFS | 1 | 0 | 无需 DDL |
| 有效时间过滤 | 1 | 0 | 无需 DDL |
| 所属分类列 | 1 | 1 | 无需 DDL |
| difficulty 补全 | 2 | 1 | 无需 DDL |
| SQL 文档 | 0 | 0 | 2 SQL 文件 |

## 实施顺序

1. difficulty 字段补全（最小改动，无破坏性）
2. template_code 自动生成
3. scenario 全链路移除（有破坏性，需同时处理 DB SQL）
4. 有效时间过滤
5. 版本编辑页扩展
6. 封面图上传迁移到 NFS
7. 前端"所属分类"列
8. DB SQL 文档补充
9. 测试验证