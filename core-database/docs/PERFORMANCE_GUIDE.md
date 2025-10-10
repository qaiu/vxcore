# 性能优化指南

## 概述

VXCore数据库模块提供了多种性能优化策略，包括连接池管理、查询优化、缓存机制等，帮助提升应用性能。

## 性能测试

### 性能测试套件

框架提供了完整的性能测试套件：

- **LambdaQueryPerformanceTest**: Lambda查询性能测试
- **MultiDataSourcePerformanceTest**: 多数据源性能测试
- **DatabasePerformanceTest**: 数据库操作性能测试

### 运行性能测试

```bash
# 运行所有性能测试
mvn test -Dtest="*PerformanceTest*"

# 运行特定性能测试
mvn test -Dtest="LambdaQueryPerformanceTest"

# 生成性能报告
mvn test -Dtest="*PerformanceTest*" -Dmaven.test.failure.ignore=true
```

### 性能基准

| 操作类型 | 性能目标 | 测试方法 |
|---------|---------|---------|
| 字段名提取 | < 1ms/次 | LambdaUtils.getFieldName() |
| 查询构建 | < 5ms/次 | LambdaQueryWrapper.buildSelect() |
| 简单查询 | < 100ms/次 | 单表查询 |
| 复杂查询 | < 1000ms/次 | 多表关联查询 |
| 数据源切换 | < 50ms/次 | 切换数据源 |
| 并发访问 | < 200ms/次 | 多线程并发 |

## 连接池优化

### 连接池配置

```java
DataSourceConfig config = new DataSourceConfig(
    "optimized_db", "mysql", "jdbc:mysql://localhost:3306/test", "user", "pass"
);

// 性能优化配置
config.setMaxPoolSize(20);        // 最大连接数
config.setMinPoolSize(5);         // 最小连接数
config.setMaxWaitQueueSize(100);  // 最大等待队列
config.setMaxIdleTime(30000);     // 最大空闲时间(30秒)
config.setMaxLifetime(600000);    // 最大生命周期(10分钟)
config.setConnectionTimeout(5000); // 连接超时(5秒)
```

### 连接池监控

```java
// 监控连接池状态
DataSourceManager manager = DataSourceManager.getInstance(vertx);

// 定期检查连接池状态
vertx.setPeriodic(30000, timerId -> {
    manager.getDataSourceStatus("optimized_db")
        .onSuccess(status -> {
            logger.info("连接池状态: 活跃={}, 空闲={}, 等待={}", 
                       status.getActiveConnections(),
                       status.getIdleConnections(),
                       status.getWaitQueueSize());
            
            // 连接池健康检查
            if (status.getWaitQueueSize() > 50) {
                logger.warn("连接池等待队列过大: {}", status.getWaitQueueSize());
            }
        });
});
```

### 连接池调优建议

1. **最大连接数**: 根据数据库服务器性能和并发需求设置
2. **最小连接数**: 保持一定数量的预热连接
3. **空闲时间**: 避免连接长时间空闲占用资源
4. **生命周期**: 定期回收连接避免内存泄漏

## 查询优化

### Lambda查询优化

```java
// 好的做法：使用索引字段
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getCode, "IPHONE15PRO")  // 假设code字段有索引
        .limit(10)
);

// 避免的做法：全表扫描
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .like(Product::getDescription, "%test%")  // 模糊查询可能全表扫描
);
```

### 查询条件优化

```java
// 优化查询条件顺序（选择性高的条件在前）
LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
    .eq(Product::getCode, "IPHONE15PRO")        // 高选择性条件
    .eq(Product::getActive, true)              // 中等选择性条件
    .ge(Product::getPrice, new BigDecimal("100.00")); // 低选择性条件

// 使用合适的比较操作符
.eq(Product::getStatus, "ACTIVE")              // 等值查询（最快）
.in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L)) // IN查询
.between(Product::getPrice, minPrice, maxPrice) // BETWEEN查询
.like(Product::getName, "%keyword%")          // LIKE查询（较慢）
```

### 分页查询优化

