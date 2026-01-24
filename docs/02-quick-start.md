# VXCore 快速开始

欢迎使用 VXCore！本指南将帮助您在 5 分钟内创建一个简单的 Web 应用程序。

## 🎯 学习目标

通过本教程，您将学会：
- 创建第一个 VXCore 应用
- 定义实体类和 DAO
- 创建 REST API 控制器
- 使用 Lambda 查询
- 运行和测试应用

## 📋 前置要求

### 环境要求
- **Java 17+**: [下载 Java](https://adoptium.net/)
- **Maven 3.8+**: [下载 Maven](https://maven.apache.org/download.cgi)
- **IDE**: IntelliJ IDEA 或 Eclipse

### 验证环境
```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本
mvn -version
```

## 🚀 第一步：创建项目

### 1.1 使用 Maven 依赖（推荐）

VXCore 已发布到 Maven 中央仓库，您可以直接在项目中引入依赖：

#### 创建新项目
```bash
# 使用 Maven 创建新项目
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-vxcore-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd my-vxcore-app
```

#### 添加 VXCore 依赖
在 `pom.xml` 中添加：

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <vxcore.version>1.1.0</vxcore.version>
    <vertx.version>4.5.21</vertx.version>
</properties>

<dependencies>
    <!-- VXCore 核心模块 -->
    <dependency>
        <groupId>cn.qaiu</groupId>
        <artifactId>core</artifactId>
        <version>${vxcore.version}</version>
    </dependency>
    
    <!-- VXCore 数据库模块 -->
    <dependency>
        <groupId>cn.qaiu</groupId>
        <artifactId>core-database</artifactId>
        <version>${vxcore.version}</version>
    </dependency>
    
    <!-- H2 数据库（用于开发测试） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.220</version>
    </dependency>
</dependencies>
```

### 1.2 克隆源码（可选）

如果您想研究源码或贡献代码，可以克隆项目：

```bash
# 克隆 VXCore 项目
git clone https://github.com/qaiu/vxcore.git
cd vxcore

# 查看项目结构
ls -la
```

### 1.3 项目结构说明
```
vxcore/
├── core/                    # 核心框架模块
├── core-database/          # 数据库操作模块
├── core-example/           # 示例模块
├── docs/                   # 文档
└── pom.xml                 # 根项目配置
```

### 1.3 编译项目
```bash
# 编译整个项目
mvn clean compile

# 如果编译成功，您会看到类似输出：
# [INFO] BUILD SUCCESS
```

## 🗄️ 第二步：配置数据库

### 2.1 使用 H2 内存数据库
VXCore 默认使用 H2 内存数据库，无需额外配置即可开始开发。

### 2.2 数据库配置（可选）
如果您想使用其他数据库，可以创建 `application.yml` 文件：

```yaml
# application.yml
datasources:
  primary:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
    driver: org.h2.Driver
    maxPoolSize: 10
    minPoolSize: 2
```

## 📝 第三步：创建实体类

### 3.1 定义用户实体
创建 `User.java` 文件：

```java
package com.example.entity;

import cn.qaiu.db.ddl.annotation.DdlColumn;
import cn.qaiu.db.ddl.annotation.DdlTable;
import cn.qaiu.db.dsl.core.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@DdlTable("users")
public class User extends BaseEntity {
    
    @DdlColumn("user_name")
    private String name;
    
    @DdlColumn("user_email")
    private String email;
    
    @DdlColumn("user_status")
    private String status = "ACTIVE";
    
    @DdlColumn("last_login_time")
    private LocalDateTime lastLoginTime;
    
    // 构造函数
    public User() {}
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // Getter 和 Setter 方法
    public String getUsername() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + getCreateTime() +
                '}';
    }
}
```

### 3.2 实体类说明
- `@DdlTable("users")`: 指定数据库表名
- `@DdlColumn("user_name")`: 指定数据库列名
- `extends BaseEntity`: 继承基础实体，自动处理 id、createTime、updateTime

## 🔧 第四步：创建 DAO

### 4.1 定义用户 DAO
创建 `UserDao.java` 文件：

```java
package com.example.dao;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import com.example.entity.User;
import io.vertx.core.Future;
import org.jooq.SortOrder;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问对象
 */
public class UserDao extends AbstractDao<User> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    /**
     * 根据邮箱查找用户
     */
    public Future<Optional<User>> findByEmail(String email) {
        return lambdaQuery()
            .eq(User::getEmail, email)
            .first();
    }
    
    /**
     * 查找活跃用户
     */
    public Future<List<User>> findActiveUsers() {
        return lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
    
    /**
     * 根据姓名模糊查询
     */
    public Future<List<User>> findByNameLike(String name) {
        return lambdaQuery()
            .like(User::getName, "%" + name + "%")
            .list();
    }
    
    /**
     * 批量创建用户
     */
    public Future<List<User>> batchCreateUsers(List<User> users) {
        return batchInsert(users);
    }
}
```

### 4.2 DAO 说明
- `extends AbstractDao<User>`: 继承抽象 DAO，提供基础 CRUD 操作
- **无参构造函数**: 框架自动处理所有初始化，无需手动传递参数
- `lambdaQuery()`: 创建 Lambda 查询包装器
- `eq(User::getEmail, email)`: 类型安全的字段引用

### 4.3 无参构造函数DAO（推荐）

```java
// 最简单的DAO - 连构造函数都没有！
public class UserDao extends AbstractDao<User, Long> {
    // 完全空的类，框架自动处理所有初始化
    // 1. 自动通过泛型获取User类型
    // 2. 自动初始化SQL执行器
    // 3. 自动获取表名和主键信息
}

// 使用方式
UserDao userDao = new UserDao(); // 无需传递任何参数！
```

## 🌐 第五步：创建控制器

### 5.1 定义用户控制器
创建 `UserController.java` 文件：

```java
package com.example.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.annotations.RequestParam;
import cn.qaiu.vx.core.annotations.RequestBody;
import cn.qaiu.vx.core.annotations.ExceptionHandler;
import cn.qaiu.vx.core.util.JsonResult;
import com.example.entity.User;
import com.example.service.UserService;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;

import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 */
@RouteHandler("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取所有用户
     */
    @RouteMapping(value = "", method = HttpMethod.GET)
    public Future<JsonResult> getAllUsers() {
        return userService.findAllUsers()
            .map(users -> JsonResult.success("查询成功", users));
    }
    
    /**
     * 根据 ID 获取用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.GET)
    public Future<JsonResult> getUserById(@RequestParam("id") Long id) {
        return userService.findUserById(id)
            .map(userOptional -> {
                if (userOptional.isPresent()) {
                    return JsonResult.success("查询成功", userOptional.get());
                } else {
                    return JsonResult.fail(404, "用户不存在");
                }
            });
    }
    
    /**
     * 创建用户
     */
    @RouteMapping(value = "", method = HttpMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) {
        return userService.createUser(user)
            .map(createdUser -> JsonResult.success("创建成功", createdUser));
    }
    
    /**
     * 更新用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.PUT)
    public Future<JsonResult> updateUser(@RequestParam("id") Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user)
            .map(updatedUser -> JsonResult.success("更新成功", updatedUser));
    }
    
    /**
     * 删除用户
     */
    @RouteMapping(value = "/{id}", method = HttpMethod.DELETE)
    public Future<JsonResult> deleteUser(@RequestParam("id") Long id) {
        return userService.deleteUser(id)
            .map(result -> {
                if (result) {
                    return JsonResult.success("删除成功");
                } else {
                    return JsonResult.fail(404, "用户不存在");
                }
            });
    }
    
    /**
     * 搜索用户
     */
    @RouteMapping(value = "/search", method = HttpMethod.GET)
    public Future<JsonResult> searchUsers(@RequestParam("keyword") String keyword) {
        return userService.searchUsers(keyword)
            .map(users -> JsonResult.success("搜索成功", users));
    }
    
    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResult handleIllegalArgument(IllegalArgumentException e) {
        return JsonResult.fail(400, "参数错误: " + e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public JsonResult handleRuntime(RuntimeException e) {
        return JsonResult.fail(500, "服务器错误: " + e.getMessage());
    }
}
```

### 5.2 控制器说明
- `@RouteHandler("/api/users")`: 指定基础路径
- `@RouteMapping`: 指定具体路由
- `@RequestParam`: 绑定请求参数
- `@RequestBody`: 绑定请求体
- `@ExceptionHandler`: 处理异常

## 🔧 第六步：创建服务层

### 6.1 定义用户服务
创建 `UserService.java` 文件：

```java
package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 */
public class UserService {
    
    private final UserDao userDao;
    
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }
    
    /**
     * 查找所有用户
     */
    public Future<List<User>> findAllUsers() {
        return userDao.findAll();
    }
    
    /**
     * 根据 ID 查找用户
     */
    public Future<Optional<User>> findUserById(Long id) {
        return userDao.findById(id);
    }
    
    /**
     * 创建用户
     */
    public Future<User> createUser(User user) {
        // 参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("用户名不能为空"));
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("邮箱不能为空"));
        }
        
        // 检查邮箱是否已存在
        return userDao.findByEmail(user.getEmail())
            .compose(existingUser -> {
                if (existingUser.isPresent()) {
                    return Future.failedFuture(new IllegalArgumentException("邮箱已存在"));
                }
                
                // 创建用户
                return userDao.create(user);
            });
    }
    
    /**
     * 更新用户
     */
    public Future<User> updateUser(User user) {
        return userDao.update(user);
    }
    
    /**
     * 删除用户
     */
    public Future<Boolean> deleteUser(Long id) {
        return userDao.delete(id);
    }
    
    /**
     * 搜索用户
     */
    public Future<List<User>> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userDao.findAll();
        }
        
        return userDao.findByNameLike(keyword);
    }
}
```

## 🚀 第七步：创建主应用

### 7.1 定义主应用类
创建 `MainApplication.java` 文件：

```java
package com.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.vx.core.verticle.RouterVerticle;
import com.example.controller.UserController;
import com.example.dao.UserDao;
import com.example.service.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;

