# 🎯 VXCore Database Module - 现代化数据库访问框架

## 概述

VXCore Database 模块是一个基于 **jOOQ DSL** 和 **Vert.x SQL Client** 的现代化数据库访问框架，提供类型安全、高性能、异步的数据库操作。

## 🚀 核心特性

### ✅ Lambda 查询增强
- **类似 MyBatis-Plus 的 Lambda 表达式**：支持 `User::getName` 类型安全的字段引用
- **Join 查询支持**：leftJoin、innerJoin、rightJoin、fullJoin
- **聚合查询**：groupBy、having、selectCount、selectSum、selectAvg 等
- **子查询支持**：exists、notExists、inSubQuery、notInSubQuery

### ✅ 多数据源支持
- **动态数据源切换**：支持运行时切换不同数据源
- **事务隔离**：每个数据源独立的事务管理
- **配置化**：支持 YAML 配置多数据源
- **注解支持**：`@DataSource` 注解指定数据源

### ✅ 批量操作优化
- **高性能批量操作**：batchInsert、batchUpdate、batchDelete
- **批量 UPSERT**：支持批量插入或更新
- **连接池优化**：使用连接池提升批量操作性能
- **事务一致性**：保证批量操作的事务完整性

### ⚡ Vert.x 异步执行
- **非阻塞数据库操作**：基于 Vert.x SQL Client 4.5+
- **Connection Pooling**：内置连接池管理
- **Future 链式调用**：支持 compose、flatMap、recover 等组合操作
- **高性能**：单线程非阻塞 I/O

## 📁 项目结构

```
core-database/src/main/java/cn/qaiu/db/
├── dsl/                           # DSL 框架
│   ├── lambda/                    # Lambda 查询
│   │   ├── LambdaQueryWrapper.java    # Lambda 查询包装器
│   │   ├── LambdaDao.java            # Lambda DAO 接口
│   │   ├── LambdaUtils.java          # Lambda 工具类
│   │   └── SFunction.java            # Lambda 函数接口
│   ├── core/                      # 核心组件
│   │   ├── AbstractDao.java          # 抽象 DAO 基类
│   │   ├── JooqExecutor.java         # jOOQ 执行器
│   │   └── executor/                # 执行器策略
│   │       ├── ExecutorStrategy.java
│   │       └── AbstractExecutorStrategy.java
│   └── common/                    # 通用工具
│       ├── PageRequest.java          # 分页请求
│       ├── PageResult.java           # 分页结果
│       └── FieldNameConverter.java   # 字段名转换器
├── datasource/                    # 多数据源支持
│   ├── DataSource.java             # 数据源注解
│   ├── DataSourceProvider.java     # 数据源提供者
│   ├── DataSourceConfig.java       # 数据源配置
│   ├── DataSourceManager.java      # 数据源管理器
│   ├── DataSourceContext.java      # 数据源上下文
│   └── DataSourceConfigLoader.java # 配置加载器
├── spi/                           # SPI 扩展
│   ├── DatabaseDriver.java         # 数据库驱动接口
│   └── DialectProvider.java        # 方言提供者
└── docs/                          # 文档
    ├── README.md                   # 模块说明
    ├── lambda/                     # Lambda 查询文档
    ├── MULTI_DATASOURCE_GUIDE.md   # 多数据源指南
    └── PARALLEL_DEVELOPMENT_SUMMARY.md # 开发总结
```

## 🔧 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cn.qaiu</groupId>
    <artifactId>vxcore-database</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 1. 定义实体类

```java
@DdlTable("users")
public class User extends BaseEntity {
    @DdlColumn("user_name")
    private String name;
    
    @DdlColumn("user_email")
    private String email;
    
    @DdlColumn("user_status")
    private String status;
    
    // getters and setters
}
```

### 2. 创建 DAO

