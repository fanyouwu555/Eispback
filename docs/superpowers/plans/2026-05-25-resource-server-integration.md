# 资源服务器集成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upload template ZIP files and their extracted resources to a resource server (NFS currently, OSS in future), record URLs for Unity client to download on demand.

**Architecture:** Add `ResourceServerService` abstraction layer in aeisp-template. NFS implementation writes files to NFS mount path and constructs public URLs. Upload flow: store ZIP locally → upload to resource server → extract → upload extracted files → record URLs → clean local temp files.

**Tech Stack:** Java 17, Spring Boot 3.2.5, NFS (mounted local path), Hutool

---

### Task 1: Create ResourceServerService interface

**Files:**
- Create: `aeisp-template/src/main/java/com/aeisp/template/service/ResourceServerService.java`

- [ ] **Create the interface**

```java
package com.aeisp.template.service;

import java.io.File;
import java.util.List;

/**
 * 资源服务器服务接口。
 *
 * <p>负责将模板资源（ZIP、提取文件等）上传到远程资源服务器（NFS/OSS），
 * 并提供 URL 访问能力。NFS 和 OSS 通过不同的实现类切换。</p>
 *
 * @author AEISP Team
 */
public interface ResourceServerService {

    /**
     * 上传单个文件到资源服务器。
     *
     * @param relativePath 相对路径，如 "1/v1.0.0/template.zip"
     * @param data         文件字节数组
     * @return 完整的可访问 URL
     */
    String uploadFile(String relativePath, byte[] data);

    /**
     * 批量上传解压后的所有文件到资源服务器。
     * 遍历 extractDir 下的所有文件，保持子目录结构。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @param extractDir 本地解压目录
     * @return 上传的所有文件相对路径列表
     */
    List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir);

    /**
     * 删除资源服务器上的单个文件。
     *
     * @param relativePath 相对路径
     */
    void deleteFile(String relativePath);

    /**
     * 删除资源服务器上整个版本目录。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     */
    void deleteVersionFiles(Long templateId, String versionNo);

    /**
     * 根据相对路径获取完整的可访问 URL。
     *
     * @param relativePath 相对路径
     * @return 完整的 URL
     */
    String getUrl(String relativePath);

    /**
     * 获取资源服务器的基础 URL。
     *
     * @return 基础 URL
     */
    String getBaseUrl();

    /**
     * 判断资源服务器上文件是否存在。
     *
     * @param relativePath 相对路径
     * @return true 如果存在
     */
    boolean fileExists(String relativePath);
}
```

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/ResourceServerService.java
git commit -m "feat(template): add ResourceServerService interface for remote resource storage"
```

---

### Task 2: Create NfsResourceServerServiceImpl

**Files:**
- Create: `aeisp-template/src/main/java/com/aeisp/template/service/impl/NfsResourceServerServiceImpl.java`

- [ ] **Create the NFS implementation**

```java
package com.aeisp.template.service.impl;

import cn.hutool.core.io.FileUtil;
import com.aeisp.template.service.ResourceServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * NFS 资源服务器实现。
 *
 * <p>NFS 挂载到本地路径后，通过本地文件写入 + URL 拼接实现资源上传。
 * 适用于阿里云 OSS 上线前的临时方案。</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
public class NfsResourceServerServiceImpl implements ResourceServerService {

    @Value("${resource-server.nfs.upload-path}")
    private String uploadPath;

    @Value("${resource-server.nfs.base-url}")
    private String baseUrl;

    @Override
    public String uploadFile(String relativePath, byte[] data) {
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        // 确保父目录存在
        FileUtil.mkdir(new File(absolutePath).getParent());
        FileUtil.writeBytes(data, absolutePath);
        log.debug("NFS 上传文件完成: {}", absolutePath);
        return getUrl(normalizedPath);
    }

