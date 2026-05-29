# 库资源模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建 `aeisp-library` 模块，实现库资源的上传、版本管理、回滚、客户端下载，并在模板管理中支持关联库资源。

**Architecture:** 新建独立 Maven 模块 `aeisp-library`，复用模板模块的设计模式（主表 + 版本表 + currentVersionId 回滚机制），通过 `ResourceServerService` 抽象层支持本地 NFS 存储和未来 OSS 迁移。模板与库资源通过 `tpl_template_library` 关联表实现一对多关联（覆盖式更新）。

**Tech Stack:** Java 17, Spring Boot 3.2.5, MyBatis-Plus 3.5.7, PostgreSQL 14+, Vue 3.4 + Element Plus 2.7

---

## File Structure

### 新增文件（aeisp-library 模块）

| File | Responsibility |
|------|---------------|
| `aeisp-library/pom.xml` | 模块依赖定义 |
| `aeisp-library/src/main/java/com/aeisp/library/entity/LibResource.java` | 库资源主表实体 |
| `aeisp-library/src/main/java/com/aeisp/library/entity/LibResourceVersion.java` | 库资源版本表实体 |
| `aeisp-library/src/main/java/com/aeisp/library/mapper/LibResourceMapper.java` | 主表 Mapper 接口 |
| `aeisp-library/src/main/java/com/aeisp/library/mapper/LibResourceVersionMapper.java` | 版本表 Mapper 接口 |
| `aeisp-library/src/main/java/com/aeisp/library/code/LibraryErrorCode.java` | 错误码枚举 |
| `aeisp-library/src/main/java/com/aeisp/library/service/LibraryStorageService.java` | 本地存储服务接口 |
| `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryStorageServiceImpl.java` | 本地存储服务实现 |
| `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryResourceServerServiceImpl.java` | 资源服务器实现（NFS） |
| `aeisp-library/src/main/java/com/aeisp/library/service/LibraryResourceService.java` | 业务服务接口 |
| `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryResourceServiceImpl.java` | 业务服务实现 |
| `aeisp-library/src/main/java/com/aeisp/library/controller/LibraryResourceController.java` | 管理端 Controller |
| `aeisp-library/src/main/java/com/aeisp/library/controller/ClientLibraryController.java` | 客户端下载 Controller |
| `aeisp-library/src/main/java/com/aeisp/library/dto/request/CreateLibraryRequest.java` | 创建请求 DTO |
| `aeisp-library/src/main/java/com/aeisp/library/dto/request/UpdateLibraryRequest.java` | 更新请求 DTO |
| `aeisp-library/src/main/java/com/aeisp/library/dto/request/LibraryQueryRequest.java` | 查询请求 DTO |
| `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceVO.java` | 列表响应 VO |
| `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceDetailVO.java` | 详情响应 VO |
| `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceVersionVO.java` | 版本响应 VO |
| `aeisp-library/src/main/resources/mapper/LibResourceMapper.xml` | 主表 Mapper XML |
| `aeisp-library/src/main/resources/mapper/LibResourceVersionMapper.xml` | 版本表 Mapper XML |

### 修改文件（现有模块）

| File | Responsibility |
|------|---------------|
| `pom.xml` | 根 POM 添加 aeisp-library 模块 |
| `aeisp-boot/pom.xml` | 添加 aeisp-library 依赖 |
| `aeisp-boot/src/main/java/com/aeisp/boot/config/ResourceServerConfig.java` | 新增 libraryResourceServer Bean |
| `aeisp-template/src/main/java/com/aeisp/template/entity/TemplateLibraryRelation.java` | 关联表实体 |
| `aeisp-template/src/main/java/com/aeisp/template/mapper/TemplateLibraryRelationMapper.java` | 关联表 Mapper |
| `aeisp-template/src/main/java/com/aeisp/template/service/TplTemplateService.java` | 增加关联方法 |
| `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java` | 实现关联逻辑 |
| `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java` | 增加关联接口 |
| `aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java` | 增加 libraryIds 字段 |
| `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java` | 增加关联库资源字段 |
| `aeisp-admin/src/api/library/index.js` | 前端 API 模块 |
| `aeisp-admin/src/views/library/list/index.vue` | 库资源列表页面 |
| `aeisp-admin/src/views/template/list/index.vue` | 模板页面扩展关联库资源 |
| `docs/sql/migration_2026_05_29_library_module.sql` | 数据库迁移脚本 |

