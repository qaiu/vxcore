# 严格的DDL映射系统

## 概述

本项目实现了一个严格的DDL（Data Definition Language）映射系统，可以自动将Java对象与数据库表结构进行同步。当修改Java对象时，系统会自动检测差异并更新数据库表结构。

## 核心特性

- **严格的DDL映射**：Java对象与数据库表的精确映射
- **自动同步**：检测并自动修复表结构差异
- **版本控制**：跟踪表结构变更历史
- **兼容性**：与现有的`@Table`注解完全兼容
- **多数据库支持**：支持MySQL、PostgreSQL、H2等数据库
- **智能表结构管理**：
  - ✅ 自动添加新字段（ADD COLUMN）
  - ✅ 自动删除多余字段（DROP COLUMN）
  - ✅ 自动修改字段属性（MODIFY COLUMN）
  - ✅ 自动更新字段注释和默认值

## 核心注解

### @DdlTable

用于标注实体类，定义表的基本信息：

```java
@DdlTable(
    value = "user_table",           // 表名
    keyFields = "id",                // 主键字段
    version = 1,                     // 表结构版本
    autoSync = true,                 // 启用自动同步
    comment = "用户表",               // 表注释
    charset = "utf8mb4",             // 字符集
    collate = "utf8mb4_unicode_ci",  // 排序规则
    engine = "InnoDB",               // 存储引擎
    dbtype = "mysql"                 // 数据库类型（可选）
)
public class User {
    // ...
}
```

### @DdlColumn

用于标注字段，定义列的详细信息：

```java
@DdlColumn(
    type = "VARCHAR",                // SQL类型
    length = 50,                     // 长度
    nullable = false,                // 是否允许NULL
    uniqueKey = "username",          // 唯一约束
    indexName = "idx_username",      // 索引名称
    comment = "用户名"                // 字段注释
)
private String username;
```

### @DdlIgnore

用于标记不需要参与DDL映射的字段：

```java
@DdlIgnore
private String transientField;
```

## 数据库类型自动识别

### dbtype字段说明

`@DdlTable`注解的`dbtype`字段用于指定数据库类型，支持以下值：

- `mysql` - MySQL数据库
- `postgresql` 或 `postgres` - PostgreSQL数据库  
- `h2` - H2数据库
- `oracle` - Oracle数据库
- `sqlserver` 或 `mssql` - SQL Server数据库

### 自动识别功能

当指定`dbtype`字段时，系统会自动识别数据库类型并应用相应的DDL语法：

```java
// MySQL数据库
@DdlTable(value = "user_table", dbtype = "mysql")
public class User {
    // 系统会自动使用MySQL语法创建表
}

// PostgreSQL数据库
@DdlTable(value = "user_table", dbtype = "postgresql")
public class User {
    // 系统会自动使用PostgreSQL语法创建表
}

// H2数据库
@DdlTable(value = "user_table", dbtype = "h2")
public class User {
    // 系统会自动使用H2语法创建表
}
```

### 简化使用方法

使用`dbtype`字段后，可以简化DDL操作，无需手动指定数据库类型：

```java
// 传统方式（需要手动指定数据库类型）
EnhancedCreateTable.createTable(User.class, JDBCType.MYSQL);
EnhancedCreateTable.syncTableStructure(User.class, JDBCType.MYSQL);

// 使用dbtype字段（自动识别数据库类型）
EnhancedCreateTable.createTable(User.class);
EnhancedCreateTable.syncTableStructure(User.class);
```

### 大小写不敏感

`dbtype`字段支持大小写不敏感的识别：

```java
@DdlTable(value = "user_table", dbtype = "MYSQL")     // 大写
@DdlTable(value = "user_table", dbtype = "MySql")     // 混合大小写
@DdlTable(value = "user_table", dbtype = "mysql")     // 小写
// 以上三种写法都会被识别为MySQL数据库
```

### 默认行为

