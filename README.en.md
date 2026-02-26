# VXCore (Weike)

[中文](README.md) | [English Documentation](docs/en/README.md)

A **lightweight** Vert.x-based Java framework focused on **JSON API services**, with core artifact **under 30MB**. Developer experience similar to Spring Boot; integrates code generator, jOOQ DSL, WebSocket, reverse proxy. Positioned like Quarkus: cloud-native, high-performance, developer-friendly.

## Introduction

**VXCore (Weike)** is lightweight-first: **JSON API only**, core size under 30MB, no feature bloat. It is a high-performance, reactive Java framework for building modern Web APIs, combining Vert.x's async model, jOOQ type-safe database access, and rich annotations for a simple yet powerful experience.

### Design: Simple yet elegant

- **Simple**: Low learning curve, intuitive APIs, quick start
- **Elegant**: Strong features and extensibility when needed
- **Balanced**: Neither over- nor under-engineered

## What's new

### v1.2.3 (2026-02)
- **Version bump**: 1.2.2 → 1.2.3

### v1.2.2 (2026-02)
- **Dependency fixes**: Vert.x 4.5.25 (Netty 4.1.130.Final, CVE-2025-67735), Jackson 2.18.3, Logback 1.5.17, PostgreSQL 42.7.10
- **Version management**: Single parent POM for all modules
- **AOP proxy fix**: ServiceRegistry applies AOP proxies correctly
- **JaCoCo**: Coverage reports for core and core-database

### v1.2.0 (2026-02)
- **DI fix**: ServiceRegistry type lookup, interface and concrete injection
- **AOP**: @Aspect/@Before/@After with Byte Buddy
- **Exception handling**: Global handler, unified JSON errors
- **Config**: YAML loading, SharedData
- **Routing**: Path variables, @RequestBody binding
- **vxcore-demo**: 7 submodules validating docs

See [vxcore-demo/ISSUES.md](vxcore-demo/ISSUES.md) for details.

## Core features

### High-performance async stack
- **Vert.x 4.5+**: Event-driven, non-blocking I/O; Netty underneath
- **Verticle model**: Lightweight concurrency units on event loop
- **Future/Promise**: Composable async; chaining, parallel aggregation, recovery
- **Throughput**: 50,000+ QPS, tens of thousands of WebSocket connections per JVM
- **Memory**: Low footprint; zero-copy I/O, connection pools

### Type-safe database (jOOQ)
- **jOOQ DSL**: Compile-time checks, no SQL injection
- **Lambda queries**: MyBatis-Plus–style Lambda API
- **No-arg DAO**: Auto-initialized, no manual wiring
- **Multi-datasource**: Dynamic switch and transaction isolation
- **Batch**: High-performance batch CRUD

### Web & JSON API
- **Annotation routing**: @RouteMapping (Spring MVC–like)
- **Parameter binding**: Overloads, type conversion, custom converters
- **Exception handling**: Global and per-handler
- **WebSocket**: Annotation-based routes and proxy

### Extras
- **Code generator**: From DB schema to three-tier code
- **Reverse proxy**: HTTP/WebSocket (Nginx-like)
- **Config**: YAML with IDE hints
- **SPI**: Custom drivers and extensions

### Modules
- **core**: Routing, annotations, DI, AOP, config
- **core-database**: DSL, Lambda, multi-datasource
- **core-generator**: Code generator (optional)
- **core-example**: Examples
- **vxcore-demo**: Doc validation (7 submodules)

## Quick start

### Requirements
- Java 17+, Maven 3.8+
- DB (optional): H2 / MySQL / PostgreSQL

### Maven

```xml
<properties>
    <vxcore.version>1.2.3</vxcore.version>
</properties>
<dependencies>
    <dependency>
        <groupId>cn.qaiu</groupId>
        <artifactId>core</artifactId>
        <version>${vxcore.version}</version>
    </dependency>
    <dependency>
        <groupId>cn.qaiu</groupId>
        <artifactId>core-database</artifactId>
        <version>${vxcore.version}</version>
    </dependency>
</dependencies>
```

### 5-minute example

```java
@RouteHandler("/api")
public class UserController {

    @RouteMapping(value = "/hello", method = RouteMethod.GET)
    public Future<JsonResult<String>> hello(@RequestParam("name") String name) {
        return Future.succeededFuture(JsonResult.data("Hello, " + name + "!"));
    }

    @RouteMapping(value = "/users", method = RouteMethod.POST)
    public Future<JsonResult<User>> createUser(@RequestBody User user) {
        return userService.createUser(user).map(JsonResult::data);
    }
}
```

```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
mvn clean compile
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
# curl http://localhost:8080/api/hello?name=VXCore
```

## Documentation

- [Overview](docs/en/01-overview.md)
- [Quick start](docs/en/02-quick-start.md)
- [Installation](docs/en/03-installation.md)
- [Architecture](docs/en/04-architecture.md)
- [Routing](docs/en/08-routing-annotations.md)
- [Configuration](docs/en/10-configuration.md)
- [Full index](docs/en/README.md)

Chinese docs: [docs/README.md](docs/README.md).

## Performance

- **HTTP**: 50,000+ QPS
- **WebSocket**: 10,000+ concurrent connections
- **DB**: 10,000+ QPS with connection pool
- **Batch**: 1000 rows &lt; 100ms
- **Latency**: Microsecond-scale event dispatch

## Contributing

1. Fork, create branch `feature/YourFeature`
2. Commit, push, open Pull Request
3. Follow project code style and add tests where applicable

## License

MIT. See [LICENSE](LICENSE).

## Contact

- **Author**: QAIU
- **Email**: qaiu@qq.com
- **Site**: https://qaiu.top
- **GitHub**: https://github.com/qaiu

---

**VXCore (Weike) — Lightweight JSON API framework for the JVM.**

[中文](README.md) | [English docs](docs/en/README.md)
