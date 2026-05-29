# 库资源模块设计文档

## 1. 概述

### 1.1 背景
新增独立的库资源管理模块，用于管理系统中的库文件资源。库资源以 ZIP 格式上传，支持版本管理和回滚。模板资源可关联一个或多个库资源。

### 1.2 范围
- 管理端：库资源的 CRUD、版本管理、回滚
- 前端页面：库资源列表、新增/编辑/详情、版本上传/回滚
- 模板关联：模板详情/编辑/新增时关联库资源
- 客户端接口：Unity 客户端通过 ID 下载库资源（JWT 认证）

### 1.3 约束
- 文件格式：ZIP
- 临时存储：本地文件系统（`D:\Users\admin\Desktop\EISP\Resource\Library`）
- 未来迁移：阿里云 OSS（通过 `ResourceServerService` 抽象层切换）
- 关联关系：当前阶段一对多（一个模板可关联多个库，一个库专属于一个模板），未来可扩展为多对多

## 2. 架构设计

### 2.1 模块划分

新建 Maven 模块 `aeisp-library`，与 `aeisp-template` 同级。

```
aeisp-library/
├── src/main/java/com/aeisp/library/
│   ├── controller/
│   │   ├── LibraryResourceController.java      # 管理端 CRUD + 版本 + 回滚
│   │   └── ClientLibraryController.java        # Unity 客户端下载接口
│   ├── service/
│   │   ├── LibraryResourceService.java
│   │   ├── LibraryResourceServiceImpl.java
│   │   ├── LibraryStorageService.java          # 本地临时存储 + 解压
│   │   └── LibraryStorageServiceImpl.java
│   ├── entity/
│   │   ├── LibResource.java                    # 库资源主表
│   │   └── LibResourceVersion.java             # 库资源版本表
│   ├── mapper/
│   │   ├── LibResourceMapper.java
│   │   └── LibResourceVersionMapper.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateLibraryRequest.java
│   │   │   ├── UpdateLibraryRequest.java
│   │   │   └── LibraryQueryRequest.java
│   │   └── vo/
│   │       ├── LibResourceVO.java
│   │       ├── LibResourceDetailVO.java
│   │       └── LibResourceVersionVO.java
│   └── code/
│       └── LibraryErrorCode.java
└── src/main/resources/mapper/
    ├── LibResourceMapper.xml
    └── LibResourceVersionMapper.xml
```

### 2.2 模块注册
- 根 `pom.xml` 的 `<modules>` 中新增 `aeisp-library`
- `aeisp-boot/pom.xml` 依赖 `aeisp-library`

### 2.3 复用机制
- **文件存储**：复用 `ResourceServerService` 接口，通过 `ResourceServerConfig` 新增 `libraryResourceServer` Bean
- **存储配置**：`sys_config` 中新增 `storage.library.path` 和 `storage.library.baseUrl`
- **权限体系**：`sys_permission` 中新增 `library:create`、`library:read`、`library:update`、`library:delete`、`library:version:manage`

### 2.4 关联表设计
在 `aeisp-template` 模块中新增关联表 `tpl_template_library`：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| template_id | BIGINT | 模板 ID |
| library_id | BIGINT | 库资源 ID |
| created_at | TIMESTAMP | 创建时间 |

- 唯一约束：`(template_id, library_id)`
- 关联更新方式：**覆盖式**（每次保存模板时传入完整的 libraryIds 列表，后端先删除旧关联再插入新关联）

## 3. 数据库设计

### 3.1 库资源主表 `lib_resource`

```sql
CREATE TABLE IF NOT EXISTS lib_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_code VARCHAR(30) NOT NULL,         -- 自动生成：LIB+yyyyMMdd+4位序号
    resource_name VARCHAR(64) NOT NULL,         -- 库资源名称
    description VARCHAR(512) DEFAULT NULL,      -- 描述
    status SMALLINT NOT NULL DEFAULT 1,         -- 0-正常, 1-上架, 2-下架, 3-违规
    current_version_id BIGINT DEFAULT NULL,     -- 当前版本ID（FK -> lib_resource_version）
    download_count BIGINT NOT NULL DEFAULT 0,   -- 下载次数
    violation_reason VARCHAR(255) DEFAULT NULL, -- 违规原因
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_code UNIQUE (resource_code)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_status ON lib_resource (status);
```

### 3.2 库资源版本表 `lib_resource_version`

