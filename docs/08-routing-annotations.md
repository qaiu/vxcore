# 路由注解指南

VXCore 提供了类似 Spring MVC 的注解式路由功能，让 Web 开发更加简洁和直观。

## 🎯 核心注解

### @RouteHandler

用于标记控制器类，指定基础路径。

```java
@RouteHandler("/api")
public class UserController {
    // 控制器方法
}
```

**属性说明**：
- `value`: 基础路径，默认为空字符串
- `description`: 控制器描述（可选）

### @RouteMapping

用于标记具体的路由方法，支持 HTTP 方法、路径、参数等配置。

```java
@RouteMapping(
    value = "/users", 
    method = HttpMethod.GET,
    description = "获取用户列表"
)
public Future<JsonResult> getUsers() {
    return Future.succeededFuture(JsonResult.success(userService.findAll()));
}
```

**属性说明**：
- `value`: 路由路径，支持路径变量
- `method`: HTTP 方法，默认为 GET
- `description`: 方法描述（可选）
- `produces`: 响应内容类型（可选）
- `consumes`: 请求内容类型（可选）

## 📝 参数注解

### @RequestParam

用于绑定请求参数到方法参数。

```java
@RouteMapping("/users")
public Future<JsonResult> getUsers(
    @RequestParam("page") int page,
    @RequestParam(value = "size", defaultValue = "10") int size,
    @RequestParam(value = "keyword", required = false) String keyword
) {
    // 处理逻辑
}
```

**属性说明**：
- `value`: 参数名称
- `required`: 是否必需，默认为 true
- `defaultValue`: 默认值

### @PathVariable

用于绑定路径变量到方法参数。

```java
@RouteMapping("/users/{id}")
public Future<JsonResult> getUser(@PathVariable("id") Long id) {
    return userService.findById(id)
        .map(user -> JsonResult.success(user));
}
```

**属性说明**：
- `value`: 路径变量名称

### @RequestBody

用于绑定请求体到方法参数。

```java
@RouteMapping(value = "/users", method = HttpMethod.POST)
public Future<JsonResult> createUser(@RequestBody User user) {
    return userService.create(user)
        .map(createdUser -> JsonResult.success(createdUser));
}
```

**属性说明**：
- `required`: 是否必需，默认为 true

## 🔄 方法重载支持

VXCore 支持方法重载，通过参数匹配算法选择最佳方法。

```java
@RouteHandler("/api")
public class UserController {
    
    // 根据ID查询用户
    @RouteMapping(value = "/user", method = HttpMethod.GET)
    public Future<JsonResult> getUser(@RequestParam("id") Long id) {
        return userService.findById(id)
            .map(user -> JsonResult.success(user));
    }
    
    // 根据用户名查询用户
    @RouteMapping(value = "/user", method = HttpMethod.GET)
    public Future<JsonResult> getUser(@RequestParam("name") String name) {
        return userService.findByName(name)
            .map(user -> JsonResult.success(user));
    }
    
    // 根据ID和状态查询用户
    @RouteMapping(value = "/user", method = HttpMethod.GET)
    public Future<JsonResult> getUser(
        @RequestParam("id") Long id,
        @RequestParam("status") String status
    ) {
        return userService.findByIdAndStatus(id, status)
            .map(user -> JsonResult.success(user));
    }
}
```

**匹配规则**：
1. 参数名匹配优先级最高
2. 参数类型匹配次之
3. 参数数量匹配最后

## 🛡️ 异常处理

### @ExceptionHandler

用于标记异常处理方法，支持局部异常处理。

```java
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping("/users/{id}")
    public Future<JsonResult> getUser(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("用户ID不能为空或小于等于0");
        }
        return userService.findById(id)
            .map(user -> JsonResult.success(user));
    }
    
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
    
    @ExceptionHandler(NotFoundException.class)
    public JsonResult handleNotFound(NotFoundException e) {
        return JsonResult.fail(404, "用户不存在");
    }
}
```

### 全局异常处理

