# VXCore Example Module

VXCore示例模块，演示core和core-database模块的功能，包括JOOQ代码生成、VertxCodeGen集成和灵活的SQL执行器。

## 功能特性

- 🚀 **JOOQ代码生成**: 充分利用JOOQ强大的代码生成能力
- 🔧 **VertxCodeGen集成**: 结合Vert.x代码生成器自动生成DAO
- 🎯 **灵活SQL执行器**: 支持多种数据库驱动和异步操作
- 🔄 **事务支持**: 完整的事务管理功能
- 📊 **多数据库支持**: PostgreSQL、MySQL、H2数据库
- 🧪 **连接测试**: 内置数据库连接验证工具

## 项目结构

```
core-example/
├── src/main/java/cn/qaiu/example/
│   ├── executor/                 # SQL执行器接口和实现
│   │   ├── SqlExecutor.java      # 执行器接口
│   │   ├── VertxJdbcExecutor.java # JDBC执行器实现
│   │   └── VertxPgExecutor.java  # PostgreSQL执行器实现
│   ├── ExampleRunner.java        # 示例运行器
│   ├── PostgreSQLConnectionTest.java # PostgreSQL连接测试
│   └── [其他示例文件...]
├── src/main/resources/
│   ├── jooq-codegen.xml          # JOOQ代码生成配置
│   ├── vertx-codegen.json        # VertxCodeGen配置
│   ├── application.properties    # 应用配置
│   └── logback.xml              # 日志配置
└── pom.xml                      # Maven配置
```

## 快速开始

### 1. 编译项目

```bash
mvn clean compile
```

### 2. 生成JOOQ代码

```bash
mvn jooq-codegen:generate
```

### 3. 运行PostgreSQL连接测试

```bash
mvn exec:java -Dexec.mainClass="cn.qaiu.example.PostgreSQLConnectionTest"
```

### 4. 运行完整示例

```bash
# PostgreSQL示例
mvn exec:java -Dexec.mainClass="cn.qaiu.example.ExampleRunner" -Dexec.args="postgresql"

# MySQL示例
mvn exec:java -Dexec.mainClass="cn.qaiu.example.ExampleRunner" -Dexec.args="mysql"

# H2示例
mvn exec:java -Dexec.mainClass="cn.qaiu.example.ExampleRunner" -Dexec.args="h2"
```

## 数据库配置

### PostgreSQL (Neon Database)

当前配置使用Neon PostgreSQL数据库：

```properties
postgresql.host=ep-sweet-poetry-adzdzocn-pooler.c-2.us-east-1.aws.neon.tech
postgresql.port=5432
postgresql.database=neondb
postgresql.user=neondb_owner
postgresql.password=npg_SP4cxkzXs9fA
postgresql.sslmode=require
postgresql.channel_binding=require
```

### MySQL

```properties
mysql.host=localhost
mysql.port=3306
mysql.database=vxcore_example
mysql.user=root
mysql.password=password
```

### H2 (测试用)

```properties
h2.url=jdbc:h2:mem:vxcore_example;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
h2.user=sa
h2.password=
```

## SQL执行器

### 接口设计

`SqlExecutor`接口提供了灵活的数据库操作抽象：

```java
public interface SqlExecutor {
    Future<RowSet<Row>> query(Query query);
    Future<SqlResult<Void>> update(Query query);
    Future<List<SqlResult<Void>>> batchUpdate(List<Query> queries);
    <T> Future<T> transaction(Function<SqlExecutor, Future<T>> operations);
    Future<RowSet<Row>> query(String sql, Map<String, Object> params);
    Future<SqlResult<Void>> update(String sql, Map<String, Object> params);
    Future<Void> close();
    boolean isClosed();
}
```

### 实现类

- **VertxJdbcExecutor**: 基于Vert.x JDBC客户端的通用实现
- **VertxPgExecutor**: 基于Vert.x PostgreSQL客户端的优化实现

## JOOQ代码生成

### 配置说明

JOOQ代码生成配置位于 `src/main/resources/jooq-codegen.xml`：

- 支持PostgreSQL、MySQL、H2数据库
- 自动生成POJOs、DAOs、Records
- 支持Java 8+特性和Vert.x兼容性
- 自定义类型映射和命名策略

### 生成命令

```bash
mvn jooq-codegen:generate
```

生成的代码将位于 `src/main/java/cn/qaiu/example/generated/` 目录。

## VertxCodeGen集成

### 配置说明

VertxCodeGen配置位于 `src/main/resources/vertx-codegen.json`：

```json
{
  "codegen": {
    "generators": [
      {
        "name": "data_object",
        "includes": [
          "cn.qaiu.example.*"
        ],
        "excludes": [
          "cn.qaiu.example.generated.*"
        ]
      }
    ]
  }
}
```

### 自动生成DAO

结合JOOQ和VertxCodeGen，可以自动生成：

- 实体类 (Data Objects)
- DAO接口和实现
- 条件查询方法
- 异步操作方法

## 开发指南

### 添加新的数据库支持

1. 在 `SqlExecutor` 接口中添加新的方法（如果需要）
2. 创建新的执行器实现类
3. 在 `ExampleRunner` 中添加对应的配置和测试方法
4. 更新JOOQ代码生成配置

### 扩展DSL功能

1. 定义实体类并添加JOOQ和VertxCodeGen注解
2. 配置JOOQ代码生成
3. 运行代码生成命令
4. 在示例中使用生成的DAO

## 故障排除

### 连接问题

1. 检查数据库连接配置
2. 验证网络连接和防火墙设置
3. 确认数据库用户权限
4. 查看详细错误日志

### 代码生成问题

1. 检查JOOQ配置文件的语法
2. 确认数据库连接正常
3. 验证目标目录权限
4. 查看Maven构建日志

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 许可证

MIT License
