# VXCore System Architecture

[中文](../04-architecture.md) | English

## Overview

VXCore uses a layered, reactive architecture: routing → controllers → services → DAO → jOOQ/DB. Built on Vert.x event loop and non-blocking I/O.

## Design: Simple yet elegant

- **Simple**: Low cognitive load, intuitive APIs
- **Elegant**: Powerful features and extensibility
- **Balanced**: No over- or under-engineering

### Principles

1. **Minimize cognitive burden** — Focus on business logic.
2. **Convention over configuration** — Sensible defaults.
3. **Type safety first** — Compile-time checks (jOOQ, annotations).
4. **Progressive complexity** — Start simple, add as needed.

### Example: controller

```java
@RouteHandler("/api")
public class UserController {
    @RouteMapping(value = "/users", method = HttpMethod.GET)
    public Future<JsonResult> getUsers(@RequestParam("page") int page) {
        return userService.findUsers(page).map(JsonResult::success);
    }
}
```

### Example: type-safe query

```java
return userDao.lambdaQuery()
    .eq(User::getStatus, "ACTIVE")
    .like(User::getName, "张%")
    .orderBy(User::getCreateTime, SortOrder.DESC)
    .list();
```

## Module layout

- **core**: Routing, annotations, DI (Dagger2), AOP, config
- **core-database**: jOOQ DSL, Lambda query, multi-datasource, batch
- **core-generator**: Code generation from DB schema
- **core-example**: Samples

## Request flow

1. HTTP request → Vert.x router
2. Router matches `@RouteHandler` + `@RouteMapping`
3. Parameter binding (`@RequestParam`, `@PathVariable`, `@RequestBody`)
4. Handler returns `Future<...>`
5. Response written as JSON (or as configured)

## Related

- [Overview](01-overview.md)
- [Quick start](02-quick-start.md)
- [Routing annotations](08-routing-annotations.md)
- [Configuration](10-configuration.md)
