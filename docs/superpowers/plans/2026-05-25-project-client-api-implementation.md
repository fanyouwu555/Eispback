# 项目模块 Unity 客户端 API 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 5 Unity client API endpoints (create/list/upload/update/delete) to `aeisp-project`, with NFS storage via reused `ResourceServerService` abstraction.

**Architecture:** Move `ResourceServerService` interface to `aeisp-common`, refactor `NfsResourceServerServiceImpl` to constructor injection for multi-instance support (template + users), create `ResourceServerConfig` in `aeisp-boot` with two beans, extend `PrjProjectService` with client methods, add `ClientProjectController`.

**Tech Stack:** Java 17, Spring Boot 3.2.5, MyBatis-Plus, PostgreSQL, NFS (HFS), Hutool

---

### Task 1: Move ResourceServerService to common + refactor NFS + CustomUserDetails move

**Files:**
- Create: `aeisp-common/src/main/java/com/aeisp/common/service/ResourceServerService.java`
- Delete: `aeisp-template/src/main/java/com/aeisp/template/service/ResourceServerService.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/NfsResourceServerServiceImpl.java`
- Create: `aeisp-boot/src/main/java/com/aeisp/boot/config/ResourceServerConfig.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java` (lines 5, 39 — import)
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java` (lines 20, 56 — import)
- Modify: `aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java` (line 15 — import)
- Modify: `aeisp-template/src/test/java/com/aeisp/template/service/impl/TemplateStorageServiceImplTest.java` (lines 3, 24, 28 — import + add deleteDirectory mock)
- Move: `aeisp-boot/src/main/java/com/aeisp/boot/security/CustomUserDetails.java` → `aeisp-common/src/main/java/com/aeisp/common/security/CustomUserDetails.java`
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/security/CustomUserDetailsService.java` (import update)
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/security/JwtAuthenticationFilter.java` (import update)
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/security/SecurityConfig.java` (import update)
- Modify: `aeisp-boot/src/main/resources/application.yml` (add users storage config)

- [ ] **Step 1: Create ResourceServerService in common**

Create `aeisp-common/src/main/java/com/aeisp/common/service/ResourceServerService.java`:

```java
package com.aeisp.common.service;

import java.io.File;
import java.util.List;

public interface ResourceServerService {

    String uploadFile(String relativePath, byte[] data);

    List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir);

    void deleteFile(String relativePath);

    void deleteVersionFiles(Long templateId, String versionNo);

    void deleteDirectory(String relativePath);

    String getUrl(String relativePath);

    String getBaseUrl();

    boolean fileExists(String relativePath);
}
```

- [ ] **Step 2: Delete old interface from template**

Delete `aeisp-template/src/main/java/com/aeisp/template/service/ResourceServerService.java`.

- [ ] **Step 3: Refactor NfsResourceServerServiceImpl**

Modify `NfsResourceServerServiceImpl.java`:
- Remove `@Service`, `@ConditionalOnProperty`, all `@Value` annotations
- Change import from `com.aeisp.template.service.ResourceServerService` to `com.aeisp.common.service.ResourceServerService`
- Add constructor: `public NfsResourceServerServiceImpl(String uploadPath, String baseUrl)`
- Replace `@Value("${resource-server.nfs.upload-path}") private String uploadPath;` → `private final String uploadPath;`
- Replace `@Value("${resource-server.nfs.base-url}") private String baseUrl;` → `private final String baseUrl;`

Add new `deleteDirectory` method (after `deleteVersionFiles`):

```java
@Override
public void deleteDirectory(String relativePath) {
    if (!isPathSafe(relativePath)) {
        log.warn("非法目录路径: {}", relativePath);
        throw new SecurityException("非法目录路径");
    }
    String normalizedPath = normalizePath(relativePath);
    String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
    if (!isWithinBaseDir(absolutePath)) {
        log.warn("路径超出允许范围: {}", absolutePath);
        throw new SecurityException("非法目录路径");
    }
    File dir = new File(absolutePath);
    if (dir.exists()) {
        FileUtil.del(dir);
        log.debug("NFS 删除目录完成: {}", absolutePath);
    }
}
```

