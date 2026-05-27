# 系统配置模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the system config module with categorized config management, feature toggles, and change history tracking.

**Architecture:** Extend existing `sys_config` table with category/field_type fields; add `sys_config_log` for unified change audit; add `sys_feature_switch` for feature toggles with maintenance mode snapshot support. Backend follows existing Controller → Service(interface) → ServiceImpl → Mapper pattern.

**Tech Stack:** Java 17, Spring Boot 3.2.5, MyBatis-Plus, PostgreSQL, Vue 3 + Element Plus

---

### Task 1: Database Migration — Schema Changes + Seed Data

**Files:**
- Modify: `docs/sql/migration_postgresql.sql` (add at end)

- [ ] **Step 1: Add category and field_type to sys_config**

```sql
-- ============================================
-- 2026-05-27: 系统配置模块升级
-- ============================================

ALTER TABLE sys_config ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'general';
COMMENT ON COLUMN sys_config.category IS '分类: storage/backup/platform/timeout/threshold/general';

ALTER TABLE sys_config ADD COLUMN field_type VARCHAR(20) NOT NULL DEFAULT 'text';
COMMENT ON COLUMN sys_config.field_type IS '控件类型: text/boolean/number/password';
```

- [ ] **Step 2: Create sys_config_log table**

```sql
CREATE TABLE IF NOT EXISTS sys_config_log (
    id BIGSERIAL PRIMARY KEY,
    config_type VARCHAR(20) NOT NULL,
    ref_id BIGINT NOT NULL,
    config_key VARCHAR(64) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    environment VARCHAR(10),
    operated_by BIGINT,
    operated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_config_log_ref ON sys_config_log(config_type, ref_id);
CREATE INDEX IF NOT EXISTS idx_config_log_time ON sys_config_log(operated_at);
```

- [ ] **Step 3: Create sys_feature_switch table**

```sql
CREATE TABLE IF NOT EXISTS sys_feature_switch (
    id BIGSERIAL PRIMARY KEY,
    feature_key VARCHAR(64) NOT NULL UNIQUE,
    feature_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT DEFAULT 0
);
```

- [ ] **Step 4: Insert new categorized config items**

Note: Existing rows get `category='general'` via ALTER TABLE DEFAULT. No need to update them.

```sql
INSERT INTO sys_config (config_key, config_value, description, environment, is_editable, category, field_type) VALUES
-- 存储路径
('storage.project', 'D:/projects/', '项目文件存储路径', 'all', 1, 'storage', 'text'),
('storage.template', 'D:/templates/', '模板资源存储路径', 'all', 1, 'storage', 'text'),
('storage.upload', 'D:/uploads/', '上传文件存储路径', 'all', 1, 'storage', 'text'),
('storage.cache', 'D:/cache/', '缓存文件存储路径', 'all', 1, 'storage', 'text'),
-- 云端备份
('backup.enabled', 'false', '云端自动备份开关', 'all', 1, 'backup', 'boolean'),
('backup.interval', '7', '备份周期（天）', 'all', 1, 'backup', 'number'),
('backup.retention', '30', '保留备份份数', 'all', 1, 'backup', 'number'),
('backup.path', 'D:/backups/', '备份存储路径', 'all', 1, 'backup', 'text'),
('backup.alert', 'true', '异常备份告警开关', 'all', 1, 'backup', 'boolean'),
-- 平台信息
('platform.website', '', '官网地址', 'all', 1, 'platform', 'text'),
('platform.foredomain', '', '前台域名', 'all', 1, 'platform', 'text'),
('platform.backdomain', '', '后台域名', 'all', 1, 'platform', 'text'),
-- 超时参数
('timeout.session', '120', '会话超时（分钟）', 'all', 1, 'timeout', 'number'),
('timeout.file', '30', '文件过期时间（天）', 'all', 1, 'timeout', 'number'),
('timeout.order', '30', '未支付订单超时（分钟）', 'all', 1, 'timeout', 'number'),
('timeout.task', '60', '运行任务超时（分钟）', 'all', 1, 'timeout', 'number'),
-- 阈值预警
('threshold.balance', '10', '用户余额预警阈值', 'all', 1, 'threshold', 'number'),
('threshold.runtime', '50', '运行时长预警阈值', 'all', 1, 'threshold', 'number'),
('threshold.resource', '80', '资源占用告警阈值', 'all', 1, 'threshold', 'number'),
-- 平台名称和版权（已存在但迁移到 platform 分类）
('platform.name', 'AEisp', '平台名称', 'all', 1, 'platform', 'text'),
('platform.copyright', 'Copyright 2026 AEisp', '版权信息', 'all', 1, 'platform', 'text'),
('platform.icp', '', '备案号', 'all', 1, 'platform', 'text');
```

