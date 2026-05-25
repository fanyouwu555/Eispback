# 全系统业务错误码设计方案

## 1. 设计目标

- 将当前所有业务异常从单一 `400` 码升级为按场景细分的独立错误码，使 Unity 客户端可以通过错误码做程序化判断（弹不同提示、跳转不同页面、触发不同逻辑）。
- 错误码纯数字，按模块分段，便于快速定位问题来源。
- 码与提示语绑定，避免同一错误在不同地方 message 不一致。
- 保持向后兼容：成功仍返回 `200`，HTTP 状态码不变，仅 `Result.code` 字段细化。

## 2. 错误码结构

4 位纯数字：`ABCD`
- `A`（千位）= 模块标识
- `BCD`（后三位）= 模块内自增序号

## 3. 模块分段表

| 区间 | 模块 | 枚举类位置 | 说明 |
|------|------|------------|------|
| 1000-1999 | aeisp-boot | `com.aeisp.boot.code.BootErrorCode` | 认证、JWT、网关通用 |
| 2000-2999 | aeisp-system | `com.aeisp.system.code.SystemErrorCode` | 后台用户/角色/菜单/字典/配置 |
| 3000-3999 | aeisp-user | `com.aeisp.user.code.UserErrorCode` | 前端用户/注册/登录日志/时长/权限 |
| 4000-4999 | aeisp-template | `com.aeisp.template.code.TemplateErrorCode` | 模板/版本/分类/购买 |
| 5000-5999 | aeisp-message | `com.aeisp.message.code.MessageErrorCode` | 通知/推送/调度 |
| 6000-6999 | aeisp-model | `com.aeisp.model.code.ModelErrorCode` | AI 模型/会话 |
| 7000-7999 | aeisp-recharge | `com.aeisp.recharge.code.RechargeErrorCode` | 套餐/订单/余额/时长 |
| 8000-8999 | aeisp-project | `com.aeisp.project.code.ProjectErrorCode` | 项目管理 |
| 9000-9999 | aeisp-common | `com.aeisp.common.code.CommonErrorCode` | 通用参数校验、系统错误、限流 |

## 4. 核心接口

在 `aeisp-common` 中定义统一接口，所有模块枚举实现它：

```java
package com.aeisp.common.code;

public interface ErrorCode {
    int getCode();
    String getMessage();
}
```

## 5. BizException 改造

在 `aeisp-common` 中新增构造函数，支持直接传入 `ErrorCode`：

```java
public BizException(ErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getMessage());
}

public BizException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getCode(), errorCode.getMessage(), cause);
}
```

## 6. 各模块错误码详细清单

### 6.1 aeisp-boot — BootErrorCode (1000-1999)

```java
public enum BootErrorCode implements ErrorCode {
    // 账号密码
    USER_NOT_FOUND(1001, "账号不存在"),
    PASSWORD_MISMATCH(1002, "密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_FROZEN(1004, "账号已被冻结"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    ACCOUNT_RESET(1006, "账号密码已重置，请重新登录"),
    LOGIN_LOCKED_BY_RETRY(1007, "密码错误次数过多，账号已锁定"),
    ACCOUNT_LOGIN_ELSEWHERE(1008, "账号已在其他设备登录"),

    // Token / JWT
    TOKEN_EXPIRED(1011, "登录已过期，请重新登录"),
    TOKEN_INVALID(1012, "Token 已失效，请重新登录"),
    TOKEN_TYPE_INVALID(1013, "无效的 Token 类型"),
    TOKEN_MISSING(1014, "未登录或 Token 已过期"),

    // 通用
    AUTH_FAILED(1050, "认证失败");
}
```

### 6.2 aeisp-system — SystemErrorCode (2000-2999)

```java
public enum SystemErrorCode implements ErrorCode {
    // 用户管理
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_CANNOT_DEMOTE_SUPER(2002, "系统中至少保留一个超级管理员，无法降级"),
    USER_CANNOT_DELETE_LAST_SUPER(2003, "系统中至少保留一个超级管理员，无法删除"),

    // 角色/菜单
    ROLE_NOT_FOUND(2011, "角色不存在"),
    MENU_NOT_FOUND(2012, "菜单不存在"),

    // 字典
    DICT_TYPE_NOT_FOUND(2021, "字典类型不存在"),
    DICT_DATA_NOT_FOUND(2022, "字典数据不存在"),

    // 配置
    CONFIG_NOT_FOUND(2031, "配置项不存在");
}
```

### 6.3 aeisp-user — UserErrorCode (3000-3999)

```java
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
}
```

### 6.4 aeisp-template — TemplateErrorCode (4000-4999)

