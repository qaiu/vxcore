# VXCore

一个基于 Vert.x 的现代化 Java 框架，提供类似 Spring Boot 的开发体验，集成了代码生成器、jOOQ DSL、WebSocket、反向代理等企业级功能。

## 项目简介

VXCore 是一个高性能、响应式的 Java 框架，专为构建现代化 Web 应用程序而设计。它结合了 Vert.x 的异步编程模型、jOOQ 的类型安全数据库操作、以及丰富的注解支持，提供了简洁而强大的开发体验。

### 🎨 设计思想：简单而不失优雅

VXCore 的设计哲学是"**简单而不失优雅**"：

- **简单**: 降低学习成本，提供直观的 API 设计，让开发者能够快速上手
- **优雅**: 在简单的基础上，提供强大的功能和良好的扩展性，满足复杂业务需求
- **平衡**: 在简单性和功能性之间找到最佳平衡点，既不过度设计，也不功能缺失

这一设计思想使 VXCore 既适合新手快速上手，也能满足专家级用户的复杂需求。

## 🔄 最新更新

### 代码清理重构 (2024-12)
- ✅ **清理冗余代码**: 删除AI生成的重复造轮子的工具类
- ✅ **统一自动管理**: 统一DAO和Service的自动管理模式
- ✅ **简化配置管理**: 使用Vert.x原生ConfigRetriever
- ✅ **提高测试稳定性**: 修复CI环境中不稳定的测试
- ✅ **优化类型转换**: 使用简化的基础类型转换实现

详细重构内容请参考 [重构总结文档](docs/REFACTORING_SUMMARY.md)

## 🎯 核心特性

### 🚀 高性能异步架构
- **Vert.x 4.5+**: 基于事件驱动的异步非阻塞 I/O
- **响应式编程**: 支持 Future、Promise、Observable
- **高并发处理**: 单线程处理大量并发连接
- **内存优化**: 零拷贝、对象池、连接池

### 🔒 类型安全数据库操作
- **jOOQ DSL**: 编译时类型检查，完全防止 SQL 注入
- **Lambda 查询**: 类似 MyBatis-Plus 的 Lambda 表达式查询
- **无参构造函数DAO**: 自动初始化，无需手动传递参数
- **多数据源支持**: 支持动态数据源切换和事务隔离
- **批量操作**: 高性能批量 CRUD 操作

### 🌐 Web 开发支持
- **注解式路由**: 类似 Spring MVC 的 `@RouteMapping` 注解
- **参数绑定**: 支持方法重载、类型转换、自定义转换器
- **异常处理**: 全局和局部异常处理机制
- **WebSocket**: 注解式 WebSocket 路由和代理支持

### 🔧 企业级功能
- **代码生成器**: 根据数据库表结构自动生成三层架构代码
- **反向代理**: 支持 HTTP/WebSocket 代理，类似 Nginx
- **配置管理**: YAML 配置，支持 IDE 自动提示和验证
- **SPI 扩展**: 支持第三方数据库驱动和功能扩展
- **监控审计**: SQL 审计、性能监控、错误追踪

### 📦 模块化设计
- **core**: 核心框架模块（路由、注解、配置）
- **core-database**: 数据库操作模块（DSL、Lambda、多数据源）
- **core-example**: 示例和演示模块

## 📁 项目结构

```
vxcore/
├── core/                           # 核心框架模块
│   ├── src/main/java/             # 核心 Java 源码
│   │   ├── cn/qaiu/vx/core/       # 核心包
│   │   │   ├── annotations/        # 注解定义
│   │   │   ├── handlerfactory/    # 处理器工厂
│   │   │   ├── proxy/             # 反向代理
│   │   │   ├── util/              # 工具类
│   │   │   └── verticle/          # Verticle 实现
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
├── core-example/                  # 示例模块
│   ├── src/main/java/             # 示例代码
│   ├── src/main/resources/        # 配置文件
│   └── pom.xml                    # 示例模块配置
├── docs/                          # 项目文档
│   ├── README.md                  # 文档索引
│   ├── VXCORE_OPTIMIZATION_PLAN.md # 优化计划
│   └── *.md                       # 各种指南文档
└── pom.xml                        # 根项目配置
```