/**
 * 主应用类
 */
public class MainApplication {
    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        // 配置数据库连接
        JsonObject config = new JsonObject()
            .put("url", "jdbc:h2:mem:testdb")
            .put("username", "sa")
            .put("password", "")
            .put("driver_class", "org.h2.Driver");
        
        // 创建数据库连接池
        JDBCPool pool = JDBCPool.pool(vertx, config);
        
        // 创建 jOOQ 执行器
        JooqExecutor executor = new JooqExecutor(pool);
        
        // 创建 DAO
        UserDao userDao = new UserDao(executor);
        
        // 创建服务
        UserService userService = new UserService(userDao);
        
        // 创建控制器
        UserController userController = new UserController(userService);
        
        // 创建路由 Verticle
        RouterVerticle routerVerticle = new RouterVerticle();
        
        // 启动应用
        vertx.deployVerticle(routerVerticle)
            .onSuccess(id -> {
                System.out.println("✅ VXCore 应用启动成功！");
                System.out.println("🌐 访问地址: http://localhost:8080");
                System.out.println("📚 API 文档: http://localhost:8080/api/users");
            })
            .onFailure(throwable -> {
                System.err.println("❌ 应用启动失败: " + throwable.getMessage());
                throwable.printStackTrace();
            });
    }
}
```

## 🧪 第八步：测试应用

### 8.1 启动应用
```bash
# 编译项目
mvn clean compile