Final class structure:
- Package: `com.aeisp.template.service.impl` (unchanged)
- Implements `ResourceServerService` (now from common)
- No Spring annotations on the class
- Two `final` fields + constructor
- All methods unchanged except the new `deleteDirectory`

- [ ] **Step 4: Create ResourceServerConfig in aeisp-boot**

Create `aeisp-boot/src/main/java/com/aeisp/boot/config/ResourceServerConfig.java`:

```java
package com.aeisp.boot.config;

import com.aeisp.common.service.ResourceServerService;
import com.aeisp.template.service.impl.NfsResourceServerServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ResourceServerConfig {

    @Primary
    @Bean("templateResourceServer")
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

- [ ] **Step 5: Update imports in template module consumers**

`TemplateStorageServiceImpl.java` line 5:
```java
// Change: import com.aeisp.template.service.ResourceServerService;
// To:     import com.aeisp.common.service.ResourceServerService;
```

`TplTemplateServiceImpl.java` line 20:
```java
// Change: import com.aeisp.template.service.ResourceServerService;
// To:     import com.aeisp.common.service.ResourceServerService;
```

`TplTemplateServiceImplTest.java` line 15:
```java
// Change: import com.aeisp.template.service.ResourceServerService;
// To:     import com.aeisp.common.service.ResourceServerService;
```

`TemplateStorageServiceImplTest.java` line 3:
```java
// Change: import com.aeisp.template.service.ResourceServerService;
// To:     import com.aeisp.common.service.ResourceServerService;
```

- [ ] **Step 6: Add deleteDirectory mock in TemplateStorageServiceImplTest**

In `TemplateStorageServiceImplTest.java`, add the missing `deleteDirectory` method to the anonymous implementation (after `fileExists`):

```java
@Override public void deleteDirectory(String relativePath) { }
```

- [ ] **Step 7: Move CustomUserDetails to aeisp-common**

Create `aeisp-common/src/main/java/com/aeisp/common/security/CustomUserDetails.java` — copy the file from `aeisp-boot/src/main/java/com/aeisp/boot/security/CustomUserDetails.java` with package change to `com.aeisp.common.security`.

Delete `aeisp-boot/src/main/java/com/aeisp/boot/security/CustomUserDetails.java`.

Update imports in aeisp-boot:
- `CustomUserDetailsService.java`: `import com.aeisp.common.security.CustomUserDetails;` (not `com.aeisp.boot.security...`)
- `JwtAuthenticationFilter.java`: same
- `SecurityConfig.java`: (check if it imports CustomUserDetails — if not, no change needed)

- [ ] **Step 8: Add users storage config to application.yml**

Add to `aeisp-boot/src/main/resources/application.yml` (after `resource-server.nfs.base-url` line):

```yaml
  users:
    upload-path: D:/Users/admin/Desktop/EISP/Users/
    base-url: http://192.168.50.215/EISP/Users/
```

Final yaml section:
```yaml
resource-server:
  type: nfs    # nfs or oss
  nfs:
    upload-path: D:/Users/admin/Desktop/EISP/Resource/Template/
    base-url: http://192.168.50.215/EISP/Resource/Template/
  users:
    upload-path: D:/Users/admin/Desktop/EISP/Users/
    base-url: http://192.168.50.215/EISP/Users/
