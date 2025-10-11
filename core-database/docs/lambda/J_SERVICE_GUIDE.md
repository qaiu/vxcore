# JService 使用指南

## 概述

JService 是一个类似 MyBatis-Plus IService 的泛型服务接口，提供了完整的数据访问服务层功能。它基于 EnhancedDao 和 LambdaDao 实现，支持基础 CRUD、分页查询、Lambda 查询、聚合查询等功能。

## 核心特性

- **泛型支持**：基于实体类型自动确定表名和字段映射
- **基础 CRUD**：插入、更新、删除、查询等完整操作
- **分页查询**：支持 PageRequest 和 PageResult
- **Lambda 查询**：类型安全的字段引用，避免硬编码字段名
- **聚合查询**：支持 COUNT、SUM、AVG、MAX、MIN 等聚合函数
- **批量操作**：支持批量插入、更新、删除
- **UPSERT 操作**：支持插入或更新操作
- **异步支持**：基于 Vert.x Future 的异步操作

## 接口定义

### JService 接口

```java
public interface JService<T, ID> {
    // 基础 CRUD 方法
    Future<Optional<T>> save(T entity);
    Future<List<T>> saveBatch(List<T> entities);
    Future<Optional<T>> updateById(T entity);
    Future<Boolean> removeById(ID id);
    Future<Optional<T>> getById(ID id);
    Future<List<T>> list();
    
    // 条件查询方法
    Future<List<T>> list(Condition condition);
    Future<List<T>> list(QueryCondition queryCondition);
    Future<Optional<T>> getOne(Condition condition);
    Future<Long> count(Condition condition);
    Future<Boolean> exists(Condition condition);
    
    // 分页查询方法
    Future<PageResult> page(PageRequest pageRequest);
    Future<PageResult> page(PageRequest pageRequest, Condition condition);
    
    // Lambda 查询方法
    LambdaQueryWrapper<T> lambdaQuery();
    Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper);
    Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper);
    Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper);
    Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size);
    
    // 便捷查询方法
    <R> Future<List<T>> listByField(SFunction<T, R> column, R value);
    <R> Future<Optional<T>> getByField(SFunction<T, R> column, R value);
    <R> Future<Long> countByField(SFunction<T, R> column, R value);
    <R> Future<Boolean> existsByField(SFunction<T, R> column, R value);
    
    // UPSERT 操作方法
    Future<LambdaDao.UpsertResult<T>> saveOrUpdate(T entity);
    Future<LambdaDao.BatchUpsertResult<T>> saveOrUpdateBatch(List<T> entities);
}
```

### JServiceImpl 实现类

```java
public abstract class JServiceImpl<T, ID> extends LambdaDao<T, ID> implements JService<T, ID> {
    public JServiceImpl(JooqExecutor executor, Class<T> entityClass) {
        super(executor, entityClass);
    }
}
```

## 使用示例

### 1. 创建服务实现类

```java
@Service
public class UserServiceImpl extends JServiceImpl<User, Long> implements UserService {
    
    public UserServiceImpl(JooqExecutor jooqExecutor) {
        super(jooqExecutor, User.class);
    }
    
    // 可以添加自定义业务方法
    public Future<List<User>> findActiveUsers() {
        return lambdaList(lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderByDesc(User::getCreateTime));
    }
}
```

### 2. 基础 CRUD 操作

```java
// 插入
User user = new User();
user.setName("张三");
user.setEmail("zhangsan@example.com");
Future<Optional<User>> savedUser = userService.save(user);

// 更新
user.setName("李四");
Future<Optional<User>> updatedUser = userService.updateById(user);

// 删除
Future<Boolean> deleted = userService.removeById(1L);

// 查询
Future<Optional<User>> user = userService.getById(1L);
Future<List<User>> allUsers = userService.list();
```

### 3. 条件查询

```java
// 使用 Condition
Condition condition = DSL.field("status").eq("ACTIVE");
Future<List<User>> activeUsers = userService.list(condition);

// 使用 QueryCondition
QueryCondition queryCondition = new QueryCondition("status", QueryCondition.ConditionType.EQUAL, "ACTIVE");
Future<List<User>> activeUsers2 = userService.list(queryCondition);

// 使用多个条件
List<QueryCondition> conditions = Arrays.asList(
    new QueryCondition("status", QueryCondition.ConditionType.EQUAL, "ACTIVE"),
    new QueryCondition("age", QueryCondition.ConditionType.GREATER_THAN, 18)
);
Future<List<User>> adultActiveUsers = userService.list(conditions);
```

### 4. Lambda 查询

```java
// 基础 Lambda 查询
Future<List<User>> users = userService.lambdaList(
    userService.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .like(User::getName, "张")
        .orderByDesc(User::getCreateTime)
        .limit(10)
);

// 分页查询
Future<LambdaPageResult<User>> pageResult = userService.lambdaPage(
    userService.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .orderByDesc(User::getCreateTime),
    1, 10
);

// 统计查询
Future<Long> count = userService.lambdaCount(
    userService.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .gt(User::getAge, 18)
);
```

### 5. 便捷查询方法

```java
// 根据字段查询
Future<List<User>> users = userService.listByField(User::getStatus, "ACTIVE");
Future<Optional<User>> user = userService.getByField(User::getEmail, "zhangsan@example.com");
Future<Long> count = userService.countByField(User::getStatus, "ACTIVE");
Future<Boolean> exists = userService.existsByField(User::getEmail, "zhangsan@example.com");
```

### 6. 分页查询

```java
// 基础分页
PageRequest pageRequest = new PageRequest(1, 10);
Future<PageResult> pageResult = userService.page(pageRequest);

// 条件分页
PageRequest pageRequest2 = new PageRequest(1, 10, "createTime", "DESC");
Future<PageResult> pageResult2 = userService.page(pageRequest2, 
    userService.lambdaQuery().eq(User::getStatus, "ACTIVE").buildCondition());
```

