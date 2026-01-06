# VXCore 重构总结

## 重构概述

本次重构涉及三个主要部分：
1. **配置管理系统** - 新增别名机制和策略模式
2. **安全认证模块** - 基于 Vert.x Auth JWT 的声明式认证鉴权
3. **自动 DDL/ORM** - 通过注解启用自动数据库同步

---

## 1. 配置管理系统

### 新增文件

#### `cn.qaiu.vx.core.config.ConfigAliasRegistry`
配置别名注册表，支持配置键的别名映射。

```java
// 使用示例
ConfigAliasRegistry registry = ConfigAliasRegistry.getInstance();

// 注册自定义别名组
registry.registerAliasGroup("timeout", "timeout", "connectTimeout", "connTimeout");

// 获取规范名称
String canonical = registry.getCanonicalName("jdbcUrl"); // 返回 "url"

// 检查别名关系
boolean isAlias = registry.isAlias("url", "jdbcUrl"); // 返回 true
```

**默认别名组：**
- 数据源: `datasources` ↔ `database` ↔ `dataSource`
- URL: `url` ↔ `jdbcUrl` ↔ `jdbc-url`
- 用户名: `username` ↔ `user` ↔ `userName`
- 密码: `password` ↔ `pwd` ↔ `pass`
- 驱动: `driverClassName` ↔ `driver` ↔ `driver-class-name`
- 端口: `port` ↔ `serverPort` ↔ `server-port`

#### `cn.qaiu.vx.core.config.ConfigResolver`
别名感知的配置值获取器。

```java
JsonObject config = new JsonObject()
    .put("url", "jdbc:mysql://localhost:3306/test")
    .put("user", "root");

ConfigResolver resolver = new ConfigResolver(config);

// 使用别名获取值
String jdbcUrl = resolver.getString("jdbcUrl");  // 自动查找 "url"
String username = resolver.getString("username"); // 自动查找 "user"

// 带默认值
Integer maxPool = resolver.getInteger("maximumPoolSize", 10);
```

#### `cn.qaiu.vx.core.config.ConfigBinder`
类型安全的配置绑定器（策略模式）。

```java
ConfigBinder binder = new ConfigBinder();

// 绑定到 POJO
ServerConfig config = binder.bind(jsonConfig, ServerConfig.class);

// 注册自定义绑定策略
binder.registerStrategy(CustomType.class, (key, value) -> new CustomType(value.toString()));
```

#### `cn.qaiu.vx.core.config.DataSourceConfigResolver`
多格式数据源配置解析器。

```java
DataSourceConfigResolver resolver = new DataSourceConfigResolver(config);

// 获取统一格式的数据源配置
JsonObject dsConfig = resolver.resolveDataSourcesConfig();

// 获取主数据源
DataSourceConfig primary = resolver.resolvePrimaryDataSource();
```

---

## 2. 安全认证模块

### 新增文件

#### 注解

| 注解 | 作用 | 示例 |
|------|------|------|
| `@Authenticated` | 需要登录 | `@Authenticated(optional = true)` |
| `@RequiresPermissions` | 需要权限 | `@RequiresPermissions({"user:read", "user:write"})` |
| `@RequiresRoles` | 需要角色 | `@RequiresRoles("admin")` |
| `@Anonymous` | 匿名访问 | `@Anonymous` |

#### `cn.qaiu.vx.core.security.SecurityContext`
安全上下文，封装用户信息和权限。

```java
// 从路由上下文获取
SecurityContext ctx = SecurityContext.fromContext(routingContext);

// 获取用户信息
String userId = ctx.getUserId();
String username = ctx.getUsername();

// 检查权限（支持通配符）
ctx.hasPermission("user:read");  // true if has "user:*" or "user:read"
ctx.hasPermission("user:*");     // true if has "user:*"

// 检查角色
ctx.hasRole("admin");
ctx.hasAnyRole("admin", "user");
ctx.hasAllRoles("admin", "user");
```

#### `cn.qaiu.vx.core.security.SecurityConfig`
安全配置类，支持从 YAML/JSON 加载。

```yaml
# application.yml
security:
  enabled: true
  jwt-enable: true
  jwt-algorithm: "RS256"
  jwt-expire-seconds: 3600
  jwt-issuer: "vxcore"
  
  # Token 配置
  token-header: "Authorization"
  token-prefix: "Bearer "
  
  # 路径认证规则（正则）
  jwt-auth-reg:
    - "/api/secure/.*"
    - "/api/admin/.*"
  
  # 忽略认证路径
  jwt-ignores-reg:
    - "/api/auth/login"
    - "/api/auth/refresh"
```

#### `cn.qaiu.vx.core.security.JwtAuthProvider`
JWT Token 生成和验证。

