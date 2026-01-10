# 性能测试说明

## 概述

本目录包含VXCore框架的实际性能测试，主要测试框架核心功能在高并发场景下的性能表现。

## 测试类说明

### WebConcurrencyPerformanceTest
- **用途**: 测试HTTP服务器的并发处理能力
- **测试内容**: 
  - 单请求响应时间基准
  - 100/500/1000并发请求处理
  - 持续压力下的吞吐量测试
  - JSON序列化响应性能
- **关键指标**: 吞吐量(req/s)、响应时间(P50/P95/P99)、成功率

### HighConcurrencyMemoryTest
- **用途**: 测试高并发场景下的内存占用和增长率
- **测试内容**:
  - 基准内存使用
  - 并发请求内存增长率
  - 持续压力下内存稳定性
  - 大响应体内存影响
  - 内存泄漏检测
- **关键指标**: 内存增长率、峰值内存、GC后内存恢复

### FrameworkComponentPerformanceTest
- **用途**: 测试框架核心组件的并发性能
- **测试内容**:
  - Router创建并发性能
  - JSON序列化/反序列化并发性能
  - EventBus消息处理并发性能
  - 高负载压力测试
- **关键指标**: 操作吞吐量、平均操作时间

## 运行方式

### 本地运行
```bash
# 运行所有性能测试
mvn test -Dtest="**/performance/**/*Test" -DCI=false

# 运行特定性能测试
mvn test -Dtest="WebConcurrencyPerformanceTest" -DCI=false
mvn test -Dtest="HighConcurrencyMemoryTest" -DCI=false
mvn test -Dtest="FrameworkComponentPerformanceTest" -DCI=false
```

### CI环境
性能测试在CI环境中默认被禁用（通过 `@DisabledIfEnvironmentVariable`）

## 性能基准

| 测试项 | 目标值 | 说明 |
|-------|--------|------|
| 单请求响应时间 | < 500ms | 基准响应时间 |
| 并发成功率 | > 90% | 高并发下的请求成功率 |
| 吞吐量 | > 1000 req/s | 持续压力下的吞吐量 |
| 每请求内存增长 | < 1KB | GC后的内存增长 |
| 内存波动 | < 50% | 相对于初始内存 |

## 注意事项

1. **资源要求**: 性能测试需要足够的系统资源，建议在配置较好的机器上运行
2. **环境隔离**: 运行性能测试时，建议关闭其他占用资源的应用程序
3. **结果解读**: 性能测试结果可能因环境而异，重点关注相对性能而非绝对数值
4. **定期运行**: 建议在代码变更后定期运行性能测试，确保性能没有显著下降

## 启用性能测试

如需在CI环境中启用性能测试，可以：

1. **移除@Disabled注解**:
```java
// 注释掉这行
// @Disabled("性能测试在CI环境中不稳定，本地可手动运行")
```

2. **使用Maven Profile**:
```xml
<profiles>
    <profile>
        <id>performance-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/performance/**/*Test.java</include>
                        </includes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

然后使用: `mvn test -P performance-tests`

## 性能基准

当前性能测试的基准值（仅供参考，实际值可能因环境而异）：

- **内存分配效率**: 10000次操作内存增长 < 10MB
- **并发性能**: 10线程1000次操作总耗时 < 5秒
- **垃圾回收影响**: GC时间占比 < 5%

## 联系方式

如有性能测试相关问题，请联系开发团队。