    @Override
    public List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir) {
        List<String> uploadedPaths = new ArrayList<>();
        if (extractDir == null || !extractDir.exists() || !extractDir.isDirectory()) {
            log.warn("解压目录不存在，跳过批量上传: {}", extractDir);
            return uploadedPaths;
        }

        Path baseDir = extractDir.toPath().toAbsolutePath().normalize();
        String versionDir = templateId + "/" + versionNo + "/";

        try (Stream<Path> walk = Files.walk(baseDir)) {
            walk.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String subPath = baseDir.relativize(filePath).toString().replace("\\", "/");
                        String relativePath = versionDir + subPath;
                        try {
                            byte[] bytes = Files.readAllBytes(filePath);
                            uploadFile(relativePath, bytes);
                            uploadedPaths.add(relativePath);
                        } catch (IOException e) {
                            log.error("上传提取文件失败: {}", relativePath, e);
                        }
                    });
        } catch (IOException e) {
            log.error("遍历解压目录失败: {}", extractDir, e);
        }

        log.info("NFS 批量上传完成: {} 个文件", uploadedPaths.size());
        return uploadedPaths;
    }

    @Override
    public void deleteFile(String relativePath) {
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        File file = new File(absolutePath);
        if (file.exists()) {
            FileUtil.del(file);
            log.debug("NFS 删除文件完成: {}", absolutePath);
        }
    }

    @Override
    public void deleteVersionFiles(Long templateId, String versionNo) {
        String versionDir = templateId + "/" + versionNo;
        String absolutePath = FileUtil.normalize(uploadPath + "/" + versionDir);
        File dir = new File(absolutePath);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.info("NFS 删除版本目录完成: {}", absolutePath);
        }
    }

    @Override
    public String getUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return baseUrl;
        }
        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return baseUrl + normalized;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean fileExists(String relativePath) {
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        return new File(absolutePath).exists();
    }

    /**
     * 规范化路径，移除前导斜杠。
     */
    private static String normalizePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        String normalized = relativePath.replace("\\", "/");
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }
}
```

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/NfsResourceServerServiceImpl.java
git commit -m "feat(template): add NFS resource server implementation"
```

---

### Task 3: Update application.yml with resource-server config

**Files:**
- Modify: `aeisp-boot/src/main/resources/application.yml` (after line 55)

- [ ] **Add resource-server configuration**

Edit `aeisp-boot/src/main/resources/application.yml`:

After the existing `template:` block (lines 53-55), add:

```yaml
resource-server:
  type: nfs    # nfs or oss
  nfs:
    upload-path: /mnt/nfs/templates/     # NFS 挂载的本地路径（Windows 可设为 Z:/templates/）
    base-url: http://localhost/EISP/Resource/Template/
```

Also remove the old `template.resource-base-url` (line 55) since it's replaced by `resource-server.nfs.base-url`:

```yaml
template:
  upload-path: ./uploads/templates/
#  resource-base-url: http://localhost/EISP/Resource/Template/    ← 删除此行
```

- [ ] **Commit**

```bash
git add aeisp-boot/src/main/resources/application.yml
git commit -m "chore: add resource-server config, remove deprecated template.resource-base-url"
```

---

### Task 4: Update TemplateStorageServiceImpl - remove resource-base-url usage

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java`

- [ ] **Remove `resourceBaseUrl` field and update `storeCoverImage()`**

The `TemplateStorageServiceImpl` currently uses `template.resource-base-url` for cover image URLs. After removing that config, it should use `ResourceServerService` for URL construction instead.

Remove the `@Value("${template.resource-base-url:...}")` field (line 36-37) and inject `ResourceServerService`:

```java
// Remove this line (36-37):
// @Value("${template.resource-base-url:http://localhost/EISP/Resource/Template/}")
// private String resourceBaseUrl;

// Add ResourceServerService dependency:
private final ResourceServerService resourceServerService;

// Constructor injection via @RequiredArgsConstructor (existing) - add field
```

Change `storeCoverImage()` (lines 206-227) from returning `resourceBaseUrl + relativeDir + filename` to:

```java
@Override
public String storeCoverImage(Long templateId, MultipartFile file) {
    String ext = "";
    String original = file.getOriginalFilename();
    if (original != null && original.contains(".")) {
        ext = original.substring(original.lastIndexOf("."));
    }
    String filename = "cover_" + System.currentTimeMillis() + ext;
    String relativeDir = "covers/" + templateId + "/";
    String absoluteDir = FileUtil.normalize(basePath + "/" + relativeDir);
    FileUtil.mkdir(absoluteDir);

    String targetPath = absoluteDir + filename;
    try (InputStream is = file.getInputStream()) {
        FileUtil.writeFromStream(is, targetPath);
    } catch (IOException e) {
        log.error("存储封面图失败: templateId={}", templateId, e);
        throw new RuntimeException("存储封面图失败", e);
    }

    // Use ResourceServerService to get the URL
    return resourceServerService.getUrl(relativeDir + filename);
}
```

Also remove the unused `getResourceUrl()` method (or keep it and delegate to `resourceServerService.getUrl()`).

Also remove the unused import of `Paths` if it becomes unused.

- [ ] **Verify compilation**

Run: `mvn compile -pl aeisp-template -am -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java
git commit -m "refactor(template): use ResourceServerService for URL construction in TemplateStorageServiceImpl"
```

---

### Task 5: Modify TplTemplateServiceImpl.createTemplate() and uploadNewVersion()

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`

