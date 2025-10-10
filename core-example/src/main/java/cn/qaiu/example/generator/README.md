# 代码生成器使用指南

## 概述

`core-generator` 模块是一个强大的代码生成器，可以根据数据库表结构或配置文件自动生成三层架构的 Java 代码，包括：

- **Entity** - 实体类
- **DAO** - 数据访问层
- **Service** - 业务逻辑层
- **Controller** - 控制器层
- **DTO** - 数据传输对象

## 支持的 DAO 风格

### 1. Lambda 风格 (默认)
基于 MyBatis-Plus 风格的 Lambda 查询，继承 `LambdaDao<Entity>`：

```java
public class UserDao extends LambdaDao<User> {
    // 自动继承基础 CRUD 方法
    // 支持链式查询：lambdaQuery().eq(User::getId, id).one()
}
```

### 2. Vert.x SQL 风格
使用 Vert.x 原生 SQL 执行器，支持 `@RowMapped` 和 `@Column` 注解：

```java
@RowMapped
public class User {
    @Column(name = "id")
    private Long id;
    
    @Column(name = "username")
    private String username;
}
```

### 3. jOOQ 风格
使用 jOOQ DSL API，类型安全的 SQL 构建：

```java
public class UserDao {
    public User findById(Long id) {
        return dsl.selectFrom(USER)
                  .where(USER.ID.eq(id))
                  .fetchOneInto(User.class);
    }
}
```

## 快速开始

### 1. 从数据库生成代码

```java
// 配置数据库连接
DatabaseConfig databaseConfig = new DatabaseConfig()
    .setUrl("jdbc:mysql://localhost:3306/mydb")
    .setUsername("root")
    .setPassword("password")
    .setDriverClassName("com.mysql.cj.jdbc.Driver");

// 配置包名
PackageConfig packageConfig = new PackageConfig()
    .setEntityPackage("com.example.entity")
    .setDaoPackage("com.example.dao")
    .setServicePackage("com.example.service")
    .setControllerPackage("com.example.controller")
    .setDtoPackage("com.example.dto");

// 配置输出路径
OutputConfig outputConfig = new OutputConfig()
    .setOutputPath("src/main/java")
    .setOverwriteExisting(true);

// 配置功能特性
FeatureConfig featureConfig = new FeatureConfig()
    .setGenerateEntity(true)
    .setGenerateDao(true)
    .setGenerateService(true)
    .setGenerateController(true)
    .setGenerateDto(true)
    .setUseLombok(true)
    .setGenerateValidation(true)
    .setDaoStyle(DaoStyle.LAMBDA);

// 创建生成上下文
GeneratorContext context = GeneratorContext.builder()
    .databaseConfig(databaseConfig)
    .packageConfig(packageConfig)
    .outputConfig(outputConfig)
    .featureConfig(featureConfig)
    .build();

// 创建生成器并生成代码
CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
generator.generateAll(Arrays.asList("users", "products"))
    .onSuccess(files -> {
        System.out.println("代码生成成功！生成文件数: " + files.size());
    })
    .onFailure(error -> {
        System.err.println("代码生成失败: " + error.getMessage());
    });
```

### 2. 从配置文件生成代码

首先创建配置文件 `generator-config.json`：

```json
{
  "tables": [
    {
      "tableName": "user",
      "description": "用户表",
      "columns": [
        {
          "columnName": "id",
          "columnType": "BIGINT",
          "columnSize": 20,
          "nullable": false,
          "primaryKey": true,
          "autoIncrement": true,
          "comment": "用户ID"
        },
        {
          "columnName": "username",
          "columnType": "VARCHAR",
          "columnSize": 50,
          "nullable": false,
          "unique": true,
          "comment": "用户名"
        }
      ]
    }
  ]
}
```

然后使用配置文件生成代码：

```java
GeneratorContext context = GeneratorContext.builder()
    .packageConfig(packageConfig)
    .outputConfig(outputConfig)
    .featureConfig(featureConfig)
    .build();

// 设置配置文件路径
context.setCustomProperty("configPath", "generator-config.json");

CodeGeneratorFacade generator = new CodeGeneratorFacade(vertx, context);
generator.generateAll(Arrays.asList("user"))
    .onSuccess(files -> {
        System.out.println("代码生成成功！");
    });
```

