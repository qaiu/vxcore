# Lambda查询指南

## 概述

VXCore数据库模块提供了类似MyBatis-Plus的Lambda查询功能，支持类型安全的字段引用和流式查询构建。

## 核心特性

- **类型安全**: 使用Lambda表达式引用实体字段，编译时检查
- **流式API**: `lambdaQuery().eq().like().orderByDesc().list()` 一行完成查询
- **字段映射**: 自动处理Java字段名（驼峰）与数据库字段名（下划线）的转换
- **注解支持**: 支持`@DdlColumn`注解自定义字段映射
- **条件式构建**: 支持 `.eq(condition, column, value)` 动态条件拼接
- **无参构造DAO**: 继承`LambdaDao`即可，框架自动初始化
- **性能优化**: 字段名缓存，避免重复反射解析

## 快速开始

### 1. 实体类定义

```java
@DdlTable("users")
public class User extends BaseEntity {

    @DdlColumn(value = "user_name", type = "VARCHAR(100)", nullable = false)
    private String name;

    @DdlColumn(value = "email", type = "VARCHAR(200)")
    private String email;

    @DdlColumn(value = "age", type = "INT")
    private Integer age;

    @DdlColumn(value = "status", type = "VARCHAR(20)", defaultValue = "ACTIVE")
    private String status = "ACTIVE";

    // BaseEntity 提供 id, createTime, updateTime, createBy, updateBy
    // getter/setter方法...
}
```

### 2. DAO类定义（推荐：无参构造 + LambdaDao）

```java
@Dao
public class UserDao extends LambdaDao<User, Long> {

    @Override
    protected <R> SFunction<User, R> getPrimaryKeyFieldLambda() {
        @SuppressWarnings("unchecked")
        SFunction<User, R> fn = (SFunction<User, R>) (SFunction<User, Long>) User::getId;
        return fn;
    }
}
```

继承 `LambdaDao<T, ID>` 即可获得：
- **CRUD方法**: `insert()` / `findById()` / `findAll()` / `update()` / `delete()`（继承自 `EnhancedDao`）
- **流式查询**: `lambdaQuery()` 返回 `LambdaQueryWrapper`，支持 `.list()` / `.one()` / `.count()` 终端操作
- **传统查询**: `lambdaList(wrapper)` / `lambdaOne(wrapper)` / `lambdaCount(wrapper)`
- **高级操作**: `insertOrUpdate()` / `batchInsertOrUpdate()` / `lambdaDelete()` / `lambdaUpdate()`

### 3. 基础查询（流式API — 推荐）

```java
// 等值查询
Future<List<User>> users = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .list();

// 范围查询
Future<List<User>> users = userDao.lambdaQuery()
    .ge(User::getAge, 18)
    .le(User::getAge, 60)
    .list();

// 模糊查询
Future<List<User>> users = userDao.lambdaQuery()
    .like(User::getName, "张")
    .list();

// IN查询
Future<List<User>> users = userDao.lambdaQuery()
    .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))
    .list();

// 单个结果查询
Future<User> user = userDao.lambdaQuery()
    .eq(User::getEmail, "alice@test.com")
    .one();

// 计数查询
Future<Long> count = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .count();
```

### 4. 复杂查询

```java
// 多条件组合 + 排序 + 分页
Future<List<User>> users = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .ge(User::getAge, 18)
    .like(User::getName, "张")
    .orderByDesc(User::getId)
    .orderByAsc(User::getName)
    .limit(10)
    .list();

// 条件式查询 — 参数为 null 时自动跳过该条件
Future<List<User>> search(String name, Integer minAge, String status) {
    return userDao.lambdaQuery()
        .like(name != null, User::getName, name)
        .ge(minAge != null, User::getAge, minAge)
        .eq(status != null, User::getStatus, status)
        .orderByDesc(User::getId)
        .list();
}

// 分页查询
Future<List<User>> users = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .executePage(1, 20);  // 第1页，每页20条
```

### 5. 传统风格（兼容）

如果偏好将查询构建与执行分离，可以使用 `LambdaDao` 上的方法：

