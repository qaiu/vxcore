# VXCore Generator 使用指南

## 概述

VXCore Generator 是一个基于数据库表结构自动生成 Controller/Service/DAO 三层架构代码的工具。它支持多种数据库，提供三种不同的 DAO 实现风格，并生成完整的 RESTful API 代码。

## 功能特性

### 🗄️ 数据源支持
- **数据库连接**: MySQL、PostgreSQL、H2
- **配置文件**: JSON/YAML 格式的表结构定义
- **自动元数据读取**: 主键、外键、索引、注释等

### 🏗️ 代码生成
- **实体类**: 支持多种注解风格
- **DAO 层**: 三种实现风格
- **Service 层**: 业务逻辑接口和实现
- **Controller 层**: RESTful API 控制器
- **DTO 层**: 请求/响应数据传输对象

### 🔧 高级特性
- **分页查询**: 自动生成分页方法
- **条件查询**: 支持复杂查询条件
- **参数校验**: Bean Validation 注解
- **DTO 转换**: 自动生成转换工具
- **异常处理**: 统一异常处理机制

## 快速开始

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

### 3. Maven 插件方式

```xml
<plugin>
    <groupId>cn.qaiu</groupId>
    <artifactId>core-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <database>
                    <url>jdbc:mysql://localhost:3306/testdb</url>
                    <username>root</username>
                    <password>password</password>
                </database>
                <tables>
                    <table>user</table>
                    <table>order</table>
                    <table>product</table>
                </tables>
                <packageName>com.example</packageName>
                <outputPath>./src/main/java</outputPath>
                <daoStyle>lambda</daoStyle>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 配置说明

### 命令行参数

| 参数 | 说明 | 示例 | 默认值 |
|------|------|------|--------|
| `--db-url` | 数据库连接URL | `jdbc:mysql://localhost:3306/db` | - |
| `--db-user` | 数据库用户名 | `root` | - |
| `--db-password` | 数据库密码 | `password` | - |
| `--db-schema` | 数据库模式名 | `public` | - |
| `--tables` | 要生成的表名(逗号分隔) | `user,order,product` | - |
| `--package` | 基础包名 | `com.example` | `com.example` |
| `--output` | 输出目录 | `./src/main/java` | `./src/main/java` |
| `--dao-style` | DAO风格 | `vertx`, `jooq`, `lambda` | `lambda` |
| `--config` | 配置文件路径 | `generator-config.json` | - |

### 功能开关

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--no-entity` | 不生成实体类 | `false` |
| `--no-dao` | 不生成DAO | `false` |
| `--no-service` | 不生成Service | `false` |
| `--no-controller` | 不生成Controller | `false` |
| `--no-dto` | 不生成DTO | `false` |
| `--overwrite` | 覆盖已存在文件 | `false` |
| `--no-comments` | 不生成注释 | `false` |
| `--no-validation` | 不生成校验注解 | `false` |

### 注解支持

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--lombok` | 使用Lombok注解 | `false` |
| `--jpa` | 使用JPA注解 | `false` |
| `--vertx` | 使用Vert.x注解 | `false` |

## 配置文件格式

### JSON 格式

```json
{
  "database": {
    "url": "jdbc:mysql://localhost:3306/testdb",
    "username": "root",
    "password": "password",
    "schema": "public"
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
    "generateDto": true,
    "generateService": true,
    "generateController": true,
    "useLombok": false,
    "useJpaAnnotations": false,
    "useVertxAnnotations": false
  },
  "output": {
    "path": "./src/main/java",
    "overwriteExisting": false,
    "encoding": "UTF-8"
  }
}
```

### YAML 格式

```yaml
database:
  url: jdbc:mysql://localhost:3306/testdb
  username: root
  password: password
  schema: public

packages:
  basePackage: com.example
  entityPackage: com.example.entity
  daoPackage: com.example.dao
  servicePackage: com.example.service
  controllerPackage: com.example.controller
  dtoPackage: com.example.dto

tables:
  - user
  - order
  - product

daoStyle: lambda

features:
  generateComments: true
  generateValidation: true
  generateDto: true
  generateService: true
  generateController: true
  useLombok: false
  useJpaAnnotations: false
  useVertxAnnotations: false

output:
  path: ./src/main/java
  overwriteExisting: false
  encoding: UTF-8
```