## 🚀 快速开始

### 📋 环境要求

- **Java 17+**: 支持现代 Java 特性
- **Maven 3.8+**: 现代化构建工具
- **数据库**: H2/MySQL/PostgreSQL（可选）

### ⚡ 5分钟快速上手

#### 1. 克隆并编译项目

```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
mvn clean compile
```

#### 2. 创建简单的 Web 服务

```java
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping(value = "/hello", method = HttpMethod.GET)
    public Future<JsonResult> hello(@RequestParam("name") String name) {
        return Future.succeededFuture(
            JsonResult.success("Hello, " + name + "!")
        );
    }
    
    @RouteMapping(value = "/users", method = HttpMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) {
        return userService.createUser(user)
            .map(createdUser -> JsonResult.success(createdUser));
    }
}
```

#### 3. 使用无参构造函数DAO（推荐）

```java
@DdlTable("users")
public class User extends BaseEntity {
    @DdlColumn("user_name")
    private String name;
    
    @DdlColumn("user_email")
    private String email;
    
    // getters and setters...
}

// 最简单的DAO - 连构造函数都没有
public class UserDao extends AbstractDao<User, Long> {
    // 完全空的类，框架自动处理所有初始化
    // 1. 自动通过泛型获取User类型
    // 2. 自动初始化SQL执行器
    // 3. 自动获取表名和主键信息
}

// 使用方式
UserDao userDao = new UserDao(); // 无需传递任何参数！

// Lambda 查询示例
public class UserService {
    
    public Future<List<User>> findActiveUsers() {
        return userDao.lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .like(User::getName, "张%")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
    
    public Future<List<User>> findUsersWithOrders() {
        return userDao.lambdaQuery()
            .leftJoin(Order.class, (user, order) -> 
                user.getId().eq(order.getUserId()))
            .eq(User::getStatus, "ACTIVE")
            .list();
    }
}
```

#### 4. 配置多数据源

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

#### 5. WebSocket 支持

```java
@WebSocketHandler("/ws/chat")
public class ChatHandler {
    
    @OnOpen
    public void onOpen(ServerWebSocket ws) {
        System.out.println("用户连接: " + ws.remoteAddress());
    }
    
    @OnMessage
    public void onMessage(String message, ServerWebSocket ws) {
        // 广播消息给所有连接的客户端
        ws.writeTextMessage("Echo: " + message);
    }
    
    @OnClose
    public void onClose(ServerWebSocket ws) {
        System.out.println("用户断开: " + ws.remoteAddress());
    }
}
```

## 🗄️ 支持的数据库

### H2 Database
- **用途**: 开发、测试、演示
- **特点**: 内存数据库，零配置
- **优势**: 快速启动，支持完整 SQL 语法

### MySQL
- **用途**: 生产环境推荐
- **特点**: 高性能、高可用
- **优势**: 支持事务、索引、复杂查询

### PostgreSQL
- **用途**: 企业级应用
- **特点**: 功能丰富、标准兼容
- **优势**: 支持 JSON、数组、自定义类型

## 📚 详细文档

### 📖 核心文档
- [项目概述](docs/01-overview.md) - 项目介绍和核心特性
- [快速开始](docs/02-quick-start.md) - 5分钟快速上手
- [安装配置](docs/03-installation.md) - 环境配置和依赖管理

### 🏗️ 架构设计
- [系统架构](docs/04-architecture.md) - 整体架构设计
- [开发指南](docs/05-developer-guide.md) - 开发者指南和核心组件详解