```java
public enum TemplateErrorCode implements ErrorCode {
    // 模板
    TEMPLATE_NOT_FOUND(4001, "模板不存在"),
    TEMPLATE_NOT_PUBLISHED(4002, "模板未上线"),
    TEMPLATE_VERSION_EXISTS(4003, "版本号已存在"),
    TEMPLATE_ALREADY_OWNED(4004, "您已拥有该模板，无需重复购买"),
    TEMPLATE_PRICE_INVALID(4005, "模板价格异常"),
    TEMPLATE_IS_FREE(4006, "该模板为免费模板，无需购买"),

    // 版本
    VERSION_NOT_FOUND(4011, "版本不存在"),
    VERSION_NOT_BELONG(4012, "版本不存在或不属于该模板"),
    VERSION_IN_USE(4013, "不能删除当前正在使用的版本"),
    VERSION_NUMBER_INVALID(4014, "非法版本号"),

    // 分类
    CATEGORY_NOT_FOUND(4021, "分类不存在"),
    CATEGORY_PARENT_NOT_FOUND(4022, "上级分类不存在"),
    CATEGORY_MAX_LEVEL(4023, "最多支持三级分类"),
    CATEGORY_HAS_CHILDREN(4024, "存在子分类，无法删除"),

    // 文件
    FILE_PATH_INVALID(4031, "非法文件路径"),
    FILE_SAVE_FAILED(4032, "存储 ZIP 文件失败"),
    FILE_READ_FAILED(4033, "读取文件失败");
}
```

### 6.5 aeisp-message — MessageErrorCode (5000-5999)

```java
public enum MessageErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND(5001, "消息不存在"),
    NOTIFICATION_ONLY_DRAFT_CAN_PUSH(5002, "只有草稿状态的消息才能推送"),
    NOTIFICATION_PUSH_SCOPE_INVALID(5003, "推送范围无效"),
    NOTIFICATION_PUSH_TARGET_EMPTY(5004, "推送目标用户为空"),
    NOTIFICATION_ONLY_SENT_CAN_REVOKE(5005, "只有已发送的消息才能撤回");
}
```

### 6.6 aeisp-model — ModelErrorCode (6000-6999)

```java
public enum ModelErrorCode implements ErrorCode {
    MODEL_NOT_FOUND(6001, "模型不存在"),
    SESSION_NOT_FOUND(6002, "会话不存在"),
    MODEL_CALL_NOT_IMPLEMENTED(6003, "模型调用功能待实现");
}
```

### 6.7 aeisp-recharge — RechargeErrorCode (7000-7999)

```java
public enum RechargeErrorCode implements ErrorCode {
    // 套餐
    PACKAGE_NOT_FOUND(7001, "套餐不存在"),

    // 订单
    ORDER_NOT_FOUND(7011, "订单不存在"),
    ORDER_STATUS_PAY_INVALID(7012, "订单状态不正确，无法支付"),
    ORDER_STATUS_REFUND_INVALID(7013, "订单状态不正确，无法退款"),
    ORDER_STATUS_CANCEL_INVALID(7014, "订单状态不正确，无法取消"),

    // 余额
    BALANCE_INSUFFICIENT(7021, "余额不足"),
    BALANCE_INSUFFICIENT_RECHARGE(7022, "余额不足，请先充值"),
    BALANCE_RECHARGE_AMOUNT_INVALID(7023, "充值金额必须大于0"),
    BALANCE_DEDUCT_AMOUNT_INVALID(7024, "抵扣金额必须大于0"),
    BALANCE_ADJUST_ZERO(7025, "调整金额不能为0"),
    BALANCE_ADJUST_NEGATIVE(7026, "调整后余额不能为负数"),

    // 时长
    DURATION_CONSUME_INVALID(7031, "消耗时长必须大于0"),
    DURATION_INSUFFICIENT(7032, "剩余时长不足");
}
```

### 6.8 aeisp-project — ProjectErrorCode (8000-8999)

```java
public enum ProjectErrorCode implements ErrorCode {
    PROJECT_NOT_FOUND(8001, "项目不存在"),
    PROJECT_ALREADY_ARCHIVED(8002, "项目已归档，无需重复操作");
}
```

### 6.9 aeisp-common — CommonErrorCode (9000-9999)