- [ ] **Inject ResourceServerService**

Add to the existing `@RequiredArgsConstructor` constructor:

```java
private final ResourceServerService resourceServerService;
```

- [ ] **Modify `createTemplate()` method**

Replace lines 87-110 (after template insert, from "存储 ZIP 文件" to the end of extract) with:

```java
// 1. 暂存 ZIP 到本地临时目录
String relativePath = templateStorageService.storeZip(template.getId(), request.getVersionNo(), request.getZipFile());

// 2. 读取本地 ZIP 文件并上传到资源服务器
String zipAbsPath = templateStorageService.getZipAbsolutePath(template.getId(), request.getVersionNo());
String storageUrl = null;
if (zipAbsPath != null) {
    try {
        byte[] zipBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(zipAbsPath));
        storageUrl = resourceServerService.uploadFile(relativePath, zipBytes);
        log.info("ZIP 上传到资源服务器完成: {}", storageUrl);
    } catch (IOException e) {
        log.error("上传 ZIP 到资源服务器失败", e);
        // 上传失败继续执行，storageUrl 为 null，不影响主流程
    }
}

// 3. 解压 ZIP 到本地
String destDir = zipAbsPath != null ? zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.')) : null;
if (zipAbsPath != null) {
    templateStorageService.extractZip(zipAbsPath, destDir);
}

// 4. 上传提取的文件到资源服务器
if (destDir != null) {
    resourceServerService.uploadExtractedFiles(template.getId(), request.getVersionNo(), new java.io.File(destDir));
}

// 5. 保存版本记录
TplTemplateVersion version = new TplTemplateVersion();
version.setTemplateId(template.getId());
version.setVersionNo(request.getVersionNo());
version.setFilePath(relativePath);
version.setStorageUrl(storageUrl);
version.setChangelog(request.getChangelog());
version.setFileSize(request.getZipFile().getSize());
version.setFileHash(calcFileHash(request.getZipFile()));
versionMapper.insert(version);

// 6. 更新当前版本
template.setCurrentVersionId(version.getId());
templateMapper.updateById(template);

// 7. 清理本地临时文件
if (zipAbsPath != null) {
    templateStorageService.deleteVersionFiles(template.getId(), request.getVersionNo());
}
```

- [ ] **Modify `uploadNewVersion()` method**

Replace lines 154-177 (from "存储 ZIP" to the end) with the same flow as above:

```java
// 1. 暂存 ZIP 到本地临时目录
String relativePath = templateStorageService.storeZip(templateId, versionNo, zipFile);

// 2. 读取本地 ZIP 文件并上传到资源服务器
String zipAbsPath = templateStorageService.getZipAbsolutePath(templateId, versionNo);
String storageUrl = null;
if (zipAbsPath != null) {
    try {
        byte[] zipBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(zipAbsPath));
        storageUrl = resourceServerService.uploadFile(relativePath, zipBytes);
        log.info("ZIP 上传到资源服务器完成: {}", storageUrl);
    } catch (IOException e) {
        log.error("上传 ZIP 到资源服务器失败", e);
    }
}

// 3. 解压 ZIP 到本地
String destDir = zipAbsPath != null ? zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.')) : null;
if (zipAbsPath != null) {
    templateStorageService.extractZip(zipAbsPath, destDir);
}

// 4. 上传提取的文件到资源服务器
if (destDir != null) {
    resourceServerService.uploadExtractedFiles(templateId, versionNo, new java.io.File(destDir));
}

// 5. 保存版本记录
TplTemplateVersion version = new TplTemplateVersion();
version.setTemplateId(templateId);
version.setVersionNo(versionNo);
version.setFilePath(relativePath);
version.setStorageUrl(storageUrl);
version.setChangelog(changelog);
version.setFileSize(zipFile.getSize());
version.setFileHash(calcFileHash(zipFile));
versionMapper.insert(version);

// 6. 更新当前版本
template.setCurrentVersionId(version.getId());
templateMapper.updateById(template);

// 7. 清理本地临时文件
if (zipAbsPath != null) {
    templateStorageService.deleteVersionFiles(templateId, versionNo);
}
```

