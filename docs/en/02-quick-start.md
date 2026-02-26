# VXCore Quick Start

[中文](../02-quick-start.md) | English

Get a simple Web API running in a few minutes.

## Goals

- Create a first VXCore app
- Define entity and DAO
- Create REST API controller
- Use Lambda query
- Run and test

## Prerequisites

- **Java 17+**: [Adoptium](https://adoptium.net/)
- **Maven 3.8+**: [Maven](https://maven.apache.org/download.cgi)
- IDE: IntelliJ IDEA or Eclipse

```bash
java -version
mvn -version
```

## Step 1: Create project

### Option A: Add VXCore to existing Maven project

In `pom.xml`:

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <vxcore.version>1.2.3</vxcore.version>
    <vertx.version>4.5.25</vertx.version>
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
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.220</version>
    </dependency>
</dependencies>
```

### Option B: Clone repo

```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
mvn clean compile
```

## Step 2: Database

H2 in-memory works out of the box. For MySQL/PostgreSQL, add driver and configure `application.yml` (see [Installation](03-installation.md)).

## Step 3: Entity and DAO

```java
@DdlTable("users")
public class User extends BaseEntity {
    @DdlColumn("user_name")
    private String name;
    @DdlColumn("user_email")
    private String email;
    // getters/setters
}

// No-arg DAO (recommended): framework initializes everything
public class UserDao extends AbstractDao<User, Long> {}
```

## Step 4: Controller

```java
@RouteHandler("/api")
public class UserController {

    @RouteMapping(value = "/hello", method = RouteMethod.GET)
    public Future<JsonResult<String>> hello(@RequestParam("name") String name) {
        return Future.succeededFuture(JsonResult.data("Hello, " + name + "!"));
    }

    @RouteMapping(value = "/users", method = RouteMethod.GET)
    public Future<JsonResult<List<User>>> getUsers() {
        return userDao.lambdaQuery()
            .eq(User::getStatus, "ACTIVE")
            .list()
            .map(JsonResult::data);
    }
}
```

## Step 5: Run

```bash
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
# curl http://localhost:8080/api/hello?name=VXCore
```

## Next

- [Installation & config](03-installation.md)
- [No-arg constructor DAO](13-no-arg-constructor-dao.md)
- [Routing annotations](08-routing-annotations.md)
- [Overview](01-overview.md)