```sql
CREATE TABLE IF NOT EXISTS lib_resource_version (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,                -- 关联库资源ID
    version_no VARCHAR(16) NOT NULL,            -- 版本号，如 "1.0.0"
    file_path VARCHAR(255) DEFAULT NULL,        -- ZIP文件相对路径
    storage_url VARCHAR(255) DEFAULT NULL,      -- 完整下载URL
    file_size BIGINT DEFAULT NULL,              -- 文件大小（字节）
    file_hash VARCHAR(64) DEFAULT NULL,         -- MD5/SHA-256
    changelog TEXT DEFAULT NULL,                -- 更新日志
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_version UNIQUE (resource_id, version_no)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_version_resource_id ON lib_resource_version (resource_id);
```

### 3.3 模板-库资源关联表 `tpl_template_library`

```sql
CREATE TABLE IF NOT EXISTS tpl_template_library (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_tpl_template_library UNIQUE (template_id, library_id)
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_library_template_id ON tpl_template_library (template_id);
```

### 3.4 系统配置

```sql
INSERT INTO sys_config (config_key, config_value, description, environment, is_editable) VALUES
('storage.library.path', 'D:/Users/admin/Desktop/EISP/Resource/Library/', '库资源本地存储路径', 'all', 1),
('storage.library.baseUrl', 'http://192.168.50.215/EISP/Resource/Library/', '库资源访问基础URL', 'all', 1);
```

### 3.5 权限菜单

```sql
-- 库资源管理目录
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible) VALUES
('库资源管理', 'library:manage', 'library', 'manage', 0, 0, 40, 'Collection', '/library', NULL, 1);

-- 库资源列表菜单（parent_id 指向上面插入的目录）
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible) VALUES
('库资源列表', 'library:list', 'library', 'list', <dir_id>, 1, 1, 'List', 'list', 'library/list/index', 1);

-- 按钮权限
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible) VALUES
('新增库资源', 'library:create', 'library', 'create', <menu_id>, 2, 1, 1),
('编辑库资源', 'library:update', 'library', 'update', <menu_id>, 2, 2, 1),
('删除库资源', 'library:delete', 'library', 'delete', <menu_id>, 2, 3, 1),
('查询库资源', 'library:read', 'library', 'read', <menu_id>, 2, 4, 1),
('版本管理', 'library:version:manage', 'library', 'manage', <menu_id>, 2, 5, 1);
```

## 4. API 设计

### 4.1 管理端接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/v1/library-resources` | `library:create` | 创建库资源（multipart，含 ZIP） |
| PUT | `/api/v1/library-resources/{id}` | `library:update` | 修改基础信息 |
| POST | `/api/v1/library-resources/{id}/versions` | `library:version:manage` | 上传新版本（multipart） |
| POST | `/api/v1/library-resources/{id}/rollback/{versionId}` | `library:version:manage` | 回滚到指定版本 |
| POST | `/api/v1/library-resources/{id}/status` | `library:update` | 上下线切换 |
| DELETE | `/api/v1/library-resources/{id}` | `library:delete` | 逻辑删除 |
| GET | `/api/v1/library-resources` | `library:read` | 分页列表，支持 name/status/ids |
| GET | `/api/v1/library-resources/{id}` | `library:read` | 详情（含版本列表+文件树） |
| GET | `/api/v1/library-resources/{id}/files` | `library:read` | 文件结构 |
| GET | `/api/v1/library-resources/{id}/files/content` | `library:read` | 读取文件内容 |
| GET | `/api/v1/library-resources/public` | 无需权限 | 上架列表（不分页，下拉选择用） |

### 4.2 客户端下载接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/v1/client/library-resources/{id}/download` | JWT 认证 | 记录日志后 302 重定向到 storage_url |

### 4.3 模板关联接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/v1/templates/{id}/libraries` | `template:update` | 设置关联库资源（覆盖式，Body: `{libraryIds: [1,2,3]}`） |
| GET | `/api/v1/templates/{id}/libraries` | `template:read` | 查询模板关联的库资源 ID 列表 |

### 4.4 DTO 定义

**CreateLibraryRequest**
```java
@NotBlank String resourceName;
String description;
@NotBlank String versionNo;
@NotNull MultipartFile zipFile;
String changelog;
```

**UpdateLibraryRequest**
```java
@NotBlank String resourceName;
String description;
```

**LibResourceVO**
```java
Long id;
String resourceCode;
String resourceName;
String description;
Integer status;
String statusLabel;
String currentVersionNo;
Long fileSize;
String fileSizeLabel;  // 自动格式化为 KB/MB
Long downloadCount;
LocalDateTime createdAt;
```

**LibResourceDetailVO**
```java
// 基础字段同 LibResourceVO
List<LibResourceVersionVO> versionList;
List<String> fileTree;
```

**LibResourceVersionVO**
```java
Long id;
String versionNo;
Long fileSize;
String fileHash;
String changelog;
LocalDateTime createdAt;
Boolean isCurrent;  // 是否为当前版本
```

## 5. 存储与回滚设计

