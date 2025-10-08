# DDL映射系统测试说明

## 测试环境设置

由于项目依赖关系，需要按以下步骤设置测试环境：

### 1. 安装依赖

```bash
# 首先安装core模块到本地仓库
mvn install -pl core -DskipTests

# 然后安装core-database模块
mvn install -pl core-database -DskipTests
```

### 2. 运行测试

```bash
# 运行所有测试
mvn test -pl core-database

# 运行特定测试
mvn test -pl core-database -Dtest=DdlTableSimpleTest

# 运行测试套件
mvn test -pl core-database -Dtest=DdlMappingTestSuite
```

## 测试文件说明

### 已创建的测试文件

1. **DdlTableSimpleTest.java** - 简化的DdlTable注解测试（无外部依赖）
2. **DdlTableTest.java** - 完整的DdlTable注解测试
3. **DdlColumnTest.java** - DdlColumn注解测试
4. **ColumnMetadataTest.java** - ColumnMetadata类测试
5. **TableMetadataTest.java** - TableMetadata类测试
6. **EnhancedCreateTableIntegrationTest.java** - 集成测试
7. **ExampleUserTest.java** - 示例类测试
8. **DdlMappingTestSuite.java** - 测试套件

### 测试配置文件

1. **logback-test.xml** - 测试日志配置
2. **test.properties** - 测试属性配置

## 测试覆盖范围

### 单元测试
- ✅ 注解定义和属性验证
- ✅ 元数据类的基本功能
- ✅ 字段到列的映射
- ✅ 类到表的映射
- ✅ SQL语句生成

### 集成测试
- ✅ 数据库连接和操作
- ✅ 表结构同步
- ✅ 错误处理

### 示例测试
- ✅ 示例类的完整功能
- ✅ 构造函数和getter/setter
- ✅ 数据转换和验证

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

## 故障排除

### 常见问题

1. **依赖解析失败**
   - 确保core模块已安装到本地仓库
   - 检查Maven配置和仓库设置

2. **测试编译失败**
   - 检查Java版本（需要Java 17+）
   - 确保所有依赖正确配置

3. **数据库连接失败**
   - 检查H2数据库配置
   - 确保测试资源文件正确

### 调试技巧

1. **启用详细日志**
   ```xml
   <logger name="cn.qaiu.db.ddl" level="DEBUG"/>
   ```

2. **使用IDE调试**
   - 在IntelliJ IDEA中设置断点
   - 使用JUnit 5测试运行器

3. **检查测试数据**
   - 验证测试类的注解配置
   - 检查数据库表结构

## 测试最佳实践

1. **测试独立性** - 每个测试应该独立运行
2. **清晰的命名** - 测试方法名应该描述测试内容
3. **适当的断言** - 使用合适的断言方法
4. **错误处理** - 测试异常情况和边界条件
5. **性能考虑** - 避免长时间运行的测试

## 扩展测试

### 添加新测试

1. 创建测试类
2. 添加测试方法
3. 更新测试套件
4. 运行测试验证

### 测试维护

1. 定期更新测试数据
2. 检查测试覆盖率
3. 优化测试性能
4. 更新测试文档

## 总结

DDL映射系统的测试提供了全面的功能验证，确保系统的可靠性和稳定性。通过单元测试、集成测试和示例测试的组合，我们能够全面验证DDL映射功能的正确性。