```java
// 传统风格：构建 wrapper，传入 dao 方法执行
LambdaQueryWrapper<User> wrapper = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .ge(User::getAge, 18);

Future<List<User>> users = userDao.lambdaList(wrapper);
Future<Optional<User>> user = userDao.lambdaOne(wrapper);
Future<Long> count = userDao.lambdaCount(wrapper);
Future<Boolean> exists = userDao.lambdaExists(wrapper);

// 便捷方法：单字段等值查询
Future<List<User>> activeUsers = userDao.lambdaList(User::getStatus, "ACTIVE");
Future<Optional<User>> user = userDao.lambdaOne(User::getEmail, "alice@test.com");
```

## 字段映射

### 默认映射规则

- Java字段名（驼峰）自动转换为数据库字段名（下划线）
- `productName` → `product_name`
- `categoryId` → `category_id`
- `isActive` → `is_active`

### 注解映射

```java
public class Product {
    
    // 使用value字段指定数据库字段名
    @DdlColumn("product_id")
    private Long id;
    
    // 使用name字段指定数据库字段名（value的别名）
    @DdlColumn(name = "product_name")
    private String name;
    
    // 默认映射：categoryId → category_id
    private Long categoryId;
}
```

### LambdaUtils工具类

```java
// 获取字段名
String fieldName = LambdaUtils.getFieldName(Product::getName);
// 返回: "product_name"

// 获取字段类型
Class<?> fieldType = LambdaUtils.getFieldType(Product::getName);
// 返回: String.class
```

## 查询条件详解

### 比较条件

```java
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")      // 等于
    .ne(User::getStatus, "DELETED")      // 不等于
    .gt(User::getAge, 18)               // 大于
    .ge(User::getAge, 18)               // 大于等于
    .lt(User::getAge, 60)               // 小于
    .le(User::getAge, 60)               // 小于等于
    .list();
```

每个条件方法都支持条件式变体，第一个参数为 `boolean`：

```java
userDao.lambdaQuery()
    .eq(name != null, User::getName, name)       // name 不为 null 时才添加条件
    .ge(minAge != null, User::getAge, minAge)     // minAge 不为 null 时才添加条件
    .list();
```

### 范围条件

```java
userDao.lambdaQuery()
    .in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))  // IN
    .notIn(User::getId, Arrays.asList(999L, 1000L))           // NOT IN
    .between(User::getAge, 18, 60)                             // BETWEEN
    .notBetween(User::getAge, 0, 12)                           // NOT BETWEEN
    .list();
```

### 模糊查询

```java
userDao.lambdaQuery()
    .like(User::getName, "张")           // LIKE '%张%'（自动加 %）
    .likeLeft(User::getName, "明")       // LIKE '%明'
    .likeRight(User::getName, "张")      // LIKE '张%'
    .notLike(User::getName, "test")      // NOT LIKE '%test%'
    .list();
```

### 空值查询

```java
userDao.lambdaQuery()
    .isNull(User::getEmail)              // IS NULL
    .isNotNull(User::getName)            // IS NOT NULL
    .list();
```

### 嵌套条件

```java
// AND 嵌套：WHERE status = 'ACTIVE' AND (age >= 18 AND age <= 60)
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .and(w -> w.ge(User::getAge, 18).le(User::getAge, 60))
    .list();

// OR 嵌套：WHERE status = 'ACTIVE' OR (status = 'PENDING' AND age > 30)
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .or(w -> w.eq(User::getStatus, "PENDING").gt(User::getAge, 30))
    .list();
```

### 排序

```java
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .orderByDesc(User::getId)            // 降序
    .orderByAsc(User::getName)           // 升序（支持多字段排序）
    .list();
```

### 分页

```java
// 方式一：limit + offset
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .limit(10)
    .offset(20)
    .list();

// 方式二：executePage（推荐）
userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .executePage(2, 10);    // 第2页，每页10条

// 方式三：lambdaPage（包含总数统计）
userDao.lambdaPage(
    userDao.lambdaQuery().eq(User::getStatus, "ACTIVE"),
    1, 20  // 第1页，每页20条
);
// 返回 LambdaPageResult: records + total + current + size
```

