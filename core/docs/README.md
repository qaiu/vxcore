# VXCore Core Module

VXCore 的核心框架模块，提供 Web 开发的基础功能。

## 🎯 核心功能

### ✅ 已完成功能

#### 🌐 Web 开发支持
- **注解式路由** (100%): 类似 Spring MVC 的 `@RouteMapping` 注解
- **参数绑定增强** (100%): 支持方法重载、类型转换、自定义转换器
- **异常处理机制** (100%): 全局和局部异常处理
- **配置元数据** (100%): IDE 自动提示和验证

#### 🔄 反向代理
- **HTTP 代理** (100%): 支持 HTTP 请求代理
- **WebSocket 代理** (100%): 支持 WebSocket 连接代理
- **配置化路由** (100%): 类似 Nginx 的配置方式

#### 🌐 WebSocket 支持
- **注解式 WebSocket** (100%): `@WebSocketHandler`、`@OnOpen`、`@OnMessage` 等
- **事件处理** (100%): 连接建立、消息处理、连接关闭
- **代理支持** (100%): WebSocket 反向代理

### 🔄 进行中功能

#### 📝 模板引擎
- **Code-gen 模板引擎** (1%): 代码生成工具
- **HTML 模板引擎** (0%): 视图渲染支持

### 📋 计划功能

#### 🔧 高级特性
- **AOP 支持** (0%): 注解式切面编程
- **事件总线** (0%): 注解式事件处理
- **拦截器重构** (0%): 统一拦截器机制

## 🏗️ 架构设计

### 核心组件

```
core/
├── annotations/           # 注解定义
│   ├── param/            # 参数注解
│   ├── websocket/        # WebSocket 注解
│   └── config/           # 配置注解
├── handlerfactory/       # 处理器工厂
│   ├── RouterHandlerFactory.java
│   └── WebSocketHandlerFactory.java
├── proxy/                # 反向代理
│   ├── WebSocketProxyHandler.java
│   └── WebSocketProxyConfig.java
├── util/                 # 工具类
│   ├── ParamUtil.java
│   ├── StringCase.java
│   └── ConfigUtil.java
└── verticle/             # Verticle 实现
    └── ReverseProxyVerticle.java
```

### 注解体系

#### 路由注解
```java
@RouteHandler("/api")
public class UserController {
    
    @RouteMapping(value = "/users", method = HttpMethod.GET)
    public Future<JsonResult> getUsers() { }
    
    @RouteMapping(value = "/users", method = HttpMethod.POST)
    public Future<JsonResult> createUser(@RequestBody User user) { }
}
```

#### 参数注解
```java
@RouteMapping("/user/{id}")
public Future<JsonResult> getUser(
    @PathVariable("id") Long id,
    @RequestParam("name") String name,
    @RequestBody User user
) { }
```

#### WebSocket 注解
```java
@WebSocketHandler("/ws/chat")
public class ChatHandler {
    
    @OnOpen
    public void onOpen(ServerWebSocket ws) { }
    
    @OnMessage
    public void onMessage(String message, ServerWebSocket ws) { }
    
    @OnClose
    public void onClose(ServerWebSocket ws) { }
}
```

#### 异常处理注解
```java
@RouteHandler("/api")
public class UserController {
    
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
}
```

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>cn.qaiu</groupId>
    <artifactId>vxcore-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. 创建控制器

```java
@RouteHandler("/api")
public class HelloController {
    
    @RouteMapping(value = "/hello", method = HttpMethod.GET)
    public Future<JsonResult> hello(@RequestParam("name") String name) {
        return Future.succeededFuture(
            JsonResult.success("Hello, " + name + "!")
        );
    }
}
```

### 3. 启动应用

```java
public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RouterVerticle());
    }
}
```

## 📚 详细文档

- [路由注解指南](../docs/08-routing-annotations.md)
- [WebSocket 开发指南](../docs/WEBSOCKET_GUIDE.md)
- [反向代理配置](../docs/WEBSOCKET_PROXY_GUIDE.md)
- [异常处理机制](../docs/09-exception-handling.md)
- [配置管理](../docs/10-configuration.md)

## 🧪 测试

```bash
# 运行 core 模块测试
mvn test -pl core

# 运行特定测试
mvn test -Dtest=RouterHandlerFactoryTest
mvn test -Dtest=WebSocketHandlerFactoryTest
```

## 🔧 配置示例

### application.yml

```yaml
server:
  host: localhost
  port: 8080
  timeout: 30000

proxy:
  enabled: true
  routes:
    - path: /api/v1/*
      target: http://backend:8080
      type: http
    - path: /ws/*
      target: ws://backend:8080
      type: websocket

datasources:
  primary:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
```

## 📈 性能特性

- **高并发**: 支持数万并发连接
- **低延迟**: 微秒级响应时间
- **内存优化**: 零拷贝、对象池
- **CPU 友好**: 单线程事件循环

## 🤝 贡献

欢迎贡献代码！请遵循项目代码规范：

- 遵循阿里巴巴 Java 开发规范
- 所有 public 方法必须有 JavaDoc
- 新功能必须包含单元测试
- 测试覆盖率 > 80%

## 📄 许可证

MIT License - 查看 [LICENSE](../LICENSE) 文件了解详情。