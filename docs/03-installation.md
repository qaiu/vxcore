# VXCore 安装配置指南

本指南将帮助您在不同环境中安装和配置 VXCore 框架。

## 📋 系统要求

### 最低要求
- **Java**: 17+ (推荐使用 OpenJDK 17 或更高版本)
- **Maven**: 3.8+ (推荐使用 3.9+)
- **内存**: 最少 2GB RAM
- **磁盘**: 最少 1GB 可用空间

### 推荐配置
- **Java**: OpenJDK 21 LTS
- **Maven**: 3.9.5+
- **内存**: 4GB+ RAM
- **磁盘**: 5GB+ 可用空间
- **IDE**: IntelliJ IDEA 2023.3+ 或 Eclipse 2023-12+

## 🖥️ 操作系统支持

### 支持的操作系统
- **Windows**: Windows 10/11 (64-bit)
- **macOS**: macOS 10.15+ (Intel/Apple Silicon)
- **Linux**: Ubuntu 20.04+, CentOS 8+, RHEL 8+

### 架构支持
- **x86_64**: Intel/AMD 64-bit
- **ARM64**: Apple Silicon, ARM64 Linux

## ☕ Java 环境安装

### Windows 安装

#### 方法一：使用 Chocolatey
```powershell
# 安装 Chocolatey (如果未安装)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 安装 OpenJDK 21
choco install openjdk21

# 验证安装
java -version
```