```java
// 使用limit和offset进行分页
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getActive, true)
        .orderByDesc(Product::getCreatedAt)
        .limit(pageSize)
        .offset(pageNumber * pageSize)
);

// 避免深度分页（使用游标分页）
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getActive, true)
        .gt(Product::getId, lastId)  // 使用ID游标
        .orderByAsc(Product::getId)
        .limit(pageSize)
);
```

## 缓存优化

### 字段名缓存

LambdaUtils自动缓存字段名解析结果：

```java
// 第一次调用会进行反射解析
String fieldName1 = LambdaUtils.getFieldName(Product::getName);

// 后续调用直接返回缓存结果
String fieldName2 = LambdaUtils.getFieldName(Product::getName);
```

### 查询结果缓存

```java
// 使用本地缓存缓存查询结果
private final Map<String, List<Product>> queryCache = new ConcurrentHashMap<>();

public List<Product> getCachedProducts(String category) {
    return queryCache.computeIfAbsent(category, key -> {
        return productDao.lambdaList(
            productDao.lambdaQuery()
                .eq(Product::getCategoryId, Long.parseLong(key))
                .eq(Product::getActive, true)
        );
    });
}

// 定期清理缓存
@Scheduled(fixedRate = 300000) // 5分钟
public void clearCache() {
    queryCache.clear();
}
```

## 并发优化

### 异步操作

```java
// 使用异步操作避免阻塞
CompletableFuture<List<Product>> future = new CompletableFuture<>();

productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getActive, true)
).onSuccess(products -> {
    future.complete(products);
}).onFailure(error -> {
    future.completeExceptionally(error);
});

// 非阻塞处理
future.thenAccept(products -> {
    // 处理查询结果
    processProducts(products);
});
```

### 并发控制

```java
// 使用信号量控制并发数
private final Semaphore semaphore = new Semaphore(10); // 最大10个并发

public void executeWithConcurrencyControl() {
    try {
        semaphore.acquire();
        
        productDao.lambdaList(
            productDao.lambdaQuery()
                .eq(Product::getActive, true)
        ).onComplete(result -> {
            semaphore.release();
            // 处理结果
        });
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

## 内存优化

### 对象池

```java
// 使用对象池重用对象
private final ObjectPool<LambdaQueryWrapper<Product>> queryWrapperPool = 
    new GenericObjectPool<>(new LambdaQueryWrapperFactory());

public LambdaQueryWrapper<Product> borrowQueryWrapper() {
    try {
        return queryWrapperPool.borrowObject();
    } catch (Exception e) {
        throw new RuntimeException("获取查询包装器失败", e);
    }
}

public void returnQueryWrapper(LambdaQueryWrapper<Product> wrapper) {
    try {
        wrapper.clear(); // 清理状态
        queryWrapperPool.returnObject(wrapper);
    } catch (Exception e) {
        // 处理异常
    }
}
```

### 内存监控

```java
// 监控内存使用
Runtime runtime = Runtime.getRuntime();

// 强制垃圾回收
System.gc();

long usedMemory = runtime.totalMemory() - runtime.freeMemory();
long maxMemory = runtime.maxMemory();

logger.info("内存使用: {} MB / {} MB", 
           usedMemory / 1024 / 1024, 
           maxMemory / 1024 / 1024);

// 内存使用率检查
double memoryUsage = (double) usedMemory / maxMemory;
if (memoryUsage > 0.8) {
    logger.warn("内存使用率过高: {}%", memoryUsage * 100);
}
```

## 数据库优化

### 索引优化

```sql
-- 为常用查询字段创建索引
CREATE INDEX idx_product_code ON products(product_code);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_active ON products(is_active);
CREATE INDEX idx_product_price ON products(price);

-- 复合索引
CREATE INDEX idx_product_category_active ON products(category_id, is_active);
CREATE INDEX idx_product_price_active ON products(price, is_active);
```

### 查询计划分析

```java
// 分析查询计划
String sql = "SELECT * FROM products WHERE category_id = ? AND is_active = ?";
logger.info("执行查询: {}", sql);

// 使用EXPLAIN分析查询计划
dataSourceManager.executeWithDataSource("test_db", executor -> {
    return executor.executeQuery("EXPLAIN " + sql, categoryId, true);
}).onSuccess(result -> {
    logger.info("查询计划: {}", result);
});
```

## 监控和诊断

### 性能监控

```java
// 查询执行时间监控
public class PerformanceMonitor {
    