```java
public class UserDao extends AbstractDao<User> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    // Lambda 查询示例
    public Future<List<User>> findActiveUsers() {
        return lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .like(User::getName, "张%")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
    
    // Join 查询示例
    public Future<List<User>> findUsersWithOrders() {
        return lambdaQuery()
            .leftJoin(Order.class, (user, order) -> 
                user.getId().eq(order.getUserId()))
            .eq(User::getStatus, "ACTIVE")
            .list();
    }
    
    // 聚合查询示例
    public Future<List<Map<String, Object>>> getUserStats() {
        return lambdaQuery()
            .select(User::getStatus, DSL.count())
            .groupBy(User::getStatus)
            .having(DSL.count().gt(10))
            .list();
    }
}
```

### 3. 多数据源配置

```yaml
# application.yml
datasources:
  primary:
    url: jdbc:mysql://localhost:3306/main_db
    username: root
    password: password
    driver: com.mysql.cj.jdbc.Driver
  secondary:
    url: jdbc:postgresql://localhost:5432/log_db
    username: postgres
    password: password
    driver: org.postgresql.Driver
```

```java
@DataSource("primary")
public class UserDao extends AbstractDao<User> {
    
    @DataSource("secondary")
    public Future<List<Log>> findUserLogs(Long userId) {
        return logDao.lambdaQuery()
            .eq(Log::getUserId, userId)
            .list();
    }
}
```

### 4. 批量操作示例

```java
public class UserService {
    
    public Future<List<User>> batchCreateUsers(List<User> users) {
        return userDao.batchInsert(users);
    }
    
    public Future<List<User>> batchUpdateUsers(List<User> users) {
        return userDao.batchUpdate(users);
    }
    
    public Future<Boolean> batchDeleteUsers(List<Long> userIds) {
        return userDao.batchDelete(userIds);
    }
    
    public Future<List<User>> batchUpsertUsers(List<User> users) {
        return userDao.batchUpsert(users);
    }
}
```

### 5. 在 Verticle 中使用

```java
public class UserVerticle extends AbstractVerticle {
    private UserDao userDao;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 初始化数据源
        DataSourceConfigLoader.loadFromFile("application.yml")
            .compose(configs -> {
                // 注册数据源
                DataSourceManager.registerDataSources(configs);
                
                // 创建 DAO
                JooqExecutor executor = DataSourceManager.getExecutor("primary");
                userDao = new UserDao(executor);
                
                return Future.succeededFuture();
            })
            .onComplete(startPromise);
    }
    
    // 业务方法示例
    private Future<User> createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setStatus("ACTIVE");
        
        return userDao.create(user);
    }
    
    private Future<List<User>> searchUsers(String keyword) {
        return userDao.lambdaQuery()
            .like(User::getName, "%" + keyword + "%")
            .or()
            .like(User::getEmail, "%" + keyword + "%")
            .list();
    }
}
```

## 🎯 核心特性详解

### 1. Lambda 查询增强

```java
// 类型安全的字段引用
public Future<List<User>> findActiveUsers() {
    return userDao.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .like(User::getName, "张%")
        .orderBy(User::getCreateTime, SortOrder.DESC)
        .list();
}

// Join 查询
public Future<List<User>> findUsersWithOrders() {
    return userDao.lambdaQuery()
        .leftJoin(Order.class, (user, order) -> 
            user.getId().eq(order.getUserId()))
        .eq(User::getStatus, "ACTIVE")
        .list();
}

// 聚合查询
public Future<List<Map<String, Object>>> getUserStats() {
    return userDao.lambdaQuery()
        .select(User::getStatus, DSL.count())
        .groupBy(User::getStatus)
        .having(DSL.count().gt(10))
        .list();
}

// 子查询
public Future<List<User>> findUsersWithOrders() {
    return userDao.lambdaQuery()
        .exists(Order.class, (order) -> 
            order.getUserId().eq(User::getId))
        .list();
}
```

### 2. 多数据源支持

