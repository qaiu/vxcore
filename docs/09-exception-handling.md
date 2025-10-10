# 异常处理机制

VXCore 提供了完善的异常处理机制，支持全局和局部异常处理，让错误处理更加统一和友好。

## 🎯 异常体系

### 基础异常类

```java
// 基础异常类
public abstract class BaseException extends RuntimeException {
    private int code;
    private String message;
    
    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    // getters and setters
}

// 业务异常
public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(400, message);
    }
    
    public BusinessException(int code, String message) {
        super(code, message);
    }
}

// 验证异常
public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(400, message);
    }
}

// 系统异常
public class SystemException extends BaseException {
    public SystemException(String message) {
        super(500, message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(500, message);
        initCause(cause);
    }
}
```

## 🛡️ 异常处理注解

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

### @GlobalExceptionHandler

用于标记全局异常处理类。

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
    
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
}
```

## 🔧 异常处理器接口

### ExceptionHandler 接口

```java
public interface ExceptionHandler<T extends Throwable> {
    
    /**
     * 处理异常
     * @param throwable 异常对象
     * @param context 路由上下文
     * @return 处理结果
     */
    JsonResult handle(T throwable, RoutingContext context);
    
    /**
     * 获取异常类型
     * @return 异常类型
     */
    Class<T> getExceptionType();
    
    /**
     * 获取优先级
     * @return 优先级，数值越小优先级越高
     */
    default int getPriority() {
        return 100;
    }
}
```

### 自定义异常处理器

```java
public class CustomExceptionHandler implements ExceptionHandler<BusinessException> {
    
    @Override
    public JsonResult handle(BusinessException e, RoutingContext context) {
        // 记录日志
        log.error("业务异常: {}", e.getMessage(), e);
        
        // 返回友好错误信息
        return JsonResult.fail(e.getCode(), e.getMessage());
    }
    
    @Override
    public Class<BusinessException> getExceptionType() {
        return BusinessException.class;
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
}
```

## 🏗️ 异常管理器

### ExceptionManager

```java
public class ExceptionManager {
    
    private static final Map<Class<? extends Throwable>, ExceptionHandler<?>> handlers = new ConcurrentHashMap<>();
    
    /**
     * 注册异常处理器
     */
    public static <T extends Throwable> void registerHandler(ExceptionHandler<T> handler) {
        handlers.put(handler.getExceptionType(), handler);
    }
    
    /**
     * 处理异常
     */
    public static JsonResult handleException(Throwable throwable, RoutingContext context) {
        // 查找匹配的异常处理器
        ExceptionHandler<?> handler = findHandler(throwable.getClass());
        
        if (handler != null) {
            try {
                return ((ExceptionHandler<Throwable>) handler).handle(throwable, context);
            } catch (Exception e) {
                log.error("异常处理器执行失败", e);
            }
        }
        
        // 使用默认处理器
        return handleDefault(throwable, context);
    }
    
    /**
     * 查找异常处理器
     */
    private static ExceptionHandler<?> findHandler(Class<? extends Throwable> exceptionType) {
        // 精确匹配
        ExceptionHandler<?> handler = handlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }
        
        // 继承关系匹配
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandler<?>> entry : handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionType)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 默认异常处理
     */
    private static JsonResult handleDefault(Throwable throwable, RoutingContext context) {
        log.error("未处理的异常", throwable);
        
        if (throwable instanceof ValidationException) {
            return JsonResult.fail(400, throwable.getMessage());
        } else if (throwable instanceof BusinessException) {
            return JsonResult.fail(500, throwable.getMessage());
        } else {
            return JsonResult.fail(500, "系统内部错误");
        }
    }
}
```

## 🚀 使用示例

### 1. 控制器异常处理

```java
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping(value = "/users", method = HttpMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) {
        // 参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("用户名不能为空");
        }
        
        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            throw new ValidationException("邮箱格式不正确");
        }
        
        return userService.create(user)
            .map(createdUser -> JsonResult.success(createdUser));
    }
    
    @RouteMapping("/users/{id}")
    public Future<JsonResult> getUser(@PathVariable("id") Long id) {
        return userService.findById(id)
            .map(user -> {
                if (user == null) {
                    throw new NotFoundException("用户不存在");
                }
                return JsonResult.success(user);
            });
    }
    
    // 局部异常处理
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
    
    @ExceptionHandler(NotFoundException.class)
    public JsonResult handleNotFound(NotFoundException e) {
        return JsonResult.fail(404, e.getMessage());
    }
}
```

### 2. 服务层异常处理

```java
@Service
public class UserService {
    
