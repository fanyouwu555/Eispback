# 项目模块（Unity 客户端 API）设计

## 背景

现有 `aeisp-project` 模块仅提供管理后台 CRUD（列表/详情/归档/删除），缺少 Unity 客户端创建项目、上传 ZIP、更新元信息、删除项目的 API。需要基于文档需求补充客户端 API，并复用现有的 `ResourceServerService` 抽象实现文件存储。

## 变更概览

1. **`ResourceServerService` 接口从 aeisp-template 移到 aeisp-common**，并扩展 `deleteDirectory` 方法
2. **NfsResourceServerServiceImpl 重构**：从 `@Value` 字段注入改为构造器注入，支持创建多个实例
3. **新增 application.yml 配置**：`resource-server.users.*` 指向 Users 存储目录
4. **`PrjProject` 实体加 `resource_url` 字段**
5. **新增 Unity 客户端 API 端点**（`/api/v1/client/projects`）
6. **新增 `ProjectErrorCode` 枚举值**

## 接口移动：ResourceServerService → aeisp-common

将 `com.aeisp.template.service.ResourceServerService` 移到 `com.aeisp.common.service.ResourceServerService`。

**接口变更** — 新增：
```java
/**
 * 递归删除资源服务器上的整个目录。
 *
 * @param relativePath 相对路径，如 "18/42/"
 */
void deleteDirectory(String relativePath);
```

该接口只依赖标准 Java 类型，可零依赖地放在 common 模块。

## NfsResourceServerServiceImpl 重构

当前实现通过 `@Value` 字段注入路径配置，无法创建多个实例。改为构造器注入：

```java
// 去掉 @Service、@ConditionalOnProperty、@Value
// 改为纯实现类，路径通过构造器传入
public class NfsResourceServerServiceImpl implements ResourceServerService {
    private final String uploadPath;
    private final String baseUrl;
    
    public NfsResourceServerServiceImpl(String uploadPath, String baseUrl) { ... }
}
```

在 `aeisp-boot` 中新增配置类 `com.aeisp.boot.config.ResourceServerConfig`，注册两个 bean：
（`@SpringBootApplication(scanBasePackages = "com.aeisp")` 保证被扫描到）

```java
@Configuration
public class ResourceServerConfig {
    
    @Bean("templateResourceServer")
    @Primary
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService templateResourceServer(
            @Value("${resource-server.nfs.upload-path}") String uploadPath,
            @Value("${resource-server.nfs.base-url}") String baseUrl) {
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }
    
    @Bean("userResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService userResourceServer(
            @Value("${resource-server.users.upload-path}") String uploadPath,
            @Value("${resource-server.users.base-url}") String baseUrl) {
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }
}
```

**application.yml 新增配置：**

```yaml
resource-server:
  type: nfs
  nfs:
    upload-path: D:/Users/admin/Desktop/EISP/Resource/Template/
    base-url: http://192.168.50.215/EISP/Resource/Template/
  users:
    upload-path: D:/Users/admin/Desktop/EISP/Users/
    base-url: http://192.168.50.215/EISP/Users/
```

**现有模板代码无需改动** — `@Primary` 保证原来注入 `ResourceServerService` 的地方拿到的是 template 实例。

## 数据表变更

```sql
-- 迁移 SQL
ALTER TABLE prj_project ADD COLUMN resource_url VARCHAR(512) DEFAULT NULL;
COMMENT ON COLUMN prj_project.resource_url IS '项目资源存储URL';
```

## 实体变更

`PrjProject.java` 增加字段：
```java
private String resourceUrl;
```

## ErrorCode 补充

```java
PROJECT_ACCESS_DENIED(8003, "无权操作该项目"),
PROJECT_FILE_INVALID(8004, "项目文件无效");
```

## 客户端项目列表 VO

新增 `com.aeisp.project.dto.vo.ClientProjectVO`，只返回客户端关心的字段：

```java
public class ClientProjectVO implements Serializable {
    private Long projectId;          // 对应 entity.id
    private String projectName;
    private Long templateId;
    private String resourceUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## Service 接口扩展

在现有 `PrjProjectService` 中新增 5 个客户端方法：

```java
// 创建项目
ClientProjectVO createClientProject(Long userId, Long templateId, String projectName);

// 用户项目列表（分页）
PageResult<ClientProjectVO> listClientProjects(Long userId, int pageNum, int pageSize);

// 上传项目 ZIP
String uploadProjectZip(Long projectId, Long userId, byte[] zipData);

// 更新项目名称
void updateClientProject(Long projectId, Long userId, String projectName);