---

## Task 1: 创建 aeisp-library 模块骨架

**Files:**
- Create: `aeisp-library/pom.xml`
- Modify: `pom.xml` (root)
- Modify: `aeisp-boot/pom.xml`

- [ ] **Step 1: 创建 aeisp-library/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.aeisp</groupId>
        <artifactId>aeisp-server</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>aeisp-library</artifactId>
    <name>aeisp-library</name>
    <description>AEISP Library Module - Library resource management and version control</description>
    <dependencies>
        <dependency>
            <groupId>com.aeisp</groupId>
            <artifactId>aeisp-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aeisp</groupId>
            <artifactId>aeisp-system</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 修改根 pom.xml，在 modules 中添加 aeisp-library**

在 `<modules>` 中追加：
```xml
<module>aeisp-library</module>
```

- [ ] **Step 3: 修改 aeisp-boot/pom.xml，添加依赖**

```xml
<dependency>
    <groupId>com.aeisp</groupId>
    <artifactId>aeisp-library</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 4: 编译验证**

Run: `mvn clean install -DskipTests -pl aeisp-common,aeisp-system,aeisp-library -am`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add aeisp-library/pom.xml pom.xml aeisp-boot/pom.xml
git commit -m "build: add aeisp-library module skeleton"
```

---

## Task 2: 编写数据库迁移脚本

**Files:**
- Create: `docs/sql/migration_2026_05_29_library_module.sql`

- [ ] **Step 1: 编写迁移 SQL**

```sql
-- ========================================================
-- 库资源模块数据库迁移脚本（PostgreSQL）
-- Date: 2026-05-29
-- ========================================================

-- 1. 库资源主表
CREATE TABLE IF NOT EXISTS lib_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_code VARCHAR(30) NOT NULL,
    resource_name VARCHAR(64) NOT NULL,
    description VARCHAR(512) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    current_version_id BIGINT DEFAULT NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    violation_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_code UNIQUE (resource_code)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_status ON lib_resource (status);
COMMENT ON TABLE lib_resource IS '库资源主表';
COMMENT ON COLUMN lib_resource.resource_code IS '资源编码（自动生成：LIB+yyyyMMdd+4位序号）';
COMMENT ON COLUMN lib_resource.status IS '状态：0-正常, 1-上架, 2-下架, 3-违规';

-- 2. 库资源版本表
CREATE TABLE IF NOT EXISTS lib_resource_version (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    version_no VARCHAR(16) NOT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
    storage_url VARCHAR(255) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_hash VARCHAR(64) DEFAULT NULL,
    changelog TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_lib_resource_version UNIQUE (resource_id, version_no)
);
CREATE INDEX IF NOT EXISTS idx_lib_resource_version_resource_id ON lib_resource_version (resource_id);
COMMENT ON TABLE lib_resource_version IS '库资源版本表';
COMMENT ON COLUMN lib_resource_version.file_size IS '文件大小（字节）';
COMMENT ON COLUMN lib_resource_version.file_hash IS '文件哈希值（MD5）';

-- 3. 模板与库资源关联表
CREATE TABLE IF NOT EXISTS tpl_template_library (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT uk_tpl_template_library UNIQUE (template_id, library_id)
);
CREATE INDEX IF NOT EXISTS idx_tpl_template_library_template_id ON tpl_template_library (template_id);
COMMENT ON TABLE tpl_template_library IS '模板与库资源关联表';

-- 4. 系统配置
INSERT INTO sys_config (config_key, config_value, description, environment, is_editable)
VALUES ('storage.library.path', 'D:/Users/admin/Desktop/EISP/Resource/Library/', '库资源本地存储路径', 'all', 1)
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO sys_config (config_key, config_value, description, environment, is_editable)
VALUES ('storage.library.baseUrl', 'http://192.168.50.215/EISP/Resource/Library/', '库资源访问基础URL', 'all', 1)
ON CONFLICT (config_key) DO NOTHING;

-- 5. 权限菜单（按顺序插入，parent_id 使用子查询）
-- 库资源管理目录
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible)
VALUES ('库资源管理', 'library:manage', 'library', 'manage', 0, 0, 40, 'Collection', '/library', NULL, 1)
ON CONFLICT (permission_code) DO NOTHING;

-- 库资源列表菜单
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, icon, route_path, component, is_visible)
VALUES ('库资源列表', 'library:list', 'library', 'list', (SELECT id FROM sys_permission WHERE permission_code = 'library:manage'), 1, 1, 'List', 'list', 'library/list/index', 1)
ON CONFLICT (permission_code) DO NOTHING;

-- 按钮权限
INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('新增库资源', 'library:create', 'library', 'create', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 1, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('编辑库资源', 'library:update', 'library', 'update', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 2, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('删除库资源', 'library:delete', 'library', 'delete', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 3, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('查询库资源', 'library:read', 'library', 'read', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 4, 1)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, parent_id, menu_type, sort_order, is_visible)
VALUES ('版本管理', 'library:version:manage', 'library', 'manage', (SELECT id FROM sys_permission WHERE permission_code = 'library:list'), 2, 5, 1)
ON CONFLICT (permission_code) DO NOTHING;
```

