# VXCore Generator Module

VXCore 代码生成器模块，基于数据库表结构自动生成 Controller/Service/DAO 三层架构代码。

## 🎯 核心功能

### ✅ 支持的功能

#### 🗄️ 数据源支持
- **数据库连接**: 支持 MySQL、PostgreSQL、H2
- **配置文件**: 支持 JSON/YAML 格式的表结构定义
- **表结构读取**: 自动读取主键、外键、索引、注释等元数据

#### 🏗️ 代码生成
- **实体类**: 支持 Vert.x SQL 注解、JPA 注解、Lombok 注解
- **DAO 层**: 支持三种风格
  - Vert.x SQL 风格 (原生 SQL + 实体映射)
  - jOOQ 风格 (类型安全 DSL)
  - MP Lambda 风格 (MyBatis-Plus 风格)
- **Service 层**: 业务逻辑接口和实现类
- **Controller 层**: RESTful API 控制器
- **DTO 层**: 请求/响应数据传输对象

#### 🔧 高级特性
- **分页查询**: 自动生成分页查询方法
- **条件查询**: 支持复杂条件查询
- **参数校验**: Bean Validation 注解
- **DTO 转换**: 自动生成转换工具类
- **异常处理**: 统一异常处理机制

## 🚀 快速开始

### 1. 从数据库生成代码

```bash
java -jar core-generator.jar \
  --db-url jdbc:mysql://localhost:3306/testdb \
  --db-user root \
  --db-password password \
  --tables user,order,product \
  --package com.example \
  --output ./src/main/java \
  --dao-style lambda
```

### 2. 从配置文件生成代码

```bash
java -jar core-generator.jar \
  --config generator-config.json \
  --output ./src/main/java
```

### 3. 配置文件示例

```json
{
  "database": {
    "url": "jdbc:mysql://localhost:3306/testdb",
    "username": "root",
    "password": "password"
  },
  "packages": {
    "basePackage": "com.example",
    "entityPackage": "com.example.entity",
    "daoPackage": "com.example.dao",
    "servicePackage": "com.example.service",
    "controllerPackage": "com.example.controller",
    "dtoPackage": "com.example.dto"
  },
  "tables": ["user", "order", "product"],
  "daoStyle": "lambda",
  "features": {
    "generateComments": true,
    "generateValidation": true,
    "generateDto": true
  }
}
```

## 📋 DAO 风格说明

### 1. Vert.x SQL 风格
```java
@RowMapped
public class User {
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
}

public class UserDao {
    public Future<Optional<User>> findById(Long id) {
        return sqlClient.preparedQuery("SELECT * FROM user WHERE id = ?")
            .execute(Tuple.of(id))
            .map(rows -> rows.size() > 0 ? Optional.of(mapRow(rows.get(0))) : Optional.empty());
    }
}
```

### 2. jOOQ 风格
```java
public class UserDao {
    public Future<Optional<User>> findById(Long id) {
        return dsl.selectFrom(USER)
            .where(USER.ID.eq(id))
            .fetchOptionalAsync()
            .map(optional -> optional.map(this::mapRecord));
    }
}
```

### 3. MP Lambda 风格
```java
public class UserDao extends LambdaDao<User> {
    public UserDao() {
        super(User.class);
    }
    
    public Future<Optional<User>> findById(Long id) {
        return lambdaQuery()
            .eq(User::getId, id)
            .one();
    }
}
```

## 🔧 配置选项

### 命令行参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `--db-url` | 数据库连接URL | `jdbc:mysql://localhost:3306/db` |
| `--db-user` | 数据库用户名 | `root` |
| `--db-password` | 数据库密码 | `password` |
| `--tables` | 要生成的表名(逗号分隔) | `user,order,product` |
| `--package` | 基础包名 | `com.example` |
| `--output` | 输出目录 | `./src/main/java` |
| `--dao-style` | DAO风格 | `vertx`, `jooq`, `lambda` |
| `--config` | 配置文件路径 | `generator-config.json` |

### 生成选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `generateComments` | 生成注释 | `true` |
| `generateValidation` | 生成校验注解 | `true` |
| `generateDto` | 生成DTO类 | `true` |
| `generateService` | 生成Service层 | `true` |
| `overwriteExisting` | 覆盖已存在文件 | `false` |

## 📚 详细文档

- [生成器使用指南](docs/GENERATOR_GUIDE.md)
- [自定义模板指南](docs/TEMPLATE_GUIDE.md)
- [配置文件格式说明](docs/CONFIG_FORMAT.md)

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=DatabaseMetadataReaderTest
mvn test -Dtest=CodeGeneratorFacadeTest
```

## 🤝 贡献

欢迎贡献代码！请遵循项目代码规范：

- 遵循阿里巴巴 Java 开发规范
- 所有 public 方法必须有 JavaDoc
- 新功能必须包含单元测试
- 测试覆盖率 > 80%

## 📄 许可证

MIT License - 查看 [LICENSE](../LICENSE) 文件了解详情。
