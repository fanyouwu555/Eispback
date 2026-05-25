# 全系统业务错误码实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将当前所有业务异常从单一 400 码升级为按场景细分的独立错误码体系，使 Unity 客户端可以通过错误码做程序化判断。

**Architecture:** 在 aeisp-common 中定义 ErrorCode 统一接口，各模块按分段实现枚举。BizException 新增支持 ErrorCode 的构造器。GlobalExceptionHandler 和 JwtAuthenticationFilter 改用模块枚举。现有硬编码字符串逐步迁移为枚举引用。

**Tech Stack:** Java 17, Spring Boot 3.2, Maven

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `aeisp-common/src/main/java/com/aeisp/common/code/ErrorCode.java` | 新建 | 统一错误码接口 |
| `aeisp-common/src/main/java/com/aeisp/common/code/CommonErrorCode.java` | 新建 | 通用错误码枚举 (9000-9999) |
| `aeisp-common/src/main/java/com/aeisp/common/exception/BizException.java` | 修改 | 新增 ErrorCode 构造器 |
| `aeisp-common/src/main/java/com/aeisp/common/Result.java` | 修改 | 新增 ErrorCode 重载方法 |
| `aeisp-common/src/main/java/com/aeisp/common/exception/GlobalExceptionHandler.java` | 修改 | 改用 CommonErrorCode |
| `aeisp-common/src/main/java/com/aeisp/common/aspect/RateLimitAspect.java` | 修改 | 改用 CommonErrorCode |
| `aeisp-boot/src/main/java/com/aeisp/boot/code/BootErrorCode.java` | 新建 | 认证错误码枚举 (1000-1999) |
| `aeisp-boot/src/main/java/com/aeisp/boot/security/JwtAuthenticationFilter.java` | 修改 | 改用 BootErrorCode |
| `aeisp-boot/src/main/java/com/aeisp/boot/controller/AuthController.java` | 修改 | 改用 BootErrorCode/CommonErrorCode |
| `aeisp-system/src/main/java/com/aeisp/system/code/SystemErrorCode.java` | 新建 | 系统错误码枚举 (2000-2999) |
| `aeisp-user/src/main/java/com/aeisp/user/code/UserErrorCode.java` | 新建 | 用户错误码枚举 (3000-3999) |
| `aeisp-template/src/main/java/com/aeisp/template/code/TemplateErrorCode.java` | 新建 | 模板错误码枚举 (4000-4999) |
| `aeisp-message/src/main/java/com/aeisp/message/code/MessageErrorCode.java` | 新建 | 消息错误码枚举 (5000-5999) |
| `aeisp-model/src/main/java/com/aeisp/model/code/ModelErrorCode.java` | 新建 | 模型错误码枚举 (6000-6999) |
| `aeisp-recharge/src/main/java/com/aeisp/recharge/code/RechargeErrorCode.java` | 新建 | 充值错误码枚举 (7000-7999) |
| `aeisp-project/src/main/java/com/aeisp/project/code/ProjectErrorCode.java` | 新建 | 项目错误码枚举 (8000-8999) |
| 各模块 ServiceImpl / Controller | 修改 | 硬编码异常迁移为枚举 |

---

### Task 1: ErrorCode 接口与通用枚举

**Files:**
- Create: `aeisp-common/src/main/java/com/aeisp/common/code/ErrorCode.java`
- Create: `aeisp-common/src/main/java/com/aeisp/common/code/CommonErrorCode.java`
- Modify: `aeisp-common/src/main/java/com/aeisp/common/exception/BizException.java`
- Modify: `aeisp-common/src/main/java/com/aeisp/common/Result.java`

- [ ] **Step 1: 新建 ErrorCode 接口**

```java
package com.aeisp.common.code;

public interface ErrorCode {
    int getCode();
    String getMessage();
}
```

- [ ] **Step 2: 新建 CommonErrorCode 枚举**

```java
package com.aeisp.common.code;

public enum CommonErrorCode implements ErrorCode {
    PARAM_MISSING(9001, "请求参数缺失"),
    PARAM_VALIDATION_FAILED(9002, "参数校验失败"),
    PARAM_BIND_FAILED(9003, "参数绑定失败"),
    RESOURCE_NOT_FOUND(9004, "请求资源不存在"),
    METHOD_NOT_ALLOWED(9005, "请求方法不支持"),
    ACCESS_DENIED(9006, "权限不足，无法访问该资源"),
    RATE_LIMITED(9007, "请求过于频繁"),
    SYSTEM_ERROR(9998, "系统繁忙，请稍后重试"),
    UNKNOWN_ERROR(9999, "未知错误");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 3: 改造 BizException 支持 ErrorCode**

在 `aeisp-common/src/main/java/com/aeisp/common/exception/BizException.java` 中新增两个构造器（保留原有构造器）：

```java
    public BizException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
