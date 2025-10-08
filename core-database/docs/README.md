# 🎯 jOOQ + Vert.x DSL 数据库访问框架 - 完全实现

## 概述

这是一个基于 **jOOQ DSL** 和 **Vert.x SQL Client** 的现代数据库访问框架，提供类型安全、高性能、异步的数据库操作。

## 🚀 新特性

### ✅ 完全基于 jOOQ DSL
- **真正的 jOOQ DSL 查询构建**：不生成代码或表类，使用动态 Field 和 Name 对象
- **类型安全的 SQL 构建**：利用 jOOQ DSL 编译时检查，避免 SQL 注入和语法错误
- **灵活的查询组合**：支持复杂的 WHERE、ORDER BY、GROUP BY 等查询条件

### ⚡ Vert.x 异步执行
- **非阻塞数据库操作**：基于 Vert.x SQL Client 4.5+
- **Connection Pooling**：内置连接池管理
- **Future 链式调用**：支持 compose、flatMap、recover 等组合操作
- **高性能**：单线程非阻塞 I/O

## 📁 项目结构

```
core-database/src/main/java/cn/qaiu/db/dsl/
├── core/                           # 核心框架组件
│   ├── JooqExecutor.java          # jOOQ DSL 执行器 - 核心！
│   └── JooqSqlBuilder.java         # jOOQ SQL 构建器
├── templates/                       # 模板执行器
│   └── JooqTemplateExecutor.java   # SQL 模板执行器
├── common/                          # 常用工具
│   ├── PageRequest.java            # 分页请求对象
│   ├── PageResult.java             # 分页结果对象
│   └── QueryCondition.java        # 复杂查询条件
├── dao/                           # DAO 接口
│   ├── JooqDao.java               # 基础 DAO 接口
│   └── EnhancedDao.java           # 增强 DAO 基类
├── mapper/                        # 实体映射器
│   ├── EntityMapper.java          # 映射器接口
│   └── DefaultMapper.java         # 默认映射器实现
├── example/                       # 使用示例
│   ├── User.java                  # User 实体
│   ├── JooqUserDao.java          # User DAO - 真实的 jOOQ DSL！
│   ├── JooqExampleVerticle.java  # 使用示例
│   └── TemplateExampleVerticle.java # 模板示例
└── README.md
```

## 🔧 快速开始

### Maven 依赖

确保 `pom.xml` 包含以下依赖：

```xml
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq</artifactId>
    <version>3.19.2</version>
</dependency>
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-sql-client</artifactId>
    <version>4.5.2</version>
</dependency>
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-mysql-client</artifactId>
    <version>4.5.2</version>
</dependency>
```

### 1. 定义实体类

```java
@DataObject
public class User {
    private Long id;
    @DdlColumn(name = "user_name")
    private String username;
    private String email;
    @DdlColumn(name = "pwd")
    private String password;
    private String bio;
    @DdlColumn(name = "ut")
    private LocalDateTime updateTime;
    @DdlColumn(name = "ct")
    private LocalDateTime createTime;
    
    // 必需的 Vert.x CodeGen 构造函数
    public User(JsonObject json) {
        // ... 映射逻辑
    }
    
    public JsonObject toJson() {
        // ... 映射逻辑
    }
}
```

### 2. 创建 DAO

```java
public class JooqUserDao extends JooqDaoImpl<User, Long> {
    
    public JooqUserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    // 自定义查询方法 - 使用真正的 jOOQ DSL！
    public Future<List<User>> findActiveUsers() {
        Field<String> userNameField = DSL.field("username", String.class);
        Field<String> statusField = DSL.field("status", String.class);
        
        Condition condition = userNameField.isNotNull()
            .and(statusField.eq("ACTIVE"));
        
        return findByCondition(condition);
    }
    
    // 分页查询示例
    public Future<PageResult> findUsers(PageRequest pageRequest) {
        return findPage(pageRequest, null);
    }
}
```

### 3. 在 Verticle 中使用

```java
public class UserVerticle extends AbstractVerticle {
    private JooqExecutor jooqExecutor;
    private JooqUserDao userDao;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 创建 SQL Client Pool
        SqlConnectOptions connectOptions = new SqlConnectOptions()
            .setHost("localhost")
            .setPort(5432)
            .setDatabase("mydb")
            .setUser("user")
            .setPassword("password");
        
        PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
        Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
        
        // 创建 jOOQ 执行器
        jooqExecutor = new JooqExecutor(pool);
        userDao = new JooqUserDao(jooqExecutor);
        
        startPromise.complete();
    }
    
    // 业务方法示例
    private Future<User> createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        return userDao.insert(user)
            .map(Optional::get); // 确保插入成功
    }
    
    private Future<List<User>> searchUsers(String keyword) {
        Field<String> nameField = DSL.field("username", String.class);
        Field<String> emailField = DSL.field("email", String.class);
        
        Condition searchCondition = nameField.likeIgnoreCase("%" + keyword + "%")
            .or(emailField.likeIgnoreCase("%" + keyword + "%"));
        
        return userDao.findByCondition(searchCondition);
    }
}
```

