# 快速开始

## 🚀 5分钟快速上手

本指南将帮助您在5分钟内快速搭建VxCore项目并运行第一个示例。

## 📋 前置条件

### 系统要求
- **Java 17+**: 确保已安装Java 17或更高版本
- **Maven 3.9+**: 确保已安装Maven 3.9或更高版本
- **Git**: 用于克隆项目

### 验证环境
```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Git版本
git --version
```

## 🔧 项目搭建

### 1. 克隆项目
```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

### 2. 编译项目
```bash
# 编译整个项目
mvn clean compile

# 或者只编译core-database模块
cd core-database
mvn clean compile
```

### 3. 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=UserDaoTest
```

## 🎯 第一个示例

### 1. 创建简单的用户实体
```java
package cn.qaiu.example;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

@DataObject
public class User implements Shareable {
    private Long id;
    private String username;
    private String email;
    private String password;
    
    public User() {}
    
    public User(JsonObject json) {
        this.id = json.getLong("id");
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.password = json.getString("password");
    }
    
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("username", username)
            .put("email", email)
            .put("password", password);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

### 2. 创建用户DAO
```java
package cn.qaiu.example;

import cn.qaiu.db.dsl.core.AbstractDao;
import cn.qaiu.db.dsl.core.JooqExecutor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserDao extends AbstractDao<User, Long> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    @Override
    protected String getTableName() {
        return "users";
    }
    
    @Override
    protected String getIdFieldName() {
        return "id";
    }
    
    // 根据用户名查找用户
    public CompletableFuture<Optional<User>> findByUsername(String username) {
        Condition condition = DSL.field("username").eq(username);
        return findOneByCondition(condition);
    }
    
    // 根据邮箱查找用户
    public CompletableFuture<Optional<User>> findByEmail(String email) {
        Condition condition = DSL.field("email").eq(email);
        return findOneByCondition(condition);
    }
    
    // 查找所有活跃用户
    public CompletableFuture<List<User>> findActiveUsers() {
        Condition condition = DSL.field("status").eq("ACTIVE");
        return findByCondition(condition);
    }
}
```

### 3. 创建Verticle示例
```java
package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.pool.DatabasePoolManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class UserServiceVerticle extends AbstractVerticle {
    
    private UserDao userDao;
    private DatabasePoolManager poolManager;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 初始化数据库连接池
        poolManager = new DatabasePoolManager(vertx, getDatabaseConfig());
        
        poolManager.getConnectionPool()
            .compose(pool -> {
                // 创建JooqExecutor
                JooqExecutor executor = new JooqExecutor(pool);
                userDao = new UserDao(executor);
                
                // 启动HTTP服务器
                return startHttpServer();
            })
            .onSuccess(v -> {
                System.out.println("UserServiceVerticle started successfully!");
                startPromise.complete();
            })
            .onFailure(throwable -> {
                System.err.println("Failed to start UserServiceVerticle: " + throwable.getMessage());
                startPromise.fail(throwable);
            });
    }
    
    private Future<Void> startHttpServer() {
        return vertx.createHttpServer()
            .requestHandler(req -> {
                String path = req.path();
                String method = req.method().name();
                
                if ("GET".equals(method) && "/users".equals(path)) {
                    handleGetUsers(req);
                } else if ("POST".equals(method) && "/users".equals(path)) {
                    handleCreateUser(req);
                } else {
                    req.response()
                        .setStatusCode(404)
                        .end("Not Found");
                }
            })
            .listen(8080)
            .mapEmpty();
    }
    
    private void handleGetUsers(io.vertx.core.http.HttpServerRequest req) {
        userDao.findAll()
            .onSuccess(users -> {
                JsonObject response = new JsonObject()
                    .put("success", true)
                    .put("data", users);
                
                req.response()
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            })
            .onFailure(throwable -> {
                JsonObject response = new JsonObject()
                    .put("success", false)
                    .put("error", throwable.getMessage());
                
                req.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            });
    }
    
    private void handleCreateUser(io.vertx.core.http.HttpServerRequest req) {
        req.bodyHandler(buffer -> {
            try {
                JsonObject userJson = buffer.toJsonObject();
                User user = new User(userJson);
                
                userDao.insert(user)
                    .onSuccess(insertedUser -> {
                        JsonObject response = new JsonObject()
                            .put("success", true)
                            .put("data", insertedUser.orElse(null));
                        
                        req.response()
                            .putHeader("Content-Type", "application/json")
                            .end(response.encode());
                    })
                    .onFailure(throwable -> {
                        JsonObject response = new JsonObject()
                            .put("success", false)
                            .put("error", throwable.getMessage());
                        
                        req.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end(response.encode());
                    });
            } catch (Exception e) {
                JsonObject response = new JsonObject()
                    .put("success", false)
                    .put("error", "Invalid JSON: " + e.getMessage());
                
                req.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            }
        });
    }
    
    private JsonObject getDatabaseConfig() {
        return new JsonObject()
            .put("url", "jdbc:h2:mem:testdb")
            .put("driver_class", "org.h2.Driver")
            .put("max_pool_size", 10)
            .put("min_pool_size", 2);
    }
}
```

### 4. 创建主启动类
```java
package cn.qaiu.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class QuickStartDemo {
    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        // 部署UserServiceVerticle
        vertx.deployVerticle(new UserServiceVerticle())
            .onSuccess(id -> {
                System.out.println("Verticle deployed with ID: " + id);
                System.out.println("Server is running on http://localhost:8080");
                System.out.println("Try: curl http://localhost:8080/users");
            })
            .onFailure(throwable -> {
                System.err.println("Failed to deploy verticle: " + throwable.getMessage());
                vertx.close();
            });
    }
}
```

## 🧪 运行示例

### 1. 编译示例
```bash
cd core-database/examples
mvn clean compile
```

### 2. 运行示例
```bash
# 运行快速开始示例
mvn exec:java -Dexec.mainClass="cn.qaiu.example.QuickStartDemo"
```

### 3. 测试API
```bash
# 获取用户列表
curl http://localhost:8080/users

# 创建新用户
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"password123"}'
```

## 🔍 示例说明

### 核心概念
1. **Verticle**: Vert.x的基本部署单元，类似于Actor模型
2. **Future**: 异步操作的抽象，支持链式调用
3. **DAO**: 数据访问对象，封装数据库操作
4. **jOOQ DSL**: 类型安全的SQL构建

### 异步编程模式
```java
// 链式调用
userDao.findById(1L)
    .compose(userOpt -> {
        if (userOpt.isPresent()) {
            return userDao.update(userOpt.get());
        }
        return Future.failedFuture("User not found");
    })
    .onSuccess(updatedUser -> {
        System.out.println("User updated: " + updatedUser);
    })
    .onFailure(throwable -> {
        System.err.println("Update failed: " + throwable.getMessage());
    });
```

## 🎯 下一步

- [安装配置](03-installation.md) - 详细的环境配置
- [DSL框架](07-dsl-framework.md) - 深入学习jOOQ DSL
- [DAO开发](10-dao-development.md) - DAO层开发指南

---

**🎯 恭喜！您已经成功运行了第一个VxCore示例！**
