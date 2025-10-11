# VXCore 代码规范

## 🎯 规范目标

本规范旨在约束 AI 代码生成，确保生成的代码符合 VXCore 项目的技术栈、架构设计和编码风格，避免生成不符合项目实际情况的代码。

## 📋 技术栈约束

### 核心框架
- **Java 17+**: 必须使用现代 Java 特性，不支持 Java 8
- **Vert.x 4.5+**: 基于事件驱动的异步非阻塞 I/O
- **jOOQ 3.19+**: 类型安全的 SQL 构建，编译时检查
- **Maven 3.8+**: 现代化构建工具
- **Dagger2**: 依赖注入框架

### 数据库支持
- **H2**: 开发、测试、演示环境
- **MySQL**: 生产环境推荐
- **PostgreSQL**: 企业级应用

### 开发工具
- **IDE**: IntelliJ IDEA / Eclipse
- **构建**: Maven
- **测试**: JUnit 5
- **文档**: Markdown

## 🏗️ 架构约束

### 设计思想：简单而不失优雅
- **简单**: 降低学习成本，提供直观的 API 设计
- **优雅**: 在简单基础上提供强大功能和良好扩展性
- **平衡**: 在简单性和功能性之间找到最佳平衡点

### 分层架构
```
Presentation Layer (表现层)
├── HTTP Client
├── WebSocket Client
└── API Client

Gateway Layer (网关层)
├── Router Handler Factory
├── Route Mapping
├── WebSocket Handler
└── Reverse Proxy

Application Layer (应用层)
├── Controller
├── Service
└── DAO

Infrastructure Layer (基础设施层)
├── Lambda Query Wrapper
├── Multi DataSource Manager
└── Executor Strategy

Data Layer (数据层)
├── Primary DB
├── Secondary DB
└── Log DB
```

## 📝 代码规范

### 1. 包命名规范

#### 核心包结构
```java
// 核心框架包
cn.qaiu.vx.core
├── annotations/          // 注解定义
├── di/                   // 依赖注入
├── util/                 // 工具类
├── verticle/             // Verticle 实现
├── handlerfactory/       // 处理器工厂
├── proxy/                // 反向代理
└── registry/             // 注册中心

// 数据库模块包
cn.qaiu.db
├── dsl/                  // DSL 框架
│   ├── lambda/           // Lambda 查询
│   └── core/             // 核心组件
├── datasource/           // 多数据源支持
└── spi/                  // SPI 扩展
```

#### 包命名规则
- 使用小写字母，多个单词用点分隔
- 包名要有明确的业务含义
- 避免使用缩写，除非是广泛认知的缩写

### 2. 类命名规范

#### 注解类
```java
// 组件注解
@Component
@Service
@Repository
@Dao
@Controller

// 路由注解
@RouteHandler
@RouteMapping
@WebSocketHandler

// 参数注解
@RequestParam
@PathVariable
@RequestBody

// 配置注解
@ConfigurationProperties
@ConfigurationProperty
```

#### 工具类
```java
// 工具类以 Util 结尾
public class StringCaseUtil
public class ParamUtil
public class ConfigUtil
public class ReflectionUtil
public class AnnotationNameGenerator
public class AutoScanPathDetector
```

#### 工厂类
```java
// 工厂类以 Factory 结尾
public class RouterHandlerFactory
public class WebSocketHandlerFactory
public class DataSourceProviderFactory
```

#### 管理器类
```java
// 管理器类以 Manager 结尾
public class DataSourceManager
public class ExceptionManager
public class ConfigManager
```

### 3. 方法命名规范

#### 查询方法
```java
// 查询方法使用 find/get 前缀
public Future<User> findById(Long id)
public Future<List<User>> findActiveUsers()
public Future<Optional<User>> getUserByEmail(String email)
public Future<PageResult<User>> getUsers(UserQuery query)
```

#### 操作方法
```java
// 操作方法使用 create/update/delete 前缀
public Future<User> createUser(User user)
public Future<User> updateUser(User user)
public Future<Void> deleteUser(Long id)
public Future<Void> deleteUsers(List<Long> ids)
```

#### 验证方法
```java
// 验证方法使用 validate/check 前缀
public boolean validateUser(User user)
public boolean checkPermission(String permission)
public boolean isValidEmail(String email)
```