### 5.1 文件上传流程

复用模板模块的两阶段上传模式：

1. **本地临时存储**：`LibraryStorageService.storeZip(resourceId, versionNo, file)`
   - 临时路径：`{storage.library.path}/temp/{resourceId}/{versionNo}/`
2. **上传资源服务器**：`resourceServerService.uploadFile(relativePath, zipBytes)`
   - 远程路径：`{resourceId}/{versionNo}/resource.zip`
3. **解压并上传提取文件**：`resourceServerService.uploadExtractedFiles(resourceId, versionNo, extractDir)`
4. **保存版本记录**：`lib_resource_version` 表插入记录
5. **更新当前版本指针**：`lib_resource.current_version_id = 新版本.id`
6. **清理临时文件**

### 5.2 回滚机制

回滚只更新主表的 `current_version_id` 指针，历史版本记录保留不删除（逻辑删除）。

```java
@Transactional
public boolean rollbackVersion(Long resourceId, Long versionId) {
    // 1. 校验库资源存在
    // 2. 校验版本存在且属于该库资源
    // 3. 更新 current_version_id
    resource.setCurrentVersionId(versionId);
    resourceMapper.updateById(resource);
}
```

### 5.3 删除机制

逻辑删除（`deleted = 1`）：
- 主表记录逻辑删除
- 版本表记录级联逻辑删除
- 资源服务器上的文件保留（避免影响已下载用户）

## 6. 前端设计

### 6.1 新增文件

```
aeisp-admin/src/views/library/list/index.vue
aeisp-admin/src/api/library/index.js
```

### 6.2 库资源列表页

- 搜索栏：名称模糊搜索、状态下拉（上架/下架/违规）、重置按钮
- 表格列：ID、编码、名称、状态、当前版本、文件大小、下载次数、创建时间、操作
- 操作按钮：详情、编辑、版本（上传新版本）、上下架切换、删除

### 6.3 新增/编辑弹窗

- 资源名称（必填）
- 描述
- 版本号（必填，创建时填写）
- 更新日志
- ZIP 文件上传（el-upload，auto-upload=false，创建时必填）

### 6.4 详情抽屉

- 基础信息（只读）
- 版本历史列表（可回滚到任意历史版本）
- 当前版本文件树

### 6.5 模板页面扩展

**新增/编辑弹窗**：
- 增加"关联库资源"多选下拉框（`el-select-v2`，multiple）
- 选项来源：`GET /api/v1/library-resources/public`
- 选项格式：`{ value: id, label: "resourceName (vversionNo)" }`
- `libraryIds` 随表单一并提交（覆盖式更新）

**详情页**：
- 新增"关联库资源"区域，显示已关联的库资源名称列表
- 通过关联 ID 调库资源接口获取名称显示

### 6.6 菜单路由

- 路径：`/library`
- 组件：`library/list/index`
- 图标：`Collection`
- 权限：`library:list`

## 7. 客户端下载设计

### 7.1 下载流程

1. Unity 客户端携带 JWT Token 请求下载接口
2. 校验库资源存在且状态为上架（status = 1）
3. 获取当前版本（或指定版本）
4. 查询版本记录获取 `storage_url`
5. 记录下载日志（`lib_resource_usage_log` 表，可选）
6. 返回 302 重定向到 `storage_url`

### 7.2 下载日志表（可选）

```sql
CREATE TABLE IF NOT EXISTS lib_resource_usage_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(64),
    version_no VARCHAR(16),
    action_type VARCHAR(20),    -- "download"
    created_at TIMESTAMP DEFAULT NULL
);
```

## 8. 错误码设计

| 错误码 | 说明 |
|--------|------|
| `LIBRARY_NOT_FOUND` | 库资源不存在 |
| `LIBRARY_CODE_EXISTS` | 资源编码已存在 |
| `VERSION_NOT_FOUND` | 版本不存在 |
| `VERSION_NOT_BELONG` | 版本不属于该库资源 |
| `VERSION_EXISTS` | 版本号已存在 |
| `LIBRARY_NOT_PUBLISHED` | 库资源未上架 |
| `FILE_UPLOAD_FAILED` | 文件上传失败 |
| `FILE_HASH_MISMATCH` | 文件哈希校验失败 |

## 9. 实现顺序建议

1. 创建 `aeisp-library` 模块骨架（pom.xml、包结构）
2. 编写数据库迁移 SQL（表 + 配置 + 权限菜单）
3. 实现实体类、Mapper、Service、Controller
4. 实现文件存储服务（LibraryStorageService + ResourceServerService 实现）
5. 实现客户端下载接口
6. 实现前端页面（库资源列表）
7. 扩展模板页面（关联库资源功能）
8. 编写单元测试
