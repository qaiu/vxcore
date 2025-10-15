# 性能测试说明

## 概述

本目录包含VXCore框架的性能测试，主要用于验证核心工具类在高并发和大数据量场景下的性能表现。

## 测试类说明

### MemoryPerformanceTest
- **用途**: 测试核心工具类的内存使用效率和垃圾回收影响
- **测试内容**: 
  - 内存分配效率
  - 垃圾回收影响
  - 内存泄漏检测
- **CI状态**: 默认禁用（@Disabled）

### ConcurrencyPerformanceTest
- **用途**: 测试核心工具类在高并发场景下的性能表现
- **测试内容**:
  - StringCase并发性能
  - 线程安全性验证
  - 并发吞吐量测试
- **CI状态**: 默认禁用（@Disabled）

## 运行方式

### 本地运行
```bash
# 运行所有性能测试
mvn test -Dtest="**/performance/**/*Test"

# 运行特定性能测试
mvn test -Dtest="MemoryPerformanceTest"
mvn test -Dtest="ConcurrencyPerformanceTest"
```

### CI环境
性能测试在CI环境中默认被禁用，原因：
1. **环境不稳定**: CI环境的资源限制和网络波动可能影响测试结果
2. **时间限制**: 性能测试通常需要较长时间，可能超出CI超时限制
3. **结果不可靠**: 在共享CI环境中，其他任务可能影响性能测试的准确性

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