#### 转换方法
```java
// 转换方法使用 convert/transform 前缀
public String convertToString(Object obj)
public User transformToEntity(UserDto dto)
public List<UserDto> convertToDtoList(List<User> users)
```

### 4. 变量命名规范

#### 常量
```java
// 常量使用全大写，下划线分隔
private static final String DEFAULT_SCAN_PACKAGE = "cn.qaiu";
private static final int MAX_RETRY_COUNT = 3;
private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);
```

#### 成员变量
```java
// 成员变量使用驼峰命名
private final Map<String, Class<?>> annotatedClasses;
private final DataSourceManager dataSourceManager;
private final ReflectionUtil reflectionUtil;
```

#### 局部变量
```java
// 局部变量使用驼峰命名
String className = clazz.getName();
Set<String> scanPaths = new HashSet<>();
List<Class<?>> controllers = new ArrayList<>();
```

#### 集合变量
```java
// 集合变量使用复数形式
List<User> users = new ArrayList<>();
Set<String> names = new HashSet<>();
Map<String, Object> properties = new HashMap<>();
```

### 5. 注解使用规范

#### 组件注解
```java
// Service 层
@Service
public class UserService {
    // 业务逻辑
}

// DAO 层
@Dao
public class UserDao extends AbstractDao<User> {
    // 数据访问逻辑
}

// Controller 层
@Controller
@RouteHandler("/api")
public class UserController {
    // 控制器逻辑
}
```

#### 路由注解
```java
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping(value = "/users", method = RouteMethod.GET)
    public Future<JsonResult> getUsers(@RequestParam("page") int page) {
        // 处理逻辑
    }
    
    @RouteMapping(value = "/users", method = RouteMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) {
        // 处理逻辑
    }
}
```

#### 配置注解
```java
@ConfigurationProperties(prefix = "datasource")
public class DataSourceConfig {
    
    @ConfigurationProperty("url")
    private String url;
    
    @ConfigurationProperty("username")
    private String username;
    
    @ConfigurationProperty("password")
    private String password;
}
```

### 6. 异常处理规范

#### 异常类命名
```java
// 业务异常以 Exception 结尾
public class BusinessException extends RuntimeException
public class ValidationException extends RuntimeException
public class DatabaseException extends RuntimeException
public class ConfigurationException extends RuntimeException
```

#### 异常处理方式
```java
// 使用 Future 的 recover 方法处理异常
public Future<User> createUser(User user) {
    return userDao.create(user)
        .recover(throwable -> {
            log.error("Failed to create user", throwable);
            return Future.failedFuture(new BusinessException("用户创建失败"));
        });
}

// 使用全局异常处理器
@ExceptionHandler(ValidationException.class)
public JsonResult handleValidation(ValidationException e) {
    return JsonResult.fail(400, e.getMessage());
}
```

### 7. 日志规范

#### 日志级别使用
```java
// DEBUG: 调试信息
LOGGER.debug("Processing request: {}", request);

// INFO: 重要信息
LOGGER.info("Application started successfully");
LOGGER.info("Scan paths detected: {}", scanPaths);

// WARN: 警告信息
LOGGER.warn("Invalid scan package: {}, skipping", packageName);
LOGGER.warn("Cannot determine package name from main class: {}", className);

// ERROR: 错误信息
LOGGER.error("Failed to create user", throwable);
LOGGER.error("Database connection failed", e);
```

#### 日志格式
```java
// 使用参数化日志，避免字符串拼接
LOGGER.info("User {} created successfully", user.getName());
LOGGER.error("Failed to process request {}: {}", requestId, errorMessage);

// 异常日志包含异常对象
LOGGER.error("Database operation failed", throwable);
```

### 8. 注释规范

#### 类注释
```java
/**
 * 自动扫描路径检测器
 * 根据启动类的位置自动配置扫描路径
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AutoScanPathDetector {
    // 类实现
}
```

#### 方法注释
```java
/**
 * 根据启动类自动检测扫描路径
 *
 * @param mainClass 启动类
 * @return 扫描路径集合
 */
public static Set<String> detectScanPaths(Class<?> mainClass) {
    // 方法实现
}
```

#### 字段注释
```java
/**
 * 日志记录器
 */
private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);

/**
 * 注解类映射
 */
private final Map<String, Set<Class<?>>> annotatedClassesMap;
```

