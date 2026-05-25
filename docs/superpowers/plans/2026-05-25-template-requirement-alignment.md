# 模板系统功能对齐改造实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将模板系统与 `模板需求.txt` 对齐，包括：统一三级分类（移除 scenario）、补全 difficulty 字段、自动生成 template_code、版本编辑页扩展、有效时间过滤、封面图迁移 NFS、前端分类列展示

**Architecture:** 全栈改动，后端 8 个 Java 文件 + 1 个 Mapper XML，前端 2 个 Vue/JS 文件 + 1 个 API 文件，DB 1 个 migration SQL + 1 个 init SQL 更新

**Tech Stack:** Spring Boot 3.2.5, MyBatis-Plus, Vue 3.4 + Element Plus, PostgreSQL 14

**执行顺序设计考量：** 各任务按"无破坏性优先"排列。scenario 移除（任务 3）涉及文件最多，放在 difficulty 和 template_code 之后，确保先完成简单独立的改动再处理大规模重构。

---

### Task 1: 补全 difficulty 字段到 DTO、VO、前端表单

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/CreateTemplateRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-admin/src/views/template/list/index.vue`
- Test: `aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java`

- [ ] **Step 1: CreateTemplateRequest.java — 添加 difficulty 字段**

在 `private String detailDesc;` 之后，`private MultipartFile zipFile;` 之前添加：
```java
    /**
     * 难度等级。
     */
    private Integer difficulty;
```

- [ ] **Step 2: UpdateTemplateRequest.java — 添加 difficulty 字段**

在 `private String detailDesc;` 之后，`private Integer sortWeight;` 之前添加：
```java
    /**
     * 难度等级。
     */
    private Integer difficulty;
```

- [ ] **Step 3: TplTemplateVO.java — 添加 difficulty 字段**

在 `private String violationReason;` 之前添加：
```java
    /**
     * 难度等级。
     */
    private Integer difficulty;
```

- [ ] **Step 4: TplTemplateDetailVO.java — 添加 difficulty 字段**

在 `private String violationReason;` 之前添加：
```java
    /**
     * 难度等级。
     */
    private Integer difficulty;
```

- [ ] **Step 5: TplTemplateServiceImpl.java — createTemplate 设置 difficulty**

`createTemplate` 方法中，在 `template.setDetailDesc(request.getDetailDesc());` 之后添加：
```java
        template.setDifficulty(request.getDifficulty());
```

`updateTemplateInfo` 方法中，在 `template.setDetailDesc(request.getDetailDesc());` 之后添加：
```java
        template.setDifficulty(request.getDifficulty());
```

- [ ] **Step 6: TplTemplateServiceImplTest.java — 更新测试**

`testCreateTemplate` 方法中，在 `request.setDetailDesc(...)` 后（如果有）添加：
```java
        request.setDifficulty(3);
```

`testUpdateTemplateInfoSuccess` 方法中，在 `request.setScenario("debug")` 后添加：
```java
        request.setDifficulty(3);
```

- [ ] **Step 7: 前端创建弹窗 — 添加难度等级选择器**

在 `list/index.vue` 的创建弹窗表单中，在"详细描述"之后、"分类"之前添加：
```html
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="createForm.difficulty" placeholder="选择难度等级" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
```

createForm reactive 中添加：`difficulty: undefined,`

- [ ] **Step 8: 前端编辑弹窗 — 添加难度等级选择器**

在编辑弹窗中添加（位置同创建弹窗）：
```html
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="editForm.difficulty" placeholder="选择难度等级" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
```

editForm reactive 中添加：`difficulty: undefined,`

handleEdit 函数中添加：`editForm.difficulty = row.difficulty`

handleEditSubmit 请求体中添加：`difficulty: editForm.difficulty,`

handleCreateSubmit FormData 中添加：
```js
  fd.append('difficulty', createForm.difficulty !== undefined ? createForm.difficulty : '')
```

- [ ] **Step 9: 前端详情弹窗 — 显示难度等级**

在详情 descriptions 的"权重"之后添加：
```html
          <el-descriptions-item label="难度等级" :span="1">
            {{ difficultyLabel(currentDetail.difficulty) || '-' }}
          </el-descriptions-item>