```

- [ ] **Step 9: Build to verify**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS (compile only, tests use real DB)

Run: `mvn test -pl aeisp-template -Dtest=TemplateStorageServiceImplTest`
Expected: All tests pass

---

### Task 2: DB migration + entity + error codes + DTOs

**Files:**
- Create: `docs/sql/migration_2026_05_25_project_resource_url.sql`
- Modify: `aeisp-project/src/main/java/com/aeisp/project/entity/PrjProject.java` (add resourceUrl)
- Modify: `aeisp-project/src/main/java/com/aeisp/project/code/ProjectErrorCode.java` (add error codes)
- Create: `aeisp-project/src/main/java/com/aeisp/project/dto/vo/ClientProjectVO.java`
- Create: `aeisp-project/src/main/java/com/aeisp/project/dto/request/CreateProjectRequest.java`
- Create: `aeisp-project/src/main/java/com/aeisp/project/dto/request/UpdateProjectRequest.java`

- [ ] **Step 1: Create migration SQL**

Create `docs/sql/migration_2026_05_25_project_resource_url.sql`:

```sql
-- 项目模块：为 prj_project 表添加资源存储 URL 字段
ALTER TABLE prj_project ADD COLUMN IF NOT EXISTS resource_url VARCHAR(512) DEFAULT NULL;
COMMENT ON COLUMN prj_project.resource_url IS '项目资源存储URL，如 http://192.168.50.215/EISP/Users/{userId}/{projectId}/';
```

- [ ] **Step 2: Add resourceUrl to PrjProject entity**

Add field to `PrjProject.java`:

```java
private String resourceUrl;
```

Place it after `private String remark;` (before `private LocalDateTime archivedAt;`).

- [ ] **Step 3: Add error codes**

Add to `ProjectErrorCode.java`:

```java
PROJECT_ACCESS_DENIED(8003, "无权操作该项目"),
PROJECT_FILE_INVALID(8004, "项目文件无效");
```

- [ ] **Step 4: Create ClientProjectVO**

Create `aeisp-project/src/main/java/com/aeisp/project/dto/vo/ClientProjectVO.java`:

```java
package com.aeisp.project.dto.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ClientProjectVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;
    private String projectName;
    private Long templateId;
    private String resourceUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 5: Create CreateProjectRequest**

Create `aeisp-project/src/main/java/com/aeisp/project/dto/request/CreateProjectRequest.java`:

```java
package com.aeisp.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    private String projectName;
}
```

- [ ] **Step 6: Create UpdateProjectRequest**

Create `aeisp-project/src/main/java/com/aeisp/project/dto/request/UpdateProjectRequest.java`:

```java
package com.aeisp.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
}
```

---

### Task 3: Service layer — client methods

**Files:**
- Modify: `aeisp-project/src/main/java/com/aeisp/project/service/PrjProjectService.java`
- Modify: `aeisp-project/src/main/java/com/aeisp/project/service/impl/PrjProjectServiceImpl.java`

- [ ] **Step 1: Add client methods to service interface**

Add to `PrjProjectService.java`:

```java
import com.aeisp.project.dto.vo.ClientProjectVO;

// After existing methods, add:
ClientProjectVO createClientProject(Long userId, Long templateId, String projectName);

PageResult<ClientProjectVO> listClientProjects(Long userId, int pageNum, int pageSize);

String uploadProjectZip(Long projectId, Long userId, byte[] zipData);

void updateClientProject(Long projectId, Long userId, String projectName);

void deleteClientProject(Long projectId, Long userId);
```

- [ ] **Step 2: Implement in PrjProjectServiceImpl**

Modify `PrjProjectServiceImpl.java`:

Changes needed:
1. Remove `@RequiredArgsConstructor`, write explicit constructor with `@Qualifier` for userResourceServer
2. Add `ResourceServerService userResourceServer` field
3. Add imports: `ResourceServerService`, `Qualifier`, `ClientProjectVO`, `CreateProjectRequest`, `UpdateProjectRequest`
4. Implement 5 new methods

Full new class content (replace entire file):

