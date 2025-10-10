# VXCore 代码生成器指南

## 🎯 概述

VXCore 代码生成器是一个强大的工具，可以根据数据库表结构或配置文件自动生成三层架构的 Java 代码，体现了"简单而不失优雅"的设计思想：

- **简单**: 一键生成完整的 CRUD 代码，无需手写样板代码
- **优雅**: 支持多种 DAO 风格，生成类型安全的代码
- **智能**: 自动处理表关系、字段映射、验证注解等

## 🚀 核心特性

### 支持的代码层
- **Entity** - 实体类，支持多种注解风格
- **DAO** - 数据访问层，支持三种风格
- **Service** - 业务逻辑层，包含接口和实现
- **Controller** - 控制器层，RESTful API
- **DTO** - 数据传输对象，请求/响应模型

### 支持的 DAO 风格

#### 1. Lambda 风格 (推荐)
基于 MyBatis-Plus 风格的 Lambda 查询，类型安全且易用：

```java
public class UserDao extends LambdaDao<User> {
    // 自动继承基础 CRUD 方法
    // 支持链式查询：lambdaQuery().eq(User::getId, id).one()
    
    public Future<List<User>> findActiveUsers() {
        return lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .like(User::getName, "张%")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
}
```

#### 2. Vert.x SQL 风格
使用 Vert.x 原生 SQL 执行器，支持 `@RowMapped` 和 `@Column` 注解：

```java
@RowMapped
public class User {
    @Column(name = "id")
    private Long id;
    
    @Column(name = "username")
    private String username;
}

public class UserDao {
    public Future<User> findById(Long id) {
        return executor.query("SELECT * FROM users WHERE id = ?", Tuple.of(id))
            .map(rows -> rows.iterator().next().to(User.class));
    }
}
```

#### 3. jOOQ 风格
使用 jOOQ DSL API，类型安全的 SQL 构建：

```java
public class UserDao {
    public Future<User> findById(Long id) {
        return executor.query(dsl -> 
            dsl.selectFrom(USER)
               .where(USER.ID.eq(id))
               .fetchOneInto(User.class)
        );
    }
}
```

