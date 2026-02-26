# VXCore Installation and Configuration

[中文](../03-installation.md) | English

Install and configure VXCore in your environment.

## Requirements

### Minimum
- **Java**: 17+ (OpenJDK 17 or later)
- **Maven**: 3.8+ (3.9+ recommended)
- **RAM**: 2GB+
- **Disk**: 1GB+ free

### Recommended
- **Java**: OpenJDK 21 LTS
- **Maven**: 3.9.5+
- **RAM**: 4GB+
- **IDE**: IntelliJ IDEA 2023.3+ or Eclipse 2023-12+

## OS support

- **Windows**: 10/11 (64-bit)
- **macOS**: 10.15+ (Intel/Apple Silicon)
- **Linux**: Ubuntu 20.04+, CentOS 8+, RHEL 8+
- **Arch**: x86_64, ARM64

## Java installation

### macOS (Homebrew)
```bash
brew install openjdk@21
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
java -version
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### Windows
- Download OpenJDK 21 from [Adoptium](https://adoptium.net/)
- Set `JAVA_HOME` and add `%JAVA_HOME%\bin` to `PATH`

## Maven installation

### macOS / Linux
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

mvn -version
```

### Windows
- Download from [Maven](https://maven.apache.org/download.cgi), unpack, set `MAVEN_HOME` and add `bin` to `PATH`
- Or: `choco install maven`

## VXCore dependency

Add to your project `pom.xml`:

```xml
<properties>
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
</dependencies>
```

## Database drivers

- **H2** (dev/test): `com.h2database:h2:2.2.220`
- **MySQL**: `com.mysql:mysql-connector-j`
- **PostgreSQL**: `org.postgresql:postgresql`

## Configuration (YAML)

Example `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

datasources:
  primary:
    url: jdbc:h2:mem:testdb
    driver: org.h2.Driver
  # For MySQL/PostgreSQL, add username, password, url, driver
```

## Verify

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
curl http://localhost:8080/api/hello?name=VXCore
```

## Next

- [Quick start](02-quick-start.md)
- [Architecture](04-architecture.md)
- [Configuration guide](10-configuration.md)