// 删除项目（含清理 NFS 文件）
void deleteClientProject(Long projectId, Long userId);
```

## Unity 客户端 API

### 认证方式

所有客户端 API 使用 JWT Bearer Token 认证（与现有管理端一致）。从 `SecurityContext` 提取 `CustomUserDetails.userId`。

新增 Controller：`com.aeisp.project.controller.ClientProjectController`，映射 `/api/v1/client/projects`。
与管理端 `PrjProjectController`（映射 `/api/v1/projects`）完全独立，互不干扰。

### 5 个端点

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | `/api/v1/client/projects` | 创建项目 |
| GET | `/api/v1/client/projects` | 用户项目列表 |
| POST | `/api/v1/client/projects/{id}/upload` | 上传项目 ZIP |
| PUT | `/api/v1/client/projects/{id}` | 更新项目名称 |
| DELETE | `/api/v1/client/projects/{id}` | 删除项目（含文件） |

### 创建项目

```
POST /api/v1/client/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "templateId": 1,
  "projectName": "我的项目"   // 可选，默认 "未命名项目"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "projectId": 42,
    "resourceUrl": "http://192.168.50.215/EISP/Users/18/42/",
    "createdAt": "2026-05-25T10:00:00"
  }
}
```

**逻辑：**
1. 从 JWT 提取 `userId`
2. `projectName` 为空则默认 "未命名项目"
3. 插入 DB 记录（status=0 进行中），resourceUrl 留空
4. 计算 resourceUrl = 配置的 users baseUrl + `{userId}/{projectId}/`
5. 回填 resourceUrl 到 DB（**此 URL 为目录 URL，后续上传 ZIP 不会更新它**）
6. 返回 projectId + resourceUrl

### 用户项目列表

客户端列表 VO 精简，只返回客户端关心的字段：

```
GET /api/v1/client/projects?page=1&size=10
Authorization: Bearer <token>
```

**响应（使用 ClientProjectVO）：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "projectId": 42,
        "projectName": "我的项目",
        "templateId": 1,
        "resourceUrl": "http://192.168.50.215/EISP/Users/18/42/",
        "status": 0,
        "createdAt": "2026-05-25T10:00:00",
        "updatedAt": "2026-05-25T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 上传项目 ZIP

```
POST /api/v1/client/projects/{projectId}/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: project.zip
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "storageUrl": "http://192.168.50.215/EISP/Users/18/42/project.zip"
  }
}
```

**逻辑：**
1. 校验项目存在且归属当前用户
2. Controller 接收 `MultipartFile`，提取 `byte[]`
3. 调用 `userResourceServer.uploadFile("18/42/project.zip", zipBytes)`
4. 返回 ZIP 的完整 URL
5. 不更新 DB 中 `resource_url`（保持目录 URL 不变）

### 更新项目名称

```
PUT /api/v1/client/projects/{projectId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectName": "新名称"
}
```

**响应：**
```json
{ "code": 200, "message": "成功" }
```

### 删除项目（含文件）

```
DELETE /api/v1/client/projects/{projectId}
Authorization: Bearer <token>
```

**响应：**
```json
{ "code": 200, "message": "成功" }
```

**逻辑：**
1. 校验项目存在且归属当前用户
2. 调用 `userResourceServer.deleteDirectory("18/42/")` 删除 NFS 目录
3. DB 逻辑删除

## 存储结构

```
D:/Users/admin/Desktop/EISP/Users/
├── 18/                          # userId
│   ├── 42/                      # projectId
│   │   ├── project.zip          # 用户上传的项目 ZIP
│   │   └── ...                  # (将来) 解压后文件
│   └── 43/
│       └── project.zip
└── 19/
    └── 44/
        └── project.zip
```

对应 URL：
```
http://192.168.50.215/EISP/Users/18/42/              # 目录 URL
http://192.168.50.215/EISP/Users/18/42/project.zip    # ZIP URL
```

## 权限与安全

- **客户端 API 不需要 `@PreAuthorize`** — 依赖 JWT 认证 + 归属校验即可
- **归属校验** — 每个请求的操作（上传/更新/删除）验证 `project.userId == currentUserId`
- **路径安全** — 复用 NfsResourceServerServiceImpl 中的 `isPathSafe` + `isWithinBaseDir` 防护

## 不涉及变更的部分

- **管理后台 API 不动** — `PrjProjectController`（`/api/v1/projects`）保持不变
- **前端页面不动** — 客户端 API 供 Unity 调用，无需管理后台 UI 变更
- **模板模块不动** — 仅 `ResourceServerService` 接口从 template 移到 common，template 代码只改 import，无逻辑变化