# 运行应用
mvn exec:java -Dexec.mainClass="com.example.MainApplication"
```

### 8.2 测试 API
```bash
# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'

# 获取所有用户
curl http://localhost:8080/api/users

# 根据 ID 获取用户
curl http://localhost:8080/api/users/1

# 搜索用户
curl http://localhost:8080/api/users/search?keyword=张

# 更新用户
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"张三丰","email":"zhangsanfeng@example.com"}'

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1
```

### 8.3 预期响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "email": "zhangsan@example.com",
      "status": "ACTIVE",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

## 🎉 恭喜！

您已经成功创建了第一个 VXCore 应用！现在您可以：

### 下一步学习
- [安装配置](03-installation.md) - 深入学习配置选项
- [无参构造函数DAO](13-no-arg-constructor-dao.md) - 掌握无参构造函数DAO的使用
- [Lambda 查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - 掌握数据库操作
- [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 学习多数据源配置
- [路由注解指南](08-routing-annotations.md) - 深入了解 Web 开发

### 扩展功能
- 添加数据验证
- 实现分页查询
- 添加缓存支持
- 集成消息队列
- 添加监控和日志

### 获取帮助
- [GitHub Issues](https://github.com/qaiu/vxcore/issues) - 提交问题
- [讨论区](https://github.com/qaiu/vxcore/discussions) - 技术讨论
- [邮件支持](mailto:qaiu@qq.com) - 直接联系

---

**🎯 继续探索 VXCore 的强大功能！**

[安装配置 →](03-installation.md) | [Lambda 查询 →](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) | [返回概述 →](01-overview.md)