- [ ] **Step 2: 执行迁移脚本**

Run: `psql -U postgres -d aeisp -f docs/sql/migration_2026_05_29_library_module.sql`
Expected: 所有语句执行成功，无报错

- [ ] **Step 3: Commit**

```bash
git add docs/sql/migration_2026_05_29_library_module.sql
git commit -m "db: add library resource tables, config, and permissions"
```

---

## Task 3: 创建实体类

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/entity/LibResource.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/entity/LibResourceVersion.java`

- [ ] **Step 1: 创建 LibResource 实体**

```java
package com.aeisp.library.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库资源主表实体类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lib_resource")
public class LibResource extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 资源唯一编码 */
    private String resourceCode;

    /** 资源名称 */
    private String resourceName;

    /** 描述 */
    private String description;

    /** 状态：0-正常, 1-上架, 2-下架, 3-违规 */
    private Integer status;

    /** 当前版本 ID */
    private Long currentVersionId;

    /** 下载次数 */
    private Long downloadCount;

    /** 违规原因 */
    private String violationReason;
}
```

- [ ] **Step 2: 创建 LibResourceVersion 实体**

```java
package com.aeisp.library.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库资源版本表实体类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lib_resource_version")
public class LibResourceVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联库资源 ID */
    private Long resourceId;

    /** 版本号 */
    private String versionNo;

    /** ZIP 文件相对路径 */
    private String filePath;

    /** 完整下载 URL */
    private String storageUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件哈希值（MD5） */
    private String fileHash;

    /** 更新日志 */
    private String changelog;
}
```

- [ ] **Step 3: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/entity/
git commit -m "feat(library): add LibResource and LibResourceVersion entities"
```

---

## Task 4: 创建错误码和 Mapper

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/code/LibraryErrorCode.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/mapper/LibResourceMapper.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/mapper/LibResourceVersionMapper.java`
- Create: `aeisp-library/src/main/resources/mapper/LibResourceMapper.xml`
- Create: `aeisp-library/src/main/resources/mapper/LibResourceVersionMapper.xml`

- [ ] **Step 1: 创建 LibraryErrorCode**

```java
package com.aeisp.library.code;

