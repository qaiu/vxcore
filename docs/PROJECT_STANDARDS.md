# VXCore 项目规范

## 🎯 规范目标

本规范旨在约束 AI 在 VXCore 项目中的行为，确保 AI 生成的内容符合项目的实际情况、技术栈、架构设计和开发流程，避免生成不符合项目要求的代码、文档和配置。

## 📋 项目基本信息

### 项目名称
- **项目名**: VXCore
- **包名**: cn.qaiu.vxcore
- **版本**: 1.0.0
- **作者**: QAIU
- **邮箱**: qaiu@qq.com
- **网站**: https://qaiu.top
- **GitHub**: https://github.com/qaiu/vxcore

### 项目描述
VXCore 是一个基于 Vert.x 的现代化 Java 框架，提供类似 Spring Boot 的开发体验，集成了代码生成器、jOOQ DSL、WebSocket、反向代理等企业级功能。

### 设计思想
**简单而不失优雅**：
- **简单**: 降低学习成本，提供直观的 API 设计
- **优雅**: 在简单基础上提供强大功能和良好扩展性
- **平衡**: 在简单性和功能性之间找到最佳平衡点

## 🏗️ 项目结构约束

### 模块结构
```
vxcore/
├── core/                           # 核心框架模块
│   ├── src/main/java/             # 核心 Java 源码
│   │   ├── cn/qaiu/vx/core/       # 核心包
│   │   │   ├── annotations/        # 注解定义
│   │   │   ├── di/                # 依赖注入
│   │   │   ├── util/              # 工具类
│   │   │   ├── verticle/          # Verticle 实现
│   │   │   ├── handlerfactory/    # 处理器工厂
│   │   │   ├── proxy/             # 反向代理
│   │   │   └── registry/          # 注册中心
│   │   └── resources/             # 资源文件
│   ├── src/test/java/             # 测试代码
│   └── pom.xml                    # 核心模块配置
├── core-database/                 # 数据库操作模块
│   ├── src/main/java/             # 数据库相关源码
│   │   ├── cn/qaiu/db/            # 数据库包
│   │   │   ├── dsl/               # DSL 框架
│   │   │   │   ├── lambda/        # Lambda 查询
│   │   │   │   └── core/          # 核心组件
│   │   │   ├── datasource/        # 多数据源支持
│   │   │   └── spi/               # SPI 扩展
│   │   └── resources/             # 资源文件
│   ├── src/test/java/             # 测试代码
│   ├── docs/                      # 文档
│   └── pom.xml                    # 数据库模块配置
├── core-generator/                # 代码生成器模块
│   ├── src/main/java/             # 生成器源码
│   ├── src/main/resources/        # 模板文件
│   └── pom.xml                    # 生成器模块配置
├── core-example/                  # 示例模块
│   ├── src/main/java/             # 示例代码
│   ├── src/main/resources/        # 配置文件
│   └── pom.xml                    # 示例模块配置
├── docs/                          # 项目文档
└── pom.xml                        # 根项目配置
```

### 包命名规范
- **核心包**: `cn.qaiu.vx.core`
- **数据库包**: `cn.qaiu.db`
- **示例包**: `cn.qaiu.example`
- **生成器包**: `cn.qaiu.generator`

## 🔧 技术栈约束

### 核心框架
- **Java 17+**: 必须使用现代 Java 特性
- **Vert.x 4.5+**: 基于事件驱动的异步非阻塞 I/O
- **jOOQ 3.19+**: 类型安全的 SQL 构建
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

### 禁止使用的技术
- ❌ Spring Boot 相关技术
- ❌ MyBatis 相关技术
- ❌ Hibernate 相关技术
- ❌ Java 8 之前的特性
- ❌ 同步阻塞的数据库操作

## 📝 代码生成约束

### 1. 注解使用约束

#### 必须使用的注解
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
@App
```

#### 禁止使用的注解
- ❌ `@SpringBootApplication`
- ❌ `@RestController`
- ❌ `@RequestMapping`
- ❌ `@Autowired`
- ❌ `@Entity`
- ❌ `@Table`
- ❌ `@Column`

### 2. 类生成约束

#### 控制器类
```java
@Controller
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping(value = "/users", method = RouteMethod.GET)
    public Future<JsonResult> getUsers(@RequestParam("page") int page) {
        return userService.findUsers(page)
            .map(users -> JsonResult.success(users));
    }
}
```

#### 服务类
```java
@Service
public class UserService {
    
    public Future<List<User>> findUsers(int page) {
        return userDao.lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
}
```

#### DAO 类
```java
@Dao
public class UserDao extends AbstractDao<User> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
}
```

### 3. 方法生成约束

#### 异步方法
- 所有数据库操作方法必须返回 `Future<T>`
- 所有 HTTP 处理方法必须返回 `Future<JsonResult>`
- 使用 `compose` 和 `recover` 处理异步操作

#### 查询方法
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

#### 操作方法
```java
// 使用 Future 进行异步操作
public Future<User> createUser(User user) {
    return userDao.create(user)
        .compose(createdUser -> {
            return sendWelcomeEmail(createdUser);
        })
        .recover(throwable -> {
            log.error("Failed to create user", throwable);
            return Future.failedFuture(new BusinessException("用户创建失败"));
        });
}
```

## 📚 文档生成约束

### 1. 文档结构
- 使用 Markdown 格式
- 包含清晰的标题层级
- 使用代码块展示示例
- 包含相关文档链接

### 2. 文档内容
- 必须基于项目实际情况
- 不能包含其他框架的内容
- 使用项目中的实际类名和方法名
- 包含完整的代码示例

### 3. 文档格式
```markdown
# 标题