```

script 中添加函数：
```js
function difficultyLabel(val) {
  const map = { 1: '入门', 2: '初级', 3: '中级', 4: '高级', 5: '专家' }
  return map[val] || val
}
```

- [ ] **Step 10: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/dto/request/CreateTemplateRequest.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java \
       aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java \
       aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java \
       aeisp-admin/src/views/template/list/index.vue
git commit -m "feat(template): add difficulty field to DTOs, VOs, frontend forms"
```

---

### Task 2: template_code 自动生成

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/mapper/TplTemplateMapper.java`
- Create: `aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml` (modify)

- [ ] **Step 1: TplTemplateMapper.java — 添加查询方法**

添加方法：
```java
    /**
     * 查询当前日期最大的模板编码。
     *
     * @param prefix 编码前缀，如 "TPL20260525"
     * @return 最大的模板编码
     */
    String selectMaxTemplateCode(@Param("prefix") String prefix);
```

- [ ] **Step 2: TplTemplateMapper.xml — 添加 SQL**

```xml
    <select id="selectMaxTemplateCode" resultType="java.lang.String">
        SELECT MAX(template_code)
        FROM tpl_template
        WHERE template_code LIKE CONCAT(#{prefix}, '%')
    </select>
```

- [ ] **Step 3: TplTemplateServiceImpl.java — 生成 template_code**

在 createTemplate 方法中，`template.setTemplateName(request.getTemplateName())` 之后添加：
```java
        // 自动生成 template_code
        template.setTemplateCode(generateTemplateCode());
```

在 TplTemplateServiceImpl 类中添加私有方法：
```java
    /**
     * 自动生成模板编码。
     * 格式：TPL + yyyyMMdd + 4位序号，如 TPL202605250001
     */
    private String generateTemplateCode() {
        String prefix = "TPL" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String maxCode = templateMapper.selectMaxTemplateCode(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() >= 16) {
            try {
                seq = Integer.parseInt(maxCode.substring(12)) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析最大模板编码序号失败: {}", maxCode);
            }
        }
        return prefix + String.format("%04d", seq);
    }
```

- [ ] **Step 4: TplTemplateVO.java — 添加 templateCode 字段（可选，供前端展示）**

在 `private Long id;` 之后添加：
```java
    /**
     * 模板编码。
     */
    private String templateCode;
```

- [ ] **Step 5: TemplateQueryRequest.java — 添加 templateCode 搜索（可选）**

在 `private String templateName;` 之后添加：
```java
    /**
     * 模板编码（精确匹配）。
     */
    private String templateCode;
```

- [ ] **Step 6: TplTemplateServiceImpl.java listTemplates — 添加 templateCode 查询条件**

在 `wrapper.like(StringUtils.hasText(request.getTemplateName())...` 之后添加：
```java
                .eq(StringUtils.hasText(request.getTemplateCode()), TplTemplate::getTemplateCode, request.getTemplateCode())
```

- [ ] **Step 7: 前端列表 — 添加 templateCode 列（可选）**

在列表表头的 ID 列后、模板名称列前添加：
```html
      <el-table-column prop="templateCode" label="编码" width="180" align="center" />
```

- [ ] **Step 8: 前端搜索 — 添加 templateCode 搜索**

搜索表单中添加：
```html
      <el-form-item label="模板编码" prop="templateCode">
        <el-input v-model="queryParams.templateCode" placeholder="编码精确搜索" clearable @keyup.enter="handleQuery" style="width: 180px" />
      </el-form-item>
```

queryParams 中添加：`templateCode: undefined,`

- [ ] **Step 9: 测试验证**

在 `TplTemplateServiceImplTest.java` 的 `testCreateTemplate` 中 verify templateCode is set：
```java
        // 验证 template_code 生成
        verify(templateMapper).insert(argThat(t -> t.getTemplateCode() != null && t.getTemplateCode().startsWith("TPL")));
```

- [ ] **Step 10: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/mapper/TplTemplateMapper.java \
       aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml \
       aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/request/TemplateQueryRequest.java \
       aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java \
       aeisp-admin/src/views/template/list/index.vue
git commit -m "feat(template): auto-generate templateCode with TPL+yyyyMMdd+seq format"
```

---

### Task 3: 移除 scenario 字段，统一三级分类

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/entity/TplTemplate.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/CreateTemplateRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/request/TemplateQueryRequest.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java`
- Modify: `aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml`
- Modify: `aeisp-admin/src/views/template/list/index.vue`
- Test: `aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java`

- [ ] **Step 1: 实体 TplTemplate.java — 移除 scenario**

删除 `private String scenario;` 及其注释块。

- [ ] **Step 2: CreateTemplateRequest.java — 移除 scenario**

删除 `@NotBlank(message = "适用场景不能为空") private String scenario;` 及注释。

- [ ] **Step 3: UpdateTemplateRequest.java — 移除 scenario**

删除 `@NotBlank(message = "适用场景不能为空") private String scenario;` 及注释。

- [ ] **Step 4: TemplateQueryRequest.java — 移除 scenario**

删除 `private String scenario;` 及注释。

- [ ] **Step 5: TplTemplateVO.java — 移除 scenario**

删除 `private String scenario;` 及注释。

- [ ] **Step 6: TplTemplateDetailVO.java — 移除 scenario**

删除 `private String scenario;` 及注释。

- [ ] **Step 7: TplTemplateMapper.xml — 移除 scenario 参数**

`selectOnlineList` 中移除：
```xml
        <if test="scenario != null and scenario != ''">
            AND scenario = #{scenario}
        </if>
```

- [ ] **Step 8: TplTemplateServiceImpl.java — 移除所有 scenario 引用**

`createTemplate` 中删除 `template.setScenario(request.getScenario());`

`updateTemplateInfo` 中删除 `template.setScenario(request.getScenario());`

`listTemplates` 的 wrapper 中删除 `.eq(StringUtils.hasText(request.getScenario()), TplTemplate::getScenario, request.getScenario())`

`listOnlineTemplates` 方法签名从 `listOnlineTemplates(String scenario)` 改为 `listOnlineTemplates()`，删除所有 scenario 参数处理。

`getStatistics` 中删除 scenario 分布统计代码块（从 `Map<String, Long> scenarioMap` 到 `result.put("scenarioDistribution", scenarioList)` 部分）。

`listCategoryTreeWithTemplates` — 将 `templateMapper.selectOnlineList(null)` 改为 `templateMapper.selectOnlineList()`

- [ ] **Step 9: TemplateController.java — 移除 scenario**

`listOnlineTemplates` 方法签名从 `(@RequestParam(value = "scenario", required = false) String scenario)` 改为无参，删除 scenario 参数。

- [ ] **Step 10: TplTemplateMapper.java — 更新 selectOnlineList 接口**

如果方法签名有 `@Param("scenario")` 参数，移除它。

- [ ] **Step 11: 前端 list/index.vue — 移除所有场景相关 UI**

搜索表单：删除场景筛选 el-form-item
```html
      <!-- 删除这整块 -->
      <el-form-item label="场景" prop="scenario">
        <el-select v-model="queryParams.scenario" placeholder="场景类型" clearable style="width: 130px">
          <el-option label="教学" value="teaching" />
          <el-option label="竞赛" value="competition" />
          <el-option label="实训" value="practice" />
        </el-select>
      </el-form-item>
```

列表列：删除场景列
```html
      <!-- 删除这整列 -->
      <el-table-column prop="scenario" label="场景" width="90" align="center">
        ...
      </el-table-column>
```

创建/编辑弹窗：删除场景选择器
```html
        <!-- 删除 -->
        <el-form-item label="场景" prop="scenario">
          <el-select v-model="createForm.scenario" placeholder="请选择场景" style="width: 200px">
            ...
          </el-select>
        </el-form-item>
```

详情弹窗：删除场景显示行
```html
          <!-- 删除 -->
          <el-descriptions-item label="场景" :span="1">
            ...
          </el-descriptions-item>
```

createForm 中删除 `scenario: 'teaching',`
editForm 中删除 `scenario: 'teaching',`
handleCreateSubmit 中删除 `fd.append('scenario', createForm.scenario)`
handleEdit 中删除 `editForm.scenario = row.scenario`
handleEditSubmit 中删除 `scenario: editForm.scenario,`
resetQuery 中删除 `queryParams.scenario = undefined`

- [ ] **Step 12: 更新测试文件**

`testCreateTemplate` 中删除 `request.setScenario("simulation");`

`testUpdateTemplateInfoSuccess` 中删除 `request.setScenario("debug");`

`testListOnlineTemplates` 中将 `when(templateMapper.selectOnlineList("simulation"))` 改为 `when(templateMapper.selectOnlineList())`；将 `tplTemplateService.listOnlineTemplates("simulation")` 改为 `tplTemplateService.listOnlineTemplates()`

- [ ] **Step 13: 更新前端 API (index.js)**

`listTemplates` 调用中可移除 scenario 参数（非必需）。

- [ ] **Step 14: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/entity/TplTemplate.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/request/CreateTemplateRequest.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/request/UpdateTemplateRequest.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/request/TemplateQueryRequest.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java \
       aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateDetailVO.java \
       aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java \
       aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java \
       aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml \
       aeisp-template/src/main/java/com/aeisp/template/mapper/TplTemplateMapper.java \
       aeisp-admin/src/views/template/list/index.vue \
       aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java
git commit -m "refactor(template): remove scenario field, unify 3-level category system"
```

---

### Task 4: 有效时间过滤 — selectOnlineList 补充 valid_time 检查

**Files:**
- Modify: `aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml`

- [ ] **Step 1: Mapper XML — 添加 valid_time 条件**

在 `selectOnlineList` 的 `AND deleted = 0` 之后添加：
```xml
          AND (valid_time IS NULL OR valid_time > NOW())
```

- [ ] **Step 2: 确认 TplTemplateServiceImpl.listOnlineTemplates() 已适配（Task 3 中已无 scenario）**

- [ ] **Step 3: Commit**

```bash
git add aeisp-template/src/main/resources/mapper/TplTemplateMapper.xml
git commit -m "fix(template): filter expired templates by valid_time in selectOnlineList"
```

---

### Task 5: 前端模板列表增加"所属分类"列

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java`
- Modify: `aeisp-admin/src/views/template/list/index.vue`

- [ ] **Step 1: TplTemplateVO.java — 添加 categoryPath 字段**

在 `private String violationReason;` 之后添加：
```java
    /**
     * 分类路径，如 "常用模板-小车类-巡线"。
     */
    private String categoryPath;
```

- [ ] **Step 2: TplTemplateServiceImpl.java — 注入 categoryService 并拼接路径**

检查 `categoryService` 是否已注入（已在第 55 行注入 `private final TplTemplateCategoryService categoryService;`）。

创建工具方法：
```java
    /**
     * 获取模板的分类路径字符串。
     */
    private String buildCategoryPath(Long topId, Long firstId, Long secondId) {
        StringBuilder sb = new StringBuilder();
        try {
            if (topId != null) {
                TplTemplateCategoryVO top = categoryService.getById(topId);
                if (top != null) sb.append(top.getName());
            }
            if (firstId != null) {
                TplTemplateCategoryVO first = categoryService.getById(firstId);
                if (first != null) sb.append("-").append(first.getName());
            }
            if (secondId != null) {
                TplTemplateCategoryVO second = categoryService.getById(secondId);
                if (second != null) sb.append("-").append(second.getName());
            }
        } catch (Exception e) {
            log.warn("构建分类路径失败: template={}-{}-{}", topId, firstId, secondId, e);
        }
        return sb.toString();
    }
```

在 `convertToVO` 方法中，`copyNewFields(template, vo);` 之后添加：
```java
        vo.setCategoryPath(buildCategoryPath(
                template.getTopCategoryId(),
                template.getFirstCategoryId(),
                template.getSecondCategoryId()));
```

- [ ] **Step 3: 前端 list/index.vue — 添加分类列**

在列表的"模板名称"列之后、"状态"列之前添加：
```html
      <el-table-column prop="categoryPath" label="所属分类" min-width="160" show-overflow-tooltip align="center" />
```

- [ ] **Step 4: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/dto/vo/TplTemplateVO.java \
       aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java \
       aeisp-admin/src/views/template/list/index.vue
git commit -m "feat(template): add categoryPath column to template list and VO"
```

---

### Task 6: 版本编辑页扩展（上传新版本时支持更多字段）

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/TplTemplateService.java`
- Modify: `aeisp-admin/src/views/template/list/index.vue`

- [ ] **Step 1: TplTemplateService.java — 扩展 uploadNewVersion 接口**

将原方法：
```java
    boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo, String changelog);
```

改为：
```java
    /**
     * 上传新版本，可选同步更新模板主表字段（onlineTime/validTime/difficulty/isPaid/feeType/price）。
     */
    boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo,
                             String changelog, String onlineTime, String validTime,
                             Integer difficulty, Integer isPaid, String feeType, java.math.BigDecimal price);
