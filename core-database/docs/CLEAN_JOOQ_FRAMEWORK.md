# 清晰的 jOOQ DSL 框架架构

## 🎯 当前问题

当前的代码中有很多 jOOQ 类型转换和泛型问题，不够清晰。让我重新设计一个更简洁的架构。

## 📋 新的清晰的框架结构

```
cn.qaiu.db.dsl/
├── core/                           # 核心框架
│   ├── JooqSqlBuilder.java        # SQL 构建器
│   ├── JooqExecutor.java          Java 执行器
│   └── AbstractDao.java           # 抽象 DAO（简化版）
├── interfaces/                     # 接口定义
│   ├── DaoInterface.java          # DAO 接口
│   └── QueryBuilder.java          # 查询构建器接口
├── annotations/                    # 注解
│   └── JooqTable.java            # 表注解
├── mapper/                         # 映射器
│   ├── EntityMapper.java         # 实体映射器接口
│   └── DefaultMapper.java        # 默认实现
└── example/                        # 示例
    ├── User.java                 # 用户实体（使用注解）
    └── UserDao.java              # 用户 DAO
```

## 🔧 关键设计原则

### 1. **统一接口**
```java
public interface JooqDao<T, ID> {
    Future<T> insert(T entity);
    Future<T> update(T entity);
    Future<Boolean> delete(ID id);
    Future<Optional<T>> findById(ID id);
    Future<List<T>> findAll();
    Future<List<T>> findByCondition(Condition condition);
}
```

### 2. **简化的 SQL 构建**
```java
public class JooqSqlBuilder {
    public String buildInsert(Table<?> table, JsonObject data);
    public String buildUpdate(Table<?> table, JsonObject data, Condition where);
    public String buildSelect(Table<?> table, Condition condition);
    public String buildDelete(Table<?> table, Condition where);
}
```

### 3. **注解驱动**
```java
@JooqTable(name = "users", primaryKey = "id")
public class User extends BaseEntity {
    @JooqColumn("username")
    private String username;
    
    @JooqColumn("email")
    private String email;
}
```

## 🚀 实现计划

1. ✅ **创建核心执行器**
2. ⏳ **创建简化的 SQL 构建器**
3. ⏳ **创建注解驱动的抽象 DAO**
4. ⏳ **重写示例类**
5. ⏳ **创建测试**

## 📝 使用示例

```java
// 1. 创建实体
@JooqTable(name = "users")
public class User extends BaseEntity {
    @JooqColumn private String username;
    @JooqColumn private String email;
}

// 2. 创建 DAO
public class UserDao extends AbstractJooqDao<User, Long> {
    public UserDao(JooqExecutor executor) {
        super(executor, User.class);
    }
    
    // 自定义查询
    public Future<List<User>> findActiveUsers() {
        return findByCondition(dsl().field("status").eq("ACTIVE"));
    }
}

// 3. 使用
UserDao userDao = new UserDao(executor);
userDao.create(user)
    .compose(savedUser -> userDao.findById(savedUser.getId()))
    .onSuccess(userOpt -> {
        // 处理结果
    });
```

## 🎯 优势

- **清晰简洁**：避免复杂的泛型和类型转换
- **注解驱动**：减少样板代码
- **统一接口**：所有 DAO 使用相同的 API
- **真正 jOOQ**：使用 jOOQ DSL 构建查询
- **异步支持**：完全基于 Vert.x Future

这样的设计会更加清晰和易用！
