# VXCore Lambda查询功能文档

## 概述

VXCore Lambda查询功能提供了类似MyBatis-Plus的Lambda表达式查询API，支持类型安全的字段引用和流畅的查询构建。通过Lambda表达式，开发者可以避免硬编码字段名，提高代码的可维护性和类型安全性。

## 核心特性

- **类型安全**: 使用Lambda表达式引用实体字段，编译时检查
- **流畅API**: 链式调用构建复杂查询条件
- **自动映射**: 自动处理实体字段到数据库列的映射
- **分页支持**: 内置分页查询功能
- **统计查询**: 支持count、exists等统计操作
- **批量操作**: 支持批量更新和删除

## 快速开始

### 1. 实体类定义

```java
@DdlTable(name = "users")
public class User extends BaseEntity<Long> {
    
    @DdlColumn(name = "username", length = 50, nullable = false)
    private String username;
    
    @DdlColumn(name = "email", length = 100, nullable = false)
    private String email;
    
    @DdlColumn(name = "status", length = 20, defaultValue = "'ACTIVE'")
    private String status;
    
    // getter和setter方法...
}
```

### 2. DAO类定义

```java
public class UserDao extends LambdaDao<User, Long> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    // 自定义查询方法...
}
```

### 3. 基础查询

```java
// 根据字段查询单个记录
Future<Optional<User>> user = userDao.findByUsername("alice");

// 根据字段查询列表
Future<List<User>> users = userDao.findByStatus("ACTIVE");
```

## 查询条件

### 基础条件

| 方法 | 说明 | 示例 |
|------|------|------|
| `eq` | 等于 | `lambdaQuery().eq(User::getStatus, "ACTIVE")` |
| `ne` | 不等于 | `lambdaQuery().ne(User::getStatus, "INACTIVE")` |
| `gt` | 大于 | `lambdaQuery().gt(User::getAge, 18)` |
| `ge` | 大于等于 | `lambdaQuery().ge(User::getBalance, 1000.0)` |
| `lt` | 小于 | `lambdaQuery().lt(User::getAge, 65)` |
| `le` | 小于等于 | `lambdaQuery().le(User::getBalance, 5000.0)` |
| `like` | 模糊查询 | `lambdaQuery().like(User::getUsername, "alice")` |
| `notLike` | 不包含 | `lambdaQuery().notLike(User::getUsername, "admin")` |
| `in` | 包含 | `lambdaQuery().in(User::getId, Arrays.asList(1L, 2L, 3L))` |
| `notIn` | 不包含 | `lambdaQuery().notIn(User::getStatus, Arrays.asList("DELETED"))` |
| `between` | 范围查询 | `lambdaQuery().between(User::getAge, 18, 65)` |
| `notBetween` | 不在范围 | `lambdaQuery().notBetween(User::getBalance, 0, 100)` |
| `isNull` | 为空 | `lambdaQuery().isNull(User::getDeletedAt)` |
| `isNotNull` | 不为空 | `lambdaQuery().isNotNull(User::getEmail)` |

### 逻辑条件

| 方法 | 说明 | 示例 |
|------|------|------|
| `and` | 且条件 | `lambdaQuery().eq(User::getStatus, "ACTIVE").and(wrapper -> wrapper.gt(User::getAge, 18))` |
| `or` | 或条件 | `lambdaQuery().eq(User::getStatus, "ACTIVE").or(wrapper -> wrapper.eq(User::getStatus, "PENDING"))` |

### 排序和分页

| 方法 | 说明 | 示例 |
|------|------|------|
| `orderByAsc` | 升序排序 | `lambdaQuery().orderByAsc(User::getCreateTime)` |
| `orderByDesc` | 降序排序 | `lambdaQuery().orderByDesc(User::getBalance)` |
| `limit` | 限制条数 | `lambdaQuery().limit(10)` |
| `offset` | 偏移量 | `lambdaQuery().offset(20)` |

### 字段选择

| 方法 | 说明 | 示例 |
|------|------|------|
| `select` | 选择字段 | `lambdaQuery().select(User::getId, User::getUsername, User::getEmail)` |

## 查询方法

### 列表查询

```java
// 简单条件查询
Future<List<User>> users = userDao.lambdaList(User::getStatus, "ACTIVE");

// 复杂条件查询
Future<List<User>> users = userDao.lambdaList(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .gt(User::getAge, 18)
    .orderByDesc(User::getCreateTime));
```

### 单个查询

```java
// 根据字段查询单个记录
Future<Optional<User>> user = userDao.lambdaOne(User::getUsername, "alice");

// 根据复杂条件查询单个记录
Future<Optional<User>> user = userDao.lambdaOne(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .gt(User::getBalance, 1000.0));
```