```

- [ ] **Step 2: TplTemplateServiceImpl.java — 实现扩展逻辑**

在 uploadNewVersion 方法中，更新版本记录保存后、更新当前版本前，添加模板字段同步逻辑：
```java
        // 同步更新模板主表字段（如果新版本提供了值）
        boolean needUpdate = false;
        if (onlineTime != null && !onlineTime.isBlank()) {
            template.setOnlineTime(parseDateTime(onlineTime));
            needUpdate = true;
        }
        if (validTime != null && !validTime.isBlank()) {
            template.setValidTime(parseDateTime(validTime));
            needUpdate = true;
        }
        if (difficulty != null) {
            template.setDifficulty(difficulty);
            needUpdate = true;
        }
        if (isPaid != null) {
            template.setIsPaid(isPaid);
            needUpdate = true;
        }
        if (feeType != null && !feeType.isBlank()) {
            template.setFeeType(feeType);
            needUpdate = true;
        }
        if (price != null) {
            template.setPrice(price);
            needUpdate = true;
        }
        if (needUpdate) {
            templateMapper.updateById(template);
        }
```

将此段代码放在 `// 5. 保存版本记录` 与 `// 6. 更新当前版本` 之间（即在 `versionMapper.insert(version)` 之后、`template.setCurrentVersionId(version.getId())` 之前）。