```java
// 配置多数据源
@ConfigurationProperties(prefix = "datasources")
public class DataSourceConfigs {
    private Map<String, DataSourceConfig> configs;
}

// 使用注解切换数据源
@DataSource("primary")
public class UserDao extends AbstractDao<User> {
    
    @DataSource("secondary")
    public Future<List<Log>> findUserLogs(Long userId) {
        return logDao.lambdaQuery()
            .eq(Log::getUserId, userId)
            .list();
    }
}

// 动态切换数据源
public Future<List<User>> findUsersFromSecondary() {
    DataSourceContext.setDataSourceName("secondary");
    try {
        return userDao.findAll();
    } finally {
        DataSourceContext.clearDataSourceName();
    }
}
```

### 3. 批量操作优化

```java
// 高性能批量插入
public Future<List<User>> batchCreateUsers(List<User> users) {
    return userDao.batchInsert(users)
        .onSuccess(result -> log.info("批量插入 {} 条用户记录", result.size()))
        .onFailure(throwable -> log.error("批量插入失败", throwable));
}

// 批量更新
public Future<List<User>> batchUpdateUsers(List<User> users) {
    return userDao.batchUpdate(users);
}

// 批量删除
public Future<Boolean> batchDeleteUsers(List<Long> userIds) {
    return userDao.batchDelete(userIds);
}

// 批量 UPSERT
public Future<List<User>> batchUpsertUsers(List<User> users) {
    return userDao.batchUpsert(users);
}
```

### 4. 异步非阻塞操作

```java
// 所有操作都返回 Future，支持链式调用
public Future<List<User>> findUsers() {
    return userDao.findAll();
}

// 支持组合操作
public Future<User> getUserWithProfile(Long userId) {
    return userDao.findById(userId)
        .compose(userOptional -> {
            if (userOptional.isPresent()) {
                return getUserProfile(userOptional.get())
                    .map(profile -> {
                        userOptional.get().setProfile(profile);
                        return userOptional.get();
                    });
            }
            return Future.failedFuture("User not found");
        });
}

// 事务支持
public Future<Void> createUserWithProfile(User user, UserProfile profile) {
    return userDao.executor.pool().getConnection()
        .compose(conn -> {
            return conn.begin()
                .compose(tx -> {
                    return userDao.insert(user)
                        .compose(insertedUser -> {
                            if (insertedUser.isPresent()) {
                                profile.setUserId(insertedUser.get().getId());
                                return profileDao.insert(profile)
                                    .map(Optional::get);
                            }
                            return Future.failedFuture("Failed to insert user");
                        })
                        .compose(v -> tx.commit().mapEmpty())
                        .onFailure(err -> tx.rollback().mapEmpty());
                })
                .onComplete(conn::close);
        });
}
```

## 📋 CRUD 操作

### 增 (Create)

```java
User user = new User();
user.setName("john_doe");
user.setEmail("john@example.com");
user.setStatus("ACTIVE");

// 插入单个用户
Future<User> insertResult = userDao.create(user);

// 批量插入用户
Future<List<User>> batchInsertResult = userDao.batchInsert(userList);
```

### 查 (Read)

```java
// 根据 ID 查询
Future<Optional<User>> userOptional = userDao.findById(1L);

// 查询所有用户
Future<List<User>> allUsers = userDao.findAll();

// Lambda 查询
Future<List<User>> activeUsers = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .list();

// 分页查询
Future<PageResult<User>> pageUsers = userDao.findPage(pageRequest, null);
```

### 改 (Update)

```java
User user = userOptional.get();
user.setEmail("newemail@example.com");

// 更新单个用户
Future<User> updateResult = userDao.update(user);

// 批量更新用户
Future<List<User>> batchUpdateResult = userDao.batchUpdate(userList);
```

### 删 (Delete)

```java
// 根据 ID 删除
Future<Boolean> deleteResult = userDao.delete(1L);

// 批量删除
Future<Boolean> batchDeleteResult = userDao.batchDelete(Arrays.asList(1L, 2L, 3L));

// 条件删除
Future<Boolean> conditionDeleteResult = userDao.batchDeleteByCondition(
    DSL.field("status").eq("INACTIVE")
);
```