- [ ] **Verify compilation**

Run: `mvn compile -pl aeisp-template -am -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java
git commit -m "feat(template): integrate resource server upload into template creation and version upload"
```

---

### Task 6: Update TplTemplateVersionVO with storageUrl and resourceBaseUrl

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVersionVO.java`

- [ ] **Add storageUrl and resourceBaseUrl fields**

```java
/**
 * ZIP 文件的资源服务器下载地址。
 */
private String storageUrl;

/**
 * 该版本资源的基础 URL，Unity 客户端用于拼接具体资源路径按需加载。
 */
private String resourceBaseUrl;
```

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVersionVO.java
git commit -m "feat(template): add storageUrl and resourceBaseUrl to version VO"
```

---

### Task 7: Populate resourceBaseUrl in version VO at service layer

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`

- [ ] **Inject value for resourceBaseUrl or use ResourceServerService**

Add `ResourceServerService` to populate `resourceBaseUrl`:

In the `convertToVersionVO()` method (line 409-415), after `BeanUtils.copyProperties`, set the new fields:

```java
private static TplTemplateVersionVO convertToVersionVO(TplTemplateVersion version) {
    TplTemplateVersionVO vo = new TplTemplateVersionVO();
    BeanUtils.copyProperties(version, vo);
    vo.setFileSize(version.getFileSize());
    vo.setFileHash(version.getFileHash());
    // storageUrl is already copied by BeanUtils.copyProperties from version.getStorageUrl()
    return vo;
}
```

Since `storageUrl` is copied via `BeanUtils.copyProperties`, that's handled. For `resourceBaseUrl`, we can set it after the conversion in `getDetail()`:

In `getDetail()` (line 247-283), after the current version is set and the VO returned, we need to set `resourceBaseUrl` on each version VO. Replace the current version building block:

```java
// 当前版本
if (template.getCurrentVersionId() != null) {
    TplTemplateVersion current = versionMapper.selectById(template.getCurrentVersionId());
    if (current != null) {
        vo.setCurrentVersion(convertToVersionVO(current));
    }
}

// 历史版本
List<TplTemplateVersion> versions = versionMapper.selectByTemplateId(templateId);
List<TplTemplateVersionVO> history = new ArrayList<>();
for (TplTemplateVersion v : versions) {
    if (template.getCurrentVersionId() == null || !v.getId().equals(template.getCurrentVersionId())) {
        history.add(convertToVersionVO(v));
    }
}
vo.setHistoryVersions(history);
```

**For resourceBaseUrl**, we need to make the instance method aware of it. Since `convertToVersionVO` is static and we need `resourceBaseUrl` from `ResourceServerService`, we should set it after conversion:

Change the version building in `getDetail()` to:

```java
// 当前版本
if (template.getCurrentVersionId() != null) {
    TplTemplateVersion current = versionMapper.selectById(template.getCurrentVersionId());
    if (current != null) {
        TplTemplateVersionVO currentVo = convertToVersionVO(current);
        currentVo.setResourceBaseUrl(resourceServerService.getBaseUrl());
        vo.setCurrentVersion(currentVo);
    }
}

// 历史版本
List<TplTemplateVersion> versions = versionMapper.selectByTemplateId(templateId);
List<TplTemplateVersionVO> history = new ArrayList<>();
for (TplTemplateVersion v : versions) {
    if (template.getCurrentVersionId() == null || !v.getId().equals(template.getCurrentVersionId())) {
        TplTemplateVersionVO historyVo = convertToVersionVO(v);
        historyVo.setResourceBaseUrl(resourceServerService.getBaseUrl());
        history.add(historyVo);
    }
}
vo.setHistoryVersions(history);
```

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java
git commit -m "feat(template): populate resourceBaseUrl in version VO from ResourceServerService"
```

