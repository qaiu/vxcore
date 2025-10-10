# 多数据源指南

## 概述

VXCore数据库模块支持多数据源管理，可以同时连接和管理多个数据库，支持动态切换和并发访问。

## 核心特性

- **多数据源支持**: 同时管理多个数据库连接
- **动态切换**: 运行时动态切换数据源
- **连接池管理**: 每个数据源独立的连接池
- **SPI扩展**: 支持自定义数据源提供者
- **故障恢复**: 自动处理数据源故障和恢复

## 快速开始

### 1. 数据源配置

```java
// 创建数据源配置
DataSourceConfig config = new DataSourceConfig(
    "primary",           // 数据源名称
    "h2",               // 数据源类型
    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",  // 连接URL
    "sa",               // 用户名
    ""                  // 密码
);
config.setDriver("org.h2.Driver");
config.setMaxPoolSize(10);
```

### 2. 数据源管理器

```java
// 获取数据源管理器实例
DataSourceManager manager = DataSourceManager.getInstance(vertx);

// 添加数据源
manager.addDataSource(config)
    .onSuccess(v -> System.out.println("数据源添加成功"))
    .onFailure(error -> System.err.println("数据源添加失败: " + error.getMessage()));
```

### 3. 使用数据源

```java
// 使用指定数据源执行操作
manager.executeWithDataSource("primary", executor -> {
    return executor.executeQuery("SELECT 1 as test");
}).onSuccess(result -> {
    System.out.println("查询结果: " + result);
}).onFailure(error -> {
    System.err.println("查询失败: " + error.getMessage());
});
```

## 数据源类型

### H2数据库

```java
DataSourceConfig h2Config = new DataSourceConfig(
    "h2_db",
    "h2",
    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
    "sa",
    ""
);
h2Config.setDriver("org.h2.Driver");
```

### MySQL数据库

```java
DataSourceConfig mysqlConfig = new DataSourceConfig(
    "mysql_db",
    "mysql",
    "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC",
    "root",
    "password"
);
mysqlConfig.setDriver("com.mysql.cj.jdbc.Driver");
```

### PostgreSQL数据库

```java
DataSourceConfig postgresConfig = new DataSourceConfig(
    "postgres_db",
    "postgresql",
    "jdbc:postgresql://localhost:5432/testdb",
    "postgres",
    "password"
);
postgresConfig.setDriver("org.postgresql.Driver");
```

## 多数据源管理

### 添加多个数据源

```java
DataSourceManager manager = DataSourceManager.getInstance(vertx);

// 主数据源
DataSourceConfig primaryConfig = new DataSourceConfig(
    "primary", "h2", "jdbc:h2:mem:primary", "sa", ""
);
primaryConfig.setDriver("org.h2.Driver");

// 从数据源
DataSourceConfig secondaryConfig = new DataSourceConfig(
    "secondary", "h2", "jdbc:h2:mem:secondary", "sa", ""
);
secondaryConfig.setDriver("org.h2.Driver");

// 添加数据源
CompletableFuture<Void> addPrimary = new CompletableFuture<>();
CompletableFuture<Void> addSecondary = new CompletableFuture<>();

manager.addDataSource(primaryConfig)
    .onSuccess(v -> addPrimary.complete(null))
    .onFailure(error -> addPrimary.completeExceptionally(error));

manager.addDataSource(secondaryConfig)
    .onSuccess(v -> addSecondary.complete(null))
    .onFailure(error -> addSecondary.completeExceptionally(error));

// 等待所有数据源添加完成
CompletableFuture.allOf(addPrimary, addSecondary)
    .thenAccept(v -> System.out.println("所有数据源添加完成"));
```

### 数据源切换

