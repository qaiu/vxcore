# Lambda查询API参考

## LambdaDao类

### 构造函数

```java
public LambdaDao(JooqExecutor executor, Class<T> entityClass)
```

**参数:**
- `executor`: jOOQ执行器实例
- `entityClass`: 实体类类型

### 查询方法

#### lambdaList - 列表查询

```java
// 简单条件查询
public <R> Future<List<T>> lambdaList(SFunction<T, R> column, R value)

// 复杂条件查询
public Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper)
```

**示例:**
```java
// 根据状态查询用户列表
Future<List<User>> users = userDao.lambdaList(User::getStatus, "ACTIVE");

// 复杂条件查询
Future<List<User>> users = userDao.lambdaList(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .gt(User::getAge, 18)
    .orderByDesc(User::getCreateTime));
```

#### lambdaOne - 单个查询

```java
// 根据字段查询单个记录
public <R> Future<Optional<T>> lambdaOne(SFunction<T, R> column, R value)

// 根据复杂条件查询单个记录
public Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper)
```

**示例:**
```java
// 根据用户名查询用户
Future<Optional<User>> user = userDao.lambdaOne(User::getUsername, "alice");

// 根据复杂条件查询用户
Future<Optional<User>> user = userDao.lambdaOne(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .gt(User::getBalance, 1000.0));
```

#### lambdaPage - 分页查询

```java
public Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size)
```

**参数:**
- `wrapper`: 查询条件包装器
- `current`: 当前页码（从1开始）
- `size`: 每页大小

**示例:**
```java
Future<LambdaPageResult<User>> pageResult = userDao.lambdaPage(
    lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .orderByDesc(User::getCreateTime),
    1,  // 当前页
    10  // 每页大小
);
```

#### lambdaCount - 计数查询

```java
public Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper)
```

**示例:**
```java
Future<Long> count = userDao.lambdaCount(lambdaQuery()
    .eq(User::getStatus, "ACTIVE"));
```

#### lambdaExists - 存在性检查

```java
public <R> Future<Boolean> lambdaExists(SFunction<T, R> column, R value)
```

**示例:**
```java
Future<Boolean> exists = userDao.lambdaExists(User::getEmail, "alice@example.com");
```

### 更新和删除方法

#### lambdaUpdate - 更新操作

```java
public Future<Integer> lambdaUpdate(LambdaQueryWrapper<T> wrapper, T entity)
```

**示例:**
```java
User updateUser = new User();
updateUser.setStatus("UPDATED");

Future<Integer> updated = userDao.lambdaUpdate(
    lambdaQuery().in(User::getId, Arrays.asList(1L, 2L, 3L)),
    updateUser
);
```

#### lambdaDelete - 删除操作

```java
public Future<Integer> lambdaDelete(LambdaQueryWrapper<T> wrapper)
```

**示例:**
```java
Future<Integer> deleted = userDao.lambdaDelete(
    lambdaQuery().eq(User::getStatus, "INACTIVE")
);
```

## LambdaQueryWrapper类

### 构造函数

```java
public LambdaQueryWrapper(DSLContext dslContext, Table<?> table, Class<T> entityClass)
```

### 条件方法

#### 基础条件

| 方法 | 签名 | 说明 |
|------|------|------|
| `eq` | `<R> LambdaQueryWrapper<T> eq(SFunction<T, R> column, R value)` | 等于 |
| `ne` | `<R> LambdaQueryWrapper<T> ne(SFunction<T, R> column, R value)` | 不等于 |
| `gt` | `<R> LambdaQueryWrapper<T> gt(SFunction<T, R> column, R value)` | 大于 |
| `ge` | `<R> LambdaQueryWrapper<T> ge(SFunction<T, R> column, R value)` | 大于等于 |
| `lt` | `<R> LambdaQueryWrapper<T> lt(SFunction<T, R> column, R value)` | 小于 |
| `le` | `<R> LambdaQueryWrapper<T> le(SFunction<T, R> column, R value)` | 小于等于 |
| `like` | `<R> LambdaQueryWrapper<T> like(SFunction<T, R> column, R value)` | 模糊查询 |
| `notLike` | `<R> LambdaQueryWrapper<T> notLike(SFunction<T, R> column, R value)` | 不包含 |
| `in` | `<R> LambdaQueryWrapper<T> in(SFunction<T, R> column, Collection<R> values)` | 包含 |
| `notIn` | `<R> LambdaQueryWrapper<T> notIn(SFunction<T, R> column, Collection<R> values)` | 不包含 |
| `between` | `<R> LambdaQueryWrapper<T> between(SFunction<T, R> column, R value1, R value2)` | 范围查询 |
| `notBetween` | `<R> LambdaQueryWrapper<T> notBetween(SFunction<T, R> column, R value1, R value2)` | 不在范围 |
| `isNull` | `LambdaQueryWrapper<T> isNull(SFunction<T, ?> column)` | 为空 |
| `isNotNull` | `LambdaQueryWrapper<T> isNotNull(SFunction<T, ?> column)` | 不为空 |