- [ ] **Step 3: TemplateController.java — 扩展 uploadNewVersion 参数**

修改方法签名，添加更多 `@RequestParam` 参数：
```java
    @PreAuthorize("hasAuthority('template:version:manage')")
    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传新版本", description = "为指定模板上传新版本，可选同步更新模板字段")
    public Result<Boolean> uploadNewVersion(@PathVariable Long id,
                                            @RequestParam("zipFile") MultipartFile zipFile,
                                            @RequestParam("versionNo") String versionNo,
                                            @RequestParam(value = "changelog", required = false) String changelog,
                                            @RequestParam(value = "onlineTime", required = false) String onlineTime,
                                            @RequestParam(value = "validTime", required = false) String validTime,
                                            @RequestParam(value = "difficulty", required = false) Integer difficulty,
                                            @RequestParam(value = "isPaid", required = false) Integer isPaid,
                                            @RequestParam(value = "feeType", required = false) String feeType,
                                            @RequestParam(value = "price", required = false) java.math.BigDecimal price) {
        return Result.success(templateService.uploadNewVersion(id, zipFile, versionNo, changelog,
                onlineTime, validTime, difficulty, isPaid, feeType, price));
    }
```

- [ ] **Step 4: 前端版本上传弹窗 — 添加字段**

在 `list/index.vue` 版本弹窗中，在"ZIP文件"字段之前添加：
```html
        <el-divider>同步更新模板信息（可选）</el-divider>
        <el-form-item label="上线时间" prop="onlineTime">
          <el-date-picker v-model="versionForm.onlineTime" type="datetime" placeholder="留空不更新" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="有效截止" prop="validTime">
          <el-date-picker v-model="versionForm.validTime" type="datetime" placeholder="留空不更新" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="versionForm.difficulty" placeholder="留空不更新" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否付费" prop="isPaid">
          <el-radio-group v-model="versionForm.isPaid">
            <el-radio :value="0">免费</el-radio>
            <el-radio :value="1">付费</el-radio>
            <el-radio :value="undefined" style="display:none">不更新</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="versionForm.isPaid === 1" label="费用类型" prop="feeType">
          <el-select v-model="versionForm.feeType" placeholder="留空不更新" clearable style="width: 200px">
            <el-option v-for="item in feeTypeOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="versionForm.isPaid === 1" label="价格" prop="price">
          <el-input-number v-model="versionForm.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="ml-2">元</span>
        </el-form-item>
```