```java
// 切换到主数据源
manager.executeWithDataSource("primary", executor -> {
    return executor.executeUpdate("CREATE TABLE users (id BIGINT PRIMARY KEY, name VARCHAR(100))");
}).onSuccess(v -> {
    System.out.println("主数据源操作完成");
    
    // 切换到从数据源
    return manager.executeWithDataSource("secondary", executor -> {
        return executor.executeUpdate("CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(100))");
    });
}).onSuccess(v -> {
    System.out.println("从数据源操作完成");
}).onFailure(error -> {
    System.err.println("数据源切换失败: " + error.getMessage());
});
```

### 并发数据源访问

```java
// 并发访问不同数据源
CompletableFuture<Void> primaryTask = new CompletableFuture<>();
CompletableFuture<Void> secondaryTask = new CompletableFuture<>();

// 主数据源操作
manager.executeWithDataSource("primary", executor -> {
    return executor.executeQuery("SELECT COUNT(*) as count FROM users");
}).onSuccess(result -> {
    System.out.println("主数据源查询结果: " + result);
    primaryTask.complete(null);
}).onFailure(error -> primaryTask.completeExceptionally(error));

// 从数据源操作
manager.executeWithDataSource("secondary", executor -> {
    return executor.executeQuery("SELECT COUNT(*) as count FROM products");
}).onSuccess(result -> {
    System.out.println("从数据源查询结果: " + result);
    secondaryTask.complete(null);
}).onFailure(error -> secondaryTask.completeExceptionally(error));

// 等待所有任务完成
CompletableFuture.allOf(primaryTask, secondaryTask)
    .thenAccept(v -> System.out.println("并发数据源访问完成"));
```

## 数据源提供者

### 内置提供者

框架内置了以下数据源提供者：

- **H2DataSourceProvider**: H2数据库支持
- **MySQLDataSourceProvider**: MySQL数据库支持
- **PostgreSQLDataSourceProvider**: PostgreSQL数据库支持

### 自定义提供者

```java
public class CustomDataSourceProvider implements DataSourceProvider {
    
    @Override
    public String getType() {
        return "custom";
    }
    
    @Override
    public Future<Pool> createPool(DataSourceConfig config) {
        // 自定义数据源创建逻辑
        return Future.succeededFuture(/* 自定义连接池 */);
    }
    
    @Override
    public boolean supports(String type) {
        return "custom".equals(type);
    }
}

// 注册自定义提供者
DataSourceProviderRegistry registry = DataSourceProviderRegistry.getInstance();
registry.registerProvider(new CustomDataSourceProvider());
```

## 配置管理

### 配置文件方式

```yaml
# application.yml
datasources:
  primary:
    type: h2
    url: jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1
    username: sa
    password: ""
    driver: org.h2.Driver
    maxPoolSize: 10
    
  secondary:
    type: mysql
    url: jdbc:mysql://localhost:3306/testdb
    username: root
    password: password
    driver: com.mysql.cj.jdbc.Driver
    maxPoolSize: 20
```

### 程序配置方式

```java
// 从配置文件加载
DataSourceConfigLoader loader = new DataSourceConfigLoader();
List<DataSourceConfig> configs = loader.loadFromFile("datasources.yml");

// 批量添加数据源
DataSourceManager manager = DataSourceManager.getInstance(vertx);
for (DataSourceConfig config : configs) {
    manager.addDataSource(config);
}
```

## 连接池配置

### 连接池参数

```java
DataSourceConfig config = new DataSourceConfig(
    "test_db", "h2", "jdbc:h2:mem:test", "sa", ""
);

// 连接池配置
config.setMaxPoolSize(20);        // 最大连接数
config.setMinPoolSize(5);         // 最小连接数
config.setMaxWaitQueueSize(100);  // 最大等待队列大小
config.setMaxIdleTime(30000);     // 最大空闲时间(ms)
config.setMaxLifetime(600000);    // 最大生命周期(ms)
```

### 连接池监控

```java
// 获取连接池状态
manager.getDataSourceStatus("test_db")
    .onSuccess(status -> {
        System.out.println("活跃连接数: " + status.getActiveConnections());
        System.out.println("空闲连接数: " + status.getIdleConnections());
        System.out.println("等待队列大小: " + status.getWaitQueueSize());
    });
```