如果不指定`dbtype`字段或指定为空字符串，系统会使用传入的数据库类型参数或默认使用MySQL：

```java
@DdlTable(value = "user_table")  // 不指定dbtype
public class User {
    // 系统会使用传入的JDBCType参数或默认MySQL
}
```

## 使用方法

### 1. 基本使用

```java
// 创建表并启用严格DDL映射
EnhancedCreateTable.createTableWithStrictMapping(pool, JDBCType.MySQL)
    .onSuccess(v -> System.out.println("表创建和同步完成"))
    .onFailure(throwable -> System.err.println("表创建失败: " + throwable.getMessage()));
```

### 2. 同步现有表

```java
// 同步所有表结构
EnhancedCreateTable.synchronizeTables(pool, JDBCType.MySQL)
    .onSuccess(v -> System.out.println("表结构同步完成"))
    .onFailure(throwable -> System.err.println("表结构同步失败: " + throwable.getMessage()));
```

### 3. 同步指定表

```java
// 同步指定表
EnhancedCreateTable.synchronizeTable(pool, User.class, JDBCType.MySQL)
    .onSuccess(v -> System.out.println("User表同步完成"))
    .onFailure(throwable -> System.err.println("User表同步失败: " + throwable.getMessage()));
```

### 4. 检查同步状态

```java
// 检查表是否需要同步
EnhancedCreateTable.needsSynchronization(pool, User.class, JDBCType.MySQL)
    .onSuccess(needsSync -> {
        if (needsSync) {
            System.out.println("表需要同步");
        } else {
            System.out.println("表已同步");
        }
    });
```

### 5. 生成表结构报告

```java
// 生成表结构报告
EnhancedCreateTable.generateTableStructureReport(pool, JDBCType.MySQL)
    .onSuccess(report -> System.out.println("表结构报告:\n" + report))
    .onFailure(throwable -> System.err.println("生成报告失败: " + throwable.getMessage()));
```

## 完整示例

```java
@Data
@DataObject
@RowMapped(formatter = SnakeCase.class)
@NoArgsConstructor
@DdlTable(
    value = "user",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "用户表"
)
public class User {

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

    @DdlColumn(
        type = "VARCHAR",
        length = 100,
        nullable = false,
        uniqueKey = "email",
        comment = "邮箱"
    )
    private String email;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = false,
        defaultValue = "CURRENT_TIMESTAMP",
        defaultValueIsFunction = true,
        comment = "创建时间"
    )
    private LocalDateTime createTime;

    @DdlColumn(
        type = "BOOLEAN",
        nullable = false,
        defaultValue = "true",
        comment = "是否激活"
    )
    private Boolean active;

    public User(JsonObject json) {
        this.id = json.getLong("id");
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.createTime = json.getLocalDateTime("createTime");
        this.active = json.getBoolean("active");
    }
}
```

## 迁移指南

### 从现有@Table注解迁移

1. **保持兼容性**：现有的`@Table`注解仍然有效
2. **逐步迁移**：可以逐步将`@Table`替换为`@DdlTable`
3. **混合使用**：可以在同一个项目中使用两种注解

### 迁移步骤

1. 将`@Table`替换为`@DdlTable`
2. 添加版本号：`version = 1`
3. 启用自动同步：`autoSync = true`
4. 为字段添加`@DdlColumn`注解
5. 测试表结构同步

## 注意事项

1. **版本管理**：每次修改表结构时，应该递增版本号
2. **数据安全**：在生产环境中使用前，请先备份数据
3. **性能考虑**：大量表同步可能影响性能，建议在低峰期执行
4. **权限要求**：确保数据库用户有足够的DDL权限

## 故障排除

### 常见问题

1. **表不存在**：系统会自动创建表
2. **列类型不匹配**：系统会自动修改列类型
3. **权限不足**：确保数据库用户有DDL权限
4. **同步失败**：检查日志中的具体错误信息