## DAO 风格说明

### 1. Vert.x SQL 风格

使用原生 SQL + 实体映射，适合需要精确控制 SQL 的场景。

**特点:**
- 使用 `SqlClient` 执行原生 SQL
- 实体类使用 `@RowMapped`, `@Column` 注解
- 手动编写 SQL 语句
- 性能最优，灵活性最高

**示例代码:**
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

使用 jOOQ DSL API，提供类型安全的 SQL 构建。

**特点:**
- 类型安全的 SQL 构建
- 编译时检查
- 支持复杂查询
- 与 core-database 集成

**示例代码:**
```java
public class UserDao {
    public Future<Optional<User>> findById(Long id) {
        return Future.fromCompletionStage(
            dsl.selectFrom(USER)
                .where(USER.ID.eq(id))
                .fetchOptionalAsync()
        ).map(optional -> optional.map(this::mapRecord));
    }
}
```

### 3. MP Lambda 风格

MyBatis-Plus 风格的 Lambda 查询，使用链式调用。

**特点:**
- 继承 `LambdaDao`
- Lambda 表达式查询
- 链式调用
- 学习成本低

**示例代码:**
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

## 生成代码结构

### 实体类 (Entity)

```java
package com.example.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体
 * 
 * @author QAIU
 * @version 1.0.0
 * @since 2024-01-01 10:00:00
 */
public class User {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    
    // 构造函数、getter/setter、equals/hashCode/toString
}
```

### DAO 层

```java
package com.example.dao;

import com.example.entity.User;
import cn.qaiu.db.dsl.core.LambdaDao;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

public class UserDao extends LambdaDao<User> {
    public UserDao() {
        super(User.class);
    }
    
    public Future<Optional<User>> findById(Long id) {
        return lambdaQuery().eq(User::getId, id).one();
    }
    
    public Future<List<User>> findAll() {
        return lambdaQuery().list();
    }
    
    // 其他 CRUD 方法
}
```

### Service 层

```java
package com.example.service;

import com.example.entity.User;
import com.example.dao.UserDao;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Future<Optional<User>> findById(Long id);
    Future<List<User>> findAll();
    Future<User> create(User user);
    Future<User> update(User user);
    Future<Boolean> deleteById(Long id);
}

public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public Future<Optional<User>> findById(Long id) {
        return userDao.findById(id);
    }
    
    // 其他方法实现
}
```

### Controller 层

```java
package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.RouteMethod;
import cn.qaiu.vx.core.util.JsonResult;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

@RouteHandler("/api/user")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<JsonResult> getById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return JsonResult.success(optional.get());
                    } else {
                        return JsonResult.fail(404, "User not found");
                    }
                });
    }
    
    // 其他 REST 接口
}
```

### DTO 层

```java
package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String name;
    
    private String email;
    
    // getter/setter
}

public class UserUpdateRequest {
    @NotNull(message = "ID不能为空")
    private Long id;
    
    private String name;
    private String email;
    
    // getter/setter
}

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    
    // getter/setter
}

public class UserDtoConverter {
    public static UserResponse toResponse(User user) {
        // 转换逻辑
    }
    
    public static User toEntity(UserCreateRequest request) {
        // 转换逻辑
    }
    
    // 其他转换方法
}
```

## 自定义模板

### 模板目录结构

```
templates/
├── entity.ftl              # 实体类模板
├── dao.ftl                 # DAO 模板 (Lambda 风格)
├── dao-vertx.ftl           # DAO 模板 (Vert.x SQL 风格)
├── dao-jooq.ftl            # DAO 模板 (jOOQ 风格)
├── service.ftl             # Service 接口模板
├── service-impl.ftl        # Service 实现类模板
├── controller.ftl           # Controller 模板
├── dto-create.ftl           # 创建请求 DTO 模板
├── dto-update.ftl           # 更新请求 DTO 模板
├── dto-response.ftl         # 响应 DTO 模板
└── dto-converter.ftl        # DTO 转换器模板
```