versionForm reactive 补充字段：
```js
const versionForm = reactive({
  versionNo: '',
  changelog: '',
  onlineTime: undefined,
  validTime: undefined,
  difficulty: undefined,
  isPaid: undefined,
  feeType: undefined,
  price: undefined
})
```

handleUploadVersion 函数中重置新增字段：
```js
  versionForm.onlineTime = undefined
  versionForm.validTime = undefined
  versionForm.difficulty = undefined
  versionForm.isPaid = undefined
  versionForm.feeType = undefined
  versionForm.price = undefined
```

handleVersionSubmit FormData 中添加：
```js
  fd.append('onlineTime', versionForm.onlineTime || '')
  fd.append('validTime', versionForm.validTime || '')
  if (versionForm.difficulty !== undefined && versionForm.difficulty !== null) {
    fd.append('difficulty', versionForm.difficulty)
  }
  if (versionForm.isPaid !== undefined && versionForm.isPaid !== null) {
    fd.append('isPaid', versionForm.isPaid)
  }
  fd.append('feeType', versionForm.feeType || '')
  if (versionForm.price !== undefined && versionForm.price !== null) {
    fd.append('price', versionForm.price)
  }
```

- [ ] **Step 5: 测试更新 — 扩展 uploadNewVersion 测试**