## 🔧 配置

### 多数据源配置

```yaml
# application.yml
datasources:
  primary:
    url: jdbc:mysql://localhost:3306/main_db
    username: root
    password: password
    driver: com.mysql.cj.jdbc.Driver
    maxPoolSize: 20
    minPoolSize: 5
    connectionTimeout: 30000
  secondary:
    url: jdbc:postgresql://localhost:5432/log_db
    username: postgres
    password: password
    driver: org.postgresql.Driver
    maxPoolSize: 10
    minPoolSize: 2
    connectionTimeout: 30000
  h2:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
    driver: org.h2.Driver
    maxPoolSize: 5
    minPoolSize: 1
```

### 数据源配置类

```java
@ConfigurationProperties(prefix = "datasources")
public class DataSourceConfigs {
    private Map<String, DataSourceConfig> configs;
    
    // getters and setters
}

public class DataSourceConfig {
    private String url;
    private String username;
    private String password;
    private String driver;
    private int maxPoolSize = 20;
    private int minPoolSize = 5;
    private long connectionTimeout = 30000;
    
    // getters and setters
}
```

### 连接池配置

```java
public class PoolConfig {
    public static PoolOptions createPoolOptions(DataSourceConfig config) {
        return new PoolOptions()
            .setMaxSize(config.getMaxPoolSize())
            .setMinSize(config.getMinPoolSize())
            .setMaxWaitQueueSize(10)
            .setMaxWaitTime(config.getConnectionTimeout())
            .setEvictionInterval(0)
            .setCachePreparedStatements(true);
    }
}
```

## 📊 表结构要求

### 必需的列

每个实体类对应的表必须包含以下列：

```sql
CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 字段命名规则

- **驼峰命名**：实体类字段使用 camelCase（如 `firstName`）
- **下划线命名**：数据库列使用 snake_case（如 `first_name`）
- **自动转换**：框架自动处理驼峰转下划线
- **手动指定**：使用 `@DdlColumn(name = "...")` 指定列名

### 字段名转换器

```java
public class FieldNameConverter {
    
    /**
     * Java字段名转数据库字段名
     */
    public static String toDatabaseField(String javaField) {
        return StringCase.toUnderlineCase(javaField);
    }
    
    /**
     * 数据库字段名转Java字段名
     */
    public static String toJavaField(String databaseField) {
        return StringCase.toCamelCase(databaseField);
    }
}
```

## 📝 实体类要求

### 实体类定义

```java
@DdlTable("users")
public class User extends BaseEntity {
    
    @DdlColumn("user_name")
    private String name;
    
    @DdlColumn("user_email")
    private String email;
    
    @DdlColumn("user_status")
    private String status;
    
    // getters and setters
}
```

### BaseEntity

框架提供 `BaseEntity` 基类，自动处理实体映射：

```java
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 生命周期回调
    @PrePersist
    public void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    // getters and setters
}
```

### DDL 注解

```java
@DdlTable("users")
public class User extends BaseEntity {
    
    // 使用 value 作为 name 的别名
    @DdlColumn(value = "user_name")
    private String name;
    
    // 自动转换为下划线命名
    private String email; // 对应 email 列
    
    // 手动指定列名
    @DdlColumn(name = "user_status")
    private String status;
}
```

## 📚 详细文档

### 核心文档
- [Lambda查询指南](lambda/LAMBDA_QUERY_GUIDE.md) - Lambda查询详解
- [多数据源指南](MULTI_DATASOURCE_GUIDE.md) - 多数据源配置和使用
- [并行开发总结](PARALLEL_DEVELOPMENT_SUMMARY.md) - 开发总结

### API 参考
- [Lambda查询API](lambda/API_REFERENCE.md) - Lambda查询API参考
- [多数据源API](MULTI_DATASOURCE_IMPLEMENTATION_SUMMARY.md) - 多数据源API参考

## 🚀 最佳实践

### 1. 实体类设计

```java
@DdlTable("users")
public class User extends BaseEntity {
    