```java
@GlobalExceptionHandler
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        log.error("系统异常", e);
        return JsonResult.fail(500, "系统内部错误");
    }
    
    @ExceptionHandler(BusinessException.class)
    public JsonResult handleBusiness(BusinessException e) {
        return JsonResult.fail(e.getCode(), e.getMessage());
    }
}
```

## 🔧 类型转换

VXCore 支持自动类型转换，包括：

### 基本类型
- `String` → `Integer`, `Long`, `Double`, `Boolean`
- `String` → `LocalDate`, `LocalDateTime`, `LocalTime`
- `String` → `BigDecimal`, `BigInteger`

### 集合类型
- `String[]` → `List<T>`, `Set<T>`
- `JsonArray` → `List<T>`, `Set<T>`

### 自定义类型
```java
// 注册自定义转换器
TypeConverterRegistry.register(UserStatus.class, value -> {
    return UserStatus.valueOf(value.toString().toUpperCase());
});

@RouteMapping("/users")
public Future<JsonResult> getUsers(@RequestParam("status") UserStatus status) {
    // 自动转换
}
```

## 📊 响应格式

### JsonResult

统一的响应格式：

```java
public class JsonResult {
    private int code;        // 状态码
    private String message;  // 消息
    private Object data;     // 数据
    private long timestamp; // 时间戳
    
    // 成功响应
    public static JsonResult success(Object data) { }
    public static JsonResult success(String message, Object data) { }
    
    // 失败响应
    public static JsonResult fail(int code, String message) { }
    public static JsonResult fail(String message) { }
}
```

### 使用示例

```java
@RouteMapping("/users")
public Future<JsonResult> getUsers() {
    return userService.findAll()
        .map(users -> JsonResult.success("查询成功", users))
        .recover(throwable -> {
            log.error("查询用户失败", throwable);
            return JsonResult.fail(500, "查询失败");
        });
}
```

## 🚀 最佳实践

### 1. 控制器设计

```java
@RouteHandler("/api/v1")
public class UserController {
    
    // 使用清晰的路径
    @RouteMapping(value = "/users", method = HttpMethod.GET)
    public Future<JsonResult> getUsers() { }
    
    // 使用路径变量
    @RouteMapping(value = "/users/{id}", method = HttpMethod.GET)
    public Future<JsonResult> getUser(@PathVariable("id") Long id) { }
    
    // 使用请求体
    @RouteMapping(value = "/users", method = HttpMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) { }
}
```

### 2. 参数验证

```java
@RouteMapping("/users")
public Future<JsonResult> createUser(@RequestBody User user) {
    // 参数验证
    if (user.getName() == null || user.getName().trim().isEmpty()) {
        throw new ValidationException("用户名不能为空");
    }
    
    if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
        throw new ValidationException("邮箱格式不正确");
    }
    
    return userService.create(user)
        .map(createdUser -> JsonResult.success(createdUser));
}
```

### 3. 异常处理

```java
@RouteHandler("/api")
public class UserController {
    
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
    
    @ExceptionHandler(NotFoundException.class)
    public JsonResult handleNotFound(NotFoundException e) {
        return JsonResult.fail(404, "资源不存在");
    }
}
```

### 4. 异步处理

```java
@RouteMapping("/users")
public Future<JsonResult> getUsers() {
    return userService.findAll()
        .map(users -> JsonResult.success(users))
        .recover(throwable -> {
            log.error("查询用户失败", throwable);
            return JsonResult.fail(500, "查询失败");
        });
}
```

## 🔍 调试技巧

### 1. 路由注册日志

```java
// 启用路由注册日志
System.setProperty("vxcore.route.debug", "true");
```

### 2. 参数绑定日志

```java
// 启用参数绑定日志
System.setProperty("vxcore.param.debug", "true");
```

### 3. 异常处理日志

```java
// 启用异常处理日志
System.setProperty("vxcore.exception.debug", "true");
```

## 📚 相关文档

- [WebSocket指南](WEBSOCKET_GUIDE.md)
- [异常处理机制](09-exception-handling.md)
- [配置管理](10-configuration.md)
- [Lambda查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md)