## 📋 快速开始

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
        },
        {
          "columnName": "email",
          "columnType": "VARCHAR",
          "columnSize": 100,
          "nullable": false,
          "comment": "邮箱"
        },
        {
          "columnName": "status",
          "columnType": "VARCHAR",
          "columnSize": 20,
          "nullable": false,
          "defaultValue": "ACTIVE",
          "comment": "状态"
        },
        {
          "columnName": "create_time",
          "columnType": "TIMESTAMP",
          "nullable": false,
          "comment": "创建时间"
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

## ⚙️ 配置选项详解

### DatabaseConfig - 数据库配置
```java
DatabaseConfig config = new DatabaseConfig()
    .setUrl("jdbc:mysql://localhost:3306/mydb")      // 数据库连接 URL
    .setUsername("root")                              // 用户名
    .setPassword("password")                          // 密码
    .setDriverClassName("com.mysql.cj.jdbc.Driver")  // 驱动类名
    .setSchema("public")                              // 数据库模式
    .setCatalog("mydb");                             // 数据库目录
```

### PackageConfig - 包名配置
```java
PackageConfig config = new PackageConfig()
    .setEntityPackage("com.example.entity")         // 实体类包名
    .setDaoPackage("com.example.dao")                // DAO 包名
    .setServicePackage("com.example.service")         // Service 包名
    .setControllerPackage("com.example.controller")   // Controller 包名
    .setDtoPackage("com.example.dto")                 // DTO 包名
    .setBasePackage("com.example");                   // 基础包名
```

### OutputConfig - 输出配置
```java
OutputConfig config = new OutputConfig()
    .setOutputPath("src/main/java")                  // 输出路径
    .setOverwriteExisting(true)                       // 是否覆盖已存在的文件
    .setCreateDirectories(true)                       // 是否创建目录
    .setEncoding("UTF-8");                            // 文件编码
```

### FeatureConfig - 功能配置
```java
FeatureConfig config = new FeatureConfig()
    .setGenerateEntity(true)                          // 是否生成实体类
    .setGenerateDao(true)                            // 是否生成 DAO
    .setGenerateService(true)                        // 是否生成 Service
    .setGenerateController(true)                      // 是否生成 Controller
    .setGenerateDto(true)                            // 是否生成 DTO
    .setGenerateComments(true)                       // 是否生成注释
    .setUseLombok(true)                              // 是否使用 Lombok 注解
    .setUseJpaAnnotations(false)                     // 是否使用 JPA 注解
    .setUseVertxAnnotations(true)                   // 是否使用 Vert.x 注解
    .setGenerateValidation(true)                     // 是否生成验证注解
    .setDaoStyle(DaoStyle.LAMBDA)                    // DAO 风格
    .setGenerateSwagger(true)                        // 是否生成 Swagger 注解
    .setGenerateTests(true);                         // 是否生成测试类
```

### TemplateConfig - 模板配置
```java
TemplateConfig config = new TemplateConfig()
    .setTemplatePath("templates")                    // 模板路径
    .setEntityTemplate("entity.ftl")                // 实体类模板
    .setDaoTemplate("dao.ftl")                       // DAO 模板
    .setServiceTemplate("service.ftl")               // Service 模板
    .setControllerTemplate("controller.ftl")          // Controller 模板
    .setDtoTemplate("dto.ftl");                      // DTO 模板
```

## 🎨 生成代码示例

### Entity 实体类
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@DdlTable("users")
public class User extends BaseEntity {
    
    @DdlColumn("id")
    @DdlId
    @DdlGeneratedValue
    private Long id;
    
    @DdlColumn("username")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    @DdlColumn("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @DdlColumn("status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @DdlColumn("create_time")
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @DdlColumn("update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
```

### DAO 数据访问层
```java
@Component
public class UserDao extends LambdaDao<User> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Future<Optional<User>> findByUsername(String username) {
        return lambdaQuery()
            .eq(User::getUsername, username)
            .one()
            .map(Optional::ofNullable);
    }
    
    /**
     * 查找活跃用户
     */
    public Future<List<User>> findActiveUsers() {
        return lambdaQuery()
            .eq(User::getStatus, UserStatus.ACTIVE)
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
    
    /**
     * 分页查询用户
     */
    public Future<PageResult<User>> findUsersPage(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = lambdaQuery();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        
        return wrapper.orderBy(User::getCreateTime, SortOrder.DESC)
            .page(page, size);
    }
}
```

### Service 业务逻辑层
```java
public interface UserService {
    Future<User> createUser(UserCreateRequest request);
    Future<User> updateUser(Long id, UserUpdateRequest request);
    Future<Optional<User>> findById(Long id);
    Future<Optional<User>> findByUsername(String username);
    Future<List<User>> findActiveUsers();
    Future<PageResult<User>> findUsersPage(int page, int size, String keyword);
    Future<Void> deleteUser(Long id);
}

@Service
public class UserServiceImpl implements UserService {
    
    private final UserDao userDao;
    
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public Future<User> createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        return userDao.findByUsername(request.getUsername())
            .compose(existingUser -> {
                if (existingUser.isPresent()) {
                    return Future.failedFuture(new BusinessException("用户名已存在"));
                }
                
                // 创建新用户
                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setStatus(UserStatus.ACTIVE);
                
                return userDao.create(user);
            });
    }
    
    @Override
    public Future<User> updateUser(Long id, UserUpdateRequest request) {
        return userDao.findById(id)
            .compose(user -> {
                if (user.isEmpty()) {
                    return Future.failedFuture(new BusinessException("用户不存在"));
                }
                
                User existingUser = user.get();
                existingUser.setEmail(request.getEmail());
                existingUser.setStatus(request.getStatus());
                
                return userDao.update(existingUser);
            });
    }
    
    @Override
    public Future<Optional<User>> findById(Long id) {
        return userDao.findById(id);
    }
    
    @Override
    public Future<Optional<User>> findByUsername(String username) {
        return userDao.findByUsername(username);
    }
    
    @Override
    public Future<List<User>> findActiveUsers() {
        return userDao.findActiveUsers();
    }
    
    @Override
    public Future<PageResult<User>> findUsersPage(int page, int size, String keyword) {
        return userDao.findUsersPage(page, size, keyword);
    }
    
    @Override
    public Future<Void> deleteUser(Long id) {
        return userDao.findById(id)
            .compose(user -> {
                if (user.isEmpty()) {
                    return Future.failedFuture(new BusinessException("用户不存在"));
                }
                
                return userDao.deleteById(id);
            });
    }
}
```

### Controller 控制器层
```java
@RouteHandler("/api/users")
@Api(tags = "用户管理")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @RouteMapping(value = "", method = HttpMethod.POST)
    @ApiOperation("创建用户")
    public Future<JsonResult<User>> createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request)
            .map(user -> JsonResult.success(user))
            .recover(throwable -> {
                if (throwable instanceof BusinessException) {
                    return Future.succeededFuture(JsonResult.fail(400, throwable.getMessage()));
                }
                return Future.succeededFuture(JsonResult.fail(500, "系统错误"));
            });
    }
    
    @RouteMapping(value = "/{id}", method = HttpMethod.PUT)
    @ApiOperation("更新用户")
    public Future<JsonResult<User>> updateUser(
            @PathVariable("id") Long id,
            @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request)
            .map(user -> JsonResult.success(user))
            .recover(throwable -> {
                if (throwable instanceof BusinessException) {
                    return Future.succeededFuture(JsonResult.fail(400, throwable.getMessage()));
                }
                return Future.succeededFuture(JsonResult.fail(500, "系统错误"));
            });
    }
    
    @RouteMapping(value = "/{id}", method = HttpMethod.GET)
    @ApiOperation("根据ID查询用户")
    public Future<JsonResult<User>> getUserById(@PathVariable("id") Long id) {
        return userService.findById(id)
            .map(user -> {
                if (user.isPresent()) {
                    return JsonResult.success(user.get());
                } else {
                    return JsonResult.fail(404, "用户不存在");
                }
            });
    }
    
    @RouteMapping(value = "/active", method = HttpMethod.GET)
    @ApiOperation("查询活跃用户")
    public Future<JsonResult<List<User>>> getActiveUsers() {
        return userService.findActiveUsers()
            .map(users -> JsonResult.success(users));
    }
    
    @RouteMapping(value = "/page", method = HttpMethod.GET)
    @ApiOperation("分页查询用户")
    public Future<JsonResult<PageResult<User>>> getUsersPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.findUsersPage(page, size, keyword)
            .map(result -> JsonResult.success(result));
    }
    
    @RouteMapping(value = "/{id}", method = HttpMethod.DELETE)
    @ApiOperation("删除用户")
    public Future<JsonResult<Void>> deleteUser(@PathVariable("id") Long id) {
        return userService.deleteUser(id)
            .map(v -> JsonResult.success())
            .recover(throwable -> {
                if (throwable instanceof BusinessException) {
                    return Future.succeededFuture(JsonResult.fail(400, throwable.getMessage()));
                }
                return Future.succeededFuture(JsonResult.fail(500, "系统错误"));
            });
    }
}
```

### DTO 数据传输对象
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户创建请求")
public class UserCreateRequest {
    
    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    @ApiModelProperty(value = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户更新请求")
public class UserUpdateRequest {
    
    @ApiModelProperty(value = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @ApiModelProperty(value = "状态")
    private UserStatus status;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户响应")
public class UserResponse {
    
    @ApiModelProperty(value = "用户ID")
    private Long id;
    
    @ApiModelProperty(value = "用户名")
    private String username;
    
    @ApiModelProperty(value = "邮箱")
    private String email;
    
    @ApiModelProperty(value = "状态")
    private UserStatus status;
    
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
```