    @DdlColumn("user_name")
    private String name;
    
    @DdlColumn("user_email") 
    private String email;
    
    private String bio;
    private BigDecimal balance;
    
    @DdlColumn("is_active")
    private Boolean active; // 推荐使用Boolean而非boolean
    
    // getters and setters
}
```

### 2. DAO 设计

```java
@Slf4j
public class UserDao extends AbstractDao<User> {

    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }

    public Future<Optional<User>> findByEmail(String email) {
        return lambdaQuery()
            .eq(User::getEmail, email)
            .first();
    }

    public Future<List<User>> findActiveUsers(int limit) {
        return lambdaQuery()
            .eq(User::getActive, true)
            .limit(limit)
            .list();
    }
    
    public Future<PageResult<User>> findUsersWithPagination(PageRequest pageRequest, String keyword) {
        LambdaQueryWrapper<User> wrapper = lambdaQuery();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getName, "%" + keyword + "%")
                .or()
                .like(User::getEmail, "%" + keyword + "%");
        }

        return wrapper.findPage(pageRequest);
    }
}
```

### 3. 服务层设计

```java
@Service
public class UserService {
    private final UserDao userDao;
    
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }
    
    public Future<User> createUser(CreateUserRequest request) {
        // 验证用户名不为空
        if (request.name == null || request.name.trim().isEmpty()) {
            return Future.failedFuture(new ValidationException("用户名不能为空"));
        }
        
        // 验证邮箱格式
        if (!isValidEmail(request.email)) {
            return Future.failedFuture(new ValidationException("邮箱格式不正确"));
        }

        return userDao.findByEmail(request.email)
            .compose(existingUser -> {
                if (existingUser.isPresent()) {
                    return Future.failedFuture(new UserAlreadyExistsException());
                }
                
                User newUser = new User();
                newUser.setName(request.name);
                newUser.setEmail(request.email);
                newUser.setActive(true);
                
                return userDao.create(newUser);
            });
    }
    
    public Future<PageResult<User>> searchUsers(String keyword, PageRequest pageRequest) {
        return userDao.findUsersWithPagination(pageRequest, keyword);
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
}
```

### 4. 错误处理

```java
public Future<List<User>> getUsers() {
    return userDao.findAll()
        .recover(throwable -> {
            log.error("Failed to get users", throwable);
            
            if (throwable instanceof ValidationException) {
                return Future.failedFuture(new BusinessException("Invalid user data"));
            } else if (throwable instanceof TimeoutException) {
                return Future.failedFuture(new ServiceUnavailableException("Database timeout"));
            } else {
                return Future.failedFuture(new InternalServerErrorException("Database error"));
            }
        })
        .onSuccess(users -> log.info("Successfully retrieved {} users", users.size()));
}
```

### 5. 配置管理

```java
public class DatabaseConfig {
    
    public static Pool createPool(Vertx vertx, JsonObject config) {
        String host = config.getString("db.host", "localhost");
        int port = config.getInteger("db.port", 5432);
        String database = config.getString("db.database", "testdb");
        String username = config.getString("db.username", "user");
        String password = config.getString("db.password", "password");
        
        SqlConnectOptions connectOptions = new SqlConnectOptions()
            .setHost(host)
            .setPort(port)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password);
        
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(config.getInteger("pool.maxSize", 10))
            .setMinSize(config.getInteger("pool.minSize", 2))
            .setMaxWaitTime(config.getLong("pool.maxWaitTime", 30000).longValue());
        