#### 方法二：手动安装
1. 访问 [Adoptium](https://adoptium.net/)
2. 下载 OpenJDK 21 LTS for Windows x64
3. 运行安装程序
4. 设置环境变量：
   ```cmd
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

### macOS 安装

#### 方法一：使用 Homebrew
```bash
# 安装 Homebrew (如果未安装)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 OpenJDK 21
brew install openjdk@21

# 设置环境变量
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
```

#### 方法二：使用 SDKMAN
```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 OpenJDK 21
sdk install java 21.0.1-tem

# 验证安装
java -version
```

### Linux 安装

#### Ubuntu/Debian
```bash
# 更新包列表
sudo apt update

# 安装 OpenJDK 21
sudo apt install openjdk-21-jdk

# 设置默认 Java 版本
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-21-openjdk-amd64/bin/java 1
sudo update-alternatives --config java

# 验证安装
java -version
```

#### CentOS/RHEL/Fedora
```bash
# 安装 OpenJDK 21
sudo dnf install java-21-openjdk-devel

# 设置环境变量
echo 'export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# 验证安装
java -version
```

## 🔧 Maven 安装

### Windows 安装
```powershell
# 使用 Chocolatey
choco install maven

# 验证安装
mvn -version
```

### macOS 安装
```bash
# 使用 Homebrew
brew install maven

# 验证安装
mvn -version
```

### Linux 安装

#### Ubuntu/Debian
```bash
sudo apt install maven
mvn -version
```

#### CentOS/RHEL/Fedora
```bash
sudo dnf install maven
mvn -version
```

## 📦 VXCore 安装

### 方法一：从源码构建

#### 1. 克隆项目
```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

#### 2. 编译项目
```bash
# 编译整个项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package
```

#### 3. 安装到本地仓库
```bash
mvn clean install
```

### 方法二：使用 Maven 依赖

#### 1. 创建新项目
```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-vxcore-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

#### 2. 添加 VXCore 依赖
编辑 `pom.xml` 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>my-vxcore-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <vxcore.version>2.0.0</vxcore.version>
        <vertx.version>4.5.2</vertx.version>
        <jooq.version>3.19.2</jooq.version>
    </properties>
    
    <dependencies>
        <!-- VXCore Core Module -->
        <dependency>
            <groupId>cn.qaiu</groupId>
            <artifactId>vxcore-core</artifactId>
            <version>${vxcore.version}</version>
        </dependency>
        
        <!-- VXCore Database Module -->
        <dependency>
            <groupId>cn.qaiu</groupId>
            <artifactId>vxcore-database</artifactId>
            <version>${vxcore.version}</version>
        </dependency>
        
        <!-- Vert.x Core -->
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-core</artifactId>
            <version>${vertx.version}</version>
        </dependency>
        
        <!-- Vert.x Web -->
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-web</artifactId>
            <version>${vertx.version}</version>
        </dependency>
        
        <!-- Vert.x JDBC Client -->
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-jdbc-client</artifactId>
            <version>${vertx.version}</version>
        </dependency>
        
        <!-- jOOQ -->
        <dependency>
            <groupId>org.jooq</groupId>
            <artifactId>jooq</artifactId>
            <version>${jooq.version}</version>
        </dependency>
        
        <!-- H2 Database (开发测试用) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
        
        <!-- MySQL Driver (生产环境) -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
        
        <!-- PostgreSQL Driver (生产环境) -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-junit5</artifactId>
            <version>${vertx.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.2</version>
            </plugin>
            
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.example.MainApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 🗄️ 数据库配置

### H2 数据库 (开发测试)

#### 内存数据库
```yaml
# application.yml
datasources:
  primary:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
    driver: org.h2.Driver
    maxPoolSize: 10
    minPoolSize: 2
```

#### 文件数据库
```yaml
# application.yml
datasources:
  primary:
    url: jdbc:h2:file:./data/testdb
    username: sa
    password: ""
    driver: org.h2.Driver
    maxPoolSize: 10
    minPoolSize: 2
```

### MySQL 数据库

#### 安装 MySQL
```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL/Fedora
sudo dnf install mysql-server

# macOS
brew install mysql

# Windows
# 下载 MySQL Installer from https://dev.mysql.com/downloads/installer/
```

#### 配置 MySQL
```sql
-- 创建数据库
CREATE DATABASE vxcore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'vxcore_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON vxcore_db.* TO 'vxcore_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 应用配置
```yaml
# application.yml
datasources:
  primary:
    url: jdbc:mysql://localhost:3306/vxcore_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: vxcore_user
    password: password
    driver: com.mysql.cj.jdbc.Driver
    maxPoolSize: 20
    minPoolSize: 5
    connectionTimeout: 30000
```

### PostgreSQL 数据库

#### 安装 PostgreSQL
```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL/Fedora
sudo dnf install postgresql postgresql-server

# macOS
brew install postgresql

# Windows
# 下载 PostgreSQL Installer from https://www.postgresql.org/download/windows/
```

#### 配置 PostgreSQL
```sql
-- 创建数据库
CREATE DATABASE vxcore_db;

-- 创建用户
CREATE USER vxcore_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE vxcore_db TO vxcore_user;
```

#### 应用配置
```yaml
# application.yml
datasources:
  primary:
    url: jdbc:postgresql://localhost:5432/vxcore_db
    username: vxcore_user
    password: password
    driver: org.postgresql.Driver
    maxPoolSize: 20
    minPoolSize: 5
    connectionTimeout: 30000
```

## 🔧 IDE 配置

### IntelliJ IDEA 配置

#### 1. 安装插件
- **Lombok Plugin**: 支持 Lombok 注解
- **Maven Helper**: Maven 依赖管理
- **Vert.x Plugin**: Vert.x 开发支持

#### 2. 项目设置
1. 打开项目：`File -> Open -> 选择项目目录`
2. 配置 JDK：`File -> Project Structure -> Project -> Project SDK`
3. 配置 Maven：`File -> Settings -> Build -> Build Tools -> Maven`

#### 3. 代码风格
```xml
<!-- .editorconfig -->
root = true

[*.java]
indent_style = space
indent_size = 4
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true
```

### Eclipse 配置

#### 1. 安装插件
- **Maven Integration for Eclipse**
- **Vert.x Tools for Eclipse**
- **Lombok**

#### 2. 项目设置
1. 导入项目：`File -> Import -> Existing Maven Projects`
2. 配置 JDK：`Project -> Properties -> Java Build Path -> Libraries`
3. 配置 Maven：`Window -> Preferences -> Maven`

## 🧪 验证安装

### 1. 创建测试项目
```bash
# 创建测试目录
mkdir vxcore-test
cd vxcore-test

# 创建 Maven 项目
mvn archetype:generate \
  -DgroupId=com.test \
  -DartifactId=vxcore-test \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

### 2. 添加依赖
按照上面的 `pom.xml` 配置添加 VXCore 依赖。

### 3. 创建测试类
```java
package com.test;

import cn.qaiu.vx.core.util.StringCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VXCoreTest {
    
    @Test
    public void testStringCase() {
        // 测试驼峰转下划线
        String result = StringCase.toUnderlineCase("userName");
        assertEquals("user_name", result);
        
        // 测试下划线转驼峰
        String result2 = StringCase.toCamelCase("user_name");
        assertEquals("userName", result2);
    }
}
```

### 4. 运行测试
```bash
mvn test
```

### 5. 预期输出
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 🚨 常见问题

### Java 版本问题
```bash
# 问题：java: 错误: 无效的源发行版: 17
# 解决：检查 JAVA_HOME 环境变量
echo $JAVA_HOME
java -version
```

### Maven 依赖问题
```bash
# 问题：依赖下载失败
# 解决：清理本地仓库
mvn dependency:purge-local-repository
mvn clean install
```

### 数据库连接问题
```bash
# 问题：数据库连接失败
# 解决：检查数据库服务状态
# MySQL
sudo systemctl status mysql

# PostgreSQL
sudo systemctl status postgresql
```

### 端口占用问题
```bash
# 问题：端口 8080 被占用
# 解决：查找并终止进程
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/macOS
lsof -ti:8080
kill -9 <PID>
```

## 📚 下一步

安装完成后，您可以：

1. [快速开始](02-quick-start.md) - 创建第一个应用
2. [项目概述](01-overview.md) - 了解框架特性
3. [Lambda 查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - 学习数据库操作
4. [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 配置多数据源

## 🆘 获取帮助

如果遇到安装问题：

- [GitHub Issues](https://github.com/qaiu/vxcore/issues) - 提交问题
- [讨论区](https://github.com/qaiu/vxcore/discussions) - 技术讨论
- [邮件支持](mailto:qaiu@qq.com) - 直接联系

---

**🎯 安装完成！开始您的 VXCore 之旅！**

[快速开始 →](02-quick-start.md) | [项目概述 →](01-overview.md) | [返回首页 →](index.md)