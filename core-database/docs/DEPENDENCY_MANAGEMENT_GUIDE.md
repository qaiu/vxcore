# VXCore 数据库依赖管理指南

## 概述

VXCore框架采用按需引入的依赖管理策略，将数据库驱动设为可选依赖，用户可以根据实际需要选择添加相应的数据库支持。

## 支持的数据库

| 数据库类型 | 描述 | 状态 |
|-----------|------|------|
| MySQL | MySQL数据库支持 | ✅ 可选依赖 |
| PostgreSQL | PostgreSQL数据库支持 | ✅ 可选依赖 |
| H2 | H2内存数据库（测试用） | ✅ 可选依赖 |
| Oracle | Oracle数据库支持 | 🔄 计划支持 |
| SQL Server | SQL Server数据库支持 | 🔄 计划支持 |

## 添加数据库依赖

### 方法1：手动添加依赖

在项目的 `pom.xml` 中添加所需的数据库依赖：

#### MySQL支持
```xml
<dependencies>
    <!-- MySQL驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.2.0</version>
    </dependency>
    
    <!-- MySQL Vert.x客户端 -->
    <dependency>
        <groupId>io.vertx</groupId>
        <artifactId>vertx-mysql-client</artifactId>
    </dependency>
</dependencies>
```

#### PostgreSQL支持
```xml
<dependencies>
    <!-- PostgreSQL驱动 -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>
    
    <!-- PostgreSQL Vert.x客户端 -->
    <dependency>
        <groupId>io.vertx</groupId>
        <artifactId>vertx-pg-client</artifactId>
    </dependency>
</dependencies>
```

#### H2支持（测试用）
```xml
<dependencies>
    <!-- H2数据库 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.220</version>
    </dependency>
</dependencies>
```

### 方法2：使用依赖管理工具

```java
import cn.qaiu.db.dependency.DependencyManager;

// 检查数据库是否支持
if (DependencyManager.isSupported("mysql")) {
    // 获取MySQL依赖信息
    List<MavenDependency> deps = DependencyManager.getMavenDependencies("mysql");
    
    // 生成Maven依赖XML
    String xml = DependencyManager.generateMavenDependencyXml("mysql");
    System.out.println(xml);
}

// 运行时检查驱动是否可用
if (DependencyManager.isDatabaseDriverAvailable(JDBCType.MySQL)) {
    // MySQL驱动可用
}
```

## 配置示例

### 多数据库配置
```yaml
# application.yml
datasources:
  mysql:
    type: mysql
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    
  postgresql:
    type: postgresql
    url: jdbc:postgresql://localhost:5432/mydb
    username: postgres
    password: password
    
  h2:
    type: h2
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
```

### 使用多数据源
```java
@DataSource("mysql")
public class UserDao extends MultiDataSourceDao<User> {
    
    @DataSource("postgresql")
    public Future<List<User>> findFromPostgreSQL() {
        return lambdaQuery().list();
    }
    
    public Future<List<User>> findFromMySQL() {
        return lambdaQuery().list();
    }
}
```

## 最佳实践

### 1. 生产环境
- 只添加实际使用的数据库驱动
- 使用具体的版本号，避免版本冲突
- 定期更新驱动版本

### 2. 开发环境
- 可以添加H2用于快速测试
- 使用与生产环境相同的数据库类型

### 3. 测试环境
- 使用H2内存数据库进行单元测试
- 使用Docker容器进行集成测试

## 故障排除

### 常见问题

#### 1. ClassNotFoundException
```
java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver
```
**解决方案**：添加MySQL驱动依赖到pom.xml

#### 2. 连接池创建失败
```
Failed to create connection pool for mysql
```
**解决方案**：检查数据库驱动和Vert.x客户端依赖是否都添加

#### 3. 方言不支持
```
Unsupported SQL dialect: MYSQL
```
**解决方案**：确保添加了对应的数据库驱动

### 依赖检查工具

```java
// 检查所有数据库驱动可用性
for (JDBCType type : JDBCType.values()) {
    boolean available = DependencyManager.isDatabaseDriverAvailable(type);
    System.out.println(type + ": " + (available ? "✅" : "❌"));
}
```

## 版本兼容性

| VXCore版本 | MySQL驱动 | PostgreSQL驱动 | H2驱动 | Vert.x版本 |
|-----------|-----------|----------------|--------|------------|
| 1.0.0 | 9.2.0 | 42.7.3 | 2.2.220 | 4.5.2+ |

## 更新日志

### v1.0.0
- ✅ 实现按需依赖管理
- ✅ 支持MySQL、PostgreSQL、H2
- ✅ 添加依赖管理工具类
- ✅ 提供Maven插件支持

### 计划功能
- 🔄 Oracle数据库支持
- 🔄 SQL Server数据库支持
- 🔄 自动依赖检测和提示
- 🔄 依赖版本管理工具
