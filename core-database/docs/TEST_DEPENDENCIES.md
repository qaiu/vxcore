# DDL映射系统测试依赖配置

## 概述

本文档说明了core-database模块中JUnit和Vert.x JUnit测试依赖的配置情况。

## 已配置的测试依赖

### 1. JUnit 5

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

**包含的组件：**
- `junit-jupiter-api` - JUnit 5 API
- `junit-jupiter-engine` - JUnit 5 测试引擎
- `junit-jupiter-params` - 参数化测试支持
- `junit-platform-commons` - 平台通用组件
- `junit-platform-engine` - 平台测试引擎

### 2. Vert.x JUnit 5

```xml
<!-- Vert.x JUnit 5 -->
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-junit5</artifactId>
    <version>${vertx.version}</version>
    <scope>test</scope>
</dependency>
```

**功能特性：**
- Vert.x测试上下文支持
- 异步测试支持
- Verticle测试支持
- 集成测试支持

### 3. Mockito

```xml
<!-- Mockito for mocking -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.8.0</version>
    <scope>test</scope>
</dependency>
```

**功能特性：**
- 对象模拟
- 行为验证
- 参数匹配
- 静态方法模拟

### 4. AssertJ

```xml
<!-- AssertJ for fluent assertions -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.25.1</version>
    <scope>test</scope>
</dependency>
```

**功能特性：**
- 流式断言API
- 丰富的断言方法
- 错误消息优化
- 集合和对象断言

### 5. H2 Database (测试专用)

```xml
<!-- H2 Database for testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.220</version>
    <scope>test</scope>
</dependency>
```

**功能特性：**
- 内存数据库
- MySQL兼容模式
- 快速测试执行
- 无需外部数据库

## Maven插件配置

### 1. Maven Surefire Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
            <include>**/*TestSuite.java</include>
        </includes>
        <systemPropertyVariables>
            <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
        </systemPropertyVariables>
        <!-- JUnit 5 support -->
        <dependencies>
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter-engine</artifactId>
                <version>5.10.1</version>
            </dependency>
        </dependencies>
    </configuration>
</plugin>
```

### 2. Maven Failsafe Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*IT.java</include>
            <include>**/*IntegrationTest.java</include>
        </includes>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 测试文件结构

```
core-database/src/test/
├── java/cn/qaiu/db/ddl/
│   ├── DdlTableSimpleTest.java           # 简化的DdlTable测试
│   ├── DdlTableTest.java                 # 完整的DdlTable测试
│   ├── DdlColumnTest.java                # DdlColumn测试
│   ├── ColumnMetadataTest.java           # ColumnMetadata测试
│   ├── TableMetadataTest.java           # TableMetadata测试
│   ├── EnhancedCreateTableIntegrationTest.java # 集成测试
│   ├── DdlMappingTestSuite.java          # 测试套件
│   └── example/
│       └── ExampleUserTest.java          # 示例类测试
└── resources/
    ├── logback-test.xml                  # 测试日志配置
    └── test.properties                   # 测试属性配置
```

## 版本兼容性

| 组件 | 版本 | 兼容性 |
|------|------|--------|
| Java | 17+ | ✅ |
| JUnit 5 | 5.10.1 | ✅ |
| Vert.x | 4.5.21 | ✅ |
| Mockito | 5.8.0 | ✅ |
| AssertJ | 3.25.1 | ✅ |
| H2 | 2.2.220 | ✅ |
| Maven Surefire | 3.2.5 | ✅ |

## 运行测试

### 1. 基本测试命令

```bash
# 运行所有测试
mvn test -pl core-database

# 运行特定测试类
mvn test -pl core-database -Dtest=DdlTableSimpleTest

# 运行测试套件
mvn test -pl core-database -Dtest=DdlMappingTestSuite
```

### 2. 集成测试命令

```bash
# 运行集成测试
mvn verify -pl core-database

# 跳过单元测试，只运行集成测试
mvn verify -pl core-database -DskipTests
```

### 3. 测试报告

```bash
# 生成测试报告
mvn surefire-report:report -pl core-database

# 查看测试报告
open core-database/target/site/surefire-report.html
```

## 测试示例

### JUnit 5 基本测试

```java
@Test
@DisplayName("测试DdlTable注解基本属性")
public void testDdlTableAnnotationProperties() {
    @DdlTable(value = "test_table", version = 1)
    class TestClass {}
    
    assertTrue(TestClass.class.isAnnotationPresent(DdlTable.class));
    DdlTable annotation = TestClass.class.getAnnotation(DdlTable.class);
    assertEquals("test_table", annotation.value());
    assertEquals(1, annotation.version());
}
```

### Vert.x JUnit 5 集成测试

```java
@ExtendWith(VertxExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class EnhancedCreateTableIntegrationTest {
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 设置测试环境
        testContext.completeNow();
    }
    
    @Test
    @DisplayName("测试创建表并启用严格DDL映射")
    public void testCreateTableWithStrictMapping(VertxTestContext testContext) {
        EnhancedCreateTable.createTableWithStrictMapping(pool, JDBCType.H2DB)
            .onComplete(testContext.succeedingThenComplete());
    }
}
```

## 故障排除

### 常见问题

1. **依赖解析失败**
   ```bash
   # 解决方案：先安装core模块
   mvn install -pl core -DskipTests
   ```

2. **JUnit 5 测试不运行**
   ```bash
   # 检查Maven Surefire插件版本
   mvn help:describe -Dplugin=org.apache.maven.plugins:maven-surefire-plugin
   ```

3. **Vert.x测试失败**
   ```bash
   # 检查Vert.x版本兼容性
   mvn dependency:tree -Dincludes=io.vertx:vertx-junit5
   ```

### 调试技巧

1. **启用详细日志**
   ```xml
   <logger name="cn.qaiu.db.ddl" level="DEBUG"/>
   ```

2. **使用IDE调试**
   - IntelliJ IDEA: 右键测试方法 → Debug
   - Eclipse: 右键测试类 → Debug As → JUnit Test

3. **检查测试环境**
   ```bash
   # 运行验证脚本
   ./core-database/verify-test-deps.sh
   ```

## 总结

core-database模块已经正确配置了JUnit 5和Vert.x JUnit 5测试依赖，支持：

- ✅ 单元测试
- ✅ 集成测试
- ✅ 异步测试
- ✅ 参数化测试
- ✅ 测试套件
- ✅ 测试报告生成

所有测试依赖都使用最新稳定版本，确保最佳的性能和兼容性。
