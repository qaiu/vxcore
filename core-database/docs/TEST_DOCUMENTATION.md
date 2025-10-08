# DDL映射系统测试文档

## 概述

本文档描述了core-database模块中DDL映射系统的测试用例和测试策略。

## 测试结构

```
core-database/src/test/
├── java/cn/qaiu/db/ddl/
│   ├── DdlTableTest.java                    # DdlTable注解测试
│   ├── DdlColumnTest.java                   # DdlColumn注解测试
│   ├── ColumnMetadataTest.java              # ColumnMetadata测试
│   ├── TableMetadataTest.java               # TableMetadata测试
│   ├── EnhancedCreateTableIntegrationTest.java # 集成测试
│   ├── DdlMappingTestSuite.java             # 测试套件
│   └── example/
│       └── ExampleUserTest.java             # 示例类测试
└── resources/
    ├── logback-test.xml                     # 测试日志配置
    └── test.properties                      # 测试配置
```

## 测试分类

### 1. 单元测试

#### 注解测试
- **DdlTableTest**: 测试`@DdlTable`注解的基本属性、默认值、保留策略和目标类型
- **DdlColumnTest**: 测试`@DdlColumn`注解的基本属性、默认值、保留策略和目标类型

#### 元数据测试
- **ColumnMetadataTest**: 测试`ColumnMetadata`类的属性设置、构造函数、字段映射和SQL生成
- **TableMetadataTest**: 测试`TableMetadata`类的属性设置、构造函数、列管理和类映射

### 2. 集成测试

#### EnhancedCreateTableIntegrationTest
- 测试创建表并启用严格DDL映射
- 测试同步表结构
- 测试同步指定表
- 测试强制同步表结构
- 测试检查表是否需要同步
- 测试检查是否有表需要同步
- 测试生成表结构报告
- 测试完整的DDL映射流程

### 3. 示例测试

#### ExampleUserTest
- 测试示例类的注解配置
- 测试字段注解
- 测试构造函数
- 测试getter和setter方法
- 测试字段类型
- 测试JsonObject构造函数处理null值
- 测试JsonObject构造函数处理无效日期

## 测试配置

### 测试依赖

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>

<!-- Vert.x JUnit 5 -->
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-junit5</artifactId>
    <version>4.4.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>4.11.0</version>
    <scope>test</scope>
</dependency>

<!-- AssertJ -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### 测试数据库

使用H2内存数据库进行测试：
- URL: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL`
- 驱动: `org.h2.Driver`
- 用户名: `sa`
- 密码: 空

### 测试日志

测试使用logback-test.xml配置，设置适当的日志级别：
- DDL相关类: DEBUG级别
- Vert.x: INFO级别
- H2数据库: WARN级别

## 运行测试

### 1. 使用Maven运行

```bash
# 运行所有测试
mvn test -pl core-database

# 运行特定测试类
mvn test -pl core-database -Dtest=DdlTableTest

# 运行测试套件
mvn test -pl core-database -Dtest=DdlMappingTestSuite

# 生成测试报告
mvn surefire-report:report -pl core-database
```

### 2. 使用测试脚本

```bash
# 运行测试脚本
./core-database/run-tests.sh
```

### 3. 在IDE中运行

- 在IntelliJ IDEA或Eclipse中直接运行测试类
- 使用JUnit 5测试运行器

## 测试覆盖范围

### 功能覆盖

- ✅ 注解定义和属性
- ✅ 元数据类的基本功能
- ✅ 字段到列的映射
- ✅ 类到表的映射
- ✅ SQL语句生成
- ✅ 数据库连接和操作
- ✅ 错误处理和异常情况

### 场景覆盖

- ✅ 正常流程测试
- ✅ 边界条件测试
- ✅ 异常情况测试
- ✅ 兼容性测试
- ✅ 性能测试（基础）

## 测试数据

### 测试类示例

```java
@DdlTable(
    value = "example_user",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "示例用户表"
)
public class ExampleUser {
    @DdlColumn(
        type = "BIGINT",
        autoIncrement = true,
        nullable = false,
        comment = "用户ID"
    )
    private Long id;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = false,
        uniqueKey = "username",
        comment = "用户名"
    )
    private String username;
}
```

### 测试数据

- 正常数据：完整的用户信息
- 边界数据：空值、null值
- 异常数据：无效日期、无效数字

## 持续集成

### GitHub Actions配置

```yaml
name: DDL Mapping Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run tests
      run: mvn test -pl core-database
```

## 测试报告

测试完成后会生成以下报告：
- Surefire报告: `target/site/surefire-report.html`
- 覆盖率报告: `target/site/jacoco/index.html`

## 故障排除

### 常见问题

1. **测试失败**: 检查数据库连接和依赖
2. **编译错误**: 确保所有依赖正确安装
3. **内存不足**: 调整JVM参数
4. **端口冲突**: 检查H2数据库端口配置

### 调试技巧

1. 启用详细日志
2. 使用断点调试
3. 检查测试数据
4. 验证数据库状态

## 扩展测试

### 添加新测试

1. 创建测试类
2. 添加测试方法
3. 更新测试套件
4. 运行测试验证

### 测试最佳实践

1. 测试命名清晰
2. 测试独立性
3. 适当的断言
4. 错误处理测试
5. 性能考虑

## 总结

DDL映射系统的测试覆盖了从注解定义到数据库操作的完整流程，确保了系统的可靠性和稳定性。通过单元测试、集成测试和示例测试的组合，我们能够全面验证系统的功能正确性。
