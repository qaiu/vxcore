# VXCore 框架示例应用

## 🚀 框架优势展示

本示例应用完整展示了 VXCore 框架的核心能力和优势，从初始化SQL到响应JSON的完整流程。

### ✨ 核心特性

#### 1. 自动ORM建表和索引
- **基于 `@DdlTable` 注解**：框架自动解析注解并生成建表SQL
- **自动索引创建**：根据注解配置自动创建索引
- **表结构同步**：支持版本控制和结构变更同步
- **多数据库支持**：MySQL、PostgreSQL、H2等

```java
@DdlTable(
    value = "dsl_user",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "DSL用户表示例",
    charset = "utf8mb4",
    collate = "utf8mb4_unicode_ci",
    engine = "InnoDB"
)
public class User extends BaseEntity {
    // 实体定义
}
```

#### 2. Lambda数据库查询
- **类型安全**：编译时检查，避免字段名错误
- **流畅API**：链式调用，代码简洁易读
- **复杂查询**：支持条件、排序、分页、聚合等
- **自动映射**：查询结果自动映射到实体对象

```java
// 复杂条件查询
return userDao.lambdaQuery()
    .like(name != null, User::getName, name)
    .eq(status != null, User::getStatus, status)
    .ge(minAge != null, User::getAge, minAge)
    .le(maxAge != null, User::getAge, maxAge)
    .orderBy(User::getCreateTime, false)
    .page(page, size);

// 聚合查询
return userDao.lambdaQuery()
    .select(
        DSL.count().as("total_count"),
        DSL.sum(User::getBalance).as("total_balance"),
        DSL.avg(User::getBalance).as("avg_balance")
    )
    .one();
```

#### 3. 自动参数绑定
- **智能绑定**：根据注解自动绑定请求参数
- **类型转换**：自动处理类型转换和验证
- **方法重载**：支持同名方法的不同参数组合
- **灵活配置**：支持默认值、必填验证等

```java
@RouteMapping(value = "/{id}", method = RouteMapping.HttpMethod.GET)
public Future<JsonResult<?>> getUserById(@PathVariable("id") Long id) {
    // 自动绑定路径参数
}

@RouteMapping(value = "/", method = RouteMapping.HttpMethod.GET)
public Future<JsonResult<?>> getUserByEmail(@RequestParam("email") String email) {
    // 自动绑定查询参数
}

@RouteMapping(value = "/", method = RouteMapping.HttpMethod.POST)
public Future<JsonResult<?>> createUser(@RequestBody JsonObject userData) {
    // 自动绑定请求体
}
```

#### 4. 全局异常处理
- **统一处理**：全局异常处理器统一处理所有异常
- **局部处理**：支持Controller级别的局部异常处理
- **类型匹配**：根据异常类型自动选择处理器
- **优雅降级**：异常处理失败时的降级策略

```java
@ExceptionHandler(ValidationException.class)
public JsonResult<?> handleValidationException(ValidationException e) {
    return JsonResult.error("参数验证失败: " + e.getMessage(), 400);
}

@ExceptionHandler(BusinessException.class)
public JsonResult<?> handleBusinessException(BusinessException e) {
    return JsonResult.error("业务错误: " + e.getMessage(), 404);
}
```

#### 5. 配置元数据支持
- **IDE提示**：配置项自动提示和类型检查
- **元数据生成**：自动生成配置元数据文件
- **类型安全**：编译时配置类型检查
- **文档生成**：自动生成配置文档

```java
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    @ConfigurationProperty(value = "host", defaultValue = "0.0.0.0")
    private String host;
    
    @ConfigurationProperty(value = "port", defaultValue = "8080")
    private Integer port;
}
```

## 🏗️ 三层架构设计

### Controller层
- **路由映射**：基于注解的路由配置
- **参数绑定**：自动参数绑定和验证
- **异常处理**：局部异常处理
- **响应封装**：统一的响应格式

### Service层
- **业务逻辑**：核心业务逻辑处理
- **数据验证**：业务规则验证
- **事务管理**：事务边界控制
- **异常处理**：业务异常抛出

