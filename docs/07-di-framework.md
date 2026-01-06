# 依赖注入框架

VXCore 集成了 Dagger2 作为依赖注入（DI）框架，提供编译时依赖注入，具有高性能和类型安全的特点。

## 📋 目录

- [概述](#概述)
- [快速开始](#快速开始)
- [核心注解](#核心注解)
- [组件定义](#组件定义)
- [模块配置](#模块配置)
- [作用域管理](#作用域管理)
- [与 VXCore 集成](#与-vxcore-集成)
- [最佳实践](#最佳实践)

## 概述

### 为什么选择 Dagger2？

| 特性 | Dagger2 | Spring DI | Guice |
|------|---------|-----------|-------|
| 注入时机 | 编译时 | 运行时 | 运行时 |
| 性能 | 最优 | 较慢 | 中等 |
| 错误检测 | 编译时 | 运行时 | 运行时 |
| 包大小 | 最小 | 较大 | 中等 |
| 学习曲线 | 中等 | 低 | 中等 |

Dagger2 的编译时注入特别适合 Vert.x 的高性能异步架构。

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                   ServiceComponent                      │
│              (Dagger2 顶层组件)                          │
├─────────────────────────────────────────────────────────┤
│  @Singleton                                             │
│  ├── ServiceModule (核心服务扫描)                        │
│  ├── DatabaseModule (数据库连接)                         │
│  └── SecurityModule (安全认证)                          │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    注解扫描                              │
│  @Service │ @Dao │ @Repository │ @Component │ @Controller│
└─────────────────────────────────────────────────────────┘
```

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.google.dagger</groupId>
    <artifactId>dagger</artifactId>
    <version>2.57.2</version>
</dependency>
<dependency>
    <groupId>com.google.dagger</groupId>
    <artifactId>dagger-compiler</artifactId>
    <version>2.57.2</version>
    <scope>provided</scope>
</dependency>
```

### 2. 定义服务

```java
@Service
public class UserService {

    private final UserDao userDao;
    private final CacheService cacheService;

    @Inject
    public UserService(UserDao userDao, CacheService cacheService) {
        this.userDao = userDao;
        this.cacheService = cacheService;
    }

    public Future<User> findById(Long id) {
        return cacheService.get("user:" + id)
            .recover(e -> userDao.findById(id)
                .compose(user -> cacheService.put("user:" + id, user)
                    .map(v -> user)));
    }
}
```

### 3. 定义 DAO

```java
@Dao
public class UserDao {

    private final DSLContextProvider dslProvider;

    @Inject
    public UserDao(DSLContextProvider dslProvider) {
        this.dslProvider = dslProvider;
    }

    public Future<User> findById(Long id) {
        return dslProvider.withContext(ctx -> 
            ctx.selectFrom(USER)
               .where(USER.ID.eq(id))
               .fetchOneInto(User.class));
    }
}
```

### 4. 使用依赖注入

```java
@RouteHandler("/api/users")
public class UserController {

    @Inject
    UserService userService;

    @GetRoute("/{id}")
    public Future<User> getUser(@PathParam Long id) {
        return userService.findById(id);
    }
}
```

## 核心注解

### VXCore 组件注解

VXCore 提供了一组语义化的组件注解：

| 注解 | 用途 | 对应层 |
|------|------|--------|
| `@Service` | 业务服务 | Service 层 |
| `@Dao` | 数据访问对象 | DAO 层 |
| `@Repository` | 数据仓库 | Repository 层 |
| `@Component` | 通用组件 | 基础设施层 |
| `@Controller` | 控制器 | Controller 层 |

```java
// Service 层
@Service
public class OrderService {
    @Inject OrderDao orderDao;
    @Inject InventoryService inventoryService;
}

// DAO 层
@Dao
public class OrderDao {
    @Inject DSLContextProvider dslProvider;
}

// Repository 层（更高级的数据访问抽象）
@Repository
public class OrderRepository {
    @Inject OrderDao orderDao;
    @Inject CacheService cacheService;
}

// 通用组件
@Component
public class EmailSender {
    @Inject EmailConfig config;
}
```

### Dagger2 标准注解

| 注解 | 说明 |
|------|------|
| `@Inject` | 标记需要注入的构造函数、字段或方法 |
| `@Module` | 标记提供依赖的模块类 |
| `@Provides` | 标记模块中提供依赖的方法 |
| `@Singleton` | 单例作用域 |
| `@Named` | 命名限定符，区分同类型的不同实例 |

## 组件定义

### 核心组件接口

```java
@Singleton
@Component(modules = {ServiceModule.class, DatabaseModule.class})
public interface ServiceComponent {

    // 注入目标
    void inject(ServiceVerticle serviceVerticle);
    void inject(UserController userController);

    // 提供依赖
    Set<Class<?>> serviceClasses();
    
    @Named("Dao")
    Set<Class<?>> daoClasses();
    
    UserService userService();
    OrderService orderService();
}
```

### 使用组件

```java
public class Application {

    public static void main(String[] args) {
        // 创建组件实例（编译时生成）
        ServiceComponent component = DaggerServiceComponent.create();
        
        // 获取依赖
        UserService userService = component.userService();
        
        // 注入到目标对象
        ServiceVerticle verticle = new ServiceVerticle();
        component.inject(verticle);
    }
}
```

## 模块配置

### 基础模块

```java
@Module
public class ServiceModule {

    @Provides
    @Singleton
    public Set<Class<?>> provideServiceClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Service.class);
    }

    @Provides
    @Singleton
    @Named("Dao")
    public Set<Class<?>> provideDaoClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Dao.class);
    }
}
```

### 数据库模块

```java
@Module
public class DatabaseModule {

    @Provides
    @Singleton
    public DSLContextProvider provideDSLContextProvider(Vertx vertx, JsonObject config) {
        return new DSLContextProvider(vertx, config);
    }

    @Provides
    @Singleton
    public SqlClient provideSqlClient(Vertx vertx, JsonObject config) {
        return MySQLPool.pool(vertx, new MySQLConnectOptions()
            .setHost(config.getString("host"))
            .setPort(config.getInteger("port"))
            .setDatabase(config.getString("database"))
            .setUser(config.getString("user"))
            .setPassword(config.getString("password")),
            new PoolOptions().setMaxSize(10));
    }
}
```

### 配置模块

```java
@Module
public class ConfigModule {

    private final JsonObject config;

    public ConfigModule(JsonObject config) {
        this.config = config;
    }

    @Provides
    @Singleton
    public JsonObject provideConfig() {
        return config;
    }

    @Provides
    @Singleton
    @Named("database")
    public JsonObject provideDatabaseConfig() {
        return config.getJsonObject("database", new JsonObject());
    }

    @Provides
    @Singleton
    @Named("security")
    public JsonObject provideSecurityConfig() {
        return config.getJsonObject("security", new JsonObject());
    }
}
```

## 作用域管理

### 单例作用域

```java
@Singleton
@Service
public class CacheService {
    // 全局唯一实例
}
```

### 自定义作用域

```java
// 定义请求作用域
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestScoped {}

// 定义请求级别的子组件
@RequestScoped
@Subcomponent(modules = RequestModule.class)
public interface RequestComponent {
    RequestContext requestContext();
}

// 在主组件中声明子组件工厂
@Singleton
@Component(modules = ServiceModule.class)
public interface ServiceComponent {
    RequestComponent.Factory requestComponentFactory();
}

// 使用
RequestComponent requestComponent = serviceComponent
    .requestComponentFactory()
    .create(new RequestModule(routingContext));
```

## 与 VXCore 集成

### 自动扫描与注册

VXCore 自动扫描带有组件注解的类并注册到 DI 容器：

```java
// 框架自动处理
@Service
public class UserService { }

@Dao  
public class UserDao { }

// 无需手动注册，框架启动时自动完成
```

### 控制器注入

```java
@RouteHandler("/api/users")
public class UserController {

    @Inject
    private UserService userService;

    @Inject
    private SecurityContext securityContext;

    @Authenticated
    @GetRoute("/profile")
    public Future<User> getProfile() {
        Long userId = securityContext.getUserId();
        return userService.findById(userId);
    }
}
```

### 服务代理注入

```java
@Service
public class OrderService {

    @Inject
    private EventBus eventBus;  // Vert.x EventBus 注入

    @Inject  
    @ProxyGen
    private InventoryService inventoryService;  // 服务代理注入

    public Future<Order> createOrder(OrderDTO dto) {
        return inventoryService.checkStock(dto.getProductId())
            .compose(available -> {
                if (!available) {
                    return Future.failedFuture("库存不足");
                }
                return saveOrder(dto);
            });
    }
}
```

### 与 AOP 集成

```java
@Service
public class UserService {

    @Inject
    private UserDao userDao;

    @Loggable(includeArgs = true)  // AOP 日志切面
    @Timed(slowThreshold = 500)    // AOP 计时切面
    public Future<User> findById(Long id) {
        return userDao.findById(id);
    }
}
```

## 最佳实践

### 1. 使用构造函数注入

```java
// ✅ 推荐：构造函数注入
@Service
public class UserService {
    private final UserDao userDao;
    private final CacheService cacheService;

    @Inject
    public UserService(UserDao userDao, CacheService cacheService) {
        this.userDao = userDao;
        this.cacheService = cacheService;
    }
}

// ❌ 避免：字段注入（测试困难）
@Service
public class UserService {
    @Inject UserDao userDao;
    @Inject CacheService cacheService;
}
```

### 2. 接口抽象

```java
// 定义接口
public interface UserRepository {
    Future<User> findById(Long id);
    Future<List<User>> findAll();
}

// 实现类
@Repository
public class UserRepositoryImpl implements UserRepository {
    @Inject
    public UserRepositoryImpl(UserDao userDao) { ... }
}

// 模块绑定
@Module
public abstract class RepositoryModule {
    @Binds
    abstract UserRepository bindUserRepository(UserRepositoryImpl impl);
}
```

### 3. 延迟初始化

```java
@Service
public class HeavyService {

    private final Provider<ExpensiveResource> resourceProvider;

    @Inject
    public HeavyService(Provider<ExpensiveResource> resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public void doWork() {
        // 只在需要时才创建
        ExpensiveResource resource = resourceProvider.get();
        resource.process();
    }
}
```

### 4. 条件依赖

```java
@Module
public class ConditionalModule {

    @Provides
    @Singleton
    public CacheService provideCacheService(JsonObject config) {
        String cacheType = config.getString("cache.type", "memory");
        
        return switch (cacheType) {
            case "redis" -> new RedisCacheService(config);
            case "memcached" -> new MemcachedCacheService(config);
            default -> new InMemoryCacheService();
        };
    }
}
```

### 5. 测试支持

```java
// 测试模块
@Module
public class TestModule {

    @Provides
    @Singleton
    public UserDao provideUserDao() {
        return Mockito.mock(UserDao.class);
    }
}

// 测试组件
@Singleton
@Component(modules = {ServiceModule.class, TestModule.class})
public interface TestComponent extends ServiceComponent {
    UserDao userDao();  // 获取 Mock 对象
}

// 测试类
class UserServiceTest {
    
    private TestComponent component;
    
    @BeforeEach
    void setUp() {
        component = DaggerTestComponent.create();
    }
    
    @Test
    void testFindById() {
        UserDao mockDao = component.userDao();
        when(mockDao.findById(1L)).thenReturn(Future.succeededFuture(testUser));
        
        UserService service = component.userService();
        // 测试...
    }
}
```

## 常见问题

### Q: 编译时报错 "Cannot find symbol: class DaggerXXXComponent"

确保已正确配置注解处理器：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.google.dagger</groupId>
                <artifactId>dagger-compiler</artifactId>
                <version>2.57.2</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Q: 如何处理循环依赖？

使用 `Provider<T>` 或 `Lazy<T>` 打破循环：

```java
@Service
public class ServiceA {
    private final Provider<ServiceB> serviceBProvider;

    @Inject
    public ServiceA(Provider<ServiceB> serviceBProvider) {
        this.serviceBProvider = serviceBProvider;
    }

    public void callB() {
        serviceBProvider.get().doSomething();
    }
}
```

## 相关文档

- [系统架构](04-architecture.md) - 整体架构设计
- [AOP 指南](15-aop-guide.md) - 切面编程与 DI 集成
- [安全框架](06-security.md) - 安全组件注入
