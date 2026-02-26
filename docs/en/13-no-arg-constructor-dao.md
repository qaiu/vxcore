# No-arg Constructor DAO

[中文](../13-no-arg-constructor-dao.md) | English

VXCore supports **no-arg constructor DAOs**: the framework initializes the DAO (executor, entity type) so you can use an empty class.

## Recommended usage

```java
public class UserDao extends AbstractDao<User, Long> {
    // Empty class — no constructor; framework infers User from generics
    // and obtains JooqExecutor from DataSourceManager.
}

// Use
UserDao userDao = new UserDao();
Future<List<User>> users = userDao.lambdaQuery().eq(User::getStatus, "ACTIVE").list();
```

## Other styles

- **Explicit executor**: `public UserDao(JooqExecutor executor) { super(executor, User.class); }`
- **Executor from manager**: `public UserDao() { super(User.class); }` — executor obtained from default datasource

No-arg (fully empty) is the simplest and recommended for most cases.

## Related

- [Quick start](02-quick-start.md)
- [Code generator](12-code-generator.md)
- [Lambda query](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) (Chinese)
- [Overview](01-overview.md)