## 子标题

### 代码示例
```java
// 实际的代码示例
(忽略)```

## 相关文档
- [链接1](path1.md)
- [链接2](path2.md)
```

## 🔧 配置生成约束

### 1. Maven 配置
```xml
<!-- 必须包含的依赖 -->
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-core</artifactId>
</dependency>
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq</artifactId>
</dependency>
<dependency>
    <groupId>com.google.dagger</groupId>
    <artifactId>dagger</artifactId>
</dependency>
```

### 2. YAML 配置
```yaml
# 数据源配置
datasources:
  primary:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    driver: org.h2.Driver
    pool:
      maxSize: 20
      minSize: 5
```

### 3. 禁止的配置
- ❌ Spring Boot 相关配置
- ❌ MyBatis 相关配置
- ❌ Hibernate 相关配置

## 🧪 测试生成约束

### 1. 测试类命名
- 单元测试: `ClassNameTest`
- 集成测试: `ClassNameIntegrationTest`
- 测试类必须使用 JUnit 5

### 2. 测试方法命名
```java
@Test
void shouldReturnUserWhenValidIdProvided() {
    // 测试逻辑
}

@Test
void shouldThrowExceptionWhenInvalidIdProvided() {
    // 测试逻辑
}
```

### 3. 测试数据
- 使用 H2 内存数据库进行测试
- 使用 Mock 对象隔离依赖
- 测试异常情况的处理

## 🚫 严格禁止事项

### 1. 技术栈禁止
- ❌ 禁止生成 Spring Boot 相关代码
- ❌ 禁止生成 MyBatis 相关代码
- ❌ 禁止生成 Hibernate 相关代码
- ❌ 禁止使用 Java 8 之前的特性
- ❌ 禁止使用同步阻塞的数据库操作

### 2. 代码结构禁止
- ❌ 禁止在 Controller 中直接操作数据库
- ❌ 禁止在 Service 中处理 HTTP 请求
- ❌ 禁止在 DAO 中包含业务逻辑
- ❌ 禁止使用同步方法处理异步操作

### 3. 命名禁止
- ❌ 禁止使用中文命名
- ❌ 禁止使用拼音命名
- ❌ 禁止使用无意义的缩写
- ❌ 禁止使用下划线命名（除了常量）

### 4. 异常处理禁止
- ❌ 禁止吞掉异常（catch 后不处理）
- ❌ 禁止使用 System.out.println 输出错误信息
- ❌ 禁止在业务代码中直接抛出 RuntimeException

## ✅ 推荐做法

### 1. 代码生成
- ✅ 使用项目中的实际类名和方法名
- ✅ 遵循项目的包结构和命名规范
- ✅ 使用异步编程模式
- ✅ 包含完整的错误处理

### 2. 文档生成
- ✅ 基于项目实际情况编写
- ✅ 包含完整的代码示例
- ✅ 使用正确的类名和方法名
- ✅ 包含相关文档链接

### 3. 配置生成
- ✅ 使用项目支持的技术栈
- ✅ 遵循项目的配置格式
- ✅ 包含必要的依赖和配置
- ✅ 使用正确的版本号

## 📊 质量要求

### 1. 代码质量
- 代码必须能够编译通过
- 代码必须符合项目规范
- 代码必须包含必要的注释
- 代码必须处理异常情况

### 2. 文档质量
- 文档必须准确描述功能
- 文档必须包含完整示例
- 文档必须使用正确的格式
- 文档必须包含相关链接

### 3. 测试质量
- 测试必须能够运行通过
- 测试必须覆盖核心逻辑
- 测试必须包含异常情况
- 测试必须使用正确的断言

## 🔍 验证检查

### 1. 代码检查
- 检查是否使用了禁止的技术栈
- 检查是否符合命名规范
- 检查是否使用了异步编程
- 检查是否包含错误处理

### 2. 文档检查
- 检查是否基于项目实际情况
- 检查是否包含完整示例
- 检查是否使用正确格式
- 检查是否包含相关链接

### 3. 配置检查
- 检查是否使用了正确的依赖
- 检查是否使用了正确的版本
- 检查是否符合项目格式
- 检查是否包含必要配置

## 📚 相关文档

- [代码规范](CODE_STANDARDS.md) - 详细的代码编写规范
- [项目概述](01-overview.md) - 项目介绍和核心特性
- [系统架构](04-architecture.md) - 整体架构设计
- [开发指南](05-developer-guide.md) - 开发者指南
- [Lambda 查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - Lambda 查询详解
- [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 多数据源配置

---

**🎯 严格遵循本规范，确保 AI 生成的内容符合 VXCore 项目要求！**