```java
JwtAuthProvider provider = new JwtAuthProvider(config);
provider.initialize().onSuccess(v -> {
    // 生成 Token
    String accessToken = provider.generateToken(userInfo);
    String refreshToken = provider.generateRefreshToken(userInfo);
    
    // 验证 Token
    provider.authenticate(token)
        .onSuccess(user -> /* 验证成功 */)
        .onFailure(err -> /* 验证失败 */);
});
```

#### `cn.qaiu.vx.core.security.SecurityInterceptor`
安全拦截器，自动处理认证和鉴权。

### 权限字符串格式

采用 `resource:action` 格式，支持通配符：

| 权限 | 说明 |
|------|------|
| `user:read` | 用户读取权限 |
| `user:write` | 用户写入权限 |
| `user:*` | 用户所有权限（匹配 user:read, user:write 等） |
| `*:read` | 所有资源的读取权限 |
| `*:*` | 超级权限 |

### 使用示例

```java
@RouteHandler("/api/secure")
@Authenticated  // 类级别认证
public class SecuredController {

    @Anonymous  // 允许匿名
    @RouteMapping(value = "/public", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getPublicResource() {
        // ...
    }

    @RequiresPermissions("user:read")
    @RouteMapping(value = "/users", method = RouteMethod.GET)
    public Future<JsonResult<List<User>>> listUsers() {
        // ...
    }

    @RequiresRoles("admin")
    @RequiresPermissions("system:admin")  // 同时需要角色和权限
    @RouteMapping(value = "/admin/config", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> updateConfig() {
        // ...
    }
}
```

---

## 3. 自动 DDL/ORM 模块

### SPI 机制

core 模块定义接口，core-database 提供实现：

```
core
└── cn.qaiu.vx.core.spi
    ├── OrmSyncProvider (接口)
    └── OrmSyncProviderRegistry (注册表)

core-database
└── cn.qaiu.db.orm
    ├── EnableDdlSync (注解)
    ├── DdlSyncStrategy (枚举)
    └── DdlSyncProvider (SPI实现)
```

### `@EnableDdlSync` 注解

```java
@App
@EnableDdlSync(
    strategy = DdlSyncStrategy.AUTO,        // 同步策略
    entityPackages = {"cn.qaiu.example.entity"},  // 实体包
    showDdl = true,                          // 显示DDL
    autoExecute = true,                      // 自动执行
    dataSource = ""                          // 数据源名称（空=默认）
)
public class MyApplication {
    public static void main(String[] args) {
        VXCoreApplication.run(args);
    }
}
```

### DDL 同步策略

| 策略 | 说明 |
|------|------|
| `AUTO` | 自动检测并同步（创建+更新） |
| `CREATE` | 只创建表，不更新已有表 |
| `UPDATE` | 只更新已有表，不创建新表 |
| `VALIDATE` | 只验证，不执行DDL |
| `NONE` | 禁用DDL同步 |

---

## 修改的现有文件

### `ConfigurationComponent`
- 集成 `ConfigResolver` 和 `ConfigBinder`
- 添加 `getConfigResolver()` 和 `getConfigBinder()` 方法
- 使用别名机制处理配置

### `DataSourceComponent`
- 使用 `DataSourceConfigResolver` 解析多格式配置
- 支持 `database`、`datasources`、`dataSource` 三种配置格式

### `RouterHandlerFactory`
- 添加 `SecurityInterceptor` 集成
- 在路由注册时将方法和类信息存入上下文
- 自动根据配置启用安全拦截

### `SharedDataUtil`
- 新增 `getGlobalConfig()` 方法

### `core/pom.xml`
- 添加 `vertx-auth-common` 依赖
- 添加 `vertx-auth-jwt` 依赖

---

## 新增示例文件

- [AuthController.java](core-example/src/main/java/cn/qaiu/example/controller/AuthController.java) - 认证控制器示例
- [SecuredController.java](core-example/src/main/java/cn/qaiu/example/controller/SecuredController.java) - 安全注解使用示例
- [DdlSyncExampleApplication.java](core-example/src/main/java/cn/qaiu/example/DdlSyncExampleApplication.java) - DDL同步示例
- [ConfigUsageExample.java](core-example/src/main/java/cn/qaiu/example/config/ConfigUsageExample.java) - 配置使用示例
- [application-security.yml](core-example/src/main/resources/application-security.yml) - 安全配置示例

---

## 设计模式使用

1. **策略模式** - `ConfigBinder` 中的 `TypeBindStrategy`
2. **单例模式** - `ConfigAliasRegistry`、`DataSourceManager`
3. **SPI 机制** - `OrmSyncProvider` 接口和 `DdlSyncProvider` 实现
4. **装饰器模式** - `SecurityInterceptor` 包装路由处理

---

## 后续建议

1. 更新 `ConfigurationComponentTest` 测试以适应新的配置行为
2. 添加 `SecurityInterceptor` 单元测试
3. 添加 `ConfigAliasRegistry` 单元测试
4. 完善文档，添加更多使用示例