### 模板变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `entity` | EntityInfo | 实体信息 |
| `package` | Map | 包信息 |
| `config` | Map | 配置信息 |
| `generatedDate` | String | 生成时间 |
| `author` | String | 作者 |
| `version` | String | 版本 |

### 自定义模板示例

```ftl
<#-- 自定义实体类模板 -->
package ${package.entityPackage};

<#if entity.imports?has_content>
<#list entity.imports as import>
import ${import};
</#list>
</#if>

<#if entity.description?has_content>
/**
 * ${entity.description}
 * 
 * @author ${author}
 * @version ${version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className} {
<#if entity.fields?has_content>

<#list entity.fields as field>
    <#if field.description?has_content>
    /**
     * ${field.description}
     */
    </#if>
    private ${field.fieldType} ${field.fieldName};
</#list>

<#list entity.fields as field>
    public ${field.fieldType} ${field.getterName}() {
        return ${field.fieldName};
    }
    
    public void ${field.setterName}(${field.fieldType} ${field.fieldName}) {
        this.${field.fieldName} = ${field.fieldName};
    }
</#list>
</#if>
}
```

## 最佳实践

### 1. 项目结构

```
src/main/java/
├── com/example/
│   ├── entity/          # 实体类
│   ├── dao/             # 数据访问层
│   ├── service/         # 业务逻辑层
│   │   └── impl/        # 业务逻辑实现
│   ├── controller/      # 控制器层
│   └── dto/             # 数据传输对象
└── resources/
    └── templates/       # 自定义模板
```

### 2. 命名规范

- **表名**: 使用下划线命名，如 `user_info`
- **实体类**: 使用驼峰命名，如 `UserInfo`
- **字段名**: 使用驼峰命名，如 `userName`
- **包名**: 使用小写，如 `com.example.entity`

### 3. 数据库设计

- **主键**: 建议使用 `BIGINT AUTO_INCREMENT`
- **时间字段**: 使用 `TIMESTAMP` 类型
- **字符串字段**: 根据实际需要设置长度
- **注释**: 为表和字段添加注释

### 4. 代码生成策略

- **增量生成**: 使用 `--overwrite` 参数控制覆盖
- **选择性生成**: 使用功能开关控制生成内容
- **模板定制**: 根据项目需求定制模板

### 5. 性能优化

- **批量操作**: 使用批量插入/更新方法
- **分页查询**: 使用分页方法避免内存溢出
- **索引优化**: 为查询字段添加索引

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库 URL、用户名、密码
   - 确认数据库服务是否启动
   - 检查网络连接

2. **表不存在**
   - 确认表名拼写正确
   - 检查数据库模式
   - 确认表是否在指定数据库中

3. **生成代码编译失败**
   - 检查依赖是否正确
   - 确认 Java 版本兼容性
   - 检查模板语法错误

4. **权限问题**
   - 确认输出目录写入权限
   - 检查文件是否被其他程序占用

### 调试技巧

1. **启用详细日志**
   ```bash
   java -jar core-generator.jar --verbose ...
   ```

2. **使用测试配置**
   ```bash
   java -jar core-generator.jar --config test-config.json --dry-run
   ```

3. **分步生成**
   ```bash
   # 只生成实体类
   java -jar core-generator.jar --no-dao --no-service --no-controller --no-dto ...
   ```

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持 MySQL、PostgreSQL、H2 数据库
- 支持三种 DAO 风格
- 支持配置文件方式
- 提供完整的 RESTful API 生成

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

### 代码规范

- 遵循阿里巴巴 Java 开发规范
- 所有 public 方法必须有 JavaDoc
- 新功能必须包含单元测试
- 测试覆盖率 > 80%

## 许可证

MIT License - 查看 [LICENSE](../../LICENSE) 文件了解详情。

## 联系方式

- 作者: QAIU
- 邮箱: qaiu@qq.com
- 网站: https://qaiu.top
- GitHub: https://github.com/qaiu/vxcore