## 配置选项

### DatabaseConfig - 数据库配置
- `url` - 数据库连接 URL
- `username` - 用户名
- `password` - 密码
- `driverClassName` - 驱动类名

### PackageConfig - 包名配置
- `entityPackage` - 实体类包名
- `daoPackage` - DAO 包名
- `servicePackage` - Service 包名
- `controllerPackage` - Controller 包名
- `dtoPackage` - DTO 包名

### OutputConfig - 输出配置
- `outputPath` - 输出路径
- `overwriteExisting` - 是否覆盖已存在的文件

### FeatureConfig - 功能配置
- `generateEntity` - 是否生成实体类
- `generateDao` - 是否生成 DAO
- `generateService` - 是否生成 Service
- `generateController` - 是否生成 Controller
- `generateDto` - 是否生成 DTO
- `useLombok` - 是否使用 Lombok 注解
- `useJpaAnnotations` - 是否使用 JPA 注解
- `useVertxAnnotations` - 是否使用 Vert.x 注解
- `generateValidation` - 是否生成验证注解
- `daoStyle` - DAO 风格 (LAMBDA, VERTX_SQL, JOOQ)

## 运行示例

### 使用 Maven 运行

```bash
# 运行代码生成器示例
mvn exec:java@run-code-generator-demo -pl core-example

# 或者直接运行主类
mvn exec:java -Dexec.mainClass="cn.qaiu.example.generator.CodeGeneratorExampleRunner" -pl core-example
```

### 使用 IDE 运行

直接运行 `CodeGeneratorExampleRunner.main()` 方法。

## 生成的文件结构

```
target/generated-sources/
├── codegen/                    # 从数据库生成的代码
│   ├── entity/
│   │   ├── User.java
│   │   └── Product.java
│   ├── dao/
│   │   ├── UserDao.java
│   │   └── ProductDao.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── UserServiceImpl.java
│   ├── controller/
│   │   ├── UserController.java
│   │   └── ProductController.java
│   └── dto/
│       ├── UserCreateRequest.java
│       ├── UserUpdateRequest.java
│       ├── UserResponse.java
│       └── UserDtoConverter.java
├── config/                     # 从配置文件生成的代码
└── dao-styles/                 # 不同 DAO 风格的代码
```

## 自定义模板

如果需要自定义生成的代码模板，可以：

1. 复制 `core-generator/src/main/resources/templates/` 下的模板文件
2. 修改模板内容
3. 在 `TemplateConfig` 中指定自定义模板路径

## 注意事项

1. **数据库连接**: 确保数据库连接配置正确，并且有足够的权限读取表结构
2. **包名冲突**: 避免生成的代码包名与现有代码冲突
3. **文件覆盖**: 使用 `overwriteExisting(true)` 时要谨慎，会覆盖已存在的文件
4. **依赖管理**: 确保项目中包含必要的依赖（如 Lombok、Validation API 等）

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库 URL、用户名、密码
   - 确认数据库驱动已添加到依赖中

2. **模板渲染失败**
   - 检查模板语法是否正确
   - 确认模板中使用的变量在上下文中存在

3. **代码编译失败**
   - 检查生成的代码是否有语法错误
   - 确认必要的依赖已添加

4. **权限问题**
   - 确认对输出目录有写权限
   - 检查文件是否被其他程序占用

### 调试技巧

1. 启用详细日志输出
2. 分步生成代码（先生成实体类，再生成其他层）
3. 检查生成的中间文件
4. 使用 IDE 的代码分析功能检查生成代码

## 扩展功能

### 添加新的 DAO 风格

1. 在 `DaoStyle` 枚举中添加新风格
2. 创建对应的模板文件
3. 在 `DaoBuilder` 中添加处理逻辑

### 添加新的代码层

1. 创建新的构建器类
2. 创建对应的模板文件
3. 在 `CodeGeneratorFacade` 中添加生成逻辑

### 自定义类型映射

1. 修改 `EntityBuilder.mapColumnTypeToJavaType()` 方法
2. 添加新的数据库类型到 Java 类型的映射规则