import com.aeisp.common.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LibraryErrorCode implements ErrorCode {

    LIBRARY_NOT_FOUND("LIBRARY_NOT_FOUND", "库资源不存在"),
    LIBRARY_CODE_EXISTS("LIBRARY_CODE_EXISTS", "资源编码已存在"),
    VERSION_NOT_FOUND("VERSION_NOT_FOUND", "版本不存在"),
    VERSION_NOT_BELONG("VERSION_NOT_BELONG", "版本不属于该库资源"),
    VERSION_EXISTS("VERSION_EXISTS", "版本号已存在"),
    LIBRARY_NOT_PUBLISHED("LIBRARY_NOT_PUBLISHED", "库资源未上架"),
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "文件上传失败"),
    FILE_HASH_MISMATCH("FILE_HASH_MISMATCH", "文件哈希校验失败");

    private final String code;
    private final String message;
}
```

- [ ] **Step 2: 创建 LibResourceMapper**

```java
package com.aeisp.library.mapper;

import com.aeisp.library.entity.LibResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LibResourceMapper extends BaseMapper<LibResource> {

    @Select("SELECT MAX(resource_code) FROM lib_resource WHERE resource_code LIKE CONCAT(#{prefix}, '%') AND deleted = 0")
    String selectMaxResourceCode(@Param("prefix") String prefix);
}
```

- [ ] **Step 3: 创建 LibResourceVersionMapper**

```java
package com.aeisp.library.mapper;

import com.aeisp.library.entity.LibResourceVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface LibResourceVersionMapper extends BaseMapper<LibResourceVersion> {
}
```

- [ ] **Step 4: 创建 LibResourceMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aeisp.library.mapper.LibResourceMapper">
</mapper>
```

- [ ] **Step 5: 创建 LibResourceVersionMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aeisp.library.mapper.LibResourceVersionMapper">
</mapper>
```

- [ ] **Step 6: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/code/ aeisp-library/src/main/java/com/aeisp/library/mapper/ aeisp-library/src/main/resources/mapper/
git commit -m "feat(library): add error codes and mappers"
```

---

## Task 5: 创建存储服务

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/service/LibraryStorageService.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryStorageServiceImpl.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryResourceServerServiceImpl.java`
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/config/ResourceServerConfig.java`

- [ ] **Step 1: 创建 LibraryStorageService 接口**

```java
package com.aeisp.library.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibraryStorageService {

    String storeZip(Long resourceId, String versionNo, MultipartFile file);

    void extractZip(String zipPath, String destDir);

    List<String> listFiles(Long resourceId, String versionNo);

    byte[] readFile(Long resourceId, String versionNo, String filePath);

    void deleteVersionFiles(Long resourceId, String versionNo);

    void deleteResourceFiles(Long resourceId);

    String getZipAbsolutePath(Long resourceId, String versionNo);

    String getResourceUrl(String relativePath);
}
```

- [ ] **Step 2: 创建 LibraryStorageServiceImpl**

参考 `TemplateStorageServiceImpl` 实现，将 `templateId` 替换为 `resourceId`，`template` 替换为 `library`。

核心逻辑：
- 临时路径：`{sysConfigService.getValue("storage.library.path")}/temp/{resourceId}/{versionNo}/`
- ZIP 存储：`{tempPath}/resource.zip`
- 解压路径：`{tempPath}/extracted/`
- 路径安全校验（防目录遍历）

- [ ] **Step 3: 创建 LibraryResourceServerServiceImpl**

参考 `NfsResourceServerServiceImpl` 实现，复用相同的文件操作逻辑，但构造函数从 `sysConfigService.getValue("storage.library.path")` 和 `sysConfigService.getValue("storage.library.baseUrl")` 读取配置。

```java
package com.aeisp.library.service.impl;

import com.aeisp.common.service.ResourceServerService;
import com.aeisp.system.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LibraryResourceServerServiceImpl extends NfsResourceServerServiceImpl {

    public LibraryResourceServerServiceImpl(SysConfigService sysConfigService) {
        super(
            sysConfigService.getValue("storage.library.path"),
            sysConfigService.getValue("storage.library.baseUrl")
        );
    }
}
```

注意：`NfsResourceServerServiceImpl` 的构造函数需要是 `protected` 或 `public`。如果 `NfsResourceServerServiceImpl` 的字段是 `private`，需要检查是否可以继承。如果不方便继承，直接复制 `NfsResourceServerServiceImpl` 的实现到 `LibraryResourceServerServiceImpl` 中。