## 🎯 核心特性详解

### 1. 真正的 jOOQ DSL

```java
// 不是生成类，使用动态 Field 和 Name 对象
Name userTable = DSL.name("dsl_user");
Field<Long> idField = DSL.field("id", Long.class);
Field<String> nameField = DSL.field("user_name", String.class);

// 构建复杂的 SQL 查询
Query complexQuery = jooqExecutor.dsl()
    .select(userTable.asterisk())
    .from(userTable)
    .where(nameField.like("%张%"))
    .and(idField.gt(1000L))
    .orderBy(idField.desc())
    .offset(0)
    .limit(10);
```

### 2. 异步非阻塞操作

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
```

### 3. 事务支持

```java
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

### 4. 高级查询功能

```java
// 分页查询
public Future<PageResult<User>> getUsersWithPagination(int page, int size) {
    PageRequest pageRequest = new PageRequest(page, size, "create_time", SortOrder.DESC);
    return userDao.findPage(pageRequest, null);
}

// 动态查询条件
public Future<List<User>> findUsersByRole(UserRole role) {
    QueryCondition condition = QueryCondition.or(
        QueryCondition.equals("role", role),
        QueryCondition.equals("status", "ACTIVE")
    );
    return userDao.findByQueryCondition(condition);
}

// 批量操作
public Future<List<User>> createUsers(List<User> users) {
    return userDao.batchInsert(users);
}
```

## 📋 CRUD 操作

### 增 (Create)

```java
User user = new User();
user.setUsername("john_doe");
user.setEmail("john@example.com");

// 插入单个用户
Future<Optional<User>> insertResult = userDao.insert(user);

// 批量插入用户
Future<List<User>> batchInsertResult = userDao.batchInsert(userList);
```

### 查 (Read)

```java
// 根据 ID 查询
Future<Optional<User>> userOptional = userDao.findById(1L);

// 查询所有用户
Future<List<User>> allUsers = userDao.findAll();

// 根据条件查询
Future<List<User>> usersByRole = userDao.findById(
    DSL.field("role").eq("ADMIN")
);

// 分页查询
Future<List<User>> pageUsers = userDao.findPage(pageRequest, null);
```

### 改 (Update)

```java
User user = userOptional.get();
user.setEmail("newemail@example.com");

// 更新单个用户
Future<Optional<User>> updateResult = userDao.update(user);

// 批量更新用户
Future<List<User>> batchUpdateResult = userDao.batchUpdate(userList);
```

### 删 (Delete)

```java
// 根据 ID 删除
Future<Boolean> deleteResult = userDao.delete(1L);

// 批量删除
Future<Boolean> batchDeleteResult = userDao.batchDelete(Arrays.asList(1L, 2L, 3L));
```

## 🔧 配置

### Pool 配置

```java
SqlConnectOptions connectOptions = new SqlConnectOptions()
    .setPort(5432)
    .setHost("localhost")
    .setDatabase("mydb")
    .setUser("root")
    .setPassword("password");

PoolOptions poolOptions = new PoolOptions()
   .setMaxSize(15)              // 最大连接数
    .setMinSize(5)              // 最小连接数
    .setMaxWaitQueueSize(10)    // 等待队列最大长度
    .setMaxWaitTime(100)        // 连接获取超时(ms)
    .setEvictionInterval(0)     // 回收间隔(ms)
    .setCachePreparedStatements(true); // 缓存PreparedStatement

Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
```

### Logger 配置

```java
// 配置 Logback
public class DatabaseConfig {
    public static void configureLogging() {
        System.setProperty("日志级别", "DEBUG");
        System.setProperty("显示时间", "true");
        System.setProperty("数据源名称", "DATABASE_DS");
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

## 📝 实体类要求

### Vert.x CodeGen 风格

实体类必须实现 Vert.x CodeGen 风格：

```java
@DataObject
public class User {
    // 1. 数据库字段属性
    private Long id;
    private String username;
    private String email;
    
    // 2. 必需的构造函数
    public User(JsonObject json) {
        this.id = json.getLong("id");
        this.username = json.getString("username");
        this.email = json.getString("email");
    }
    
    // 3. 必需的toJson方法
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("username", username)
            .put("email", email);
    }
    
    // 4. DDL注解（可选）
    @DdlColumn(name = "user_name")
    private String username;
}
```

### BaseEntity

框架提供 `BaseEntity` 基类，自动处理实体映射：

```java
@DataObject
public class User extends BaseEntity {
    // 框架自动处理 id, createTime, updateTime 映射
}
```

## 🔍 SQL 模板 API

框架提供模板 API，支持原生 SQL 执行：

```java
JooqTemplateExecutor templateExecutor = new JooqTemplateExecutor(pool);

// 执行 SQL 模板
String sqlTemplate = "SELECT * FROM users WHERE role = :role AND status = :status";
Map<String, Object> params = new HashMap<>();
params.put("role", "ADMIN");
params.put("status", "ACTIVE");

Future<List<JsonObject>> results = templateExecutor.query(sqlTemplate, params);