## 故障处理

### 数据源故障检测

```java
// 检测数据源健康状态
manager.checkDataSourceHealth("test_db")
    .onSuccess(healthy -> {
        if (healthy) {
            System.out.println("数据源健康");
        } else {
            System.out.println("数据源不健康");
        }
    })
    .onFailure(error -> {
        System.err.println("健康检查失败: " + error.getMessage());
    });
```

### 故障恢复

```java
// 自动重试机制
manager.executeWithDataSource("test_db", executor -> {
    return executor.executeQuery("SELECT 1");
}).recover(error -> {
    // 故障恢复逻辑
    System.err.println("查询失败，尝试恢复: " + error.getMessage());
    
    // 重新创建数据源
    return manager.recreateDataSource("test_db")
        .compose(v -> manager.executeWithDataSource("test_db", executor -> {
            return executor.executeQuery("SELECT 1");
        }));
}).onSuccess(result -> {
    System.out.println("恢复后查询成功: " + result);
});
```

## 性能优化

### 连接池优化

```java
// 根据应用负载调整连接池大小
DataSourceConfig config = new DataSourceConfig(
    "high_load_db", "mysql", "jdbc:mysql://localhost:3306/high_load", "user", "pass"
);

// 高负载配置
config.setMaxPoolSize(50);        // 增加最大连接数
config.setMinPoolSize(10);        // 增加最小连接数
config.setMaxWaitQueueSize(200);  // 增加等待队列
config.setMaxIdleTime(60000);     // 增加空闲时间
```

### 查询优化

```java
// 使用合适的数据源执行不同类型的查询
// 读操作使用从数据源
manager.executeWithDataSource("secondary", executor -> {
    return executor.executeQuery("SELECT * FROM large_table");
});

// 写操作使用主数据源
manager.executeWithDataSource("primary", executor -> {
    return executor.executeUpdate("INSERT INTO large_table VALUES (?, ?)", 1, "data");
});
```

## 最佳实践

### 1. 数据源命名

```java
// 使用有意义的名称
DataSourceConfig config = new DataSourceConfig(
    "user_database",     // 清晰的数据源名称
    "mysql",
    "jdbc:mysql://localhost:3306/users",
    "user",
    "password"
);
```

### 2. 连接池配置

```java
// 根据应用需求配置连接池
DataSourceConfig config = new DataSourceConfig(/* ... */);

// 开发环境：小连接池
if (isDevelopment()) {
    config.setMaxPoolSize(5);
    config.setMinPoolSize(1);
}

// 生产环境：大连接池
if (isProduction()) {
    config.setMaxPoolSize(50);
    config.setMinPoolSize(10);
}
```

### 3. 错误处理

```java
// 完善的错误处理
manager.executeWithDataSource("test_db", executor -> {
    return executor.executeQuery("SELECT * FROM table");
}).onSuccess(result -> {
    // 处理成功结果
    processResult(result);
}).onFailure(error -> {
    // 记录错误日志
    logger.error("数据源操作失败", error);
    
    // 尝试故障恢复
    attemptRecovery(error);
});
```

### 4. 资源清理

```java
// 应用关闭时清理资源
@PreDestroy
public void cleanup() {
    if (dataSourceManager != null) {
        dataSourceManager.close()
            .onSuccess(v -> logger.info("数据源管理器关闭成功"))
            .onFailure(error -> logger.error("数据源管理器关闭失败", error));
    }
}
```

## 示例项目

完整示例请参考：
- `core-database/src/test/java/cn/qaiu/db/datasource/MultiDataSourceTest.java`
- `core-database/src/test/java/cn/qaiu/db/performance/MultiDataSourcePerformanceTest.java`
- `core-database/src/test/java/cn/qaiu/db/integration/DatabaseIntegrationTest.java`