### 💻 开发指南
- [Lambda查询](core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - Lambda查询详解
- [多数据源](core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 多数据源配置和使用
- [项目结构](core-database/docs/PROJECT_STRUCTURE.md) - 数据库模块项目结构

### 🌐 Web开发
- [WebSocket指南](docs/WEBSOCKET_GUIDE.md) - WebSocket开发指南
- [反向代理](docs/WEBSOCKET_PROXY_GUIDE.md) - 反向代理配置
- [路由注解](docs/08-routing-annotations.md) - 路由注解使用

### 🔧 高级特性
- [代码生成器](docs/12-code-generator.md) - 代码生成器使用指南
- [异常处理](docs/09-exception-handling.md) - 异常处理机制
- [配置管理](docs/10-configuration.md) - 配置管理详解
- [集成测试](docs/INTEGRATION_TEST_GUIDE.md) - 集成测试指南
- [Git工作流](docs/29-git-workflow.md) - Git工作流规范

## 🧪 测试

项目包含完整的测试套件，覆盖率达到 80%+：

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl core
mvn test -pl core-database

# 运行特定数据库测试
mvn test -Dtest=*H2*
mvn test -Dtest=*MySQL*
mvn test -Dtest=*PostgreSQL*

# 生成测试覆盖率报告
mvn test jacoco:report
```

## 📊 性能特性

### 🚀 高性能指标
- **并发处理**: 支持数万并发连接
- **响应时间**: 微秒级响应延迟
- **内存使用**: 低内存占用，高效对象池
- **CPU 利用率**: 单线程事件循环，CPU 友好

### 📈 基准测试
- **HTTP 请求**: 50,000+ QPS
- **WebSocket 连接**: 10,000+ 并发连接
- **数据库查询**: 10,000+ QPS
- **批量操作**: 1000 条记录 < 100ms

## 🤝 贡献指南

我们欢迎社区贡献！请查看：

### 📋 贡献流程
1. **Fork 项目** - 点击右上角 Fork 按钮
2. **创建分支** - `git checkout -b feature/AmazingFeature`
3. **提交更改** - `git commit -m 'Add some AmazingFeature'`
4. **推送分支** - `git push origin feature/AmazingFeature`
5. **创建 PR** - 在 GitHub 上创建 Pull Request

### 📝 代码规范
- **Java 规范**: 遵循阿里巴巴 Java 开发规范
- **注释要求**: 所有 public 方法必须有 JavaDoc
- **测试要求**: 新功能必须包含单元测试
- **提交信息**: 使用清晰的提交信息

### 🧪 测试要求
- **覆盖率**: 新代码测试覆盖率 > 80%
- **测试类型**: 单元测试 + 集成测试
- **测试数据**: 使用 H2 内存数据库

## 📄 许可证

本项目采用 **MIT 许可证** - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- **作者**: QAIU
- **邮箱**: qaiu@qq.com
- **网站**: https://qaiu.top
- **GitHub**: https://github.com/qaiu

## 📈 版本历史

### v2.0.0 (当前版本)
- ✅ **无参构造函数DAO**: 自动初始化，无需手动传递参数，极大简化DAO使用
- ✅ **代码生成器**: 根据数据库表结构自动生成三层架构代码
- ✅ **Lambda 查询增强**: 支持 Join、聚合查询、子查询
- ✅ **批量操作**: batchInsert、batchUpdate、batchDelete
- ✅ **多数据源支持**: 动态数据源切换和事务隔离
- ✅ **注解式路由**: 类似 Spring MVC 的路由注解
- ✅ **参数绑定增强**: 支持方法重载、类型转换
- ✅ **异常处理机制**: 全局和局部异常处理
- ✅ **WebSocket 支持**: 注解式 WebSocket 路由
- ✅ **反向代理**: HTTP/WebSocket 代理支持
- ✅ **配置元数据**: IDE 自动提示和验证
- ✅ **SPI 扩展**: 支持第三方数据库驱动扩展

### v1.0.0
- ✅ 初始版本发布
- ✅ 支持 H2、MySQL、PostgreSQL
- ✅ 完整的 DSL 框架
- ✅ 集成 jOOQ 支持
- ✅ 全面的测试覆盖

## 🎯 未来规划

### 即将发布 (v2.1.0)
- 🔄 **Code-gen 模板引擎**: 代码生成工具
- 🔄 **HTML 模板引擎**: 视图渲染支持
- 🔄 **AOP 支持**: 注解式切面编程
- 🔄 **事件总线**: 注解式事件处理

### 长期规划
- 📋 **微服务支持**: 服务发现、配置中心
- 📋 **监控集成**: Prometheus、Grafana
- 📋 **云原生**: Docker、Kubernetes 支持
- 📋 **多语言**: Kotlin、Scala 支持

---

**🎯 VXCore - 让 Java Web 开发更简单、更高效、更现代！**