### 字段选择

```java
userDao.lambdaQuery()
    .select(User::getName, User::getEmail)  // 只查询指定字段
    .eq(User::getStatus, "ACTIVE")
    .list();
```

## 高级功能

### UPSERT（插入或更新）

```java
// 单条 UPSERT — 根据主键判断插入或更新
Future<UpsertResult<User>> result = userDao.insertOrUpdate(user);
result.onSuccess(r -> {
    if (r.isInserted()) { /* 新插入 */ }
    else { /* 已更新 */ }
});

// 批量 UPSERT
Future<BatchUpsertResult<User>> result = userDao.batchInsertOrUpdate(userList);
result.onSuccess(r -> {
    System.out.println("插入: " + r.getInsertCount() + ", 更新: " + r.getUpdateCount());
});
```

### 条件式删除和更新

```java
// 按条件删除（必须带条件，防止误删全表）
Future<Integer> deleted = userDao.lambdaDelete(
    userDao.lambdaQuery().eq(User::getStatus, "INACTIVE")
);

// 按条件更新（必须带条件）
User updateEntity = new User();
updateEntity.setStatus("INACTIVE");
Future<Integer> updated = userDao.lambdaUpdate(
    userDao.lambdaQuery().lt(User::getAge, 18),
    updateEntity
);
```

### 子查询

```java
// EXISTS 子查询
Future<List<Order>> orders = orderDao.lambdaQuery()
    .exists(User.class, sub -> sub.eq(User::getStatus, "VIP"))
    .list();

// IN 子查询
Future<List<Order>> orders = orderDao.lambdaQuery()
    .inSubQuery(Order::getUserId, User.class, sub -> sub.eq(User::getStatus, "VIP"))
    .list();
```

### 聚合函数

```java
// 选择聚合字段
Future<List<User>> result = userDao.lambdaQuery()
    .select(User::getStatus)
    .selectCount()
    .groupBy(User::getStatus)
    .having(agg -> agg.countGt(5))
    .list();

// 可用聚合选择：selectCount() / selectSum() / selectAvg() / selectMax() / selectMin()
```

### 连接查询

```java
// 左连接
userDao.lambdaQuery()
    .leftJoin(Department.class, (user, dept) -> /* join condition */)
    .eq(User::getStatus, "ACTIVE")
    .list();

// 支持: innerJoin / leftJoin / rightJoin / fullJoin
```

## 性能优化

### 字段名缓存

LambdaUtils自动缓存字段名解析结果，避免重复反射操作：

```java
// 第一次调用会进行反射解析并缓存
String fieldName1 = LambdaUtils.getFieldName(User::getName);

// 后续调用直接返回缓存结果
String fieldName2 = LambdaUtils.getFieldName(User::getName);
```

### 查询优化建议

1. **合理使用索引**: 确保查询条件涉及的字段有索引
2. **避免全表扫描**: 使用合适的WHERE条件
3. **分页查询**: 大数据量查询使用 `limit()` / `executePage()`
4. **字段选择**: 使用 `select()` 只查询需要的字段
5. **条件式构建**: 使用 `.eq(condition, column, value)` 避免拼接空条件

```java
// 好的做法：使用索引字段 + 分页
Future<List<User>> users = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .orderByDesc(User::getId)
    .limit(10)
    .list();

// 好的做法：只查需要的字段
Future<List<User>> users = userDao.lambdaQuery()
    .select(User::getName, User::getEmail)
    .eq(User::getStatus, "ACTIVE")
    .list();

// 避免的做法：无条件全表模糊查询
Future<List<User>> users = userDao.lambdaQuery()
    .like(User::getName, "test")  // 全表 LIKE 扫描
    .list();
```

## 错误处理

### 常见错误