### 调试技巧

1. 启用详细日志：设置日志级别为DEBUG
2. 检查SQL语句：查看生成的DDL语句
3. 手动执行：可以手动执行生成的SQL语句进行测试

## 字段减少功能示例

框架支持智能的字段减少功能，当实体类中删除字段时，框架会自动检测并生成相应的DROP COLUMN语句。

### 示例：用户表字段减少

**初始实体类（10个字段）：**
```java
@DdlTable(
    value = "user_table",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "用户表",
    dbtype = "mysql"
)
public class User {
    @DdlColumn(type = "BIGINT", autoIncrement = true, comment = "用户ID")
    private Long id;
    
    @DdlColumn(type = "VARCHAR", length = 50, comment = "用户名")
    private String username;
    
    @DdlColumn(type = "VARCHAR", length = 100, comment = "邮箱")
    private String email;
    
    @DdlColumn(type = "DECIMAL", precision = 10, scale = 2, comment = "余额")
    private BigDecimal balance;  // 这个字段将在简化版本中删除
    
    @DdlColumn(type = "TIMESTAMP", comment = "更新时间")
    private LocalDateTime updateTime;  // 这个字段也将被删除
    
    @DdlColumn(type = "TEXT", comment = "备注")
    private String remark;  // 这个字段也将被删除
    
    // ... 其他字段
}
```

**简化后的实体类（7个字段）：**
```java
@DdlTable(
    value = "user_table",
    keyFields = "id",
    version = 2,  // 版本号增加
    autoSync = true,
    comment = "简化用户表",
    dbtype = "mysql"
)
public class SimplifiedUser {
    @DdlColumn(type = "BIGINT", autoIncrement = true, comment = "用户ID")
    private Long id;
    
    @DdlColumn(type = "VARCHAR", length = 50, comment = "用户名")
    private String username;
    
    @DdlColumn(type = "VARCHAR", length = 100, comment = "邮箱")
    private String email;
    
    // balance、updateTime、remark字段被删除
    // 框架会自动检测到这些字段的缺失并生成相应的DROP COLUMN语句
}
```

**框架自动生成的SQL：**
```sql
-- 检测到多余字段，自动生成删除语句
ALTER TABLE `user_table` DROP COLUMN `balance`;
ALTER TABLE `user_table` DROP COLUMN `update_time`;
ALTER TABLE `user_table` DROP COLUMN `remark`;

-- 检测到字段属性变化，自动生成修改语句
ALTER TABLE `user_table` MODIFY COLUMN `username` VARCHAR(50) NOT NULL COMMENT '用户名';
ALTER TABLE `user_table` MODIFY COLUMN `email` VARCHAR(100) NOT NULL COMMENT '邮箱';
```

**测试结果：**
```
✅ 框架在MySQL中自动更新表成功！
📊 MySQL表结构 (user_table - 更新后):
  - password: varchar(255) NOT NULL   
  - create_time: timestamp NOT NULL  DEFAULT CURRENT_TIMESTAMP
  - active: tinyint(1) NOT NULL  DEFAULT 1 
  - id: bigint NOT NULL PRI  auto_increment
  - email: varchar(100) NOT NULL   
  - age: int NULL  DEFAULT 0 
  - username: varchar(50) NOT NULL   
✅ 第二步完成：框架在MySQL中自动更新表结构成功！
   - 总字段数: 7
   - 减少字段: balance, update_time, remark
```

## 扩展功能

### 自定义类型映射

可以通过修改`ColumnMetadata`类中的`JAVA_TO_SQL_TYPE_MAP`来添加自定义类型映射。

### 自定义约束

可以通过扩展`@DdlColumn`注解来添加更多约束类型。

### 数据库特定功能

可以通过检查`JDBCType`来为不同数据库提供特定的DDL语句。

## 贡献

欢迎提交Issue和Pull Request来改进这个DDL映射系统。
