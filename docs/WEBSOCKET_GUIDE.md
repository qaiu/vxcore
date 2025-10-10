# VXCore WebSocket 使用指南

## 概述

VXCore框架提供了类似Spring的WebSocket注解支持，让开发者可以轻松创建WebSocket处理器。框架支持连接建立、消息接收、连接关闭和错误处理等事件。

## 核心注解

### @WebSocketHandler
标记WebSocket处理器类：

```java
@WebSocketHandler(value = "/ws/chat", description = "聊天WebSocket处理器")
public class ChatWebSocketHandler {
    // WebSocket处理器实现
}
```

**属性说明：**
- `value`: WebSocket路径
- `description`: 处理器描述
- `enabled`: 是否启用（默认true）
- `order`: 注册顺序

### @OnOpen
标记连接建立事件处理方法：

```java
@OnOpen("用户连接")
public void onOpen(ServerWebSocket socket) {
    // 连接建立时的处理逻辑
}
```

### @OnMessage
标记消息接收事件处理方法：

```java
@OnMessage(type = OnMessage.MessageType.TEXT, value = "处理文本消息")
public void onTextMessage(ServerWebSocket socket, String message) {
    // 处理文本消息
}

@OnMessage(type = OnMessage.MessageType.BINARY, value = "处理二进制消息")
public void onBinaryMessage(ServerWebSocket socket, String message) {
    // 处理二进制消息
}
```

**消息类型：**
- `TEXT`: 文本消息
- `BINARY`: 二进制消息
- `PING`: Ping消息
- `PONG`: Pong消息

### @OnClose
标记连接关闭事件处理方法：

```java
@OnClose("用户断开连接")
public void onClose(ServerWebSocket socket) {
    // 连接关闭时的处理逻辑
}
```

### @OnError
标记错误处理事件方法：

```java
@OnError({Exception.class})
public void onError(ServerWebSocket socket, Throwable error) {
    // 错误处理逻辑
}
```

## 完整示例

### 1. 回显WebSocket处理器

```java
@WebSocketHandler(value = "/ws/echo", description = "回显WebSocket处理器")
public class EchoWebSocketHandler {
    
    @OnOpen("连接建立")
    public void onOpen(ServerWebSocket socket) {
        socket.writeTextMessage("Echo WebSocket connected!");
    }
    
    @OnMessage(type = OnMessage.MessageType.TEXT, value = "回显文本消息")
    public void onTextMessage(ServerWebSocket socket, String message) {
        socket.writeTextMessage("Echo: " + message);
    }
    
    @OnClose("连接关闭")
    public void onClose(ServerWebSocket socket) {
        // 连接关闭处理
    }
    
    @OnError({Exception.class})
    public void onError(ServerWebSocket socket, Throwable error) {
        socket.writeTextMessage("Error: " + error.getMessage());
    }
}
```

### 2. 聊天室WebSocket处理器

```java
@WebSocketHandler(value = "/ws/chat", description = "聊天WebSocket处理器")
public class ChatWebSocketHandler {
    
    private static final ConcurrentHashMap<String, ServerWebSocket> connections = new ConcurrentHashMap<>();
    
    @OnOpen("用户连接")
    public void onOpen(ServerWebSocket socket) {
        String connectionId = socket.textHandlerID();
        connections.put(connectionId, socket);
        
        // 广播新用户加入
        broadcastMessage("系统", "用户 " + connectionId + " 加入了聊天室");
    }
    
    @OnMessage(type = OnMessage.MessageType.TEXT, value = "处理聊天消息")
    public void onTextMessage(ServerWebSocket socket, String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String username = parts[0].trim();
            String content = parts[1].trim();
            broadcastMessage(username, content);
        }
    }
    
    @OnClose("用户断开连接")
    public void onClose(ServerWebSocket socket) {
        String connectionId = socket.textHandlerID();
        connections.remove(connectionId);
        broadcastMessage("系统", "用户 " + connectionId + " 离开了聊天室");
    }
    
    private void broadcastMessage(String username, String content) {
        String message = String.format("[%s] %s: %s", 
                LocalDateTime.now(), username, content);
        
        connections.values().forEach(socket -> {
            socket.writeTextMessage(message);
        });
    }
}
```