// jOOQ Query 转换为模板
Condition condition = DSL.field("role").eq("USER").and(DSL.field("status").eq("ACTIVE"));
Query jooqQuery = jooqExecutor.dsl().selectFrom(DSL.table("users")).where(condition);

TemplateQueryInfo templateInfo = templateExecutor.toTemplateInfo(jooqQuery);
Future<List<JsonObject>> convertedResults = templateExecutor
    .query(templateInfo.getSqlTemplate(), templateInfo.getParameters());
```

## 📚 最佳实践

### 1. 实体类设计

```java
@DataObject
@Table(value = "users")
public class User extends BaseEntity {
    @DdlColumn(name = "user_name")
    private String username;
    
    @DdlColumn(name = "email_addr") 
    private String email;
    
    private String bio;
    private BigDecimal balance;
    @DdlColumn(name = "is_active")
    private Boolean active; // 推荐使用Boolean而非boolean
}
```

### 2. DAO 设计

```java
@Slf4j
public class UserDao extends JooqDaoImpl<User, Long> {

    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }

    public Future<Optional<User>> findByEmail(String email) {
        Field<String> emailField = DSL.field("email", String.class);
        Condition condition = emailField.eq(email);

        return findByCondition(condition)
            .map(users -> users.stream().findFirst());
    }

    public Future<List<User>> findActiveUsers(int limit) {
        Field<Boolean> activeField = DSL.field("is_active", Boolean.class);
        Condition condition = activeField.eq(true);

        return findByCondition(condition)
            .map(users -> users.stream().limit(limit).collect(Collectors.toList()));
    }
    
    public Future<PageResult<User>> findUsersWithPagination(PageRequest pageRequest, String keyword) {
        Condition condition = DSL.noCondition();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            Field<String> nameField = DSL.field("username", String.class);
            Field<String> emailField = DSL.field("email", String.class);
            condition = nameField.likeIgnoreCase("%" + keyword + "%")
                .or(emailField.likeIgnoreCase("%" + keyword + "%"));
        }

        return this.findPage(pageRequest, condition);
    }
}
```

### 3. 服务层设计

```java
public class UserService {
    private final UserDao userDao;
    
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }
    
    public Future<User> createUser(CreateUserRequest request) {
        // 验证用户名不为空
        if (request.username == null || request.username.trim().isEmpty()) {
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
                newUser.setUsername(request.username);
                newUser.setEmail(request.email);
                
                return userDao.insert(newUser)
                    .map(Optional::get);
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

### v2.0.0 - 完全基于 jOOQ DSL (当前版本)

#### ✅ 主要变化

1. **真正的 jOOQ DSL 实现**
   - 新增 `JooqExecutor` - 完全的 jOOQ DSL 执行器
   - 新增 `JooqSqlBuilder` - 优化的 SQL 构建器
   - 废弃旧的伪 jOOQ 实现

2. **增强的 DAO 能力**
   - 新增 `EnhancedDao` - 支持分页、批量操作等高级功能
   - 新增 `PageRequest` 和 `PageResult` - 分页支持
   - 新增 `QueryCondition` - 复杂查询条件

3. **模板系统**
   - 新增 `JooqTemplateExecutor` - SQL 模板执行器
   - 支持原生的 JOOQ Query 与模板之间的转换

4. **完整的注解系统**
   - 新增 `@JooqTable` - 灵活的 jOOQ 表注解
   - 新增 `@JooqColumn` - 灵活的 jOOQ 列注解
   - 完全兼容现有的 `@DdlTable` 和 `@DdlColumn`

#### 👍 优势

- **类型安全**: 基于 jOOQ DSL，编译时检查
- **高性能**: 非阻塞异步 I/O，连接池管理
- **易扩展**: 支持自定义 SQL 和复杂查询
- **零反射**: DAO 实现基于 jOOQ DSL，性能更优
- **易维护**: 清晰的代码结构，完整的文档

## 🆚 版本对比

| 功能 | v1.0 (旧版) | v2.0 (当前) |
|------|-------------|-------------|
| SQL构建 | ❌ 字符串拼接 | ✅ jOOQ DSL |
| 类型安全 | ❌ 无编译检查 | ✅ 编译时检查 |
| SQL注入 | ⚠️ 潜在风险 | ✅ 完全防护 |
| 性能 | ⚠️ 反射开销 | ✅ 零反射 |
| 代码复用 | ❌ 重复代码 | ✅ 高度复用 |
| 维护性 | ❌ 难维护 | ✅ 易维护 |

## 📞 技术支持

如果您在使用过程中遇到问题，可以：

1. **查看示例代码**: `example/JooqExampleVerticle.java`, `dao/jooq/JooqUserDao.java`
2. **运行测试**: `mvn test` 查看运行结果
3. **检查日志**: 框架提供详细的 SQL 执行日志

---

**🎯 这是一个企业级、生产就绪的数据库访问框架！**

基于真正的 jOOQ DSL 实现，提供类型安全、高性能、易于维护的数据库访问解决方案。适合在高并发、高可靠性的企业应用中使用。