### 7. 批量操作

```java
// 批量插入
List<User> users = Arrays.asList(user1, user2, user3);
Future<List<User>> savedUsers = userService.saveBatch(users);

// 批量更新
Future<List<User>> updatedUsers = userService.updateBatchById(users);

// 批量删除
List<Long> ids = Arrays.asList(1L, 2L, 3L);
Future<Integer> deletedCount = userService.removeByIds(ids);
```

### 8. UPSERT 操作

```java
// 插入或更新
User user = new User();
user.setEmail("zhangsan@example.com");
user.setName("张三");
Future<LambdaDao.UpsertResult<User>> result = userService.saveOrUpdate(user);

// 批量插入或更新
List<User> users = Arrays.asList(user1, user2, user3);
Future<LambdaDao.BatchUpsertResult<User>> batchResult = userService.saveOrUpdateBatch(users);

// 检查结果
result.onSuccess(upsertResult -> {
    if (upsertResult.isInserted()) {
        System.out.println("插入成功: " + upsertResult.getEntity());
    } else {
        System.out.println("更新成功: " + upsertResult.getEntity());
    }
});
```

### 9. 聚合查询

```java
// 统计查询
Future<Long> totalCount = userService.count();
Future<Long> activeCount = userService.count(
    userService.lambdaQuery().eq(User::getStatus, "ACTIVE").buildCondition()
);

// 聚合函数
Future<Optional<Object>> maxAge = userService.max("age");
Future<Optional<Object>> minAge = userService.min("age");
Future<Optional<Object>> avgAge = userService.avg("age");
Future<Optional<Object>> sumScore = userService.sum("score");
```

## Lambda 查询包装器

LambdaQueryWrapper 提供了丰富的查询条件构建方法：

### 条件方法

```java
// 等于
.eq(User::getName, "张三")

// 不等于
.ne(User::getStatus, "DELETED")

// 大于
.gt(User::getAge, 18)

// 大于等于
.ge(User::getScore, 60)

// 小于
.lt(User::getAge, 65)

// 小于等于
.le(User::getScore, 100)

// 模糊查询
.like(User::getName, "张")
.likeLeft(User::getName, "三")
.likeRight(User::getName, "张")

// IN 查询
.in(User::getStatus, Arrays.asList("ACTIVE", "PENDING"))

// 为空/不为空
.isNull(User::getDeletedAt)
.isNotNull(User::getUpdatedAt)

// BETWEEN 查询
.between(User::getAge, 18, 65)

// 嵌套条件
.and(wrapper -> wrapper.eq(User::getStatus, "ACTIVE").gt(User::getAge, 18))
.or(wrapper -> wrapper.eq(User::getRole, "ADMIN").eq(User::getRole, "SUPER_ADMIN"))
```

### 排序方法

```java
// 升序排序
.orderByAsc(User::getCreateTime)

// 降序排序
.orderByDesc(User::getCreateTime)

// 多字段排序
.orderByDesc(User::getCreateTime)
.orderByAsc(User::getName)
```

### 分页方法

```java
// 限制数量
.limit(10)

// 偏移量
.offset(20)

// 分页
.page(1, 10)  // 第1页，每页10条
```

## 最佳实践

### 1. 服务层设计

```java
@Service
public class UserServiceImpl extends JServiceImpl<User, Long> implements UserService {
    
    public UserServiceImpl(JooqExecutor jooqExecutor) {
        super(jooqExecutor, User.class);
    }
    
    // 业务方法
    public Future<List<User>> findActiveUsers() {
        return lambdaList(lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderByDesc(User::getCreateTime));
    }
    
    public Future<LambdaPageResult<User>> findUsersByPage(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = lambdaQuery()
            .eq(User::getStatus, "ACTIVE");
            
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(User::getName, keyword);
        }
        
        return lambdaPage(wrapper.orderByDesc(User::getCreateTime), page, size);
    }
}
```

### 2. 控制器使用

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public Future<ResponseEntity<List<User>>> listUsers() {
        return userService.findActiveUsers()
            .map(users -> ResponseEntity.ok(users));
    }
    
    @GetMapping("/page")
    public Future<ResponseEntity<LambdaPageResult<User>>> pageUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return userService.findUsersByPage(page, size, keyword)
            .map(result -> ResponseEntity.ok(result));
    }
}
```

### 3. 错误处理

```java
public Future<List<User>> findUsersSafely() {
    return userService.list()
        .recover(throwable -> {
            LOGGER.error("查询用户失败", throwable);
            return Future.succeededFuture(new ArrayList<>());
        });
}
```

## 注意事项

1. **实体类要求**：实体类需要有无参构造函数，字段需要有对应的 getter/setter 方法
2. **主键字段**：实体类需要有主键字段，默认为 `id` 字段
3. **异步操作**：所有方法都返回 `Future`，需要使用 `onSuccess`、`onFailure` 或 `compose` 处理结果
4. **事务管理**：批量操作和 UPSERT 操作建议在事务中执行
5. **性能优化**：大量数据操作时建议使用批量方法，并设置合适的批次大小

## 扩展功能

JService 支持以下扩展功能：

- **自定义查询**：继承 JServiceImpl 并添加自定义查询方法
- **缓存集成**：在服务层添加缓存逻辑
- **事件发布**：在数据操作后发布领域事件
- **审计日志**：记录数据变更日志
- **权限控制**：根据用户权限过滤查询结果

通过 JService，开发者可以快速构建功能完整的数据访问服务层，提高开发效率并保持代码的一致性。