### DAO层
- **数据访问**：数据库操作封装
- **Lambda查询**：类型安全的查询构建
- **结果映射**：自动结果映射
- **连接管理**：数据库连接管理

## 📊 完整流程演示

### 1. 应用启动流程
```
1. 加载配置 (application.yml)
2. 初始化数据库连接
3. 自动建表 (基于@DdlTable注解)
4. 初始化DAO层
5. 初始化Service层
6. 注册Controller
7. 启动HTTP服务器
```

### 2. 请求处理流程
```
1. 接收HTTP请求
2. 路由匹配和方法重载解析
3. 参数绑定和类型转换
4. Controller方法调用
5. Service业务逻辑处理
6. DAO数据库操作
7. 结果映射和响应
8. 异常处理 (如有)
```

### 3. 数据库操作流程
```
1. Lambda查询构建
2. SQL生成和参数绑定
3. 数据库执行
4. 结果集处理
5. 实体对象映射
6. 返回结果
```

## 🚀 快速开始

### 1. 启动应用
```bash
mvn exec:java -Dexec.mainClass="cn.qaiu.example.VXCoreExampleRunner"
```

### 2. 访问API
- **健康检查**: `GET http://localhost:8080/api/system/health`
- **用户列表**: `GET http://localhost:8080/api/user/`
- **创建用户**: `POST http://localhost:8080/api/user/`
- **产品列表**: `GET http://localhost:8080/api/product/`

### 3. 测试框架特性

#### 测试自动参数绑定
```bash
# 路径参数绑定
curl "http://localhost:8080/api/user/1"

# 查询参数绑定
curl "http://localhost:8080/api/user/?email=test@example.com"

# 请求体绑定
curl -X POST "http://localhost:8080/api/user/" \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com","password":"123456"}'
```

#### 测试方法重载
```bash
# 测试方法重载解析
curl "http://localhost:8080/api/system/test/overload?id=1"
curl "http://localhost:8080/api/system/test/overload?name=test"
curl "http://localhost:8080/api/system/test/overload?id=1&name=test"
```

#### 测试异常处理
```bash
# 测试不同类型的异常
curl "http://localhost:8080/api/system/test/exception?type=validation"
curl "http://localhost:8080/api/system/test/exception?type=business"
curl "http://localhost:8080/api/system/test/exception?type=system"
```

#### 测试Lambda查询
```bash
# 复杂条件查询
curl "http://localhost:8080/api/user/?name=张&status=ACTIVE&page=1&size=10"

# 聚合查询
curl "http://localhost:8080/api/user/statistics"
curl "http://localhost:8080/api/product/stats/category"
```

## 📈 性能优势

1. **类型安全**：编译时检查，减少运行时错误
2. **自动优化**：框架自动优化SQL生成和执行
3. **连接池**：高效的数据库连接池管理
4. **异步处理**：基于Vert.x的异步非阻塞处理
5. **内存优化**：智能的对象映射和缓存

## 🔧 扩展能力

1. **多数据源**：支持多数据源配置和动态切换
2. **插件机制**：支持自定义插件和扩展
3. **监控集成**：内置监控和指标收集
4. **缓存支持**：多级缓存策略
5. **分布式支持**：支持分布式部署和集群

## 📚 技术栈

- **框架核心**: VXCore (基于Vert.x)
- **数据库**: H2 (内存数据库，支持MySQL语法)
- **ORM**: jOOQ + 自定义Lambda查询
- **配置**: YAML + 元数据支持
- **日志**: SLF4J + Logback
- **测试**: JUnit 5

## 🎯 总结

VXCore框架通过以下特性实现了从初始化SQL到响应JSON的完整自动化：

1. **自动建表**：基于注解的自动建表和索引
2. **类型安全**：Lambda查询的类型安全保证
3. **智能绑定**：自动参数绑定和类型转换
4. **统一处理**：全局异常处理和响应格式化
5. **配置管理**：元数据支持的配置管理
6. **架构清晰**：标准的三层架构设计

这些特性使得开发者可以专注于业务逻辑，而无需关心底层的技术细节，大大提高了开发效率和代码质量。