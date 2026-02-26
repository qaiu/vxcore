# Code Generator

[中文](../12-code-generator.md) | English

VXCore code generator produces three-tier Java code from database tables or config: Entity, DAO, Service, Controller, DTO.

## Features

- **Entity** — with `@DdlTable` / `@DdlColumn` or similar
- **DAO** — Lambda style (recommended), Vert.x SQL style, or jOOQ style
- **Service** — interface + implementation
- **Controller** — REST endpoints
- **DTO** — request/response models

## Lambda-style DAO (recommended)

```java
public class UserDao extends LambdaDao<User> {
    public Future<List<User>> findActiveUsers() {
        return lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .orderBy(User::getCreateTime, SortOrder.DESC)
            .list();
    }
}
```

## Quick start

- Run the generator from DB connection or from a config file (table list, package names, output path).
- Generated code follows project conventions; adjust templates if needed.

## Related

- [No-arg constructor DAO](13-no-arg-constructor-dao.md)
- [Lambda query](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) (Chinese)
- [Overview](01-overview.md)
