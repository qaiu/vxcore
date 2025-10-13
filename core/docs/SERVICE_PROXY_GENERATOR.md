# VXCore Service Proxy Generator

## 概述

VXCore Service Proxy Generator 是一个自定义的Java注解处理器，专门为VXCore项目设计，用于自动生成Vert.x服务代理类。它基于Vert.x的`@ProxyGen`注解，但针对项目需求进行了优化。

## 功能特性

1. **自动代理生成**：自动为标注了`@ProxyGen`的服务接口生成Event Bus代理类
2. **智能类型处理**：正确处理`Future<T>`、`List<T>`、`JsonObject`等Vert.x常用类型
3. **Event Bus集成**：生成的代理类自动通过Event Bus进行服务调用
4. **异常处理**：内置ServiceException支持，确保错误信息正确传递

## 使用方法

### 1. 定义服务接口

服务接口需要：
- 使用`@ProxyGen`注解标注
- 所有方法返回`Future<T>`类型
- 方法参数支持基本类型、JsonObject、实体类等

```java
package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.SimpleJService;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import cn.qaiu.example.entity.User;

/**
 * 用户服务接口
 */
@ProxyGen
public interface UserService extends SimpleJService<User, Long> {
    
    /**
     * 根据用户名查询用户
     */
    Future<User> findByUsername(String username);
    
    /**
     * 更新用户状态
     */
    Future<Boolean> updateStatus(Long userId, Integer status);
}
```

### 2. 生成代理类

编译项目时，注解处理器会自动生成代理类：

```bash
mvn clean compile
```

生成的代理类位于：`src/main/generated/{package}/UserServiceVertxEBProxy.java`

### 3. 使用代理类

```java
// 创建代理实例
UserService userService = new UserServiceVertxEBProxy(vertx, "user.service.address");

// 调用服务方法
userService.findByUsername("admin")
    .onSuccess(user -> {
        System.out.println("找到用户：" + user.getUsername());
    })
    .onFailure(err -> {
        System.err.println("查询失败：" + err.getMessage());
    });
```

## 生成的代理类结构

代理类会包含以下内容：

```java
public class UserServiceVertxEBProxy implements UserService {
    private Vertx _vertx;
    private String _address;
    private DeliveryOptions _options;
    private boolean closed;
    
    // 构造函数
    public UserServiceVertxEBProxy(Vertx vertx, String address) { ... }
    public UserServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) { ... }
    
    // 实现的接口方法
    @Override
    public Future<User> findByUsername(String username) {
        // Event Bus请求代码
        ...
    }
}
```

## 工作原理

### 1. 注解处理阶段

编译时，`ServiceProxyGenerator`会：
1. 扫描所有标注了`@ProxyGen`的接口
2. 分析接口的方法签名和参数类型
3. 生成对应的代理类源码

### 2. 代理调用流程

```
客户端调用代理方法
    ↓
代理方法将参数封装为JsonObject
    ↓
通过Event Bus发送请求到服务地址
    ↓
服务端处理请求并返回结果
    ↓
代理方法解析响应并返回Future
```

### 3. 消息格式

Event Bus消息格式：

**请求消息：**
```json
{
  "action": "findByUsername",
  "username": "admin"
}
```

**响应消息：**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com"
}
```

## 配置说明

### Maven配置

`core/pom.xml`中的相关配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.vertx</groupId>
                        <artifactId>vertx-codegen</artifactId>
                        <version>${vertx.version}</version>
                    </path>
                </annotationProcessorPaths>
                <generatedSourcesDirectory>
                    ${project.basedir}/src/main/generated
                </generatedSourcesDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 注解处理器注册

`core/src/main/resources/META-INF/services/javax.annotation.processing.Processor`:

```
cn.qaiu.vx.core.codegen.ServiceProxyGenerator
```

## 支持的类型

### 返回类型

- `Future<T>` - 必需，所有方法必须返回Future
- `T`可以是：
  - 基本类型：`String`, `Integer`, `Long`, `Boolean`等
  - Vert.x类型：`JsonObject`, `JsonArray`
  - 实体类：任何可序列化的POJO
  - 集合类型：`List<T>`

### 参数类型

- 基本类型：`String`, `Integer`, `Long`, `Boolean`等
- Vert.x类型：`JsonObject`, `JsonArray`
- 实体类：任何可序列化的POJO
- 集合类型：`List<T>`

## 最佳实践

### 1. 服务接口设计

```java
@ProxyGen
public interface OrderService extends SimpleJService<Order, Long> {
    