### 分页查询

```java
Future<LambdaPageResult<User>> pageResult = userDao.lambdaPage(
    lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .orderByDesc(User::getCreateTime),
    1,  // 当前页
    10  // 每页大小
);

pageResult.onSuccess(result -> {
    logger.info("总记录数: {}", result.getTotal());
    logger.info("当前页: {}", result.getCurrent());
    logger.info("每页大小: {}", result.getSize());
    result.getRecords().forEach(user -> 
        logger.info("用户: {}", user.getUsername()));
});
```

### 统计查询

```java
// 计数查询
Future<Long> count = userDao.lambdaCount(lambdaQuery()
    .eq(User::getStatus, "ACTIVE"));

// 存在性检查
Future<Boolean> exists = userDao.lambdaExists(User::getEmail, "alice@example.com");
```

## 更新和删除

### 更新操作

```java
// 批量更新
Future<Integer> updated = userDao.lambdaUpdate(
    lambdaQuery().in(User::getId, Arrays.asList(1L, 2L, 3L)),
    createUserWithStatus("UPDATED")
);

// 创建更新对象
private User createUserWithStatus(String status) {
    User user = new User();
    user.setStatus(status);
    return user;
}
```

### 删除操作

```java
// 批量删除
Future<Integer> deleted = userDao.lambdaDelete(
    lambdaQuery().eq(User::getStatus, "INACTIVE")
);
```

## DdlColumn注解

### value字段

`DdlColumn`注解支持`value`字段作为`name`字段的别名，提供更简洁的写法：

```java
// 使用name字段
@DdlColumn(name = "product_name", length = 100)
private String name;

// 使用value字段（推荐）
@DdlColumn(value = "product_name", length = 100)
private String name;
```

### 字段映射

Lambda查询会自动处理Java字段名到数据库列名的映射：

- Java字段名: `categoryId` → 数据库列名: `category_id`
- Java字段名: `emailVerified` → 数据库列名: `email_verified`

## 最佳实践

### 1. 实体类设计

```java
@DdlTable(name = "users")
public class User extends BaseEntity<Long> {
    
    @DdlColumn(value = "username", length = 50, nullable = false)
    private String username;
    
    @DdlColumn(value = "email", length = 100, nullable = false, unique = true)
    private String email;
    
    @DdlColumn(value = "status", length = 20, defaultValue = "'ACTIVE'")
    private String status;
    
    // 提供完整的getter和setter方法
    // 实现toJson()方法用于序列化
}
```

### 2. DAO类设计

```java
public class UserDao extends LambdaDao<User, Long> {
    
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    // 提供业务相关的查询方法
    public Future<Optional<User>> findByUsername(String username) {
        return lambdaOne(User::getUsername, username);
    }
    
    public Future<List<User>> findActiveUsers() {
        return lambdaList(User::getStatus, "ACTIVE");
    }
    
    public Future<LambdaPageResult<User>> findUsersByPage(long current, long size) {
        return lambdaPage(lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderByDesc(User::getCreateTime), current, size);
    }
}
```

### 3. 复杂查询

```java
// 嵌套条件查询
Future<List<User>> users = userDao.lambdaList(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .and(wrapper -> wrapper
        .ge(User::getAge, 18)
        .or(subWrapper -> subWrapper
            .ge(User::getBalance, 1000.0)
            .eq(User::getEmailVerified, true)))
    .orderByDesc(User::getCreateTime));

// 字段选择查询
Future<List<User>> users = userDao.lambdaList(lambdaQuery()
    .select(User::getId, User::getUsername, User::getEmail)
    .eq(User::getStatus, "ACTIVE")
    .orderByAsc(User::getUsername));
```

## 示例代码

完整的示例代码请参考：

- [LambdaQueryDemo.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/LambdaQueryDemo.java) - 快速入门示例
- [UserDao.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/UserDao.java) - 用户DAO示例
- [ProductDao.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/ProductDao.java) - 产品DAO示例

## 注意事项

1. **类型安全**: Lambda表达式在编译时检查，避免字段名拼写错误
2. **性能考虑**: 复杂查询建议使用索引优化
3. **事务管理**: 更新和删除操作建议在事务中执行
4. **错误处理**: 使用Vert.x的Future处理异步操作结果
5. **字段映射**: 确保实体字段名与数据库列名正确映射

## 扩展功能

- **自定义条件**: 可以扩展LambdaQueryWrapper添加自定义查询条件
- **结果映射**: 支持自定义结果映射器
- **缓存支持**: 可以集成缓存层提高查询性能
- **审计功能**: 支持查询审计和日志记录
