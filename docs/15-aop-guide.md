# AOP 切面编程指南

VXCore 提供了基于 Byte Buddy 的声明式 AOP（面向切面编程）框架，支持方法拦截、日志记录、性能监控等横切关注点。

## 📋 目录

- [概述](#概述)
- [快速开始](#快速开始)
- [核心注解](#核心注解)
- [切点表达式](#切点表达式)
- [通知类型](#通知类型)
- [内置切面](#内置切面)
- [自定义切面](#自定义切面)
- [与 Vert.x 异步集成](#与-vertx-异步集成)
- [配置说明](#配置说明)
- [最佳实践](#最佳实践)

## 概述

### 什么是 AOP？

AOP（Aspect-Oriented Programming）是一种编程范式，用于将横切关注点（如日志、安全、事务）从业务逻辑中分离出来。

### VXCore AOP 特点

- **基于 Byte Buddy**：高性能字节码操作，支持运行时动态代理
- **支持 Java Agent**：可通过 `-javaagent` 参数实现类重定义
- **异步友好**：完美支持 Vert.x `Future<T>` 返回值
- **编译时安全**：切点表达式在启动时验证
- **与 Dagger2 集成**：代理对象可无缝注入

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                    AopComponent                         │
│              (LifecycleComponent, priority=12)          │
├─────────────────────────────────────────────────────────┤
│  1. 安装 Byte Buddy Agent                               │
│  2. 扫描 @Aspect 注解的类                                │
│  3. 解析切点表达式                                       │
│  4. 构建拦截器链                                         │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  AspectProcessor                        │
│               (Byte Buddy 代理工厂)                      │
├─────────────────────────────────────────────────────────┤
│  AspectRegistry ──▶ PointcutMatcher ──▶ InterceptorChain│
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    目标方法执行                          │
│  Before ──▶ Around(proceed) ──▶ After/AfterReturning   │
└─────────────────────────────────────────────────────────┘
```

## 快速开始

### 1. 添加依赖

```xml
<!-- 在 pom.xml 中，byte-buddy 已包含在 core 模块 -->
<dependency>
    <groupId>cn.qaiu</groupId>
    <artifactId>core</artifactId>
    <version>${vxcore.version}</version>
</dependency>
```

### 2. 启用 AOP

在 `application.yml` 中配置：

```yaml
aop:
  enabled: true
  scan-packages:
    - cn.qaiu.example.aspect
  agent:
    enabled: true  # 启用 Byte Buddy Agent
```

### 3. 定义切面

```java
@Aspect
@Order(10)
public class LoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* cn.qaiu.*.service.*.*(..))")
    public void logBefore(JoinPoint jp) {
        LOG.info("调用方法: {}", jp.getSignature());
    }

    @AfterReturning(value = "execution(* cn.qaiu.*.service.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint jp, Object result) {
        LOG.info("方法 {} 返回: {}", jp.getShortSignature(), result);
    }
}
```

### 4. 使用简化注解

```java
@Service
public class UserService {

    @Loggable(includeArgs = true, includeResult = true)
    @Timed(slowThreshold = 500)
    public Future<User> findById(Long id) {
        return userDao.findById(id);
    }
}
```

## 核心注解

### 切面定义注解

| 注解 | 作用 | 目标 |
|------|------|------|
| `@Aspect` | 标记切面类 | 类 |
| `@Order` | 指定切面优先级（数值越小越先执行） | 类 |
| `@Pointcut` | 定义可重用的切点 | 方法 |

### 通知注解

| 注解 | 执行时机 | 说明 |
|------|---------|------|
| `@Before` | 目标方法执行前 | 可阻止方法执行 |
| `@After` | 目标方法执行后（finally） | 无论是否异常都执行 |
| `@AfterReturning` | 目标方法正常返回后 | 可访问返回值 |
| `@AfterThrowing` | 目标方法抛出异常后 | 可访问异常对象 |
| `@Around` | 包围目标方法 | 完全控制方法执行 |

## 切点表达式

### execution 表达式

匹配方法执行：

```java
// 完整格式
execution(modifiers? return-type declaring-type.method-name(params) throws?)

// 示例
@Before("execution(* cn.qaiu.*.service.*.*(..))")
// 解释：
//   *                    - 任意返回类型
//   cn.qaiu.*.service    - cn.qaiu 下任意子包的 service 包
//   *                    - 任意类
//   *                    - 任意方法
//   (..)                 - 任意参数

// 更多示例
@Before("execution(public * *(..))")              // 所有 public 方法
@Before("execution(* cn.qaiu.*.service.User*.*(..))")  // User 开头的类的所有方法
@Before("execution(Future cn.qaiu..*.*(..))")     // 返回 Future 的方法
```

### @annotation 表达式

匹配带有指定注解的方法：

```java
// 匹配带有 @Loggable 注解的方法
@Around("@annotation(cn.qaiu.vx.core.aop.Loggable)")

// 匹配带有 @Transactional 注解的方法
@Around("@annotation(cn.qaiu.vx.core.aop.Transactional)")
```

### within 表达式

匹配指定类型内的所有方法：

```java
// 匹配 UserService 类的所有方法
@Before("within(cn.qaiu.example.service.UserService)")

// 匹配 service 包下所有类的方法
@Before("within(cn.qaiu.*.service.*)")
```

### 组合表达式

使用逻辑运算符组合切点：

```java
@Aspect
public class CombinedAspect {

    // 定义可重用的切点
    @Pointcut("execution(* cn.qaiu.*.service.*.*(..))")
    public void serviceMethod() {}

    @Pointcut("@annotation(cn.qaiu.vx.core.aop.Loggable)")
    public void loggableMethod() {}

    // 组合切点：service 方法 且 带有 @Loggable 注解
    @Before("serviceMethod() && loggableMethod()")
    public void logServiceMethod(JoinPoint jp) {
        // ...
    }

    // 或逻辑
    @Before("serviceMethod() || @annotation(cn.qaiu.vx.core.aop.Timed)")
    public void monitorMethod(JoinPoint jp) {
        // ...
    }
}
```

## 通知类型

### @Before - 前置通知

```java
@Before("execution(* cn.qaiu.*.service.*.*(..))")
public void beforeAdvice(JoinPoint jp) {
    LOG.info("准备执行: {}", jp.getSignature());
    LOG.info("参数: {}", Arrays.toString(jp.getArgs()));
    
    // 可以进行参数校验、权限检查等
    if (jp.getArgs()[0] == null) {
        throw new IllegalArgumentException("参数不能为空");
    }
}
```

### @After - 后置通知（Finally）

```java
@After("execution(* cn.qaiu.*.service.*.*(..))")
public void afterAdvice(JoinPoint jp) {
    // 无论方法是否成功，都会执行
    LOG.info("方法执行完成: {}", jp.getSignature());
    
    // 适合清理资源、记录日志等
}
```

### @AfterReturning - 返回后通知

```java
@AfterReturning(
    value = "execution(* cn.qaiu.*.service.*.*(..))",
    returning = "result"
)
public void afterReturningAdvice(JoinPoint jp, Object result) {
    LOG.info("方法 {} 返回: {}", jp.getShortSignature(), result);
    
    // 可以对返回值进行后处理（但不能修改）
    if (result instanceof User user) {
        LOG.info("查询到用户: {}", user.getUsername());
    }
}
```

### @AfterThrowing - 异常后通知

```java
@AfterThrowing(
    value = "execution(* cn.qaiu.*.service.*.*(..))",
    throwing = "ex"
)
public void afterThrowingAdvice(JoinPoint jp, Throwable ex) {
    LOG.error("方法 {} 抛出异常: {}", jp.getSignature(), ex.getMessage());
    
    // 可以进行异常日志记录、告警等
    if (ex instanceof BusinessException) {
        alertService.send("业务异常: " + ex.getMessage());
    }
}
```

### @Around - 环绕通知

```java
@Around("execution(* cn.qaiu.*.service.*.*(..))")
public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
    long startTime = System.currentTimeMillis();
    
    LOG.info("方法开始: {}", pjp.getSignature());
    
    try {
        // 执行目标方法
        Object result = pjp.proceed();
        
        long duration = System.currentTimeMillis() - startTime;
        LOG.info("方法完成: {}, 耗时: {}ms", pjp.getSignature(), duration);
        
        return result;
        
    } catch (Throwable e) {
        long duration = System.currentTimeMillis() - startTime;
        LOG.error("方法异常: {}, 耗时: {}ms, 错误: {}", 
            pjp.getSignature(), duration, e.getMessage());
        throw e;
    }
}
```

## 内置切面

### @Loggable - 日志切面

自动记录方法调用日志：

```java
@Service
public class UserService {

    // 基本用法
    @Loggable
    public Future<User> findById(Long id) { ... }

    // 完整配置
    @Loggable(
        level = LogLevel.DEBUG,      // 日志级别
        includeArgs = true,          // 记录参数
        includeResult = true,        // 记录返回值
        includeTime = true,          // 记录执行时间
        prefix = "[UserService]"     // 日志前缀
    )
    public Future<List<User>> findAll() { ... }
}
```

输出示例：
```
INFO  [UserService] 调用方法 UserService.findById, 参数: [123]
INFO  [UserService] 方法 UserService.findById 完成 (耗时 15ms), 返回: User{id=123, name='John'}
```

### @Timed - 计时切面

自动统计方法执行时间：

```java
@Service
public class OrderService {

    // 基本用法
    @Timed
    public Future<Order> createOrder(OrderDTO dto) { ... }

    // 设置慢方法阈值
    @Timed(
        value = "order.create",      // 指标名称
        slowThreshold = 1000,        // 慢方法阈值（毫秒）
        unit = TimeUnit.MILLISECONDS,
        recordMetrics = true,        // 记录到指标系统
        tags = {"module=order", "type=create"}
    )
    public Future<Order> createOrderWithPayment(OrderDTO dto) { ... }
}
```

输出示例：
```
DEBUG [TIMING] OrderService.createOrder 执行时间 45ms
WARN  [SLOW] order.create 执行时间 1523 ms (阈值: 1000 ms)
```

## 自定义切面

### 事务切面示例

```java
@Aspect
@Order(5)  // 高优先级，确保在其他切面之前执行
public class TransactionAspect {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionAspect.class);

    @Inject
    private TransactionManager txManager;

    @Around("@annotation(cn.qaiu.vx.core.aop.Transactional)")
    public Object manageTransaction(ProceedingJoinPoint pjp) throws Throwable {
        LOG.debug("开启事务: {}", pjp.getSignature());
        
        return txManager.begin()
            .compose(tx -> {
                try {
                    Object result = pjp.proceed();
                    
                    if (result instanceof Future<?> future) {
                        return future
                            .compose(r -> tx.commit().map(v -> r))
                            .recover(e -> tx.rollback()
                                .compose(v -> Future.failedFuture(e)));
                    }
                    
                    return tx.commit().map(v -> result);
                    
                } catch (Throwable e) {
                    return tx.rollback()
                        .compose(v -> Future.failedFuture(e));
                }
            });
    }
}

// 使用
@Service
public class OrderService {

    @Transactional
    public Future<Order> createOrder(OrderDTO dto) {
        return orderDao.insert(dto)
            .compose(order -> inventoryService.decreaseStock(dto.getProductId())
                .map(v -> order));
    }
}
```

### 缓存切面示例

```java
@Aspect
@Order(20)
public class CacheAspect {

    @Inject
    private CacheService cacheService;

    @Around("@annotation(cn.qaiu.vx.core.aop.Cacheable)")
    public Object checkCache(ProceedingJoinPoint pjp) throws Throwable {
        Method method = pjp.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        
        String cacheKey = buildCacheKey(cacheable.value(), pjp.getArgs());
        
        // 先查缓存
        return cacheService.get(cacheKey)
            .recover(e -> {
                // 缓存未命中，执行方法
                try {
                    Object result = pjp.proceed();
                    
                    if (result instanceof Future<?> future) {
                        return future.compose(r -> 
                            cacheService.put(cacheKey, r, cacheable.ttl())
                                .map(v -> r));
                    }
                    
                    return cacheService.put(cacheKey, result, cacheable.ttl())
                        .map(v -> result);
                        
                } catch (Throwable ex) {
                    return Future.failedFuture(ex);
                }
            });
    }
}
```

### 限流切面示例

```java
@Aspect
@Order(1)  // 最高优先级
public class RateLimitAspect {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Before("@annotation(cn.qaiu.vx.core.aop.RateLimit)")
    public void checkRateLimit(JoinPoint jp) {
        Method method = jp.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        String key = method.toString();
        RateLimiter limiter = limiters.computeIfAbsent(key, 
            k -> RateLimiter.create(rateLimit.permitsPerSecond()));
        
        if (!limiter.tryAcquire()) {
            throw new RateLimitExceededException("请求过于频繁，请稍后再试");
        }
    }
}
```

## 与 Vert.x 异步集成

### 处理 Future 返回值

VXCore AOP 框架完美支持 Vert.x 的异步编程模型：

```java
@Aspect
public class AsyncAwareAspect {

    @Around("execution(Future cn.qaiu..*.*(..))")
    public Object handleAsync(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = pjp.proceed();
        
        // 检查是否为 Future
        if (result instanceof Future<?> future) {
            return future
                .map(r -> {
                    long duration = System.currentTimeMillis() - startTime;
                    LOG.info("异步方法完成: {}, 耗时: {}ms", 
                        pjp.getShortSignature(), duration);
                    return r;
                })
                .recover(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    LOG.error("异步方法失败: {}, 耗时: {}ms, 错误: {}", 
                        pjp.getShortSignature(), duration, e.getMessage());
                    return Future.failedFuture(e);
                });
        }
        
        return result;
    }
}
```

### 异步通知

```java
@Aspect
public class AsyncNotificationAspect {

    @Inject
    private EventBus eventBus;

    @AfterReturning(
        value = "execution(* cn.qaiu.*.service.OrderService.createOrder(..))",
        returning = "result"
    )
    public void notifyOrderCreated(JoinPoint jp, Object result) {
        if (result instanceof Future<?> future) {
            future.onSuccess(order -> {
                // 异步发送通知
                eventBus.publish("order.created", JsonObject.mapFrom(order));
            });
        }
    }
}
```

## 配置说明

### 完整配置选项

```yaml
aop:
  # 是否启用 AOP
  enabled: true
  
  # 切面扫描包路径
  scan-packages:
    - cn.qaiu.example.aspect
    - cn.qaiu.common.aspect
  
  # Byte Buddy Agent 配置
  agent:
    enabled: true  # 是否启用 Agent（用于类重定义）
```

### JVM 启动参数（可选）

如果需要在类加载前织入切面，可以使用 `-javaagent` 参数：

```bash
java -javaagent:/path/to/byte-buddy-agent.jar -jar your-app.jar
```

## 最佳实践

### 1. 合理设置切面优先级

```java
// 执行顺序：数值越小越先执行
@Aspect @Order(1)   // 限流（最先）
public class RateLimitAspect { }

@Aspect @Order(5)   // 事务
public class TransactionAspect { }

@Aspect @Order(10)  // 安全
public class SecurityAspect { }

@Aspect @Order(50)  // 缓存
public class CacheAspect { }

@Aspect @Order(100) // 日志（最后）
public class LoggingAspect { }
```

### 2. 避免切面中的副作用

```java
// ✅ 好的做法：只读操作
@Before("execution(* cn.qaiu.*.service.*.*(..))")
public void logMethod(JoinPoint jp) {
    LOG.info("调用: {}", jp.getSignature());  // 只记录日志
}

// ❌ 避免：修改参数或状态
@Before("execution(* cn.qaiu.*.service.*.*(..))")
public void badAdvice(JoinPoint jp) {
    Object[] args = jp.getArgs();
    args[0] = "modified";  // 不要修改参数！
}
```

### 3. 精确的切点表达式

```java
// ✅ 好的做法：精确匹配
@Around("execution(* cn.qaiu.example.service.UserService.findById(..))")

// ❌ 避免：过于宽泛
@Around("execution(* *.*(..))")  // 匹配所有方法，性能差
```

### 4. 处理异常

```java
@Around("execution(* cn.qaiu.*.service.*.*(..))")
public Object safeAround(ProceedingJoinPoint pjp) throws Throwable {
    try {
        return pjp.proceed();
    } catch (Throwable e) {
        // 记录日志但重新抛出，不要吞掉异常
        LOG.error("方法执行异常: {}", pjp.getSignature(), e);
        throw e;  // 重新抛出
    }
}
```

### 5. 性能考虑

```java
// 缓存反射结果
@Aspect
public class OptimizedAspect {
    
    private final Map<Method, Loggable> annotationCache = new ConcurrentHashMap<>();
    
    @Around("@annotation(cn.qaiu.vx.core.aop.Loggable)")
    public Object optimizedAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Loggable loggable = annotationCache.computeIfAbsent(
            pjp.getMethod(), 
            m -> m.getAnnotation(Loggable.class)
        );
        // ...
    }
}
```

## API 参考

### JoinPoint 接口

```java
public interface JoinPoint {
    Object getTarget();           // 获取目标对象
    Method getMethod();           // 获取目标方法
    Object[] getArgs();           // 获取方法参数
    String getSignature();        // 获取完整方法签名
    String getShortSignature();   // 获取简短方法签名
    Class<?> getTargetClass();    // 获取目标类
}
```

### ProceedingJoinPoint 接口

```java
public interface ProceedingJoinPoint extends JoinPoint {
    Object proceed() throws Throwable;           // 执行目标方法
    Object proceed(Object[] args) throws Throwable;  // 使用新参数执行
}
```

### AopUtils 工具类

```java
// 创建代理
UserService proxy = AopUtils.proxy(userService);

// 注册切面
AopUtils.registerAspect(MyAspect.class);

// 扫描切面
AopUtils.scanAspects("cn.qaiu.example.aspect");

// 初始化 AOP 框架
AopUtils.initialize();
```

## 相关文档

- [安全框架](06-security.md) - 安全相关切面
- [依赖注入](07-di-framework.md) - 切面与 DI 集成
- [配置管理](10-configuration.md) - AOP 配置选项