```

并添加 import：
```java
import com.aeisp.common.code.ErrorCode;
```

- [ ] **Step 4: 改造 Result 支持 ErrorCode**

在 `aeisp-common/src/main/java/com/aeisp/common/Result.java` 中新增两个静态方法（保留原有方法）：

```java
    public static <T> Result<T> error(ErrorCode errorCode) {
        return Result.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return Result.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
```

并添加 import：
```java
import com.aeisp.common.code.ErrorCode;
```

- [ ] **Step 5: 编译验证 aeisp-common**

Run: `mvn clean install -pl aeisp-common`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add aeisp-common/src/main/java/com/aeisp/common/code/ErrorCode.java
git add aeisp-common/src/main/java/com/aeisp/common/code/CommonErrorCode.java
git add aeisp-common/src/main/java/com/aeisp/common/exception/BizException.java
git add aeisp-common/src/main/java/com/aeisp/common/Result.java
git commit -m "feat(common): add ErrorCode interface, CommonErrorCode enum, and ErrorCode-aware BizException/Result"
```

---

### Task 2: aeisp-boot 认证错误码与过滤器改造

**Files:**
- Create: `aeisp-boot/src/main/java/com/aeisp/boot/code/BootErrorCode.java`
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/security/JwtAuthenticationFilter.java`
- Modify: `aeisp-boot/src/main/java/com/aeisp/boot/controller/AuthController.java`

- [ ] **Step 1: 新建 BootErrorCode 枚举**

```java
package com.aeisp.boot.code;

import com.aeisp.common.code.ErrorCode;

public enum BootErrorCode implements ErrorCode {
    USER_NOT_FOUND(1001, "账号不存在"),
    PASSWORD_MISMATCH(1002, "密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_FROZEN(1004, "账号已被冻结"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    ACCOUNT_RESET(1006, "账号密码已重置，请重新登录"),
    LOGIN_LOCKED_BY_RETRY(1007, "密码错误次数过多，账号已锁定"),
    ACCOUNT_LOGIN_ELSEWHERE(1008, "账号已在其他设备登录"),
    TOKEN_EXPIRED(1011, "登录已过期，请重新登录"),
    TOKEN_INVALID(1012, "Token 已失效，请重新登录"),
    TOKEN_TYPE_INVALID(1013, "无效的 Token 类型"),
    TOKEN_MISSING(1014, "未登录或 Token 已过期"),
    AUTH_FAILED(1050, "认证失败");

    private final int code;
    private final String message;

    BootErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 改造 JwtAuthenticationFilter**

修改 `aeisp-boot/src/main/java/com/aeisp/boot/security/JwtAuthenticationFilter.java`：

添加 import：
```java
import com.aeisp.boot.code.BootErrorCode;
```

将 `writeUnauthorized` 方法签名改为接收 `ErrorCode`：
```java
    private void writeUnauthorized(HttpServletResponse response, BootErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
```

将所有 `writeUnauthorized(response, "...")` 调用替换为对应的枚举：
- `writeUnauthorized(response, "Token 已失效，请重新登录")` → `writeUnauthorized(response, BootErrorCode.TOKEN_INVALID)`
- `writeUnauthorized(response, "无效的 Token 类型")` → `writeUnauthorized(response, BootErrorCode.TOKEN_TYPE_INVALID)`
- `writeUnauthorized(response, "账号密码已重置，请重新登录")` → `writeUnauthorized(response, BootErrorCode.ACCOUNT_RESET)`
- `writeUnauthorized(response, "账号已被" + label)` → 保留动态字符串，改为 `Result.error(BootErrorCode.ACCOUNT_DISABLED.getCode(), "账号已被" + label)` 或新建一个辅助方法。为保持简单，暂时保留 `Result.error(int, String)` 用法，但改用枚举的 code：
  ```java
  Result<Void> result = Result.error(BootErrorCode.ACCOUNT_DISABLED.getCode(), "账号已被" + label);
  ```
- `writeUnauthorized(response, "未登录或 Token 已过期")` → `writeUnauthorized(response, BootErrorCode.TOKEN_MISSING)`

- [ ] **Step 3: 改造 AuthController 登录/刷新/信息接口**

修改 `aeisp-boot/src/main/java/com/aeisp/boot/controller/AuthController.java`：

添加 import：
```java
import com.aeisp.boot.code.BootErrorCode;
import com.aeisp.common.code.CommonErrorCode;
```

将以下 `Result.error` 调用替换：
- `Result.error(ResultCode.UNAUTHORIZED, "用户名或密码错误")` → `Result.error(BootErrorCode.PASSWORD_MISMATCH)`
- `Result.error(ResultCode.UNAUTHORIZED, "账号已禁用")` → `Result.error(BootErrorCode.ACCOUNT_DISABLED)`
- `Result.error(ResultCode.INTERNAL_ERROR, "登录失败，请稍后重试")` → `Result.error(CommonErrorCode.SYSTEM_ERROR, "登录失败，请稍后重试")`
- `Result.error(ResultCode.UNAUTHORIZED, "Refresh Token 已失效")` → `Result.error(BootErrorCode.TOKEN_INVALID)`
- `Result.error(ResultCode.UNAUTHORIZED, "无效的 Refresh Token")` → `Result.error(BootErrorCode.TOKEN_TYPE_INVALID)`
- `Result.error(ResultCode.UNAUTHORIZED, "Refresh Token 已过期或无效")` → `Result.error(BootErrorCode.TOKEN_MISSING)`
- `Result.error(ResultCode.UNAUTHORIZED, "Token 不能为空")` → `Result.error(BootErrorCode.TOKEN_MISSING)`
- `Result.error(ResultCode.UNAUTHORIZED, "Token 已过期或无效")` → `Result.error(BootErrorCode.TOKEN_INVALID)`

- [ ] **Step 4: 编译验证 aeisp-boot**

Run: `mvn clean install -pl aeisp-boot -am`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add aeisp-boot/src/main/java/com/aeisp/boot/code/BootErrorCode.java
git add aeisp-boot/src/main/java/com/aeisp/boot/security/JwtAuthenticationFilter.java
git add aeisp-boot/src/main/java/com/aeisp/boot/controller/AuthController.java
git commit -m "feat(boot): add BootErrorCode and adapt JwtAuthenticationFilter + AuthController"
```

---

### Task 3: 全局异常处理器与限流改造

**Files:**
- Modify: `aeisp-common/src/main/java/com/aeisp/common/exception/GlobalExceptionHandler.java`
- Modify: `aeisp-common/src/main/java/com/aeisp/common/aspect/RateLimitAspect.java`

- [ ] **Step 1: 改造 GlobalExceptionHandler**

修改 `aeisp-common/src/main/java/com/aeisp/common/exception/GlobalExceptionHandler.java`：

添加 import：
```java
import com.aeisp.common.code.CommonErrorCode;
```

替换以下 return 语句：
- `Result.error(ResultCode.BAD_REQUEST, message)`（MethodArgumentNotValidException）→ `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message)`
- `Result.error(ResultCode.BAD_REQUEST, message)`（ConstraintViolationException）→ `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message)`
- `Result.error(ResultCode.BAD_REQUEST, message)`（BindException）→ `Result.error(CommonErrorCode.PARAM_BIND_FAILED, message)`
- `Result.error(ResultCode.FORBIDDEN, "权限不足，无法访问该资源")`（AccessDeniedException）→ `Result.error(CommonErrorCode.ACCESS_DENIED)`
- `Result.error(ResultCode.BAD_REQUEST, "请求参数缺失: " + e.getParameterName())`（MissingServletRequestParameterException）→ `Result.error(CommonErrorCode.PARAM_MISSING, "请求参数缺失: " + e.getParameterName())`
- `Result.error(ResultCode.NOT_FOUND, "请求资源不存在")`（NoResourceFoundException）→ `Result.error(CommonErrorCode.RESOURCE_NOT_FOUND)`
- `Result.error(ResultCode.METHOD_NOT_ALLOWED, "请求方法不支持: " + e.getMethod())`（HttpRequestMethodNotSupportedException）→ `Result.error(CommonErrorCode.METHOD_NOT_ALLOWED, "请求方法不支持: " + e.getMethod())`
- `Result.error(ResultCode.INTERNAL_ERROR, "系统繁忙，请稍后重试")`（Exception）→ `Result.error(CommonErrorCode.SYSTEM_ERROR)`
- `Result.error(ResultCode.INTERNAL_ERROR, e.getMessage())`（SysException）→ `Result.error(CommonErrorCode.SYSTEM_ERROR, e.getMessage())`

- [ ] **Step 2: 改造 RateLimitAspect**

修改 `aeisp-common/src/main/java/com/aeisp/common/aspect/RateLimitAspect.java`：

添加 import：
```java
import com.aeisp.common.code.CommonErrorCode;
```

替换限流抛出异常：
```java
throw new BizException(CommonErrorCode.RATE_LIMITED, rateLimit.message());
```

- [ ] **Step 3: 编译验证 aeisp-common**

Run: `mvn clean install -pl aeisp-common`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-common/src/main/java/com/aeisp/common/exception/GlobalExceptionHandler.java
git add aeisp-common/src/main/java/com/aeisp/common/aspect/RateLimitAspect.java
git commit -m "feat(common): adapt GlobalExceptionHandler and RateLimitAspect to CommonErrorCode"
```

---

### Task 4: aeisp-system 模块错误码

**Files:**
- Create: `aeisp-system/src/main/java/com/aeisp/system/code/SystemErrorCode.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/service/impl/SysUserServiceImpl.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/service/impl/SysRoleServiceImpl.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/SysUserController.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/SysRoleController.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/SysMenuController.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/DictDataController.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/DictTypeController.java`
- Modify: `aeisp-system/src/main/java/com/aeisp/system/controller/SysConfigController.java`

- [ ] **Step 1: 新建 SystemErrorCode 枚举**

```java
package com.aeisp.system.code;

import com.aeisp.common.code.ErrorCode;

public enum SystemErrorCode implements ErrorCode {
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_CANNOT_DEMOTE_SUPER(2002, "系统中至少保留一个超级管理员，无法降级"),
    USER_CANNOT_DELETE_LAST_SUPER(2003, "系统中至少保留一个超级管理员，无法删除"),
    ROLE_NOT_FOUND(2011, "角色不存在"),
    MENU_NOT_FOUND(2012, "菜单不存在"),
    DICT_TYPE_NOT_FOUND(2021, "字典类型不存在"),
    DICT_DATA_NOT_FOUND(2022, "字典数据不存在"),
    CONFIG_NOT_FOUND(2031, "配置项不存在");

    private final int code;
    private final String message;

    SystemErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 system 模块硬编码异常**

在 `aeisp-system` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("用户不存在")` | `new BizException(SystemErrorCode.USER_NOT_FOUND)` |
| `new BizException("角色不存在")` | `new BizException(SystemErrorCode.ROLE_NOT_FOUND)` |
| `new BizException("菜单不存在")` | `new BizException(SystemErrorCode.MENU_NOT_FOUND)` |
| `new BizException("字典类型不存在")` | `new BizException(SystemErrorCode.DICT_TYPE_NOT_FOUND)` |
| `new BizException("字典数据不存在")` | `new BizException(SystemErrorCode.DICT_DATA_NOT_FOUND)` |
| `new BizException("配置项不存在")` | `new BizException(SystemErrorCode.CONFIG_NOT_FOUND)` |
| `new BizException("系统中至少保留一个超级管理员，无法降级")` | `new BizException(SystemErrorCode.USER_CANNOT_DEMOTE_SUPER)` |
| `new BizException("系统中至少保留一个超级管理员，无法删除")` | `new BizException(SystemErrorCode.USER_CANNOT_DELETE_LAST_SUPER)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |
| `Result.error(ResultCode.NOT_FOUND, ...)` | `Result.error(CommonErrorCode.RESOURCE_NOT_FOUND, ...)` |

- [ ] **Step 3: 编译验证 aeisp-system**

Run: `mvn clean install -pl aeisp-system -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-system/src/main/java/com/aeisp/system/code/SystemErrorCode.java
git add aeisp-system/src/main/java/com/aeisp/system/
git commit -m "feat(system): add SystemErrorCode and migrate system module exceptions"
```

---

### Task 5: aeisp-user 模块错误码

**Files:**
- Create: `aeisp-user/src/main/java/com/aeisp/user/code/UserErrorCode.java`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/service/impl/UsrUserServiceImpl.java`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/controller/UserController.java`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/controller/UserPermissionController.java`
- Modify: `aeisp-user/src/main/java/com/aeisp/user/controller/AuthController.java`

- [ ] **Step 1: 新建 UserErrorCode 枚举**

```java
package com.aeisp.user.code;

import com.aeisp.common.code.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(3001, "用户不存在"),
    USERNAME_EXISTS(3002, "用户名已注册"),
    PHONE_EXISTS(3003, "手机号已注册"),
    EMAIL_EXISTS(3004, "邮箱已注册"),
    VERIFICATION_CODE_ERROR(3005, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(3006, "验证码已过期"),
    USER_STATUS_ABNORMAL(3007, "用户状态异常"),
    FIRST_RECHARGE_REQUIRED(3008, "您尚未完成首次充值，请先充值"),
    COMPETITION_ACCOUNT(3009, "比赛专用账号，正在跳转比赛环境");

    private final int code;
    private final String message;

    UserErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 user 模块硬编码异常**

在 `aeisp-user` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("用户不存在")` | `new BizException(UserErrorCode.USER_NOT_FOUND)` |
| `new BizException("用户名已注册")` | `new BizException(UserErrorCode.USERNAME_EXISTS)` |
| `new BizException("手机号已注册")` | `new BizException(UserErrorCode.PHONE_EXISTS)` |
| `new BizException("邮箱已注册")` | `new BizException(UserErrorCode.EMAIL_EXISTS)` |
| `new BizException("验证码错误")` | `new BizException(UserErrorCode.VERIFICATION_CODE_ERROR)` |
| `new BizException("验证码已过期")` | `new BizException(UserErrorCode.VERIFICATION_CODE_EXPIRED)` |
| `new BizException("用户状态异常")` | `new BizException(UserErrorCode.USER_STATUS_ABNORMAL)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-user**

Run: `mvn clean install -pl aeisp-user -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-user/src/main/java/com/aeisp/user/code/UserErrorCode.java
git add aeisp-user/src/main/java/com/aeisp/user/
git commit -m "feat(user): add UserErrorCode and migrate user module exceptions"
```

---

### Task 6: aeisp-template 模块错误码

**Files:**
- Create: `aeisp-template/src/main/java/com/aeisp/template/code/TemplateErrorCode.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateVersionServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/UserTemplateServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/service/impl/TplTemplateCategoryServiceImpl.java`
- Modify: `aeisp-template/src/main/java/com/aeisp/template/controller/TemplateController.java`

- [ ] **Step 1: 新建 TemplateErrorCode 枚举**

```java
package com.aeisp.template.code;

import com.aeisp.common.code.ErrorCode;

public enum TemplateErrorCode implements ErrorCode {
    TEMPLATE_NOT_FOUND(4001, "模板不存在"),
    TEMPLATE_NOT_PUBLISHED(4002, "模板未上线"),
    TEMPLATE_VERSION_EXISTS(4003, "版本号已存在"),
    TEMPLATE_ALREADY_OWNED(4004, "您已拥有该模板，无需重复购买"),
    TEMPLATE_PRICE_INVALID(4005, "模板价格异常"),
    TEMPLATE_IS_FREE(4006, "该模板为免费模板，无需购买"),
    VERSION_NOT_FOUND(4011, "版本不存在"),
    VERSION_NOT_BELONG(4012, "版本不存在或不属于该模板"),
    VERSION_IN_USE(4013, "不能删除当前正在使用的版本"),
    VERSION_NUMBER_INVALID(4014, "非法版本号"),
    CATEGORY_NOT_FOUND(4021, "分类不存在"),
    CATEGORY_PARENT_NOT_FOUND(4022, "上级分类不存在"),
    CATEGORY_MAX_LEVEL(4023, "最多支持三级分类"),
    CATEGORY_HAS_CHILDREN(4024, "存在子分类，无法删除"),
    FILE_PATH_INVALID(4031, "非法文件路径"),
    FILE_SAVE_FAILED(4032, "存储 ZIP 文件失败"),
    FILE_READ_FAILED(4033, "读取文件失败");

    private final int code;
    private final String message;

    TemplateErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 template 模块硬编码异常**

在 `aeisp-template` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("模板不存在")` | `new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND)` |
| `new BizException("模板未上线")` | `new BizException(TemplateErrorCode.TEMPLATE_NOT_PUBLISHED)` |
| `new BizException("版本号已存在")` | `new BizException(TemplateErrorCode.TEMPLATE_VERSION_EXISTS)` |
| `new BizException("您已拥有该模板，无需重复购买")` | `new BizException(TemplateErrorCode.TEMPLATE_ALREADY_OWNED)` |
| `new BizException("模板价格异常")` | `new BizException(TemplateErrorCode.TEMPLATE_PRICE_INVALID)` |
| `new BizException("该模板为免费模板，无需购买")` | `new BizException(TemplateErrorCode.TEMPLATE_IS_FREE)` |
| `new BizException("版本不存在")` | `new BizException(TemplateErrorCode.VERSION_NOT_FOUND)` |
| `new BizException("版本不存在或不属于该模板")` | `new BizException(TemplateErrorCode.VERSION_NOT_BELONG)` |
| `new BizException("不能删除当前正在使用的版本")` | `new BizException(TemplateErrorCode.VERSION_IN_USE)` |
| `new BizException("非法版本号")` | `new BizException(TemplateErrorCode.VERSION_NUMBER_INVALID)` |
| `new BizException("分类不存在")` | `new BizException(TemplateErrorCode.CATEGORY_NOT_FOUND)` |
| `new BizException("上级分类不存在")` | `new BizException(TemplateErrorCode.CATEGORY_PARENT_NOT_FOUND)` |
| `new BizException("最多支持三级分类")` | `new BizException(TemplateErrorCode.CATEGORY_MAX_LEVEL)` |
| `new BizException("存在子分类，无法删除")` | `new BizException(TemplateErrorCode.CATEGORY_HAS_CHILDREN)` |
| `new BizException("非法文件路径")` | `new BizException(TemplateErrorCode.FILE_PATH_INVALID)` |
| `new BizException("存储 ZIP 文件失败")` | `new BizException(TemplateErrorCode.FILE_SAVE_FAILED)` |
| `new BizException("读取文件失败")` | `new BizException(TemplateErrorCode.FILE_READ_FAILED)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-template**

Run: `mvn clean install -pl aeisp-template -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-template/src/main/java/com/aeisp/template/code/TemplateErrorCode.java
git add aeisp-template/src/main/java/com/aeisp/template/
git commit -m "feat(template): add TemplateErrorCode and migrate template module exceptions"
```

---

### Task 7: aeisp-message 模块错误码

**Files:**
- Create: `aeisp-message/src/main/java/com/aeisp/message/code/MessageErrorCode.java`
- Modify: `aeisp-message/src/main/java/com/aeisp/message/service/impl/MsgNotificationServiceImpl.java`
- Modify: `aeisp-message/src/main/java/com/aeisp/message/controller/MsgUserNotificationController.java`

- [ ] **Step 1: 新建 MessageErrorCode 枚举**

```java
package com.aeisp.message.code;

import com.aeisp.common.code.ErrorCode;

public enum MessageErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND(5001, "消息不存在"),
    NOTIFICATION_ONLY_DRAFT_CAN_PUSH(5002, "只有草稿状态的消息才能推送"),
    NOTIFICATION_PUSH_SCOPE_INVALID(5003, "推送范围无效"),
    NOTIFICATION_PUSH_TARGET_EMPTY(5004, "推送目标用户为空"),
    NOTIFICATION_ONLY_SENT_CAN_REVOKE(5005, "只有已发送的消息才能撤回");

    private final int code;
    private final String message;

    MessageErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 message 模块硬编码异常**

在 `aeisp-message` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("消息不存在")` | `new BizException(MessageErrorCode.NOTIFICATION_NOT_FOUND)` |
| `new BizException("只有草稿状态的消息才能推送")` | `new BizException(MessageErrorCode.NOTIFICATION_ONLY_DRAFT_CAN_PUSH)` |
| `new BizException("推送范围无效")` | `new BizException(MessageErrorCode.NOTIFICATION_PUSH_SCOPE_INVALID)` |
| `new BizException("推送目标用户为空")` | `new BizException(MessageErrorCode.NOTIFICATION_PUSH_TARGET_EMPTY)` |
| `new BizException("只有已发送的消息才能撤回")` | `new BizException(MessageErrorCode.NOTIFICATION_ONLY_SENT_CAN_REVOKE)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-message**

Run: `mvn clean install -pl aeisp-message -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-message/src/main/java/com/aeisp/message/code/MessageErrorCode.java
git add aeisp-message/src/main/java/com/aeisp/message/
git commit -m "feat(message): add MessageErrorCode and migrate message module exceptions"
```

---

### Task 8: aeisp-model 模块错误码

**Files:**
- Create: `aeisp-model/src/main/java/com/aeisp/model/code/ModelErrorCode.java`
- Modify: `aeisp-model/src/main/java/com/aeisp/model/service/impl/AiSessionServiceImpl.java`
- Modify: `aeisp-model/src/main/java/com/aeisp/model/service/impl/ModelServiceImpl.java`

- [ ] **Step 1: 新建 ModelErrorCode 枚举**

```java
package com.aeisp.model.code;

import com.aeisp.common.code.ErrorCode;

public enum ModelErrorCode implements ErrorCode {
    MODEL_NOT_FOUND(6001, "模型不存在"),
    SESSION_NOT_FOUND(6002, "会话不存在"),
    MODEL_CALL_NOT_IMPLEMENTED(6003, "模型调用功能待实现");

    private final int code;
    private final String message;

    ModelErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 model 模块硬编码异常**

在 `aeisp-model` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("模型不存在")` | `new BizException(ModelErrorCode.MODEL_NOT_FOUND)` |
| `new BizException("会话不存在")` | `new BizException(ModelErrorCode.SESSION_NOT_FOUND)` |
| `new BizException("模型调用功能待实现")` | `new BizException(ModelErrorCode.MODEL_CALL_NOT_IMPLEMENTED)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-model**

Run: `mvn clean install -pl aeisp-model -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-model/src/main/java/com/aeisp/model/code/ModelErrorCode.java
git add aeisp-model/src/main/java/com/aeisp/model/
git commit -m "feat(model): add ModelErrorCode and migrate model module exceptions"
```

---

### Task 9: aeisp-recharge 模块错误码

**Files:**
- Create: `aeisp-recharge/src/main/java/com/aeisp/recharge/code/RechargeErrorCode.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/service/impl/OrderServiceImpl.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/service/impl/DurationServiceImpl.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/service/impl/BalanceServiceImpl.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/service/impl/PackageServiceImpl.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/controller/OrderController.java`
- Modify: `aeisp-recharge/src/main/java/com/aeisp/recharge/controller/BalanceController.java`

- [ ] **Step 1: 新建 RechargeErrorCode 枚举**

```java
package com.aeisp.recharge.code;

import com.aeisp.common.code.ErrorCode;

public enum RechargeErrorCode implements ErrorCode {
    PACKAGE_NOT_FOUND(7001, "套餐不存在"),
    ORDER_NOT_FOUND(7011, "订单不存在"),
    ORDER_STATUS_PAY_INVALID(7012, "订单状态不正确，无法支付"),
    ORDER_STATUS_REFUND_INVALID(7013, "订单状态不正确，无法退款"),
    ORDER_STATUS_CANCEL_INVALID(7014, "订单状态不正确，无法取消"),
    BALANCE_INSUFFICIENT(7021, "余额不足"),
    BALANCE_INSUFFICIENT_RECHARGE(7022, "余额不足，请先充值"),
    BALANCE_RECHARGE_AMOUNT_INVALID(7023, "充值金额必须大于0"),
    BALANCE_DEDUCT_AMOUNT_INVALID(7024, "抵扣金额必须大于0"),
    BALANCE_ADJUST_ZERO(7025, "调整金额不能为0"),
    BALANCE_ADJUST_NEGATIVE(7026, "调整后余额不能为负数"),
    DURATION_CONSUME_INVALID(7031, "消耗时长必须大于0"),
    DURATION_INSUFFICIENT(7032, "剩余时长不足");

    private final int code;
    private final String message;

    RechargeErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 recharge 模块硬编码异常**

在 `aeisp-recharge` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("套餐不存在")` | `new BizException(RechargeErrorCode.PACKAGE_NOT_FOUND)` |
| `new BizException("订单不存在")` | `new BizException(RechargeErrorCode.ORDER_NOT_FOUND)` |
| `new BizException("订单状态不正确，无法支付")` | `new BizException(RechargeErrorCode.ORDER_STATUS_PAY_INVALID)` |
| `new BizException("订单状态不正确，无法退款")` | `new BizException(RechargeErrorCode.ORDER_STATUS_REFUND_INVALID)` |
| `new BizException("订单状态不正确，无法取消")` | `new BizException(RechargeErrorCode.ORDER_STATUS_CANCEL_INVALID)` |
| `new BizException("余额不足")` | `new BizException(RechargeErrorCode.BALANCE_INSUFFICIENT)` |
| `new BizException("余额不足，请先充值")` | `new BizException(RechargeErrorCode.BALANCE_INSUFFICIENT_RECHARGE)` |
| `new BizException("充值金额必须大于0")` | `new BizException(RechargeErrorCode.BALANCE_RECHARGE_AMOUNT_INVALID)` |
| `new BizException("抵扣金额必须大于0")` | `new BizException(RechargeErrorCode.BALANCE_DEDUCT_AMOUNT_INVALID)` |
| `new BizException("调整金额不能为0")` | `new BizException(RechargeErrorCode.BALANCE_ADJUST_ZERO)` |
| `new BizException("调整后余额不能为负数")` | `new BizException(RechargeErrorCode.BALANCE_ADJUST_NEGATIVE)` |
| `new BizException("消耗时长必须大于0")` | `new BizException(RechargeErrorCode.DURATION_CONSUME_INVALID)` |
| `new BizException("剩余时长不足")` | `new BizException(RechargeErrorCode.DURATION_INSUFFICIENT)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-recharge**

Run: `mvn clean install -pl aeisp-recharge -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-recharge/src/main/java/com/aeisp/recharge/code/RechargeErrorCode.java
git add aeisp-recharge/src/main/java/com/aeisp/recharge/
git commit -m "feat(recharge): add RechargeErrorCode and migrate recharge module exceptions"
```

---

### Task 10: aeisp-project 模块错误码

**Files:**
- Create: `aeisp-project/src/main/java/com/aeisp/project/code/ProjectErrorCode.java`
- Modify: `aeisp-project/src/main/java/com/aeisp/project/service/impl/PrjProjectServiceImpl.java`

- [ ] **Step 1: 新建 ProjectErrorCode 枚举**

```java
package com.aeisp.project.code;

import com.aeisp.common.code.ErrorCode;

public enum ProjectErrorCode implements ErrorCode {
    PROJECT_NOT_FOUND(8001, "项目不存在"),
    PROJECT_ALREADY_ARCHIVED(8002, "项目已归档，无需重复操作");

    private final int code;
    private final String message;

    ProjectErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 2: 迁移 project 模块硬编码异常**

在 `aeisp-project` 模块的所有 ServiceImpl 和 Controller 中，搜索所有 `new BizException(...)` 和 `Result.error(...)` 调用，按以下映射替换：

| 原有代码 | 替换为 |
|---------|--------|
| `new BizException("项目不存在")` | `new BizException(ProjectErrorCode.PROJECT_NOT_FOUND)` |
| `new BizException("项目已归档，无需重复操作")` | `new BizException(ProjectErrorCode.PROJECT_ALREADY_ARCHIVED)` |
| `Result.error(ResultCode.BAD_REQUEST, ...)` | `Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, ...)` |

- [ ] **Step 3: 编译验证 aeisp-project**

Run: `mvn clean install -pl aeisp-project -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add aeisp-project/src/main/java/com/aeisp/project/code/ProjectErrorCode.java
git add aeisp-project/src/main/java/com/aeisp/project/
git commit -m "feat(project): add ProjectErrorCode and migrate project module exceptions"
```

---

### Task 11: 全量编译与 ResultCode 清理

**Files:**
- Modify: `aeisp-common/src/main/java/com/aeisp/common/constant/ResultCode.java`
- Modify: `aeisp-common/src/main/java/com/aeisp/common/util/JwtUtil.java`

- [ ] **Step 1: 全量编译验证**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: 清理 JwtUtil 中的 ResultCode 引用（如有）**

检查 `aeisp-common/src/main/java/com/aeisp/common/util/JwtUtil.java` 是否引用了 `ResultCode`。若有，替换为 `CommonErrorCode` 或 `BootErrorCode` 中合适的枚举。

- [ ] **Step 3: 清理 ResultCode 业务常量（保留兼容值）**

修改 `aeisp-common/src/main/java/com/aeisp/common/constant/ResultCode.java`，保留以下常量作为向后兼容，但添加 `@Deprecated` 注解：

```java
    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int BAD_REQUEST = 400;

    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int UNAUTHORIZED = 401;

    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int FORBIDDEN = 403;

    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int NOT_FOUND = 404;

    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int METHOD_NOT_ALLOWED = 405;

    /** @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode} */
    @Deprecated
    public static final int TOO_MANY_REQUESTS = 429;
```

保留 `SUCCESS = 200` 和 `INTERNAL_ERROR = 500` 不标记 `@Deprecated`（因为 `INTERNAL_ERROR` 仍可能在非业务场景使用，但建议逐步迁移）。

- [ ] **Step 4: 最终全量编译验证**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add aeisp-common/src/main/java/com/aeisp/common/constant/ResultCode.java
git add aeisp-common/src/main/java/com/aeisp/common/util/JwtUtil.java
git commit -m "chore(common): deprecate old ResultCode business constants, mark INTERNAL_ERROR compatible"
```

---

## Self-Review

**1. Spec coverage:**
- ErrorCode 接口 ✓ (Task 1)
- BizException 改造 ✓ (Task 1)
- CommonErrorCode ✓ (Task 1)
- BootErrorCode + JwtAuthenticationFilter ✓ (Task 2)
- AuthController 改造 ✓ (Task 2)
- GlobalExceptionHandler 改造 ✓ (Task 3)
- RateLimitAspect 改造 ✓ (Task 3)
- SystemErrorCode + 迁移 ✓ (Task 4)
- UserErrorCode + 迁移 ✓ (Task 5)
- TemplateErrorCode + 迁移 ✓ (Task 6)
- MessageErrorCode + 迁移 ✓ (Task 7)
- ModelErrorCode + 迁移 ✓ (Task 8)
- RechargeErrorCode + 迁移 ✓ (Task 9)
- ProjectErrorCode + 迁移 ✓ (Task 10)
- ResultCode 清理 ✓ (Task 11)
- 新增错误码 1007/1008/3008/3009 ✓ (Task 2 Step 1, Task 5 Step 1)

**2. Placeholder scan:**
- 无 TBD/TODO
- 所有枚举给出完整代码
- 所有改造步骤给出具体文件路径和替换映射

**3. Type consistency:**
- `ErrorCode.getCode()` 返回 `int`，与 `Result.error(int, String)` 和 `BizException(Integer, String)` 兼容
- `BootErrorCode` 等枚举均 `implements ErrorCode`
- `BizException(ErrorCode)` 构造器在 Task 1 中定义，后续任务中使用的模式一致
