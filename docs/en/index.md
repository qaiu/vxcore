# VXCore (Weike) Documentation

[中文](../index.md) | English

Lightweight, JSON API–focused framework. Core under 30MB. Full docs and guides.

## Quick navigation

### Getting started
- [Overview](01-overview.md)
- [Quick start](02-quick-start.md)
- [Installation](03-installation.md)

### Architecture & development
- [System architecture](04-architecture.md)
- [Routing annotations](08-routing-annotations.md)
- [Exception handling](09-exception-handling.md)
- [Configuration](10-configuration.md)
- [Code generator](12-code-generator.md)
- [No-arg constructor DAO](13-no-arg-constructor-dao.md)

### More
- [Lambda query](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) (Chinese)
- [Multi-datasource](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) (Chinese)
- [WebSocket](../WEBSOCKET_GUIDE.md) (Chinese)
- [Git workflow](../29-git-workflow.md) (Chinese)

## 5-minute try

```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
mvn clean compile
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
# curl http://localhost:8080/api/hello?name=VXCore
```

Maven (1.2.3):

```xml
<dependency>
    <groupId>cn.qaiu</groupId>
    <artifactId>core</artifactId>
    <version>1.2.3</version>
</dependency>
```

[Full doc index](README.md) | [中文文档](../README.md)