```java
package com.aeisp.project.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.project.code.ProjectErrorCode;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.ClientProjectVO;
import com.aeisp.project.dto.vo.PrjProjectVO;
import com.aeisp.project.entity.PrjProject;
import com.aeisp.project.enums.ProjectStatusEnum;
import com.aeisp.project.mapper.PrjProjectMapper;
import com.aeisp.project.service.PrjProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PrjProjectServiceImpl implements PrjProjectService {

    private final PrjProjectMapper projectMapper;
    private final ResourceServerService userResourceServer;

    public PrjProjectServiceImpl(PrjProjectMapper projectMapper,
                                 @Qualifier("userResourceServer") ResourceServerService userResourceServer) {
        this.projectMapper = projectMapper;
        this.userResourceServer = userResourceServer;
    }

    // --- existing admin methods (unchanged) ---

    @Override
    public PageResult<PrjProjectVO> listProjects(ProjectQueryRequest request) {
        Page<PrjProject> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<PrjProject> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(request.getKeyword()), PrjProject::getProjectName, request.getKeyword())
                .eq(request.getUserId() != null, PrjProject::getUserId, request.getUserId())
                .eq(request.getStatus() != null, PrjProject::getStatus, request.getStatus())
                .ge(request.getStartTime() != null, PrjProject::getCreatedAt, request.getStartTime())
                .le(request.getEndTime() != null, PrjProject::getCreatedAt, request.getEndTime())
                .orderByDesc(PrjProject::getIsPinned)
                .orderByDesc(PrjProject::getCreatedAt);

        Page<PrjProject> resultPage = projectMapper.selectPage(page, wrapper);
        List<PrjProjectVO> voList = new ArrayList<>();
        for (PrjProject p : resultPage.getRecords()) {
            voList.add(convertToVO(p));
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public PrjProjectVO getDetail(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return convertToVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveProject(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (Integer.valueOf(2).equals(project.getStatus())) {
            throw new BizException(ProjectErrorCode.PROJECT_ALREADY_ARCHIVED);
        }
        project.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
        project.setArchivedAt(LocalDateTime.now());
        return projectMapper.updateById(project) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return projectMapper.deleteById(id) > 0;
    }

    private PrjProjectVO convertToVO(PrjProject project) {
        PrjProjectVO vo = new PrjProjectVO();
        BeanUtils.copyProperties(project, vo);
        ProjectStatusEnum statusEnum = ProjectStatusEnum.fromCode(project.getStatus());
        vo.setStatusLabel(statusEnum != null ? statusEnum.getDescription() : "未知");
        return vo;
    }

    // --- new client methods ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientProjectVO createClientProject(Long userId, Long templateId, String projectName) {
        PrjProject project = new PrjProject();
        project.setUserId(userId);
        project.setTemplateId(templateId);
        project.setProjectName(StringUtils.hasText(projectName) ? projectName : "未命名项目");
        project.setStatus(ProjectStatusEnum.IN_PROGRESS.getCode());
        projectMapper.insert(project);

        // 生成 resourceUrl: base + {userId}/{projectId}/
        String resourceUrl = userResourceServer.getUrl(userId + "/" + project.getId() + "/");
        project.setResourceUrl(resourceUrl);
        projectMapper.updateById(project);

        return toClientVO(project);
    }

    @Override
    public PageResult<ClientProjectVO> listClientProjects(Long userId, int pageNum, int pageSize) {
        Page<PrjProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PrjProject> wrapper = new LambdaQueryWrapper<PrjProject>()
                .eq(PrjProject::getUserId, userId)
                .orderByDesc(PrjProject::getCreatedAt);

        Page<PrjProject> resultPage = projectMapper.selectPage(page, wrapper);
        List<ClientProjectVO> voList = new ArrayList<>();
        for (PrjProject p : resultPage.getRecords()) {
            voList.add(toClientVO(p));
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public String uploadProjectZip(Long projectId, Long userId, byte[] zipData) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        String relativePath = userId + "/" + projectId + "/project.zip";
        return userResourceServer.uploadFile(relativePath, zipData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClientProject(Long projectId, Long userId, String projectName) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        project.setProjectName(projectName);
        projectMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClientProject(Long projectId, Long userId) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        // 删除 NFS 上的用户项目文件
        String relativePath = userId + "/" + projectId + "/";
        try {
            userResourceServer.deleteDirectory(relativePath);
        } catch (Exception e) {
            log.warn("删除项目文件失败，继续逻辑删除: projectId={}, path={}", projectId, relativePath, e);
        }

        // DB 逻辑删除
        projectMapper.deleteById(projectId);
    }

    private ClientProjectVO toClientVO(PrjProject project) {
        ClientProjectVO vo = new ClientProjectVO();
        BeanUtils.copyProperties(project, vo);
        vo.setProjectId(project.getId());
        return vo;
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

---

### Task 4: Client controller

**Files:**
- Create: `aeisp-project/src/main/java/com/aeisp/project/controller/ClientProjectController.java`

- [ ] **Step 1: Create ClientProjectController**

Create `aeisp-project/src/main/java/com/aeisp/project/controller/ClientProjectController.java`:

```java
package com.aeisp.project.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.project.dto.request.CreateProjectRequest;
import com.aeisp.project.dto.request.UpdateProjectRequest;
import com.aeisp.project.dto.vo.ClientProjectVO;
import com.aeisp.project.service.PrjProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/client/projects")
@RequiredArgsConstructor
@Tag(name = "客户端项目管理", description = "Unity 客户端项目创建、上传、更新、删除")
public class ClientProjectController {