    // ✅ 推荐：方法名清晰，参数类型明确
    Future<Order> findById(Long id);
    Future<List<Order>> findByUserId(Long userId);
    Future<Boolean> updateStatus(Long orderId, String status);
    
    // ❌ 不推荐：参数过多，应该使用DTO封装
    // Future<Order> createOrder(Long userId, String product, Integer quantity, 
    //                          Double price, String address, String phone);
    
    // ✅ 推荐：使用DTO封装复杂参数
    Future<Order> createOrder(CreateOrderDTO dto);
}
```

### 2. 错误处理

```java
userService.findById(userId)
    .onSuccess(user -> {
        // 处理成功情况
    })
    .onFailure(err -> {
        if (err instanceof ServiceException) {
            ServiceException se = (ServiceException) err;
            // 处理服务异常
            log.error("服务错误 [{}]: {}", se.failureCode(), se.getMessage());
        } else {
            // 处理其他异常
            log.error("未知错误", err);
        }
    });
```

### 3. 服务地址约定

建议使用统一的服务地址命名规范：

```java
// 格式：{模块}.{实体}.service
public static final String USER_SERVICE_ADDRESS = "example.user.service";
public static final String ORDER_SERVICE_ADDRESS = "example.order.service";
public static final String PRODUCT_SERVICE_ADDRESS = "example.product.service";
```

## 故障排查

### 1. 代理类未生成

**问题**：编译后在`src/main/generated`目录下找不到代理类

**解决方案**：
```bash
# 1. 清理并重新编译
mvn clean compile

# 2. 检查是否有编译错误
mvn compile -X

# 3. 确认接口有@ProxyGen注解
```

### 2. 编译错误

**问题**：编译时报错"cannot find symbol: UserServiceVertxEBProxy"

**解决方案**：
- 先执行`mvn clean compile`生成代理类
- 刷新IDE项目结构
- 确保`src/main/generated`目录被标记为源码目录

### 3. 运行时错误

**问题**：调用代理方法时抛出异常

**解决方案**：
- 确认服务端已注册并监听对应的Event Bus地址
- 检查DeliveryOptions配置是否正确
- 验证消息格式和参数类型匹配

## 与SimpleJService集成

`SimpleJService`是专门为服务代理设计的简化接口，只包含Vert.x `@ProxyGen`能够处理的方法：

```java
public interface SimpleJService<T, ID> {
    Future<Optional<T>> findById(ID id);
    Future<List<T>> findAll();
    Future<T> save(T entity);
    Future<Boolean> deleteById(ID id);
    Future<List<T>> query(LambdaQueryWrapper<T> wrapper);
    Future<LambdaPageResult<T>> page(LambdaQueryWrapper<T> wrapper, int pageNum, int pageSize);
}
```

继承`SimpleJService`的服务接口会自动获得这些基础CRUD方法的代理实现。

## 示例项目

完整示例请参考：`core-example`模块

- `cn.qaiu.example.service.UserService` - 服务接口定义
- `cn.qaiu.example.service.UserServiceImpl` - 服务实现
- `generated/cn/qaiu/example/service/UserServiceVertxEBProxy.java` - 生成的代理类

## 技术细节

### 注解处理器实现

`ServiceProxyGenerator`继承自`AbstractProcessor`，在编译时处理`@ProxyGen`注解：

```java
@SupportedAnnotationTypes("io.vertx.codegen.annotations.ProxyGen")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ServiceProxyGenerator extends AbstractProcessor {
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, 
                          RoundEnvironment roundEnv) {
        // 处理注解并生成代理类
        ...
    }
}
```

### 代码生成策略

1. **类型推断**：自动识别方法返回类型和参数类型
2. **导入优化**：只导入必要的类，避免冗余
3. **代码格式**：生成的代码遵循统一的格式规范
4. **错误处理**：编译时报告详细的错误信息

## 总结

VXCore Service Proxy Generator 提供了一个强大而灵活的服务代理生成解决方案，专门为VXCore项目优化。通过自动化代理类生成，简化了Vert.x服务开发，提高了开发效率。