## 🛠️ 运行示例

### 使用 Maven 运行
```bash
# 运行代码生成器示例
mvn exec:java@run-code-generator-demo -pl core-example

# 或者直接运行主类
mvn exec:java -Dexec.mainClass="cn.qaiu.example.generator.CodeGeneratorExampleRunner" -pl core-example
```

### 使用 IDE 运行
直接运行 `CodeGeneratorExampleRunner.main()` 方法。

## 📁 生成的文件结构

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

## 🎨 自定义模板

### 模板文件结构
```
templates/
├── entity.ftl                  # 实体类模板
├── dao.ftl                     # DAO 模板
├── service.ftl                 # Service 接口模板
├── service-impl.ftl            # Service 实现模板
├── controller.ftl              # Controller 模板
├── dto-create.ftl              # 创建请求 DTO 模板
├── dto-update.ftl              # 更新请求 DTO 模板
├── dto-response.ftl             # 响应 DTO 模板
└── dto-converter.ftl           # DTO 转换器模板
```

### 自定义模板示例
```freemarker
<#-- entity.ftl - 实体类模板 -->
package ${packageConfig.entityPackage};

<#if featureConfig.useLombok>
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
</#if>
<#if featureConfig.useJpaAnnotations>
import javax.persistence.*;
</#if>
<#if featureConfig.useVertxAnnotations>
import cn.qaiu.vx.core.annotations.*;
</#if>
<#if featureConfig.generateValidation>
import javax.validation.constraints.*;
</#if>

<#if featureConfig.useLombok>
@Data
@NoArgsConstructor
@AllArgsConstructor
</#if>
<#if featureConfig.useVertxAnnotations>
@DdlTable("${tableInfo.tableName}")
</#if>
public class ${tableInfo.className} <#if tableInfo.hasBaseEntity>extends BaseEntity</#if> {
    
<#list tableInfo.columns as column>
    <#if featureConfig.useVertxAnnotations>
    @DdlColumn("${column.columnName}")
    <#if column.primaryKey>
    @DdlId
    <#if column.autoIncrement>
    @DdlGeneratedValue
    </#if>
    </#if>
    </#if>
    <#if featureConfig.generateValidation>
    <#if !column.nullable && !column.primaryKey>
    @NotNull(message = "${column.comment}不能为空")
    </#if>
    <#if column.columnType == "VARCHAR" && column.columnSize??>
    @Size(max = ${column.columnSize}, message = "${column.comment}长度不能超过${column.columnSize}个字符")
    </#if>
    </#if>
    private ${column.javaType} ${column.fieldName};
    
</#list>
}
```