    public Future<User> create(User user) {
        return userDao.findByName(user.getUsername())
            .compose(existingUser -> {
                if (existingUser != null) {
                    throw new BusinessException("用户名已存在");
                }
                
                return userDao.create(user);
            });
    }
    
    public Future<User> findById(Long id) {
        return userDao.findById(id)
            .map(user -> {
                if (user == null) {
                    throw new NotFoundException("用户不存在");
                }
                return user;
            });
    }
}
```

### 3. 全局异常处理

```java
@GlobalExceptionHandler
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        return JsonResult.fail(400, e.getMessage());
    }
    
    @ExceptionHandler(BusinessException.class)
    public JsonResult handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return JsonResult.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(NotFoundException.class)
    public JsonResult handleNotFound(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return JsonResult.fail(404, e.getMessage());
    }
    
    @ExceptionHandler(SystemException.class)
    public JsonResult handleSystem(SystemException e) {
        log.error("系统异常", e);
        return JsonResult.fail(500, "系统内部错误");
    }
    
    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        log.error("未处理异常", e);
        return JsonResult.fail(500, "系统内部错误");
    }
}
```

## 🔍 异常处理优先级

### 处理顺序

1. **局部异常处理器** - 控制器内的 `@ExceptionHandler`
2. **全局异常处理器** - 标记 `@GlobalExceptionHandler` 的类
3. **默认异常处理器** - 系统默认处理

### 优先级规则

```java
@RouteHandler("/api")
public class UserController {
    
    // 局部处理器，优先级最高
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, "局部处理: " + e.getMessage());
    }
}

@GlobalExceptionHandler
public class GlobalExceptionHandler {
    
    // 全局处理器，优先级次之
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, "全局处理: " + e.getMessage());
    }
}
```

## 📊 异常日志

### 日志配置

```yaml
# application.yml
logging:
  level:
    cn.qaiu.vx.core.exception: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 异常统计

```java
@Component
public class ExceptionStatistics {
    
    private final AtomicLong validationCount = new AtomicLong(0);
    private final AtomicLong businessCount = new AtomicLong(0);
    private final AtomicLong systemCount = new AtomicLong(0);
    
    public void recordException(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            validationCount.incrementAndGet();
        } else if (throwable instanceof BusinessException) {
            businessCount.incrementAndGet();
        } else if (throwable instanceof SystemException) {
            systemCount.incrementAndGet();
        }
    }
    
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("validation", validationCount.get());
        stats.put("business", businessCount.get());
        stats.put("system", systemCount.get());
        return stats;
    }
}
```

## 🚀 最佳实践

### 1. 异常分类

```java
// 按业务领域分类
public class UserException extends BusinessException {
    public UserException(String message) {
        super(400, message);
    }
}

public class OrderException extends BusinessException {
    public OrderException(String message) {
        super(400, message);
    }
}

// 按错误类型分类
public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(400, message);
    }
}

public class AuthorizationException extends BaseException {
    public AuthorizationException(String message) {
        super(401, message);
    }
}
```

### 2. 异常信息国际化

```java
public class I18nException extends BaseException {
    private String messageKey;
    private Object[] args;
    
    public I18nException(String messageKey, Object... args) {
        super(400, messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
    
    public String getLocalizedMessage(Locale locale) {
        return MessageSource.getMessage(messageKey, args, locale);
    }
}
```

### 3. 异常链处理

```java
@ExceptionHandler(Exception.class)
public JsonResult handleException(Exception e) {
    // 处理异常链
    Throwable rootCause = getRootCause(e);
    
    if (rootCause instanceof ValidationException) {
        return JsonResult.fail(400, rootCause.getMessage());
    } else if (rootCause instanceof BusinessException) {
        return JsonResult.fail(500, rootCause.getMessage());
    } else {
        return JsonResult.fail(500, "系统内部错误");
    }
}

private Throwable getRootCause(Throwable throwable) {
    Throwable cause = throwable.getCause();
    if (cause == null) {
        return throwable;
    }
    return getRootCause(cause);
}
```

## 📚 相关文档

- [路由注解指南](08-routing-annotations.md)
- [WebSocket指南](WEBSOCKET_GUIDE.md)
- [配置管理](10-configuration.md)
- [Lambda查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md)