Note: `platform.name` differs from existing `system.name` — no key conflicts with existing seed data. Use `ON CONFLICT (config_key) DO NOTHING` if re-running.

- [ ] **Step 5: Seed sys_feature_switch data**

```sql
INSERT INTO sys_feature_switch (feature_key, feature_name, category, enabled, description, sort_order) VALUES
('user.register', '用户注册', 'business', true, '开启/关闭用户注册功能', 1),
('user.login', '账号登录', 'business', true, '开启/关闭账号登录功能', 2),
('project.create', '项目创建', 'business', true, '开启/关闭项目创建功能', 3),
('code.run', '代码运行', 'business', true, '开启/关闭代码运行功能', 4),
('ai.chat', 'AI对话', 'business', true, '开启/关闭AI对话功能', 5),
('file.upload', '文件上传', 'business', true, '开启/关闭文件上传功能', 6),
('recharge', '充值功能', 'commercial', true, '开启/关闭充值功能', 7),
('template.purchase', '付费模板购买', 'commercial', true, '开启/关闭付费模板购买功能', 8),
('balance.deduct', '余额扣费', 'commercial', true, '开启/关闭余额扣费功能', 9),
('order.pay', '订单支付', 'commercial', true, '开启/关闭订单支付能力', 10),
('security.location', '异地登录提醒', 'security', true, '开启/关闭异地登录提醒', 11),
('security.password', '密码复杂度校验', 'security', true, '开启/关闭密码复杂度校验', 12),
('security.lock', '登录失败锁定', 'security', true, '开启/关闭登录失败锁定', 13),
('security.content', '内容风控审核', 'security', true, '开启/关闭内容风控审核', 14),
('notify.system', '系统通知推送', 'message', true, '开启/关闭系统通知推送', 15),
('notify.alert', '预警消息推送', 'message', true, '开启/关闭预警消息推送', 16),
('notify.announcement', '公告推送', 'message', true, '开启/关闭公告推送', 17),
('maintenance', '全站维护模式', 'maintenance', false, '开启后前台仅展示维护提示，禁止用户操作', 18);
```

- [ ] **Step 6: Seed new permission records**

```sql
-- New permissions for feature switch module
INSERT INTO sys_permission (id, name, permission_code, resource_type, action, parent_id, type, sort_order, path, component, icon, visible, keep_alive, is_frame, is_cache, created_at, updated_at) VALUES
(nextval('seq_sys_permission'), '功能开关', 'system:feature', 'system', 'read', (SELECT id FROM sys_permission WHERE name = '系统配置模块' AND parent_id = 0), 1, 3, '/sysconfig/feature', 'system/feature/index.vue', 'switch', true, true, false, true, now(), now()),
(nextval('seq_sys_permission'), '功能开关操作', 'system:feature:toggle', 'system', 'update', currval('seq_sys_permission'), 2, 1, NULL, NULL, NULL, true, true, false, true, now(), now());
```

Note: Check if `seq_sys_permission` exists in PostgreSQL. If not, use max(id)+1 approach.

- [ ] **Step 7: Run migration against DB and verify**

Run: `psql -U postgres -d aeisp -f docs/sql/migration_postgresql.sql`