在 `TplTemplateServiceImplTest.java` 中添加测试：
```java
    @Test
    void testUploadNewVersionWithExtraFields() throws IOException {
        Path tempDir = Files.createTempDirectory("tpl-test-ext-");
        Path tempFile = tempDir.resolve("template.zip");
        Files.write(tempFile, "dummy zip content".getBytes());

        try {
            TplTemplate template = new TplTemplate();
            template.setId(1L);
            template.setCurrentVersionId(10L);
            when(templateMapper.selectById(1L)).thenReturn(template);
            when(templateStorageService.storeZip(anyLong(), anyString(), any())).thenReturn("templates/1/v1.1.0/template.zip");
            when(templateStorageService.getZipAbsolutePath(anyLong(), anyString())).thenReturn(tempFile.toString());
            doNothing().when(templateStorageService).extractZip(anyString(), anyString());
            when(resourceServerService.uploadExtractedFiles(anyLong(), anyString(), any())).thenReturn(List.of());
            when(versionMapper.insert(any(TplTemplateVersion.class))).thenAnswer(inv -> {
                TplTemplateVersion v = inv.getArgument(0);
                v.setId(20L);
                return 1;
            });
            when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

            MockMultipartFile mockFile = new MockMultipartFile("file", "test.zip", "application/zip", "content".getBytes());
            assertTrue(tplTemplateService.uploadNewVersion(1L, mockFile, "v1.1.0", "bug fixes",
                    "2026-06-01T00:00:00", "2026-12-31T23:59:59", 4, 1, "onetime", new java.math.BigDecimal("99.00")));

            assertEquals("v1.1.0", template.getCurrentVersionNo()); // after convertToVO
            // 验证模板字段被更新
            verify(templateMapper, times(2)).updateById(any(TplTemplate.class)); // 1 for needUpdate + 1 for currentVersionId
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }
```