        return Pool.pool(vertx, connectOptions, poolOptions);
    }
}
```

## ⚠️ 注意事项

### 性能考虑

1. **连接池大小**：根据应用负载调整 `PoolOptions`
2. **批量操作**：使用 `batchInsert/batchUpdate` 而非循环插入
3. **查询优化**：使用 `LIMIT` 限制结果集大小  
4. **索引设计**：确保常用查询字段有索引
5. **事务边界**：合理控制事务范围，避免长时间锁定

### 开发注意事项

1. **Future 链**：避免嵌套 `compose`，使用 `recover` 处理异常
2. **错误处理**：使用合适的异常类型，避免捕获 `Exception`
3. **资源释放**：Pool 会自动管理连接，无需手动释放
4. **日志记录**：在关键操作点添加适当的日志记录
5. **单元测试**：测试异步逻辑时注意使用 `CountDownLatch` 或测试工具类

## 🚨 版本要求

- **Java**: 17+ (使用现代 Java 特性)
- **jOOQ**: 3.19.2+ (修复了多子查询的BUG)
- **Vert.x**: 4.5.2+ (最新稳定版)
- **Maven**: 3.9.0+ (支持最新的插件和依赖解析)

## 📝 更新记录

### v2.0.0 - Lambda查询增强 (当前版本)

#### ✅ 主要变化

1. **Lambda查询增强**
   - 新增 `LambdaQueryWrapper` - 支持类似MyBatis-Plus的Lambda表达式
   - 新增 Join查询支持 - leftJoin、innerJoin、rightJoin、fullJoin
   - 新增聚合查询 - groupBy、having、selectCount、selectSum等
   - 新增子查询支持 - exists、notExists、inSubQuery等

2. **多数据源支持**
   - 新增 `DataSourceManager` - 数据源管理器
   - 新增 `DataSourceContext` - 线程本地数据源上下文
   - 新增 `@DataSource` 注解 - 数据源切换注解
   - 新增 `DataSourceConfigLoader` - 配置加载器

3. **批量操作优化**
   - 新增 `batchInsert` - 批量插入
   - 新增 `batchUpdate` - 批量更新
   - 新增 `batchDelete` - 批量删除
   - 新增 `batchUpsert` - 批量插入或更新

4. **执行器策略模式**
   - 新增 `ExecutorStrategy` 接口 - 执行器策略
   - 新增 `AbstractExecutorStrategy` - 抽象执行器策略
   - 支持不同数据库类型的执行器

#### 👍 优势

- **类型安全**: 基于 jOOQ DSL，编译时检查
- **高性能**: 非阻塞异步 I/O，连接池管理
- **易扩展**: 支持自定义 SQL 和复杂查询
- **零反射**: DAO 实现基于 jOOQ DSL，性能更优
- **易维护**: 清晰的代码结构，完整的文档

## 🆚 版本对比

| 功能 | v1.0 (旧版) | v2.0 (当前) |
|------|-------------|-------------|
| Lambda查询 | ❌ 不支持 | ✅ 完整支持 |
| Join查询 | ❌ 不支持 | ✅ 完整支持 |
| 聚合查询 | ❌ 不支持 | ✅ 完整支持 |
| 子查询 | ❌ 不支持 | ✅ 完整支持 |
| 多数据源 | ❌ 不支持 | ✅ 完整支持 |
| 批量操作 | ❌ 不支持 | ✅ 完整支持 |
| 类型安全 | ⚠️ 部分支持 | ✅ 完全支持 |
| 性能 | ⚠️ 一般 | ✅ 优秀 |

## 📞 技术支持

如果您在使用过程中遇到问题，可以：

1. **查看示例代码**: `core-example/` 目录
2. **运行测试**: `mvn test` 查看运行结果
3. **检查日志**: 框架提供详细的 SQL 执行日志
4. **查看文档**: `docs/` 目录下的详细文档

---

**🎯 VXCore Database - 现代化、高性能、类型安全的数据库访问框架！**

基于 jOOQ DSL 和 Vert.x 实现，提供 Lambda 查询、多数据源支持、批量操作等企业级功能。适合在高并发、高可靠性的企业应用中使用。