## 🔧 高级功能

### 1. 表关系处理
```java
// 自动处理外键关系
public class OrderDao extends LambdaDao<Order> {
    
    public Future<List<Order>> findOrdersWithUser() {
        return lambdaQuery()
            .leftJoin(User.class, (order, user) -> 
                order.getUserId().eq(user.getId()))
            .list();
    }
}
```

### 2. 批量操作生成
```java
public class UserDao extends LambdaDao<User> {
    
    // 自动生成批量操作方法
    public Future<int[]> batchInsert(List<User> users) {
        return batchInsert(users);
    }
    
    public Future<int[]> batchUpdate(List<User> users) {
        return batchUpdate(users);
    }
    
    public Future<int[]> batchDelete(List<Long> ids) {
        return batchDeleteByIds(ids);
    }
}
```

### 3. 查询条件构建
```java
public class UserDao extends LambdaDao<User> {
    
    public Future<List<User>> findUsers(UserQuery query) {
        LambdaQueryWrapper<User> wrapper = lambdaQuery();
        
        if (query.getUsername() != null) {
            wrapper.like(User::getUsername, query.getUsername());
        }
        
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        
        if (query.getStartDate() != null && query.getEndDate() != null) {
            wrapper.between(User::getCreateTime, query.getStartDate(), query.getEndDate());
        }
        
        return wrapper.orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
}
```

## 📚 最佳实践

### 1. 代码生成策略
- **开发阶段**: 使用覆盖模式，快速迭代
- **生产阶段**: 使用增量模式，保护自定义代码
- **团队协作**: 统一代码生成配置，避免冲突

### 2. 自定义扩展
```java
// 自定义生成器
public class CustomCodeGenerator extends CodeGeneratorFacade {
    
    @Override
    public Future<List<String>> generateEntity(TableInfo tableInfo) {
        // 自定义实体类生成逻辑
        return super.generateEntity(tableInfo)
            .compose(files -> {
                // 生成额外的自定义代码
                return generateCustomCode(tableInfo);
            });
    }
    
    private Future<List<String>> generateCustomCode(TableInfo tableInfo) {
        // 实现自定义代码生成
        return Future.succeededFuture(Arrays.asList());
    }
}
```