    public <T> Future<T> monitorQuery(String queryName, Supplier<Future<T>> querySupplier) {
        long startTime = System.currentTimeMillis();
        
        return querySupplier.get()
            .onSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("查询 {} 执行时间: {} ms", queryName, duration);
                
                // 性能告警
                if (duration > 1000) {
                    logger.warn("查询 {} 执行时间过长: {} ms", queryName, duration);
                }
            })
            .onFailure(error -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.error("查询 {} 执行失败，耗时: {} ms", queryName, duration, error);
            });
    }
}

// 使用监控
PerformanceMonitor monitor = new PerformanceMonitor();

monitor.monitorQuery("product_list", () -> {
    return productDao.lambdaList(
        productDao.lambdaQuery()
            .eq(Product::getActive, true)
    );
});
```

### 慢查询日志

```java
// 慢查询检测
public class SlowQueryDetector {
    
    private static final long SLOW_QUERY_THRESHOLD = 1000; // 1秒
    
    public <T> Future<T> detectSlowQuery(String sql, Future<T> queryFuture) {
        long startTime = System.currentTimeMillis();
        
        return queryFuture.onComplete(result -> {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > SLOW_QUERY_THRESHOLD) {
                logger.warn("慢查询检测: SQL={}, 耗时={}ms", sql, duration);
                
                // 记录慢查询详情
                recordSlowQuery(sql, duration, result);
            }
        });
    }
    
    private void recordSlowQuery(String sql, long duration, AsyncResult<?> result) {
        // 记录到慢查询日志
        logger.warn("SLOW_QUERY: {}ms - {}", duration, sql);
    }
}
```

## 最佳实践

### 1. 查询优化

- 使用索引字段作为查询条件
- 避免SELECT *，只查询需要的字段
- 合理使用LIMIT限制结果集大小
- 避免深度分页，使用游标分页

### 2. 连接池管理

- 根据应用负载调整连接池大小
- 监控连接池状态，及时发现问题
- 设置合理的连接超时时间
- 定期回收长时间空闲的连接

### 3. 缓存策略

- 缓存频繁访问的数据
- 设置合理的缓存过期时间
- 避免缓存雪崩和缓存穿透
- 监控缓存命中率

### 4. 并发控制

- 使用异步操作避免阻塞
- 合理控制并发数
- 使用连接池管理数据库连接
- 避免长时间持有连接

### 5. 监控和诊断

- 定期监控性能指标
- 记录慢查询日志
- 分析性能瓶颈
- 及时优化问题查询

## 性能测试示例

```java
@Test
@DisplayName("性能基准测试")
void testPerformanceBenchmark() {
    // 测试字段名提取性能
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
        String fieldName = LambdaUtils.getFieldName(Product::getName);
        assertEquals("product_name", fieldName);
    }
    long duration = System.currentTimeMillis() - startTime;
    double avgTime = (double) duration / 10000;
    
    logger.info("字段名提取性能: {} ms/次", String.format("%.4f", avgTime));
    assertTrue(avgTime < 1.0, "字段名提取性能不达标");
    
    // 测试查询构建性能
    startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
        LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
            .eq(Product::getActive, true)
            .ge(Product::getPrice, new BigDecimal("100.00"))
            .orderByDesc(Product::getPrice)
            .limit(10);
        
        wrapper.buildSelect();
    }
    duration = System.currentTimeMillis() - startTime;
    avgTime = (double) duration / 1000;
    
    logger.info("查询构建性能: {} ms/次", String.format("%.4f", avgTime));
    assertTrue(avgTime < 5.0, "查询构建性能不达标");
}
```

## 总结

性能优化是一个持续的过程，需要：

1. **定期监控**: 使用性能监控工具跟踪关键指标
2. **分析瓶颈**: 识别性能瓶颈并制定优化策略
3. **持续优化**: 根据监控结果持续优化代码和配置
4. **测试验证**: 使用性能测试验证优化效果

通过合理的配置和优化，VXCore数据库模块能够提供高性能的数据访问能力。