1. **字段不存在**: 确保Lambda表达式引用的getter方法在实体类中存在
2. **类型不匹配**: 确保查询条件的值与字段类型匹配
3. **注解错误**: 确保`@DdlColumn`注解的字段名正确
4. **executor为null**: 流式终端方法（`.list()` / `.one()` / `.count()`）需要 `LambdaQueryWrapper` 持有 `JooqExecutor`，通过 `LambdaDao.lambdaQuery()` 创建的 wrapper 会自动注入

### 调试技巧

```java
// 打印生成的SQL
LambdaQueryWrapper<User> wrapper = userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .orderByDesc(User::getId);

String sql = wrapper.buildSelect().getSQL();
System.out.println("Generated SQL: " + sql);
```

## 最佳实践

1. **优先使用流式API**: `lambdaQuery().eq().list()` 比传统 `lambdaList(wrapper)` 更简洁
2. **善用条件式构建**: `.eq(param != null, User::getField, param)` 避免手动 if 判断
3. **继承LambdaDao**: 获得完整的 CRUD + Lambda 查询能力
4. **重写getPrimaryKeyFieldLambda()**: 支持 `insertOrUpdate()` 等高级操作
5. **利用类型安全**: 字段变更时编译器会自动报错，避免运行时错误

## API 速查表

### LambdaQueryWrapper 条件方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `eq` | 等于 | `.eq(User::getStatus, "ACTIVE")` |
| `ne` | 不等于 | `.ne(User::getStatus, "DELETED")` |
| `gt` / `ge` | 大于 / 大于等于 | `.ge(User::getAge, 18)` |
| `lt` / `le` | 小于 / 小于等于 | `.lt(User::getAge, 60)` |
| `like` | 模糊匹配（自动加 %） | `.like(User::getName, "张")` |
| `likeLeft` / `likeRight` | 左/右模糊 | `.likeRight(User::getName, "张")` |
| `notLike` | 不包含 | `.notLike(User::getName, "test")` |
| `in` | IN 查询 | `.in(User::getStatus, list)` |
| `notIn` | NOT IN | `.notIn(User::getId, list)` |
| `between` | 范围 | `.between(User::getAge, 18, 60)` |
| `isNull` / `isNotNull` | 空值判断 | `.isNotNull(User::getEmail)` |
| `and` / `or` | 嵌套条件 | `.and(w -> w.eq(...).gt(...))` |

### LambdaQueryWrapper 终端方法（流式API）

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `list()` | `Future<List<T>>` | 查询列表 |
| `one()` | `Future<T>` | 查询单条 |
| `count()` | `Future<Long>` | 查询数量 |
| `executePage(current, size)` | `Future<List<T>>` | 分页查询 |

### LambdaDao 方法

| 方法 | 说明 |
|------|------|
| `lambdaQuery()` | 创建查询包装器 |
| `lambdaList(wrapper)` | 传统风格：执行查询返回列表 |
| `lambdaOne(wrapper)` | 传统风格：查询单条 |
| `lambdaCount(wrapper)` | 传统风格：查询数量 |
| `lambdaExists(wrapper)` | 检查是否存在 |
| `lambdaDelete(wrapper)` | 按条件删除 |
| `lambdaUpdate(wrapper, entity)` | 按条件更新 |
| `lambdaPage(wrapper, current, size)` | 分页查询（含总数） |
| `insertOrUpdate(entity)` | UPSERT 操作 |
| `batchInsertOrUpdate(list)` | 批量 UPSERT |

## 示例项目

完整示例请参考：
- **demo-13-database** — 完整的数据库功能验证模块（CRUD + Lambda 查询 + REST API）
  - `vxcore-demo/demo-13-database/src/main/java/cn/qaiu/demo/database/dao/UserDao.java`
  - `vxcore-demo/demo-13-database/src/main/java/cn/qaiu/demo/database/service/UserService.java`
- `core-database/src/test/java/cn/qaiu/db/dsl/lambda/SimpleLambdaTest.java`
- `core-database/src/test/java/cn/qaiu/db/dsl/lambda/LambdaQueryUnitTest.java`
- `core-database/src/main/java/cn/qaiu/db/dsl/lambda/example/Product.java`
- `core-database/src/main/java/cn/qaiu/db/dsl/lambda/example/ProductDao.java`