### 3. 模板定制
- 根据项目需求定制模板
- 保持模板的简洁性和可读性
- 定期更新模板以适应框架升级

## 🚨 注意事项

### 1. 数据库连接
- 确保数据库连接配置正确
- 确认有足够的权限读取表结构
- 支持 H2、MySQL、PostgreSQL 等数据库

### 2. 包名冲突
- 避免生成的代码包名与现有代码冲突
- 使用不同的包名前缀区分不同模块

### 3. 文件覆盖
- 使用 `overwriteExisting(true)` 时要谨慎
- 建议先备份重要文件
- 使用版本控制管理生成的代码

### 4. 依赖管理
- 确保项目中包含必要的依赖
- Lombok、Validation API、Swagger 等
- 根据生成的代码风格添加相应依赖

## 🔍 故障排除

### 常见问题

#### 1. 数据库连接失败
```bash
# 检查数据库配置
- 数据库 URL 是否正确
- 用户名密码是否正确
- 数据库驱动是否已添加
- 数据库服务是否启动
```

#### 2. 模板渲染失败
```bash
# 检查模板语法
- 模板文件是否存在
- 模板语法是否正确
- 变量名是否匹配
- 编码格式是否正确
```

#### 3. 代码编译失败
```bash
# 检查生成代码
- 生成的代码是否有语法错误
- 必要的依赖是否已添加
- 包名是否正确
- 导入语句是否正确
```

#### 4. 权限问题
```bash
# 检查文件权限
- 输出目录是否有写权限
- 文件是否被其他程序占用
- 磁盘空间是否充足
```

### 调试技巧

1. **启用详细日志**
   ```java
   // 设置日志级别
   System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
   ```

2. **分步生成代码**
   ```java
   // 先生成实体类，再生成其他层
   generator.generateEntity("users")
       .compose(v -> generator.generateDao("users"))
       .compose(v -> generator.generateService("users"));
   ```

3. **检查中间文件**
   - 查看生成的中间文件
   - 检查模板渲染结果
   - 验证代码语法正确性

4. **使用 IDE 分析**
   - 使用 IDE 的代码分析功能
   - 检查生成的代码质量
   - 验证依赖关系

## 📈 性能优化

### 1. 批量生成
```java
// 批量生成多个表的代码
List<String> tables = Arrays.asList("users", "products", "orders", "categories");
generator.generateAll(tables)
    .onSuccess(files -> {
        System.out.println("批量生成完成，文件数: " + files.size());
    });
```

### 2. 异步处理
```java
// 异步生成代码，不阻塞主线程
generator.generateAll(tables)
    .onComplete(result -> {
        if (result.succeeded()) {
            // 处理成功结果
        } else {
            // 处理失败结果
        }
    });
```

### 3. 缓存优化
```java
// 使用缓存避免重复生成
Map<String, List<String>> generatedFiles = new ConcurrentHashMap<>();

public Future<List<String>> generateWithCache(String tableName) {
    if (generatedFiles.containsKey(tableName)) {
        return Future.succeededFuture(generatedFiles.get(tableName));
    }
    
    return generator.generateTable(tableName)
        .onSuccess(files -> generatedFiles.put(tableName, files));
}
```

## 🎯 总结

VXCore 代码生成器体现了"简单而不失优雅"的设计思想：

### 简单
- **一键生成**: 无需手写样板代码
- **配置简单**: 最小化配置需求
- **使用便捷**: 支持多种生成方式

### 优雅
- **类型安全**: 生成类型安全的代码
- **多种风格**: 支持不同的 DAO 风格
- **可扩展**: 支持自定义模板和扩展

### 智能
- **自动处理**: 自动处理表关系、字段映射
- **智能生成**: 根据表结构智能生成代码
- **验证支持**: 自动生成验证注解

通过使用 VXCore 代码生成器，开发者可以：
- 快速搭建项目基础架构
- 减少重复代码编写
- 提高开发效率
- 保持代码一致性
- 专注于业务逻辑实现

---

**🎯 VXCore 代码生成器 - 让代码生成更简单、更优雅、更智能！**

[返回首页 →](index.md) | [系统架构 →](04-architecture.md) | [开发者指南 →](05-developer-guide.md)