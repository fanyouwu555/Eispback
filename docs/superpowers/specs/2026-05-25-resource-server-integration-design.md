# 资源服务器集成设计

## 背景

当前模板 ZIP 上传只保存到本地磁盘 `./uploads/templates/`，未上传到资源服务器。Unity 客户端需要按需加载模板资源（ZIP 包和单独文件如图片），需要将模板资源真正上传到资源服务器并提供 URL 访问。

## 目标

1. 抽象资源服务器层，NFS（当前）和阿里云 OSS（将来）可切换
2. ZIP 上传后自动提取并上传所有资源到资源服务器，保持目录结构
3. 记录资源路径到数据库，Unity 客户端通过 URL 按需加载

## 整体架构

新增 `ResourceServerService` 抽象层，与现有 `TemplateStorageService` 职责分离：

```
Controller → TplTemplateServiceImpl
  ├── TemplateStorageService     (本地暂存、解压、文件树读取 —— 保持不变)
  └── ResourceServerService ★    (上传到资源服务器、获取URL —— 新增)
```

```
              ┌─────────────────────────────┐
              │   ResourceServerService     │
              │   (接口)                     │
              ├─────────────┬───────────────┤
              │ NFS实现(当前) │ OSS实现(将来) │
              └─────────────┴───────────────┘
```

## 目录结构

资源服务器上每个版本的目录结构：

```
{resourceBaseUrl}/{templateId}/{versionNo}/
  ├── template.zip              ← ZIP 包
  ├── textures/
  │   └── bg.png                ← 提取的资源
  ├── prefabs/
  │   └── xxx.prefab
  └── ...（ZIP 中原有的所有文件）
```

ZIP 和提取的资源在同一目录下，Unity 客户端可基于同一 URL 前缀按需加载。

## ResourceServerService 接口

```java
public interface ResourceServerService {

    /** 上传单个文件到资源服务器，返回可访问 URL */
    String uploadFile(String relativePath, byte[] data);

    /** 批量上传解压后的所有文件，返回相对路径列表 */
    List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir);

    /** 删除单个文件 */
    void deleteFile(String relativePath);

    /** 删除整个版本目录 */
    void deleteVersionFiles(Long templateId, String versionNo);

    /** 根据相对路径获取完整 URL */
    String getUrl(String relativePath);

    /** 判断文件是否存在 */
    boolean fileExists(String relativePath);
}
```

## NFS 实现（NfsResourceServerServiceImpl）

NFS 挂载到本地路径后，本质是本地文件写入 + URL 构造：

- `uploadFile(relativePath, data)` → 写入 `{nfsUploadPath}/{relativePath}` → 返回 `{baseUrl}/{relativePath}`
- `uploadExtractedFiles(templateId, versionNo, extractDir)` → 遍历提取目录，保持子目录结构，逐文件上传
- 路径安全校验复现现有的 `isPathSafe()` / `isWithinBaseDir()` 逻辑

## 配置（application.yml）

```yaml
resource-server:
  type: nfs    # nfs 或 oss
  nfs:
    upload-path: /mnt/nfs/templates/     # NFS 挂载的本地路径
    base-url: http://localhost/EISP/Resource/Template/
  # oss:
  #   endpoint: oss-cn-hangzhou.aliyuncs.com
  #   bucket: aeisp
  #   access-key: xxx
  #   secret-key: xxx
```

原有的 `template.upload-path` 和 `template.resource-base-url` 保持不动（`upload-path` 用于本地临时目录，`resource-base-url` 由 NFS 配置的 `base-url` 替代）。

## 上传流程变更

### 创建模板 + 上传首个版本

```
1. templateMapper.insert(template)                              ← 创建模板记录
2. templateStorageService.storeZip(...)                          ← 暂存 ZIP 到本地临时目录
3. resourceServerService.uploadFile(relativePath, zipBytes)      ← ★ 上传 ZIP 到资源服务器 → 得到 storageUrl
4. templateStorageService.extractZip(zipAbsPath, destDir)        ← 解压到本地临时目录
5. resourceServerService.uploadExtractedFiles(id, version, destDir)  ← ★ 上传所有提取的文件
6. version.setStorageUrl(storageUrl)
   version.setFilePath(relativePath)
   version.setFileSize(...)
   version.setFileHash(...)
   versionMapper.insert(version)                                 ← 保存版本记录
7. template.setCurrentVersionId(version.getId())
   templateMapper.updateById(template)
8. templateStorageService.deleteVersionFiles(id, versionNo)      ← 清理本地临时文件
```

### 上传新版本

流程同上，增加了版本号重复校验。

## DB 变更

`tpl_template_version` 表已有 `storage_url` 字段，无需新增表。

`tpl_template_version.storage_url` → 存储 ZIP 文件的资源服务器访问 URL。

## VO 变更

`TplTemplateVersionVO` 新增字段：

```java
private String storageUrl;         // ZIP 文件的资源服务器下载地址
private String resourceBaseUrl;    // 该版本资源的基础 URL，Unity 拼接使用
```

## Unity 客户端使用方式

1. 调用 `GET /api/v1/templates/public/{id}` 获取模板详情
2. 从返回的 `currentVersion.storageUrl` 下载 ZIP
3. 从 `currentVersion.resourceBaseUrl` 拼接具体资源路径按需加载：
   - 图片：`{resourceBaseUrl}/textures/bg.png`
   - 其他文件：`{resourceBaseUrl}/{ZIP内相对路径}`

## 下载端点

清理本地临时文件后，管理端下载 `GET /api/v1/templates/{id}/download` 不再能从本地流式返回。

有两种处理方式供选择：

1. **HTTP 重定向**：Controller 返回 302 指向 `storageUrl`，浏览器/客户端自动跳转到资源服务器下载
2. **代理流式下载**：后端从资源服务器下载文件后再流式返回给客户端（增加一次网络传输）

推荐方式 1（重定向），简单高效，不经过后端中转。

- **管理端下载**：改为返回 302 重定向到 `storage_url`
- **Unity 客户端**：通过详情接口获取 URL 后自行下载，无需额外端点

## 未来 OSS 扩展

新增 `OssResourceServerServiceImpl` 实现 `ResourceServerService`：

- `uploadFile()` → OSS SDK `putObject()` → 返回 OSS 外链
- `uploadExtractedFiles()` → 逐文件 `putObject()`
- `deleteFile()` → OSS SDK `deleteObject()`
- 配置 `resource-server.type: oss` 切换