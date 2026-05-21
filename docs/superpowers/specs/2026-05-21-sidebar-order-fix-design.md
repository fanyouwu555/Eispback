# 侧边栏菜单排序修复设计

**日期**: 2026-05-21
**状态**: 已审批

## 问题概述

侧边栏菜单渲染顺序与 EISP 后端管理页面说明文档.docx 不一致，存在两处差异：

| # | 文档要求 | 当前实际 | 原因 |
|---|---------|---------|------|
| 9 | 系统配置模块 | 运营统计模块 | `sort_order` 值在种子数据中颠倒 |
| 10 | 运营统计模块 | 系统配置模块 | 同上 |
| 仪表盘/消息管理/项目管理 | 显示完整目录→子项层级 | 子项作为直接顶级链接 | `hasOneShowingChild` 单子项折叠 |

## 根因分析

### 排序颠倒

`docs/sql/init-postgresql.sql` 和 `docs/sql/migration_postgresql.sql` 中两条目录的 `sort_order` 与文档次序相反：

```sql
-- 当前（错误）
(9, '运营统计模块', ..., sort_order=9, ...),
(10, '系统配置模块', ..., sort_order=10, ...);
-- 修正
(9, '运营统计模块', ..., sort_order=10, ...),
(10, '系统配置模块', ..., sort_order=9, ...);
```

id=9（运营统计模块）的子项 id=33,34 的 `parent_id=9` 保持不变；id=10（系统配置模块）的子项 id=35,36 的 `parent_id=10` 保持不变。仅交换 `sort_order` 值。

### 单子项折叠

`vue-element-admin` 的 `SidebarItem.vue` 的 `hasOneShowingChild` 方法：当目录只有 **1 个可见子项**且父目录无 `alwaysShow` 标志时，将子项提升为直接链接，隐藏父目录。

受影响目录：仪表盘（1 子项）、消息管理（1 子项）、项目管理（1 子项）。

已在 `aeisp-admin/src/utils/dynamic-routes.js` 的 `convertMenu` 中为所有目录路由设置 `route.meta.alwaysShow = true`。

## 修改文件

| 文件 | 修改内容 |
|------|---------|
| `docs/sql/init-postgresql.sql` | 交换 id=9,10 的 sort_order（9↔10） |
| `docs/sql/migration_postgresql.sql` | 同上 |
| `aeisp-admin/src/utils/dynamic-routes.js` | 目录路由加 `alwaysShow: true` |

## 验证方式

1. 执行修正后的 SQL（手动 UPDATE 生产数据库，或重新导入 init SQL）
2. 重新登录系统
3. 检查侧边栏：9-系统配置模块, 10-运营统计模块
4. 检查仪表盘/消息管理/项目管理是否显示为可展开的目录
5. 用 Playwright 截图验证