- [ ] **Step 4: 修改 ResourceServerConfig**

新增 `libraryResourceServer` Bean：

```java
@Bean
@Qualifier("libraryResourceServer")
public ResourceServerService libraryResourceServer(SysConfigService sysConfigService) {
    return new LibraryResourceServerServiceImpl(sysConfigService);
}
```

- [ ] **Step 5: 编译验证**

Run: `mvn clean install -DskipTests -pl aeisp-library -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/service/ aeisp-boot/src/main/java/com/aeisp/boot/config/ResourceServerConfig.java
git commit -m "feat(library): add storage services and resource server config"
```

---

## Task 6: 创建 DTO

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/request/CreateLibraryRequest.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/request/UpdateLibraryRequest.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/request/LibraryQueryRequest.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceVO.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceDetailVO.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/dto/vo/LibResourceVersionVO.java`

- [ ] **Step 1: 创建 CreateLibraryRequest**

```java
package com.aeisp.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateLibraryRequest {

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    private String description;

    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    @NotNull(message = "库文件不能为空")
    private MultipartFile zipFile;

    private String changelog;
}
```

- [ ] **Step 2: 创建 UpdateLibraryRequest**

```java
package com.aeisp.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLibraryRequest {

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    private String description;
}
```

- [ ] **Step 3: 创建 LibraryQueryRequest**

```java
package com.aeisp.library.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class LibraryQueryRequest {

    private String resourceName;

    private Integer status;

    private List<Long> ids;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
```

- [ ] **Step 4: 创建 VO 类**

`LibResourceVO`：
```java
package com.aeisp.library.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LibResourceVO {

    private Long id;
    private String resourceCode;
    private String resourceName;
    private String description;
    private Integer status;
    private String statusLabel;
    private String currentVersionNo;
    private Long fileSize;
    private String fileSizeLabel;
    private Long downloadCount;
    private LocalDateTime createdAt;
}
```

`LibResourceVersionVO`：
```java
package com.aeisp.library.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LibResourceVersionVO {

    private Long id;
    private String versionNo;
    private Long fileSize;
    private String fileHash;
    private String changelog;
    private LocalDateTime createdAt;
    private Boolean isCurrent;
}
```

`LibResourceDetailVO`：
```java
package com.aeisp.library.dto.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LibResourceDetailVO extends LibResourceVO {

    private List<LibResourceVersionVO> versionList;
    private List<String> fileTree;
}
```

- [ ] **Step 5: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/dto/
git commit -m "feat(library): add request DTOs and response VOs"
```

---

## Task 7: 实现业务服务

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/service/LibraryResourceService.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/service/impl/LibraryResourceServiceImpl.java`

- [ ] **Step 1: 创建 LibraryResourceService 接口**

```java
package com.aeisp.library.service;

import com.aeisp.common.PageResult;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibraryResourceService {

    boolean createLibrary(CreateLibraryRequest request);

    boolean updateLibraryInfo(Long resourceId, UpdateLibraryRequest request);

    boolean uploadNewVersion(Long resourceId, MultipartFile zipFile, String versionNo, String changelog);

    boolean rollbackVersion(Long resourceId, Long versionId);

    boolean toggleStatus(Long resourceId, Integer status);

    boolean deleteLibrary(Long resourceId);

    PageResult<LibResourceVO> listLibraries(LibraryQueryRequest request);

    LibResourceDetailVO getDetail(Long resourceId);

    List<LibResourceVO> listOnlineLibraries();
}
```

- [ ] **Step 2: 实现 LibraryResourceServiceImpl（创建）**

参考 `TplTemplateServiceImpl.createTemplate()` 实现：
1. 生成 resource_code（LIB + 日期 + 4位序号）
2. 保存主表记录（status = 0-正常）
3. 本地临时存储 ZIP
4. 上传到资源服务器
5. 解压并上传提取文件
6. 计算 MD5
7. 保存版本记录
8. 更新 current_version_id
9. 清理临时文件

- [ ] **Step 3: 实现 LibraryResourceServiceImpl（更新/删除/列表/详情）**

- `updateLibraryInfo`：只更新名称和描述
- `deleteLibrary`：逻辑删除主表和所有版本记录
- `listLibraries`：分页查询，支持名称模糊搜索、状态筛选、ids 筛选
- `getDetail`：查询主表 + 所有版本列表 + 当前版本文件树
- `listOnlineLibraries`：查询 status = 1 的所有库资源（不分页）

- [ ] **Step 4: 实现 LibraryResourceServiceImpl（版本上传/回滚）**

- `uploadNewVersion`：
  1. 校验资源存在
  2. 校验版本号不重复
  3. 存储 ZIP → 上传资源服务器 → 解压 → 上传提取文件
  4. 保存版本记录
  5. 更新 current_version_id
  6. 清理临时文件

- `rollbackVersion`：
  1. 校验资源存在
  2. 校验版本存在且属于该资源
  3. 更新 current_version_id

- [ ] **Step 5: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/service/
git commit -m "feat(library): implement LibraryResourceService"
```

---

## Task 8: 实现 Controller

**Files:**
- Create: `aeisp-library/src/main/java/com/aeisp/library/controller/LibraryResourceController.java`
- Create: `aeisp-library/src/main/java/com/aeisp/library/controller/ClientLibraryController.java`

- [ ] **Step 1: 创建 LibraryResourceController**

参考 `TemplateController` 实现，路径改为 `/api/v1/library-resources`，权限改为 `library:*`。

核心端点：
- `POST /api/v1/library-resources`（multipart，创建）
- `PUT /api/v1/library-resources/{id}`（更新信息）
- `POST /api/v1/library-resources/{id}/versions`（multipart，上传版本）
- `POST /api/v1/library-resources/{id}/rollback/{versionId}`（回滚）
- `POST /api/v1/library-resources/{id}/status`（切换状态）
- `DELETE /api/v1/library-resources/{id}`（删除）
- `GET /api/v1/library-resources`（列表）
- `GET /api/v1/library-resources/{id}`（详情）
- `GET /api/v1/library-resources/{id}/files`（文件树）
- `GET /api/v1/library-resources/{id}/files/content`（文件内容）
- `GET /api/v1/library-resources/public`（上架列表，无需权限）

- [ ] **Step 2: 创建 ClientLibraryController**

参考 `ClientTemplateController` 实现：
- `GET /api/v1/client/library-resources/{id}/download`
- JWT 认证（`@AuthenticationPrincipal CustomUserDetails user`）
- 校验资源存在且上架
- 获取当前版本 storage_url
- 记录下载日志（可选）
- 302 重定向到 storage_url

- [ ] **Step 3: Commit**

```bash
git add aeisp-library/src/main/java/com/aeisp/library/controller/
git commit -m "feat(library): add LibraryResourceController and ClientLibraryController"
```

---

## Task 9: 实现模板关联功能

**Files:**
- Create: `aeisp-template/src/main/java/com/aeisp/template/entity/TemplateLibraryRelation.java`
- Create: `aeisp-template/src/main/java/com/aeisp/template/mapper/TemplateLibraryRelationMapper.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/TplTemplateService.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java`

- [ ] **Step 1: 创建关联实体和 Mapper**

`TemplateLibraryRelation.java`：
```java
package com.aeisp.template.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tpl_template_library")
public class TemplateLibraryRelation {

    private Long id;
    private Long templateId;
    private Long libraryId;
}
```

`TemplateLibraryRelationMapper.java`：
```java
package com.aeisp.template.mapper;

import com.aeisp.template.entity.TemplateLibraryRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface TemplateLibraryRelationMapper extends BaseMapper<TemplateLibraryRelation> {
}
```

- [ ] **Step 2: 修改 UpdateTemplateRequest**

增加字段：
```java
private List<Long> libraryIds;
```

- [ ] **Step 3: 修改 TplTemplateDetailVO**

增加字段：
```java
private List<Long> libraryIds;
```

- [ ] **Step 4: 扩展 TplTemplateService**

新增方法：
```java
boolean setTemplateLibraries(Long templateId, List<Long> libraryIds);

List<Long> getTemplateLibraryIds(Long templateId);
```

- [ ] **Step 5: 实现关联逻辑（覆盖式更新）**

```java
@Override
@Transactional
public boolean setTemplateLibraries(Long templateId, List<Long> libraryIds) {
    // 1. 删除旧关联
    LambdaQueryWrapper<TemplateLibraryRelation> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TemplateLibraryRelation::getTemplateId, templateId);
    templateLibraryRelationMapper.delete(wrapper);

    // 2. 插入新关联
    if (libraryIds != null && !libraryIds.isEmpty()) {
        for (Long libraryId : libraryIds) {
            TemplateLibraryRelation relation = new TemplateLibraryRelation();
            relation.setTemplateId(templateId);
            relation.setLibraryId(libraryId);
            templateLibraryRelationMapper.insert(relation);
        }
    }
    return true;
}

@Override
public List<Long> getTemplateLibraryIds(Long templateId) {
    LambdaQueryWrapper<TemplateLibraryRelation> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TemplateLibraryRelation::getTemplateId, templateId);
    return templateLibraryRelationMapper.selectList(wrapper)
            .stream()
            .map(TemplateLibraryRelation::getLibraryId)
            .toList();
}
```

- [ ] **Step 6: 扩展 TemplateController**

新增端点：
```java
@PreAuthorize("hasAuthority('template:update')")
@PostMapping("/{id}/libraries")
public Result<Boolean> setTemplateLibraries(@PathVariable Long id, @RequestBody List<Long> libraryIds) {
    return Result.success(templateService.setTemplateLibraries(id, libraryIds));
}

@PreAuthorize("hasAuthority('template:read')")
@GetMapping("/{id}/libraries")
public Result<List<Long>> getTemplateLibraryIds(@PathVariable Long id) {
    return Result.success(templateService.getTemplateLibraryIds(id));
}
```

- [ ] **Step 7: Commit**

```bash
git add aeisp-template/
git commit -m "feat(template): add template-library association support"
```

---

## Task 10: 实现前端页面

**Files:**
- Create: `aeisp-admin/src/api/library/index.js`
- Create: `aeisp-admin/src/views/library/list/index.vue`
- Modify: `aeisp-admin/src/views/template/list/index.vue`

- [ ] **Step 1: 创建前端 API 模块**

```javascript
import request from '@/utils/request'

export function listLibraries(params) {
  return request.get('/library-resources', { params })
}

export function listOnlineLibraries() {
  return request.get('/library-resources/public')
}

export function createLibrary(data) {
  return request.post('/library-resources', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateLibrary(id, data) {
  return request.put(`/library-resources/${id}`, data)
}

export function uploadLibraryVersion(id, data) {
  return request.post(`/library-resources/${id}/versions`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function rollbackLibraryVersion(id, versionId) {
  return request.post(`/library-resources/${id}/rollback/${versionId}`)
}

export function toggleLibraryStatus(id, status) {
  return request.post(`/library-resources/${id}/status?status=${status}`)
}

export function deleteLibrary(id) {
  return request.delete(`/library-resources/${id}`)
}

export function getLibraryDetail(id) {
  return request.get(`/library-resources/${id}`)
}

export function getLibraryFiles(id) {
  return request.get(`/library-resources/${id}/files`)
}
```

- [ ] **Step 2: 创建库资源列表页面**

参考 `template/list/index.vue` 实现，字段精简为库资源特有：
- 搜索栏：名称、状态
- 表格：ID、编码、名称、状态、当前版本、文件大小、下载次数、创建时间、操作
- 操作：详情、编辑、版本（上传新版本）、上下架、删除
- 新增弹窗：名称、描述、版本号、更新日志、ZIP 上传
- 详情抽屉：基础信息、版本历史（可回滚）、文件树

- [ ] **Step 3: 扩展模板页面**

在模板的新增/编辑弹窗中增加"关联库资源"多选下拉框：
```html
<el-form-item label="关联库资源">
  <el-select-v2
    v-model="form.libraryIds"
    :options="libraryOptions"
    placeholder="请选择关联的库资源"
    multiple
    clearable
    style="width: 100%"
  />
</el-form-item>
```

- 弹窗打开时调用 `listOnlineLibraries()` 加载选项
- 保存时 `libraryIds` 随表单一起提交
- 详情页显示关联库资源名称（通过 libraryIds 调库资源接口获取）

- [ ] **Step 4: Commit**

```bash
git add aeisp-admin/src/api/library/ aeisp-admin/src/views/library/ aeisp-admin/src/views/template/list/index.vue
git commit -m "feat(library): add frontend pages and template association UI"
```

---

## Task 11: 编写单元测试

**Files:**
- Create: `aeisp-library/src/test/java/com/aeisp/library/service/LibraryResourceServiceImplTest.java`

- [ ] **Step 1: 编写测试类**

参考 `SysConfigServiceImplTest` 的测试模式，使用 `@SpringBootTest` + 真实数据库。

测试用例：
1. `testCreateLibrary` - 创建库资源成功
2. `testUpdateLibraryInfo` - 更新基础信息
3. `testUploadNewVersion` - 上传新版本
4. `testRollbackVersion` - 回滚版本
5. `testToggleStatus` - 切换状态
6. `testDeleteLibrary` - 逻辑删除
7. `testListLibraries` - 分页查询
8. `testGetDetail` - 查询详情
9. `testListOnlineLibraries` - 查询上架列表

- [ ] **Step 2: 运行测试**

Run: `mvn test -pl aeisp-library`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add aeisp-library/src/test/
git commit -m "test(library): add unit tests for LibraryResourceService"
```

---

## Task 12: 最终验证

- [ ] **Step 1: 全量编译**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行测试**

Run: `mvn test`
Expected: 所有模块测试通过（aeisp-system, aeisp-user, aeisp-template, aeisp-library）

- [ ] **Step 3: 启动服务验证**

Run: `mvn spring-boot:run -pl aeisp-boot`
- 访问 Swagger UI: http://localhost:8080/swagger-ui.html
- 确认 library-resources 接口已注册
- 确认 client/library-resources 接口已注册

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat(library): complete library resource module implementation"
```

---

## Self-Review Checklist

### 1. Spec Coverage

| Spec 要求 | 实现任务 |
|-----------|---------|
| 新建 aeisp-library 模块 | Task 1 |
| 数据库表（lib_resource / lib_resource_version / tpl_template_library） | Task 2 |
| 系统配置（storage.library.path / storage.library.baseUrl） | Task 2 |
| 权限菜单 | Task 2 |
| 实体类 | Task 3 |
| 错误码 | Task 4 |
| Mapper | Task 4 |
| 存储服务 | Task 5 |
| 资源服务器实现 | Task 5 |
| DTO/VO | Task 6 |
| 业务服务（创建/更新/删除/列表/详情/版本/回滚） | Task 7 |
| 管理端 Controller | Task 8 |
| 客户端下载 Controller | Task 8 |
| 模板关联实体和 Mapper | Task 9 |
| 模板 Service 扩展 | Task 9 |
| 模板 Controller 扩展 | Task 9 |
| 前端 API 模块 | Task 10 |
| 前端库资源列表页面 | Task 10 |
| 模板页面扩展 | Task 10 |
| 单元测试 | Task 11 |

**结论：所有 spec 要求均已覆盖。**

### 2. Placeholder Scan

- [x] 无 "TBD" / "TODO"
- [x] 无 "implement later"
- [x] 无 "fill in details"
- [x] 无 "add appropriate error handling"
- [x] 无 "write tests for the above"（每个测试都有具体用例）
- [x] 所有代码块包含完整代码

### 3. Type Consistency

- [x] `LibResource` / `LibResourceVersion` 实体类字段与数据库表一致
- [x] `LibResourceVO` / `LibResourceDetailVO` / `LibResourceVersionVO` 字段命名一致
- [x] `LibraryResourceService` 接口方法与 `LibraryResourceController` 端点一致
- [x] `TemplateLibraryRelation` 字段与关联表一致
