# VXCore (Weike) — Project Overview

[中文](../01-overview.md) | English

## Introduction

**VXCore (Weike)** is lightweight-first: **JSON API only**, core artifact **under 30MB**, no feature bloat. It is a modern Java Web framework based on **Vert.x** and **jOOQ**, with a Spring Boot–like developer experience: code generator, Lambda queries, multi-datasource, WebSocket, reverse proxy. Positioned like Quarkus: lightweight, cloud-native, high-performance, developer experience first.

### Core value

- **Lightweight**: Core &lt; 30MB, JSON API focused
- **High performance**: Vert.x async non-blocking I/O, tens of thousands of concurrent connections
- **Type-safe**: jOOQ DSL compile-time checks, no SQL injection
- **Web**: Annotation routing, WebSocket, reverse proxy
- **Multi-datasource**: Dynamic switch and transaction isolation
- **Extensible**: Lambda query, batch ops, SPI

## Architecture (high level)

VXCore uses a layered design on top of a reactive, event-loop model: Router → Controller → Service → DAO → jOOQ/DB. See [System architecture](04-architecture.md) for details.

## Design: Simple yet elegant

- **Simple**: Low learning curve, intuitive APIs
- **Elegant**: Strong features and extensibility when needed
- **Balanced**: No over- or under-engineering

### Principles

1. **Minimize cognitive load** — Focus on business logic, not framework details.
2. **Convention over configuration** — Sensible defaults, less config.
3. **Type safety first** — Catch errors at compile time.
4. **Progressive complexity** — Start simple, add as needed.

## Reactive model (Vert.x)

VXCore is event-driven and non-blocking. Event Loop threads run callbacks; I/O is registered and returns immediately, so threads never block on I/O.

### Verticle

Each **Verticle** runs on a single Event Loop — no shared mutable state, no locks.

### Future composition

Use **Future** for chaining and composition (serial and parallel), e.g. `compose()`, `Future.all()`, `recover()`.

## Type-safe database (jOOQ)

Lambda-style API with compile-time checks:

```java
public Future<List<User>> findActiveUsers() {
    return userDao.lambdaQuery()
        .eq(User::getStatus, "ACTIVE")
        .like(User::getName, "张%")
        .orderBy(User::getCreateTime, SortOrder.DESC)
        .list();
}
```

## Annotation-driven development

Spring MVC–style routing and exception handling:

```java
@RouteHandler("/api")
public class UserController {
    @RouteMapping(value = "/users", method = HttpMethod.GET)
    public Future<JsonResult> getUsers(@RequestParam("page") int page) {
        return userService.findUsers(page).map(JsonResult::success);
    }
    @ExceptionHandler(ValidationException.class)
    public JsonResult handleValidation(ValidationException e) {
        return JsonResult.fail(400, e.getMessage());
    }
}
```

## Tech stack

- **Java 17+**, **Vert.x 4.5+**, **jOOQ 3.19+**, **Maven 3.8+**
- **DB**: H2, MySQL, PostgreSQL

## Performance

| Aspect | Spring MVC (Tomcat) | VXCore (Vert.x) |
|--------|---------------------|-----------------|
| Concurrency | Thread-per-request, blocking | Event loop, non-blocking |
| Memory per connection | ~1 MB | Few KB |
| 10k concurrent memory | ~10 GB | Hundreds of MB |
| Throughput | Thread-pool bound | Near I/O limit |
| Latency | Thread scheduling | Microsecond-scale |

Metrics: 50,000+ QPS HTTP, 10,000+ WebSocket connections, 10,000+ DB QPS, 1000 rows &lt; 100ms batch.

## Quick start

```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
mvn clean compile
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
# curl http://localhost:8080/api/hello?name=VXCore
```

## Learning path

- **Getting started**: [Quick start](02-quick-start.md), [Installation](03-installation.md), [No-arg DAO](13-no-arg-constructor-dao.md)
- **Next**: [Lambda query](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md), [Multi-datasource](../core-database/docs/MULTI_DATASOURCE_GUIDE.md), [Routing](08-routing-annotations.md), [Exception handling](09-exception-handling.md)
- **Advanced**: [WebSocket](WEBSOCKET_GUIDE.md), [Proxy](WEBSOCKET_PROXY_GUIDE.md), [Configuration](10-configuration.md)

## Help

- [GitHub Issues](https://github.com/qaiu/vxcore/issues)
- [Discussions](https://github.com/qaiu/vxcore/discussions)
- Email: qaiu@qq.com

---

**VXCore (Weike) — Lightweight JSON API framework.** [Quick start](02-quick-start.md) | [Installation](03-installation.md) | [Docs index](README.md)