- [ ] **Step 6: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/TplTemplateService.java \
       aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java \
       aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java \
       aeisp-admin/src/views/template/list/index.vue \
       aeisp-template/src/test/java/com/aeisp/template/service/impl/TplTemplateServiceImplTest.java
git commit -m "feat(template): extend version upload with template field sync (onlineTime, validTime, difficulty, isPaid, feeType, price)"
```

---

### Task 7: 封面图上传迁移到资源服务器

**Files:**
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java`

- [ ] **Step 1: TemplateStorageServiceImpl.java — storeCoverImage 改为上传到资源服务器**

将原方法替换为：
```java
    @Override
    public String storeCoverImage(Long templateId, MultipartFile file) {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = System.currentTimeMillis() + ext;
        String relativePath = templateId + "/cover/" + filename;

        try {
            byte[] bytes = file.getBytes();
            return resourceServerService.uploadFile(relativePath, bytes);
        } catch (IOException e) {
            log.error("上传封面图到资源服务器失败: templateId={}", templateId, e);
            throw new RuntimeException("上传封面图失败", e);
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/service/impl/TemplateStorageServiceImpl.java
git commit -m "feat(template): migrate cover image upload to ResourceServerService (NFS/OSS)"
```

---

### Task 8: 更新数据库 SQL 文档

**Files:**
- Modify: `docs/sql/init-postgresql.sql`
- Create: `docs/sql/migration_2026_05_25_template_alignment.sql`

- [ ] **Step 1: 创建迁移 SQL**

```sql
-- ============================================================
-- Migration: 模板系统功能对齐
-- Date: 2026-05-25
-- Description: 移除 scenario，为已有字段添加注释
-- ============================================================

-- 1. 移除 scenario 字段（统一三级分类体系）
ALTER TABLE tpl_template DROP COLUMN IF EXISTS scenario;

-- 2. 补充字段注释
COMMENT ON COLUMN tpl_template.difficulty IS '难度等级：1-入门, 2-初级, 3-中级, 4-高级, 5-专家';
COMMENT ON COLUMN tpl_template.template_code IS '模板编码（自动生成：TPL+yyyyMMdd+4位序号）';
COMMENT ON COLUMN tpl_template.top_category_id IS '顶级分类 ID';
COMMENT ON COLUMN tpl_template.first_category_id IS '一级分类 ID';
COMMENT ON COLUMN tpl_template.second_category_id IS '二级分类 ID';
```

- [ ] **Step 2: 更新 init-postgresql.sql 中的 tpl_template 表定义**

将 tpl_template 的 scenario 列从 DDL 中移除。将补充字段（top_category_id, first_category_id 等）合并到初始 DDL。

找到 `scenario VARCHAR(32) DEFAULT NULL` 行，删除。

确保 tpl_template_category 表 ADD 到 init-postgresql.sql 中（在 tpl_template 表之后）：
```sql
-- 模板分类表
CREATE TABLE IF NOT EXISTS tpl_template_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    level INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0
);
COMMENT ON TABLE tpl_template_category IS '模板三级分类表';
COMMENT ON COLUMN tpl_template_category.name IS '分类名称';
COMMENT ON COLUMN tpl_template_category.parent_id IS '上级分类 ID';
COMMENT ON COLUMN tpl_template_category.level IS '层级：0-顶级, 1-一级, 2-二级';
COMMENT ON COLUMN tpl_template_category.sort_order IS '排序顺序';
```

- [ ] **Step 3: Commit**

```bash
git add docs/sql/migration_2026_05_25_template_alignment.sql \
       docs/sql/init-postgresql.sql
git commit -m "docs: add migration SQL for template alignment, update init SQL"
```

---

### Task 9: 构建验证 + 运行测试

- [ ] **Step 1: Maven 编译**

```bash
mvn clean compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 运行模板模块测试**

```bash
mvn test -pl aeisp-template
```

Expected: All tests pass

- [ ] **Step 3: 全量编译**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 最终确认**

```bash
git log --oneline -10
git status
```