## 客户端连接示例

### JavaScript客户端

```javascript
// 连接WebSocket
const socket = new WebSocket('ws://localhost:8080/ws/echo');

// 连接建立
socket.onopen = function(event) {
    console.log('WebSocket连接已建立');
    socket.send('Hello, WebSocket!');
};

// 接收消息
socket.onmessage = function(event) {
    console.log('收到消息:', event.data);
};

// 连接关闭
socket.onclose = function(event) {
    console.log('WebSocket连接已关闭');
};

// 错误处理
socket.onerror = function(error) {
    console.error('WebSocket错误:', error);
};
```

### Java客户端（使用Vert.x）

```java
WebSocketClient client = WebSocketClient.create(vertx);
client.connect(8080, "localhost", "/ws/echo")
    .onSuccess(webSocket -> {
        System.out.println("WebSocket连接已建立");
        
        webSocket.textMessageHandler(message -> {
            System.out.println("收到消息: " + message);
        });
        
        webSocket.writeTextMessage("Hello from Java client!");
    })
    .onFailure(error -> {
        System.err.println("连接失败: " + error.getMessage());
    });
```

## 配置说明

### 1. 路径配置
WebSocket路径会自动添加网关前缀：

```java
// 如果网关前缀是 /api
@WebSocketHandler(value = "/ws/chat")
// 实际路径将是 /api/ws/chat
```

### 2. 处理器注册
WebSocket处理器会自动注册到路由器，无需手动配置。

### 3. 异常处理
WebSocket异常会被自动捕获并调用对应的@OnError方法。

## 最佳实践

### 1. 连接管理
- 使用ConcurrentHashMap管理连接
- 在@OnOpen中记录连接信息
- 在@OnClose中清理连接资源

### 2. 消息处理
- 使用消息类型区分不同类型的消息
- 对消息进行格式验证
- 处理消息解析异常

### 3. 错误处理
- 为不同类型的异常定义不同的处理方法
- 向客户端发送友好的错误消息
- 记录详细的错误日志

### 4. 性能优化
- 避免在WebSocket处理器中执行耗时操作
- 使用异步处理长时间任务
- 合理设置心跳间隔

## 注意事项

1. **线程安全**: WebSocket处理器可能被多个线程同时访问，注意线程安全
2. **资源管理**: 及时清理连接和资源，避免内存泄漏
3. **异常处理**: 妥善处理WebSocket异常，避免连接异常断开
4. **消息格式**: 定义清晰的消息格式，便于客户端解析

## 故障排除

### 常见问题

1. **连接失败**
   - 检查WebSocket路径是否正确
   - 确认服务器已启动
   - 检查防火墙设置

2. **消息发送失败**
   - 检查连接状态
   - 确认消息格式正确
   - 查看服务器日志

3. **处理器未注册**
   - 确认类上有@WebSocketHandler注解
   - 检查包扫描路径
   - 查看启动日志

## 扩展功能

### 1. 消息广播
```java
private void broadcastMessage(String message) {
    connections.values().forEach(socket -> {
        if (socket != null && !socket.isClosed()) {
            socket.writeTextMessage(message);
        }
    });
}
```

### 2. 用户认证
```java
@OnOpen("用户连接")
public void onOpen(ServerWebSocket socket) {
    // 从查询参数获取token
    String token = socket.query();
    if (isValidToken(token)) {
        // 连接有效
    } else {
        socket.close((short) 1008, "Invalid token");
    }
}
```

### 3. 房间管理
```java
private static final Map<String, Set<ServerWebSocket>> rooms = new ConcurrentHashMap<>();

public void joinRoom(String roomId, ServerWebSocket socket) {
    rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(socket);
}

public void leaveRoom(String roomId, ServerWebSocket socket) {
    Set<ServerWebSocket> roomSockets = rooms.get(roomId);
    if (roomSockets != null) {
        roomSockets.remove(socket);
    }
}
```
