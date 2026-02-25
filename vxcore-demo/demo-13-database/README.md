# demo-13-database — 数据库功能验证模块

## 概述

本模块验证 `core-database` 的核心功能，使用 H2 内存数据库演示完整的 DAO 注入、CRUD 操作和 Lambda 查询。

### 验证清单

| # | 功能点 | 状态 |
|---|--------|------|
| 1 | 数据源初始化（`DataSourceManager` + H2） | ✅ |
| 2 | 无参构造 DAO（`LambdaDao<User, Long>` 自动泛型解析） | ✅ |
| 3 | CRUD（insert / findById / findAll / update / delete） | ✅ |
| 4 | Lambda 查询（`lambdaQuery().like().eq().orderByDesc().list()`） | ✅ |
| 5 | DAO 注入到 Controller（`@Dao` + `@Inject`） | ✅ |
| 6 | Service 注入 DAO（`@Service` + 直接实例化 DAO） | ✅ |
| 7 | 实体类（`BaseEntity` 继承 + `@DdlTable` / `@DdlColumn`） | ✅ |

## 模块结构

```
demo-13-database/
├── pom.xml
├── README.md
└── src/main/
    ├── java/cn/qaiu/demo/database/
    │   ├── DatabaseApp.java              # @App 入口
    │   ├── entity/
    │   │   └── User.java                 # @DdlTable 实体，继承 BaseEntity
    │   ├── dao/
    │   │   └── UserDao.java              # extends LambdaDao<User, Long>，无参构造
    │   ├── service/
    │   │   └── UserService.java          # @Service，包含 CRUD + Lambda 查询
    │   └── controller/
    │       └── UserController.java       # @RouteHandler，REST API
    └── resources/
        ├── app.yml                       # 激活 dev profile
        ├── app-dev.yml                   # H2 数据源 + server 配置
        └── logback.xml
```

## 快速启动

```bash
cd vxcore-demo/demo-13-database
mvn compile exec:java
```

服务在 `http://localhost:18013` 启动。

## REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/users/create` | 创建用户 |
| GET  | `/api/users/list` | 查询所有用户 |
| GET  | `/api/users/{id}` | 按 ID 查询 |
| POST | `/api/users/update/{id}` | 更新用户 |
| GET  | `/api/users/delete/{id}` | 删除用户 |
| GET  | `/api/users/search?name=xxx` | Lambda 模糊搜索 |
| GET  | `/api/users/search?status=xxx` | Lambda 状态过滤 |

## 关键代码说明

### 实体类 — BaseEntity 继承

```java
@DdlTable("users")
public class User extends BaseEntity {

    @DdlColumn(value = "user_name", type = "VARCHAR(100)", nullable = false)
    private String name;

    @DdlColumn(value = "email", type = "VARCHAR(200)")
    private String email;

    @DdlColumn(value = "age", type = "INT")
    private Integer age;

    @DdlColumn(value = "status", type = "VARCHAR(20)", defaultValue = "ACTIVE")
    private String status = "ACTIVE";

    // BaseEntity 提供 id, createTime, updateTime, createBy, updateBy
}
```

### DAO — 无参构造 + Lambda 支持

```java
@Dao
public class UserDao extends LambdaDao<User, Long> {

    @Override
    protected <R> SFunction<User, R> getPrimaryKeyFieldLambda() {
        SFunction<User, R> fn = (SFunction<User, R>) (SFunction<User, Long>) User::getId;
        return fn;
    }
}
```

框架自动通过泛型解析 `User` 类型和 `Long` 主键类型，并从 `DataSourceManager` 获取默认的 `JooqExecutor`。继承 `LambdaDao` 而非 `AbstractDao`，即可获得 `lambdaQuery()` 能力。

### Service — Lambda 查询示例

```java
@Service
public class UserService {

    private final UserDao userDao = new UserDao();

    // 模糊搜索 + 排序
    public Future<List<User>> searchByName(String name) {
        return userDao.lambdaQuery()
                .like(User::getName, name)
                .orderByDesc(User::getId)
                .list();
    }

    // 精确匹配
    public Future<List<User>> findByStatus(String status) {
        return userDao.lambdaQuery()
                .eq(User::getStatus, status)
                .list();
    }
}
```

### Controller — 依赖注入

```java
@RouteHandler("/api/users")
public class UserController {

    @Inject
    UserService userService;   // 由框架 ServiceRegistry 自动注入

    @RouteMapping(value = "/create", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> createUser(@RequestBody JsonObject body) {
        User user = new User(body);
        return userService.createUser(user)
            .map(opt -> JsonResult.data(opt.map(User::toJson).orElse(new JsonObject())));
    }
}
```

## 接口验证流程

以下 curl 命令可完整验证所有功能点：