```java
public enum CommonErrorCode implements ErrorCode {
    // 参数 / 请求
    PARAM_MISSING(9001, "请求参数缺失"),
    PARAM_VALIDATION_FAILED(9002, "参数校验失败"),
    PARAM_BIND_FAILED(9003, "参数绑定失败"),
    RESOURCE_NOT_FOUND(9004, "请求资源不存在"),
    METHOD_NOT_ALLOWED(9005, "请求方法不支持"),

    // 权限 / 限流
    ACCESS_DENIED(9006, "权限不足，无法访问该资源"),
    RATE_LIMITED(9007, "请求过于频繁"),

    // 系统
    SYSTEM_ERROR(9998, "系统繁忙，请稍后重试"),
    UNKNOWN_ERROR(9999, "未知错误");
}
```

## 7. 全局异常处理器适配

`GlobalExceptionHandler` 的现有逻辑不需要大改，因为它已经通过 `e.getCode()` 和 `e.getMessage()` 取值。唯一需要调整的是框架级异常目前硬编码了 `ResultCode.BAD_REQUEST` 等，需改为 `CommonErrorCode`：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    // ... 拼装 message ...
    return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, message);
}
```

同理：
- `ConstraintViolationException` -> `CommonErrorCode.PARAM_VALIDATION_FAILED`
- `BindException` -> `CommonErrorCode.PARAM_BIND_FAILED`
- `MissingServletRequestParameterException` -> `CommonErrorCode.PARAM_MISSING`
- `NoResourceFoundException` -> `CommonErrorCode.RESOURCE_NOT_FOUND`
- `HttpRequestMethodNotSupportedException` -> `CommonErrorCode.METHOD_NOT_ALLOWED`
- `AccessDeniedException` -> `CommonErrorCode.ACCESS_DENIED`
- `RateLimitAspect` -> `CommonErrorCode.RATE_LIMITED`
- `Exception.class` -> `CommonErrorCode.SYSTEM_ERROR`

## 8. JwtAuthenticationFilter 适配

目前 `JwtAuthenticationFilter` 在 Token 校验失败时直接写死 `ResultCode.UNAUTHORIZED` 和不同字符串。改为使用 `BootErrorCode`：

| 场景 | 当前 message | 新错误码 |
|------|-------------|----------|
| Token 在黑名单 | Token 已失效，请重新登录 | 1012 |
| Token type 不是 access | 无效的 Token 类型 | 1013 |
| 用户被全局拉黑 | 账号密码已重置，请重新登录 | 1006 |
| 用户状态禁用 | 账号已被禁用 | 1003 |
| 用户状态冻结 | 账号已被冻结 | 1004 |
| 用户状态锁定 | 账号已被锁定 | 1005 |
| JWT 解析失败 | 未登录或 Token 已过期 | 1014 |

## 9. 现有代码迁移示例

### 旧写法
```java
// ServiceImpl
if (template == null) {
    throw new BizException("模板不存在");
}

// Controller
return Result.error(ResultCode.BAD_REQUEST, "请求参数缺失: xxx");
```

### 新写法
```java
// ServiceImpl
if (template == null) {
    throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
}

// Controller（框架级异常已由 GlobalExceptionHandler 统一处理，一般不需要手动写）
return Result.error(CommonErrorCode.PARAM_MISSING, "请求参数缺失: xxx");
```

## 10. 对 Unity 客户端的影响

- `Result.code` 从原来的 `200/400/401/403/500` 扩展为 `1001-9999`。
- `Result.message` 仍然保留中文提示语，可直接展示给用户。
- Unity 客户端推荐用 `switch` 或常量表映射错误码，做差异化处理：
  - `1002`（密码错误）：清空密码输入框，聚焦密码框，累计计数
  - `1007`（密码错误次数过多锁定）：提示锁定，跳转登录页或等待解锁
  - `1008`（异地登录）：弹出强制下线弹窗，显示异地 IP，提供踢出/重新登录选项
  - `1011/1012/1014`（Token 过期/失效）：自动调用刷新 Token 接口，失败则跳转登录页
  - `3008`（未首次充值）：跳转充值引导页或弹出充值窗口
  - `3009`（比赛专用账号）：自动跳转比赛环境入口
  - `7021/7022`（余额不足）：弹出充值引导弹窗
  - `4001`（模板不存在）：返回上一页并提示"模板已下架"

## 11. 实施顺序建议

1. `aeisp-common`：新建 `ErrorCode` 接口，改造 `BizException`，新建 `CommonErrorCode`
2. `aeisp-boot`：新建 `BootErrorCode`，改造 `JwtAuthenticationFilter`，改造 `AuthController`（如有）
3. `aeisp-common`：改造 `GlobalExceptionHandler` 和 `RateLimitAspect`
4. 按模块逐个迁移（template、recharge、message、model、project、system、user）
5. 清理废弃的 `ResultCode` 常量（保留 `SUCCESS` 和 `INTERNAL_ERROR` 等作为兼容，业务码不再使用）