## 🔧 框架特定规范

### 1. Vert.x 规范

#### 异步编程
```java
// 使用 Future 进行异步编程
public Future<User> createUser(User user) {
    return userDao.create(user)
        .compose(createdUser -> {
            // 异步处理后续逻辑
            return sendWelcomeEmail(createdUser);
        })
        .recover(throwable -> {
            // 错误处理
            return Future.failedFuture(new BusinessException("用户创建失败"));
        });
}
```

#### 路由处理
```java
// 路由处理器必须返回 Future
@RouteMapping("/users")
public Future<JsonResult> getUsers() {
    return userService.findUsers()
        .map(users -> JsonResult.success(users));
}
```

### 2. jOOQ 规范

#### Lambda 查询
```java
// 使用 Lambda 表达式进行类型安全查询
public Future<List<User>> findActiveUsers() {
    return userDao.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .like(User::getName, "张%")
        .orderBy(User::getCreateTime, SortOrder.DESC)
        .list();
}
```

#### 批量操作
```java
// 批量操作使用 batch 前缀
public Future<int[]> batchInsertUsers(List<User> users) {
    return userDao.batchInsert(users);
}

public Future<int[]> batchUpdateUsers(List<User> users) {
    return userDao.batchUpdate(users);
}
```

### 3. Dagger2 规范

#### 组件定义
```java
@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {
    
    void inject(ServiceVerticle serviceVerticle);
    
    Set<Class<?>> serviceClasses();
    
    @Named("Dao")
    Set<Class<?>> daoClasses();
}
```

#### 模块定义
```java
@Module
public class ServiceModule {
    
    @Provides
    @Singleton
    public Set<Class<?>> provideServiceClasses() {
        Reflections reflections = ReflectionUtil.getReflections();
        return reflections.getTypesAnnotatedWith(Service.class);
    }
}
```

## 🚫 禁止事项

### 1. 技术栈禁止
- ❌ 禁止使用 Spring Boot 相关注解和类
- ❌ 禁止使用 MyBatis 相关注解和类
- ❌ 禁止使用 Hibernate 相关注解和类
- ❌ 禁止使用 Java 8 之前的特性
- ❌ 禁止使用同步阻塞的数据库操作

### 2. 命名禁止
- ❌ 禁止使用中文命名
- ❌ 禁止使用拼音命名
- ❌ 禁止使用无意义的缩写
- ❌ 禁止使用下划线命名（除了常量）

### 3. 代码结构禁止
- ❌ 禁止在 Controller 中直接操作数据库
- ❌ 禁止在 Service 中处理 HTTP 请求
- ❌ 禁止在 DAO 中包含业务逻辑
- ❌ 禁止使用同步方法处理异步操作

### 4. 异常处理禁止
- ❌ 禁止吞掉异常（catch 后不处理）
- ❌ 禁止使用 System.out.println 输出错误信息
- ❌ 禁止在业务代码中直接抛出 RuntimeException

## ✅ 推荐做法

### 1. 代码组织
- ✅ 使用包结构清晰组织代码
- ✅ 每个类职责单一，功能明确
- ✅ 使用接口定义契约，实现类提供具体实现
- ✅ 使用枚举定义常量集合

### 2. 错误处理
- ✅ 使用 Future 的 recover 方法处理异常
- ✅ 定义具体的业务异常类
- ✅ 使用全局异常处理器统一处理
- ✅ 记录详细的错误日志

### 3. 性能优化
- ✅ 使用连接池管理数据库连接
- ✅ 使用批量操作处理大量数据
- ✅ 使用缓存减少重复计算
- ✅ 使用异步操作提高并发性能

### 4. 测试规范
- ✅ 编写单元测试覆盖核心逻辑
- ✅ 使用 Mock 对象隔离依赖
- ✅ 测试异常情况的处理
- ✅ 保持测试代码的可读性

## 📚 相关文档

- [项目概述](01-overview.md) - 项目介绍和核心特性
- [系统架构](04-architecture.md) - 整体架构设计
- [开发指南](05-developer-guide.md) - 开发者指南
- [Lambda 查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - Lambda 查询详解
- [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 多数据源配置

---

**🎯 遵循本规范，确保生成的代码符合 VXCore 项目要求！**