```bash
# 1. 创建用户
curl -s -X POST http://localhost:18013/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@test.com","age":25}'

curl -s -X POST http://localhost:18013/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","email":"bob@test.com","age":30}'

curl -s -X POST http://localhost:18013/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Charlie","email":"charlie@test.com","age":35}'

# 2. 查询所有用户
curl -s http://localhost:18013/api/users/list

# 3. 按 ID 查询
curl -s http://localhost:18013/api/users/1

# 4. 更新用户
curl -s -X POST http://localhost:18013/api/users/update/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Updated","email":"alice_new@test.com","age":26}'

# 5. 删除用户
curl -s http://localhost:18013/api/users/delete/3

# 6. Lambda 查询 — 按名称模糊搜索
curl -s "http://localhost:18013/api/users/search?name=Ali"

# 7. Lambda 查询 — 按状态过滤
curl -s "http://localhost:18013/api/users/search?status=ACTIVE"
```

## 并发性能测试

使用 Apache Benchmark (`ab`) 在 macOS 本地环境对 H2 内存数据库进行压测。

> 测试环境：macOS, H2 内存数据库, 连接池 max_pool_size=10, Vert.x Event Loop

### 中等并发（50 并发）

| 接口 | 请求数 | 并发数 | QPS | 平均延迟 | P50 | P95 | P99 | 最大延迟 | 失败数 |
|------|--------|--------|-----|----------|-----|-----|-----|----------|--------|
| `GET /api/users/list` | 1000 | 50 | **1,435** | 34.8ms | 21ms | 112ms | 181ms | 243ms | 0 |
| `GET /api/users/1` | 1000 | 50 | **3,859** | 13.0ms | 11ms | 19ms | 24ms | 35ms | 0 |
| `GET /api/users/search?name=Perf` | 1000 | 50 | **1,991** | 25.1ms | 14ms | 113ms | 163ms | 171ms | 0 |
| `POST /api/users/create` | 500 | 50 | **2,253** | 22.2ms | 19ms | 46ms | 49ms | 50ms | 0 |

### 高并发（100 并发）

| 接口 | 请求数 | 并发数 | QPS | 平均延迟 | P50 | P90 | P99 | 最大延迟 | 失败数 |
|------|--------|--------|-----|----------|-----|-----|-----|----------|--------|
| `GET /api/users/1` | 5000 | 100 | **166** | 601ms | 33ms | 3,710ms | 4,098ms | 4,207ms | 0 |
| `GET /api/users/search?name=Perf` | 5000 | 100 | **176** | 567ms | 33ms | 3,587ms | 4,242ms | 4,264ms | 0 |

### 性能分析

**50 并发表现优异**：
- 单记录查询 QPS 达到 **3,859**，P99 仅 24ms
- 写入操作 QPS **2,253**，P99 控制在 49ms
- Lambda 模糊搜索 QPS **1,991**
- 全量列表查询 QPS **1,435**（受返回数据量影响）
- **零失败率**

**100 并发出现瓶颈**：
- P50 依然维持在 33ms，说明大部分请求仍快速完成
- P90 飙升至 3.5s+，原因是 H2 连接池 `max_pool_size=10` 远小于 100 并发，排队等待连接
- QPS 降至 ~170，受限于连接池吞吐
- **零失败率**，服务保持稳定

**瓶颈定位**：高并发下的延迟来自 H2 JDBC 连接池（默认 10 连接）。生产环境可通过以下方式优化：
1. 增大 `max_pool_size`（建议设为并发数的 1.5-2 倍）
2. 使用 MySQL/PostgreSQL 等生产级数据库
3. 利用 Vert.x 非阻塞 I/O 优势，配合异步数据库驱动

## 已知注意事项

1. **DDL 同步**：当前设置为 `DdlSyncStrategy.NONE`，表结构通过 H2 JDBC URL 的 `INIT` 参数创建。框架的 DDL 同步在处理 `BaseEntity` 继承字段时存在限制（`getDeclaredFields()` 不含父类字段）。
2. **Service 中 DAO 注入**：`@Service` 类由框架 `ServiceRegistry` 通过无参构造实例化，不支持字段注入 DAO。解决方案是在 Service 中直接 `new UserDao()`，利用 DAO 的无参构造自动初始化能力。
3. **Controller 中 Service 注入**：`@Inject UserService` 字段注入由 `RouterHandlerFactory` 从 `ServiceRegistry` 获取实例并注入，正常工作。

## 依赖

- `core` — VXCore 核心框架
- `core-database` — 数据库 ORM 模块（DAO、Lambda 查询、DDL 同步）
- `h2` — H2 内存数据库驱动
- `vertx-core` / `vertx-web` — Vert.x 运行时
- `dagger` / `dagger-compiler` — 依赖注入
- `logback-classic` — 日志