#### 逻辑条件

| 方法 | 签名 | 说明 |
|------|------|------|
| `and` | `LambdaQueryWrapper<T> and(Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> condition)` | 且条件 |
| `or` | `LambdaQueryWrapper<T> or(Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> condition)` | 或条件 |

#### 排序和分页

| 方法 | 签名 | 说明 |
|------|------|------|
| `orderByAsc` | `LambdaQueryWrapper<T> orderByAsc(SFunction<T, ?> column)` | 升序排序 |
| `orderByDesc` | `LambdaQueryWrapper<T> orderByDesc(SFunction<T, ?> column)` | 降序排序 |
| `limit` | `LambdaQueryWrapper<T> limit(int count)` | 限制条数 |
| `offset` | `LambdaQueryWrapper<T> offset(int count)` | 偏移量 |

#### 字段选择

| 方法 | 签名 | 说明 |
|------|------|------|
| `select` | `LambdaQueryWrapper<T> select(SFunction<T, ?>... columns)` | 选择字段 |

### 构建方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `buildSelect` | `Query buildSelect()` | 构建SELECT查询 |
| `buildCount` | `Query buildCount()` | 构建COUNT查询 |
| `buildExists` | `Query buildExists()` | 构建EXISTS查询 |
| `buildCondition` | `Condition buildCondition()` | 构建WHERE条件 |

## LambdaPageResult类

### 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `records` | `List<T>` | 当前页记录列表 |
| `total` | `Long` | 总记录数 |
| `current` | `Long` | 当前页码 |
| `size` | `Long` | 每页大小 |

### 方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `getRecords` | `List<T> getRecords()` | 获取当前页记录 |
| `getTotal` | `Long getTotal()` | 获取总记录数 |
| `getCurrent` | `Long getCurrent()` | 获取当前页码 |
| `getSize` | `Long getSize()` | 获取每页大小 |
| `getPages` | `Long getPages()` | 获取总页数 |
| `hasNext` | `Boolean hasNext()` | 是否有下一页 |
| `hasPrevious` | `Boolean hasPrevious()` | 是否有上一页 |

## LambdaUtils类

### 方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `getFieldName` | `String getFieldName(SFunction<?, ?> func)` | 获取字段名 |
| `getField` | `<T, R> Field<R> getField(DSLContext dslContext, Table<?> table, SFunction<T, R> func)` | 获取jOOQ字段 |

## SFunction接口

```java
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
    // 继承Function和Serializable接口
}
```

**用途:** 用于Lambda表达式中的字段引用，支持序列化。

## 使用示例

### 基础查询

```java
// 根据状态查询用户
Future<List<User>> users = userDao.lambdaList(User::getStatus, "ACTIVE");

// 根据用户名查询单个用户
Future<Optional<User>> user = userDao.lambdaOne(User::getUsername, "alice");
```

完整的示例代码请参考：

- [LambdaQueryDemo.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/LambdaQueryDemo.java) - 快速入门示例
- [UserDao.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/UserDao.java) - 用户DAO示例
- [ProductDao.java](../src/main/java/cn/qaiu/db/dsl/lambda/example/ProductDao.java) - 产品DAO示例

### 复杂查询

```java
// 多条件查询
Future<List<User>> users = userDao.lambdaList(lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .gt(User::getAge, 18)
    .le(User::getBalance, 5000.0)
    .orderByDesc(User::getCreateTime)
    .limit(10));
```

### 嵌套条件

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
```

### 分页查询

```java
// 分页查询
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

### 更新和删除

```java
// 批量更新
User updateUser = new User();
updateUser.setStatus("UPDATED");

Future<Integer> updated = userDao.lambdaUpdate(
    lambdaQuery().in(User::getId, Arrays.asList(1L, 2L, 3L)),
    updateUser
);

// 批量删除
Future<Integer> deleted = userDao.lambdaDelete(
    lambdaQuery().eq(User::getStatus, "INACTIVE")
);
```