---

### Task 8: Update download endpoint to 302 redirect

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java`

- [ ] **Change downloadZip to 302 redirect**

Replace the current `downloadZip` method (lines 179-220) with:

```java
/**
 * 下载当前版本的 ZIP（重定向到资源服务器）。
 */
@PreAuthorize("hasAuthority('template:read')")
@GetMapping("/{id}/download")
@Operation(summary = "下载 ZIP", description = "下载模板指定版本的 ZIP 文件，重定向到资源服务器（需有下载权限）")
public ResponseEntity<Void> downloadZip(@PathVariable Long id,
                                         @RequestParam(value = "versionNo", required = false) String versionNo) {
    TplTemplateDetailVO detail = templateService.getDetail(id);
    if (detail == null) {
        return ResponseEntity.notFound().build();
    }

    // 鉴权：付费模板需检查购买权限
    Long userId = extractUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    if (Boolean.TRUE.equals(detail.getIsPaid()) && !userTemplateService.hasAccess(userId, id)) {
        return ResponseEntity.status(403).build();
    }

    // 免费模板自动授权
    if (!Boolean.TRUE.equals(detail.getIsPaid())) {
        userTemplateService.grantFreeAccess(userId, id);
    }

    // 查找版本记录获取 storageUrl
    String targetVersion = versionNo != null ? versionNo
            : (detail.getCurrentVersion() != null ? detail.getCurrentVersion().getVersionNo() : null);
    if (targetVersion == null) {
        return ResponseEntity.notFound().build();
    }

    // 用 MyBatis-Plus 查询版本
    TplTemplateVersion version = versionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TplTemplateVersion>()
                    .eq(TplTemplateVersion::getTemplateId, id)
                    .eq(TplTemplateVersion::getVersionNo, targetVersion));
    if (version == null || version.getStorageUrl() == null) {
        return ResponseEntity.notFound().build();
    }

    // 下载计数 +1
    templateService.incrementDownloadCount(id);

    // 记录模板下载日志
    recordUsageLog(id, detail.getTemplateName(), targetVersion, "download");

    // 302 重定向到资源服务器 URL
    return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
            .header(org.springframework.http.HttpHeaders.LOCATION, version.getStorageUrl())
            .build();
}
```

Remove unused imports (`FileSystemResource`, `Resource`, `File`, `MediaType`) and add any needed ones.

- [ ] **Verify compilation**

Run: `mvn compile -pl aeisp-template -am -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java
git commit -m "feat(template): change download endpoint to 302 redirect to resource server URL"
```

---

### Task 9: Update delete operations to clean up resource server files

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`

- [ ] **Update `deleteTemplate()` to clean resource server files**

After the existing deletion of local files (line 219: `templateStorageService.deleteTemplateFiles(templateId)`), add resource server cleanup:

Since we delete all versions under a template, we need to get all version numbers first and delete each from the resource server. Add this before the local file deletion:

```java
// 清理资源服务器文件
LambdaQueryWrapper<TplTemplateVersion> versionQuery = new LambdaQueryWrapper<>();
versionQuery.eq(TplTemplateVersion::getTemplateId, templateId);
List<TplTemplateVersion> allVersions = versionMapper.selectList(versionQuery);
for (TplTemplateVersion v : allVersions) {
    resourceServerService.deleteVersionFiles(templateId, v.getVersionNo());
}
```

And at the class level, inject `ResourceServerService` (already done in Task 5).

- [ ] **Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java
git commit -m "feat(template): clean up resource server files when deleting template"
```

---

### Task 10: Self-Review and Integration Test

- [ ] **Verify all files compile together**

Run: `mvn compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Run existing tests**

Run: `mvn test -pl aeisp-template -am`
Expected: All existing tests pass

- [ ] **Verify the complete flow by checking file list**

List all changed/created files:
```
aeisp-template/src/main/java/com/aeisp/template/service/ResourceServerService.java                      (new)
aeisp-template/src/main/java/com/aeisp/template/service/impl/NfsResourceServerServiceImpl.java           (new)
aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java              (modified)
aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java                  (modified)
aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVersionVO.java                          (modified)
aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java                        (modified)
aeisp-boot/src/main/resources/application.yml                                                             (modified)
```