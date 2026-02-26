# Configuration

[中文](../10-configuration.md) | English

VXCore supports YAML configuration, optional metadata for IDE hints, and validation.

## Configuration annotations

### @ConfigurationProperties

Marks a config class and binds a prefix.

```java
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    @ConfigurationProperty(description = "Server port")
    private int port = 8080;
    // getters/setters
}
```

### @ConfigurationProperty

Marks a property; optional key, description, defaultValue, type.

## YAML structure

Example `application.yml`:

```yaml
server:
  port: 8080
  host: localhost

datasources:
  primary:
    url: jdbc:h2:mem:testdb
    driver: org.h2.Driver
  secondary:
    url: jdbc:mysql://localhost:3306/db
    username: root
    password: secret
    driver: com.mysql.cj.jdbc.Driver
```

## Loading and access

- Config is loaded at startup (e.g. from classpath or file).
- Use `SharedData` or injected config beans to read values at runtime.
- Override with system properties or environment variables where supported.

## Datasource config

Under `datasources.<name>`: `url`, `username`, `password`, `driver`, and optional pool settings. See [Multi-datasource](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) (Chinese) for details.

## Related

- [Routing](08-routing-annotations.md)
- [Exception handling](09-exception-handling.md)
- [Installation](03-installation.md)
- [Overview](01-overview.md)