    private final PrjProjectService projectService;

    @PostMapping
    @Operation(summary = "创建项目", description = "用户创建新项目，返回 projectId 和资源 URL")
    public Result<ClientProjectVO> createProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateProjectRequest request) {
        ClientProjectVO vo = projectService.createClientProject(
                user.getUserId(), request.getTemplateId(), request.getProjectName());
        return Result.success(vo);
    }

    @GetMapping
    @Operation(summary = "用户项目列表", description = "获取当前用户的项目列表（分页）")
    public Result<PageResult<ClientProjectVO>> listProjects(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(projectService.listClientProjects(user.getUserId(), page, size));
    }

    @PostMapping("/{projectId}/upload")
    @Operation(summary = "上传项目 ZIP", description = "上传 ZIP 文件到项目的 NFS 存储目录")
    public Result<Map<String, String>> uploadZip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String storageUrl = projectService.uploadProjectZip(
                projectId, user.getUserId(), file.getBytes());
        return Result.success(Map.of("storageUrl", storageUrl));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目名称", description = "更新项目的名称等元信息")
    public Result<Void> updateProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        projectService.updateClientProject(projectId, user.getUserId(), request.getProjectName());
        return Result.success();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目（含文件）", description = "删除项目记录并清理 NFS 上的文件")
    public Result<Void> deleteProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId) {
        projectService.deleteClientProject(projectId, user.getUserId());
        return Result.success();
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

---

### Task 5: Full build + run verification

**Files:** None — verification only

- [ ] **Step 1: Run full build with tests**

Run: `mvn clean install`
Expected: BUILD SUCCESS (all modules compile, all tests pass, including template tests)

- [ ] **Step 2: Execute migration SQL**

```bash
docker exec -i $(docker ps -qf "name=postgres") psql -U postgres -d aeisp < docs/sql/migration_2026_05_25_project_resource_url.sql
```

Expected: `ALTER TABLE` output with no errors

- [ ] **Step 3: Start backend**

```bash
mvn spring-boot:run -pl aeisp-boot
```

Expected: Starts on :8080 with no errors

- [ ] **Step 4: Verify API via curl**

Create a project:
```bash
curl -X POST http://localhost:8080/api/v1/client/projects \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"templateId": 1, "projectName": "测试项目"}'
```

Expected: Response with `projectId` and `resourceUrl` containing `/EISP/Users/`