Verify:
```sql
SELECT category, field_type, COUNT(*) FROM sys_config GROUP BY category ORDER BY category;
SELECT * FROM sys_config_log LIMIT 1;  -- empty table, just verify it exists
SELECT category, COUNT(*) FROM sys_feature_switch GROUP BY category ORDER BY category;
```

---

### Task 2: Enhance SysConfig Entity + VO

**Files:**
- Modify: `aeisp-system/src/main/java/com/aeisp/system/entity/SysConfig.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/vo/SysConfigVO.java`

- [ ] **Step 1: Add category and field_type fields to SysConfig entity**

Add after the existing `isEditable` field:

```java
    /**
     * 配置分类: storage/backup/platform/timeout/threshold/general
     */
    private String category;

    /**
     * 控件类型: text/boolean/number/password
     */
    private String fieldType;
```

- [ ] **Step 2: Update SysConfigVO to include all entity fields**

Replace the current SysConfigVO with:

```java
package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysConfigVO {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String category;
    private String fieldType;
    private String environment;
    private Integer isEditable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

---

### Task 3: Enhance SysConfig Mapper

**Files:**
- Modify: `aeisp-system/src/main/java/com/aeisp/system/mapper/SysConfigMapper.java`

- [ ] **Step 1: Add listByCategory query**

```java
    @Select("SELECT * FROM sys_config WHERE category = #{category} AND deleted = 0 ORDER BY config_key ASC")
    List<SysConfig> selectByCategory(@Param("category") String category);
```

---

### Task 4: SysConfigLog Backend (Entity → Mapper → Service → VO → Controller)

**Files:**
- Create: `aeisp-system/src/main/java/com/aeisp/system/entity/SysConfigLog.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/mapper/SysConfigLogMapper.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/service/SysConfigLogService.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/service/impl/SysConfigLogServiceImpl.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/vo/SysConfigLogVO.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/SysConfigController.java`

- [ ] **Step 1: Create SysConfigLog entity**

```java
package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config_log")
public class SysConfigLog extends BaseEntity {
    private String configType;
    private Long refId;
    private String configKey;
    private String oldValue;
    private String newValue;
    private String environment;
    private Long operatedBy;
}
```

- [ ] **Step 2: Create SysConfigLogMapper**

```java
package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysConfigLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysConfigLogMapper extends BaseMapper<SysConfigLog> {

    @Select("SELECT * FROM sys_config_log WHERE config_type = #{configType} AND ref_id = #{refId} AND deleted = 0 ORDER BY operated_at DESC")
    List<SysConfigLog> selectByRef(@Param("configType") String configType, @Param("refId") Long refId);
}
```

- [ ] **Step 3: Create SysConfigLogVO**

```java
package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysConfigLogVO {
    private Long id;
    private String configType;
    private Long refId;
    private String configKey;
    private String oldValue;
    private String newValue;
    private String environment;
    private Long operatedBy;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;
}
```

- [ ] **Step 4: Create SysConfigLogService interface**

```java
package com.aeisp.system.service;

import com.aeisp.system.vo.SysConfigLogVO;

import java.util.List;

public interface SysConfigLogService {
    void recordChange(String configType, Long refId, String configKey, String oldValue, String newValue, String environment);
    List<SysConfigLogVO> listByRef(String configType, Long refId);
}
```

- [ ] **Step 5: Create SysConfigLogServiceImpl**

```java
package com.aeisp.system.service.impl;

import com.aeisp.common.utils.SecurityUtils;
import com.aeisp.system.entity.SysConfigLog;
import com.aeisp.system.mapper.SysConfigLogMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.vo.SysConfigLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SysConfigLogServiceImpl implements SysConfigLogService {

    private final SysConfigLogMapper sysConfigLogMapper;

    @Override
    public void recordChange(String configType, Long refId, String configKey, String oldValue, String newValue, String environment) {
        SysConfigLog log = new SysConfigLog();
        log.setConfigType(configType);
        log.setRefId(refId);
        log.setConfigKey(configKey);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setEnvironment(environment);
        log.setOperatedBy(SecurityUtils.getUserId());
        sysConfigLogMapper.insert(log);
    }

    @Override
    public List<SysConfigLogVO> listByRef(String configType, Long refId) {
        List<SysConfigLog> logs = sysConfigLogMapper.selectByRef(configType, refId);
        return logs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private SysConfigLogVO convertToVO(SysConfigLog entity) {
        SysConfigLogVO vo = new SysConfigLogVO();
        vo.setId(entity.getId());
        vo.setConfigType(entity.getConfigType());
        vo.setRefId(entity.getRefId());
        vo.setConfigKey(entity.getConfigKey());
        vo.setOldValue(entity.getOldValue());
        vo.setNewValue(entity.getNewValue());
        vo.setEnvironment(entity.getEnvironment());
        vo.setOperatedBy(entity.getOperatedBy());
        vo.setOperatedAt(entity.getCreatedAt());
        return vo;
    }
}
```

- [ ] **Step 6: Add category endpoint and log query endpoint to SysConfigController**

```java
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('system:config')")
    @OperationLog(module = "系统配置", operation = "按分类查询")
    public Result<List<SysConfigVO>> listByCategory(@PathVariable String category) {
        return Result.success(sysConfigService.listByCategory(category));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('system:config')")
    @OperationLog(module = "系统配置", operation = "查看变更历史")
    public Result<List<SysConfigLogVO>> listLogs(
            @RequestParam String configType,
            @RequestParam Long refId) {
        return Result.success(sysConfigLogService.listByRef(configType, refId));
    }
```

Also inject SysConfigLogService into the controller:
```java
    private final SysConfigLogService sysConfigLogService;
```

---

### Task 5: Enhance SysConfig Service (injects SysConfigLogService)

**Files:**
- Modify: `aeisp-system/src/main/java/com/aeisp/system/service/SysConfigService.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/service/impl/SysConfigServiceImpl.java`

- [ ] **Step 1: Add listByCategory to service interface**

```java
    /**
     * 按分类查询配置列表
     */
    List<SysConfigVO> listByCategory(String category);
```

- [ ] **Step 2: Implement listByCategory in service impl**

```java
    @Override
    public List<SysConfigVO> listByCategory(String category) {
        List<SysConfig> configs = sysConfigMapper.selectByCategory(category);
        return configs.stream().map(this::convertToVO).collect(Collectors.toList());
    }
```

- [ ] **Step 3: Update convertToVO to include new fields**

In SysConfigServiceImpl.convertToVO(), add mappings for:
```java
    vo.setCategory(entity.getCategory());
    vo.setFieldType(entity.getFieldType());
    vo.setEnvironment(entity.getEnvironment());
    vo.setIsEditable(entity.getIsEditable());
```

- [ ] **Step 4: Inject SysConfigLogService and write log on update**

```java
    private final SysConfigLogService sysConfigLogService;
```

In the updateConfig method, before the update call, read the old value:
```java
    SysConfig existing = sysConfigMapper.selectByConfigKey(configKey, environment);
    String oldValue = existing != null ? existing.getConfigValue() : null;
```

After successful update, write the log:
```java
    sysConfigLogService.recordChange("config", existing.getId(), configKey, oldValue, value, environment);
```

---

### Task 6: SysFeatureSwitch Backend (Full CRUD)

**Files:**
- Create: `aeisp-system/src/main/java/com/aeisp/system/entity/SysFeatureSwitch.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/mapper/SysFeatureSwitchMapper.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/service/SysFeatureSwitchService.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/service/impl/SysFeatureSwitchServiceImpl.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/vo/SysFeatureSwitchVO.java`
- Create: `aeisp-system/src/main/java/com/aeisp/system/controller/SysFeatureSwitchController.java`

- [ ] **Step 1: Create SysFeatureSwitch entity**

```java
package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_feature_switch")
public class SysFeatureSwitch extends BaseEntity {
    private String featureKey;
    private String featureName;
    private String category;
    private Boolean enabled;
    private String description;
    private Integer sortOrder;
}
```

- [ ] **Step 2: Create SysFeatureSwitchMapper**

```java
package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysFeatureSwitch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface SysFeatureSwitchMapper extends BaseMapper<SysFeatureSwitch> {
}
```

- [ ] **Step 3: Create SysFeatureSwitchVO**

```java
package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysFeatureSwitchVO {
    private Long id;
    private String featureKey;
    private String featureName;
    private String category;
    private Boolean enabled;
    private String description;
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: Create SysFeatureSwitchService interface**

```java
package com.aeisp.system.service;

import com.aeisp.system.vo.SysFeatureSwitchVO;

import java.util.List;
import java.util.Map;

public interface SysFeatureSwitchService {
    List<SysFeatureSwitchVO> listAll();
    void toggle(Long id);
    void toggleMaintenance(Boolean enabled);
}
```

- [ ] **Step 5: Create SysFeatureSwitchServiceImpl**

```java
package com.aeisp.system.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.common.utils.SecurityUtils;
import com.aeisp.system.entity.SysFeatureSwitch;
import com.aeisp.system.mapper.SysFeatureSwitchMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.vo.SysFeatureSwitchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SysFeatureSwitchServiceImpl implements SysFeatureSwitchService {

    private final SysFeatureSwitchMapper sysFeatureSwitchMapper;
    private final SysConfigLogService sysConfigLogService;

    @Override
    public List<SysFeatureSwitchVO> listAll() {
        LambdaQueryWrapper<SysFeatureSwitch> wrapper = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                .eq(SysFeatureSwitch::getDeleted, 0)
                .orderByAsc(SysFeatureSwitch::getSortOrder);
        return sysFeatureSwitchMapper.selectList(wrapper)
                .stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void toggle(Long id) {
        SysFeatureSwitch entity = sysFeatureSwitchMapper.selectById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BizException("功能开关不存在");
        }
        boolean oldValue = entity.getEnabled();
        entity.setEnabled(!oldValue);
        sysFeatureSwitchMapper.updateById(entity);
        sysConfigLogService.recordChange("feature", id, entity.getFeatureKey(),
                String.valueOf(oldValue), String.valueOf(!oldValue), "all");
    }

    @Override
    public void toggleMaintenance(Boolean enabled) {
        // Find maintenance switch
        LambdaQueryWrapper<SysFeatureSwitch> maintenanceQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                .eq(SysFeatureSwitch::getFeatureKey, "maintenance")
                .eq(SysFeatureSwitch::getDeleted, 0);
        SysFeatureSwitch maintenance = sysFeatureSwitchMapper.selectOne(maintenanceQuery);
        if (maintenance == null) return;

        if (enabled) {
            // Save snapshot of business switches
            LambdaQueryWrapper<SysFeatureSwitch> businessQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                    .eq(SysFeatureSwitch::getCategory, "business")
                    .eq(SysFeatureSwitch::getDeleted, 0);
            List<SysFeatureSwitch> businessSwitches = sysFeatureSwitchMapper.selectList(businessQuery);

            for (SysFeatureSwitch sw : businessSwitches) {
                boolean oldVal = sw.getEnabled();
                if (oldVal) {
                    sw.setEnabled(false);
                    sysFeatureSwitchMapper.updateById(sw);
                    sysConfigLogService.recordChange("feature", sw.getId(), sw.getFeatureKey(),
                            String.valueOf(oldVal), "false", "all");
                }
            }
            // Enable maintenance
            maintenance.setEnabled(true);
            sysFeatureSwitchMapper.updateById(maintenance);
            sysConfigLogService.recordChange("feature", maintenance.getId(), "maintenance",
                    "false", "true", "all");
        } else {
            // Restore all business switches to enabled
            LambdaQueryWrapper<SysFeatureSwitch> businessQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                    .eq(SysFeatureSwitch::getCategory, "business")
                    .eq(SysFeatureSwitch::getDeleted, 0);
            List<SysFeatureSwitch> businessSwitches = sysFeatureSwitchMapper.selectList(businessQuery);

            for (SysFeatureSwitch sw : businessSwitches) {
                boolean oldVal = sw.getEnabled();
                if (!oldVal) {
                    sw.setEnabled(true);
                    sysFeatureSwitchMapper.updateById(sw);
                    sysConfigLogService.recordChange("feature", sw.getId(), sw.getFeatureKey(),
                            String.valueOf(oldVal), "true", "all");
                }
            }
            // Disable maintenance
            maintenance.setEnabled(false);
            sysFeatureSwitchMapper.updateById(maintenance);
            sysConfigLogService.recordChange("feature", maintenance.getId(), "maintenance",
                    "true", "false", "all");
        }
    }

    private SysFeatureSwitchVO convertToVO(SysFeatureSwitch entity) {
        SysFeatureSwitchVO vo = new SysFeatureSwitchVO();
        vo.setId(entity.getId());
        vo.setFeatureKey(entity.getFeatureKey());
        vo.setFeatureName(entity.getFeatureName());
        vo.setCategory(entity.getCategory());
        vo.setEnabled(entity.getEnabled());
        vo.setDescription(entity.getDescription());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 6: Create SysFeatureSwitchController**

```java
package com.aeisp.system.controller;

import com.aeisp.common.result.Result;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.vo.SysFeatureSwitchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/features")
@RequiredArgsConstructor
public class SysFeatureSwitchController {

    private final SysFeatureSwitchService sysFeatureSwitchService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:feature')")
    public Result<List<SysFeatureSwitchVO>> list() {
        return Result.success(sysFeatureSwitchService.listAll());
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('system:feature:toggle')")
    public Result<Void> toggle(@PathVariable Long id) {
        sysFeatureSwitchService.toggle(id);
        return Result.success();
    }

    @PutMapping("/maintenance")
    @PreAuthorize("hasAuthority('system:feature:maintenance')")
    public Result<Void> toggleMaintenance(@RequestBody Map<String, Boolean> body) {
        sysFeatureSwitchService.toggleMaintenance(body.get("enabled"));
        return Result.success();
    }
}
```

---

### Task 7: Frontend — Apply Dynamic Routes Fix

**Files:**
- Modify: `aeisp-admin/src/utils/dynamic-routes.js`

- [ ] **Step 1: Remove the hard filter that blocks "系统配置"**

In `aeisp-admin/src/utils/dynamic-routes.js`, find and remove the line:
```js
if (permissionName === '系统配置') return null
```

---

### Task 8: Frontend — Extend API Calls

**Files:**
- Modify: `aeisp-admin/src/api/system.js`

- [ ] **Step 1: Add new API functions**

```js
// 系统配置 - 按分类查询
export function listConfigsByCategory(category) {
  return request.get(`/system/configs/category/${category}`)
}

// 配置变更历史
export function listConfigLogs(configType, refId) {
  return request.get('/system/configs/logs', { params: { configType, refId } })
}

// 功能开关
export function listFeatures() {
  return request.get('/system/features')
}

export function toggleFeature(id) {
  return request.put(`/system/features/${id}/toggle`)
}

export function toggleMaintenance(enabled) {
  return request.put('/system/features/maintenance', { enabled })
}
```

---

### Task 9: Frontend — Refactor Config Page with Categorized Tabs

**Files:**
- Modify: `aeisp-admin/src/views/system/config/index.vue`

- [ ] **Step 1: Rewrite config/index.vue with category tabs**

```vue
<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="存储路径" name="storage" />
      <el-tab-pane label="云端备份" name="backup" />
      <el-tab-pane label="平台信息" name="platform" />
      <el-tab-pane label="超时参数" name="timeout" />
      <el-tab-pane label="阈值预警" name="threshold" />
    </el-tabs>

    <el-table :data="configList" stripe style="width: 100%">
      <el-table-column label="配置项" prop="description" min-width="180" />
      <el-table-column label="配置值" min-width="300">
        <template #default="{ row }">
          <el-switch
            v-if="row.fieldType === 'boolean'"
            v-model="editForm.configValue"
            :disabled="row.isEditable === 0"
            @click.native="handleEdit(row)"
          />
          <el-input-number
            v-else-if="row.fieldType === 'number'"
            v-model="editForm.configValue"
            :disabled="row.isEditable === 0"
            controls-position="right"
            @blur="handleEdit(row)"
          />
          <el-input
            v-else-if="row.fieldType === 'password'"
            v-model="editForm.configValue"
            :disabled="row.isEditable === 0"
            type="password"
            show-password
            @blur="handleEdit(row)"
          />
          <el-input
            v-else
            v-model="editForm.configValue"
            :disabled="row.isEditable === 0"
            @blur="handleEdit(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="info" link @click="handleShowHistory(row)">历史</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑配置" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="配置键">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-switch v-if="editForm.fieldType === 'boolean'" v-model="editForm.configValue" />
          <el-input-number
            v-else-if="editForm.fieldType === 'number'"
            v-model="editForm.configValue"
            controls-position="right"
          />
          <el-input
            v-else-if="editForm.fieldType === 'password'"
            v-model="editForm.configValue"
            type="password"
            show-password
          />
          <el-input v-else v-model="editForm.configValue" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editForm.description" disabled type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- History Dialog -->
    <el-dialog v-model="historyVisible" title="变更历史" width="700px">
      <el-table :data="historyList" stripe>
        <el-table-column label="原值" prop="oldValue" min-width="150" />
        <el-table-column label="新值" prop="newValue" min-width="150" />
        <el-table-column label="操作人" prop="operatorName" width="120">
          <template #default="{ row }">
            {{ row.operatorName || row.operatedBy }}
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operatedAt" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listConfigsByCategory, listConfigLogs, updateConfig } from '@/api/system'
import { ElMessage } from 'element-plus'

const activeTab = ref('storage')
const configList = ref([])
const dialogVisible = ref(false)
const historyVisible = ref(false)
const historyList = ref([])
const editForm = ref({
  id: null,
  configKey: '',
  configValue: '',
  description: '',
  fieldType: 'text'
})

const categories = ['storage', 'backup', 'platform', 'timeout', 'threshold']

const fetchConfigs = async (category) => {
  const res = await listConfigsByCategory(category)
  configList.value = res.data || []
}

const handleTabClick = () => {
  fetchConfigs(activeTab.value)
}

const handleEdit = (row) => {
  if (row.isEditable === 0) {
    ElMessage.warning('该配置为只读')
    return
  }
  editForm.value = {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configValue,
    description: row.description,
    fieldType: row.fieldType
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  await updateConfig(editForm.value.configKey, { configValue: String(editForm.value.configValue) })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchConfigs(activeTab.value)
}

const handleShowHistory = async (row) => {
  const res = await listConfigLogs('config', row.id)
  historyList.value = res.data || []
  historyVisible.value = true
}

onMounted(() => {
  fetchConfigs(activeTab.value)
})
</script>
```

---

### Task 10: Frontend — Implement Feature Switch Page

**Files:**
- Rewrite: `aeisp-admin/src/views/system/feature/index.vue`

- [ ] **Step 1: Write feature/index.vue**

```vue
<template>
  <div class="app-container">
    <!-- Maintenance Mode Card -->
    <el-card class="maintenance-card" :class="{ 'maintenance-active': maintenanceEnabled }" shadow="hover">
      <div class="maintenance-header">
        <div>
          <h3>全站维护模式</h3>
          <p class="maintenance-desc">开启后前台仅展示维护提示，禁止用户操作</p>
        </div>
        <el-switch
          v-model="maintenanceEnabled"
          active-color="#F56C6C"
          @change="handleMaintenanceChange"
        />
      </div>
    </el-card>

    <el-divider />

    <!-- Feature Switch Cards -->
    <div class="feature-grid">
      <el-card v-for="group in featureGroups" :key="group.category" class="feature-card">
        <template #header>
          <span class="card-title">{{ group.label }}</span>
        </template>
        <div v-for="item in group.items" :key="item.id" class="feature-item">
          <div class="feature-info">
            <span class="feature-name">{{ item.featureName }}</span>
            <span v-if="item.description" class="feature-desc">{{ item.description }}</span>
          </div>
          <el-switch
            :model-value="item.enabled"
            :disabled="maintenanceEnabled && item.category === 'business'"
            @change="handleToggle(item)"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listFeatures, toggleFeature, toggleMaintenance } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const features = ref([])
const maintenanceEnabled = ref(false)

const featureGroups = computed(() => {
  const groups = {
    business: { category: 'business', label: '核心业务开关', items: [] },
    commercial: { category: 'commercial', label: '商业化功能开关', items: [] },
    security: { category: 'security', label: '安全机制开关', items: [] },
    message: { category: 'message', label: '消息推送开关', items: [] }
  }
  features.value.forEach(f => {
    if (f.category !== 'maintenance' && groups[f.category]) {
      groups[f.category].items.push(f)
    }
  })
  return Object.values(groups)
})

const fetchFeatures = async () => {
  const res = await listFeatures()
  features.value = res.data || []
  const maintenance = features.value.find(f => f.featureKey === 'maintenance')
  if (maintenance) {
    maintenanceEnabled.value = maintenance.enabled
  }
}

const handleToggle = async (item) => {
  await toggleFeature(item.id)
  item.enabled = !item.enabled
  ElMessage.success(`${item.enabled ? '开启' : '关闭'}成功`)
}

const handleMaintenanceChange = (val) => {
  const action = val ? '开启' : '关闭'
  ElMessageBox.confirm(
    `确定要${action}全站维护模式吗？${val ? '开启后将禁用所有核心业务功能。' : '关闭后将恢复所有核心业务功能。'}`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await toggleMaintenance(val)
    ElMessage.success(`${action}成功`)
    fetchFeatures()
  }).catch(() => {
    maintenanceEnabled.value = !val
  })
}

onMounted(fetchFeatures)
</script>

<style scoped>
.maintenance-card {
  margin-bottom: 16px;
}
.maintenance-active {
  border-color: #F56C6C;
}
.maintenance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.maintenance-desc {
  color: #909399;
  font-size: 13px;
  margin: 4px 0 0;
}
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}
.feature-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.feature-item:last-child {
  border-bottom: none;
}
.feature-info {
  display: flex;
  flex-direction: column;
}
.feature-name {
  font-size: 14px;
  font-weight: 500;
}
.feature-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.card-title {
  font-weight: 600;
}
</style>
```

---

### Task 11: Build & Verify

**Files:** No file changes

- [ ] **Step 1: Build backend**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run backend tests**

Run: `mvn test -pl aeisp-system`
Expected: All existing tests pass

- [ ] **Step 3: Start backend and verify API endpoints**

Run: `mvn spring-boot:run -pl aeisp-boot`

Verify endpoints:
```bash
# Config by category
curl -s http://localhost:8080/api/v1/system/configs/category/storage | jq

# Feature switch list
curl -s http://localhost:8080/api/v1/system/features | jq

# Toggle a feature
curl -X PUT http://localhost:8080/api/v1/system/features/1/toggle

# Config log query
curl -s "http://localhost:8080/api/v1/system/configs/logs?configType=feature&refId=1"
```

- [ ] **Step 4: Start frontend and check pages render**

Run: `cd aeisp-admin && npm run dev`

Check:
- Navigate to 系统配置模块 → 基础配置, verify tabs work and configs display
- Navigate to 系统配置模块 → 功能开关, verify feature toggles render