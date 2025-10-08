# 安装配置

## 📋 环境要求

### 系统要求
- **操作系统**: Windows 10+, macOS 10.15+, Linux (Ubuntu 18.04+)
- **Java**: OpenJDK 17+ 或 Oracle JDK 17+
- **Maven**: 3.9.0+
- **内存**: 最少 4GB RAM，推荐 8GB+
- **磁盘**: 最少 1GB 可用空间

### 开发工具推荐
- **IDE**: IntelliJ IDEA 2023+, Eclipse 2023+, VS Code
- **数据库**: MySQL 8.0+, PostgreSQL 13+, H2 (用于测试)
- **版本控制**: Git 2.30+

## 🔧 Java环境配置

### 1. 安装Java 17+

#### Windows
```bash
# 使用Chocolatey安装
choco install openjdk17

# 或下载Oracle JDK
# 访问 https://www.oracle.com/java/technologies/downloads/
```

#### macOS
```bash
# 使用Homebrew安装
brew install openjdk@17

# 设置环境变量
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Linux (Ubuntu/Debian)
```bash
# 安装OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# 设置默认Java版本
sudo update-alternatives --config java
```

### 2. 验证Java安装
```bash
java -version
# 应该显示类似: openjdk version "17.0.x" 2023-xx-xx

javac -version
# 应该显示: javac 17.0.x
```

### 3. 设置JAVA_HOME
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17

# macOS/Linux
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$JAVA_HOME/bin:$PATH
```

## 📦 Maven配置

### 1. 安装Maven

#### Windows
```bash
# 使用Chocolatey
choco install maven

# 或手动下载
# 访问 https://maven.apache.org/download.cgi
```

#### macOS
```bash
# 使用Homebrew
brew install maven
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven
```

### 2. 验证Maven安装
```bash
mvn -version
# 应该显示Maven版本和Java版本信息
```

### 3. Maven配置优化

#### 创建Maven设置文件
```xml
<!-- ~/.m2/settings.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <!-- 本地仓库路径 -->
  <localRepository>${user.home}/.m2/repository</localRepository>
  
  <!-- 镜像配置 -->
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  
  <!-- 配置文件 -->
  <profiles>
    <profile>
      <id>jdk-17</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>17</jdk>
      </activation>
      <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

## 🗄️ 数据库配置

### 1. MySQL配置

#### 安装MySQL
```bash
# macOS
brew install mysql
brew services start mysql

# Ubuntu/Debian
sudo apt install mysql-server
sudo systemctl start mysql

# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
```

#### 创建数据库和用户
```sql
-- 连接到MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE vxcore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'vxcore'@'localhost' IDENTIFIED BY 'vxcore123';
GRANT ALL PRIVILEGES ON vxcore_test.* TO 'vxcore'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

#### 测试连接
```bash
mysql -u vxcore -p vxcore_test
```

### 2. PostgreSQL配置

#### 安装PostgreSQL
```bash
# macOS
brew install postgresql
brew services start postgresql

# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl start postgresql
```

#### 创建数据库和用户
```sql
-- 切换到postgres用户
sudo -u postgres psql

-- 创建用户
CREATE USER vxcore WITH PASSWORD 'vxcore123';

-- 创建数据库
CREATE DATABASE vxcore_test OWNER vxcore;

-- 授权
GRANT ALL PRIVILEGES ON DATABASE vxcore_test TO vxcore;

-- 退出
\q
```

### 3. H2数据库（测试用）

H2是内存数据库，无需额外安装，Maven会自动下载依赖。

## 🔧 项目配置

### 1. 克隆项目
```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

### 2. 配置数据库连接

#### 开发环境配置
```properties
# core-database/src/main/resources/app.properties
# MySQL配置
db.url=jdbc:mysql://localhost:3306/vxcore_test?useSSL=false&serverTimezone=UTC
db.driver=com.mysql.cj.jdbc.Driver
db.username=vxcore
db.password=vxcore123

# PostgreSQL配置
# db.url=jdbc:postgresql://localhost:5432/vxcore_test
# db.driver=org.postgresql.Driver
# db.username=vxcore
# db.password=vxcore123

# H2配置（测试用）
# db.url=jdbc:h2:mem:testdb
# db.driver=org.h2.Driver
# db.username=sa
# db.password=

# 连接池配置
db.pool.max_size=20
db.pool.min_size=5
db.pool.max_wait_time=30000
db.pool.max_lifetime=1800000
```

#### 测试环境配置
```properties
# core-database/src/test/resources/test.properties
# H2内存数据库配置
db.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
db.driver=org.h2.Driver
db.username=sa
db.password=

# 连接池配置
db.pool.max_size=10
db.pool.min_size=2
db.pool.max_wait_time=10000
db.pool.max_lifetime=600000
```

### 3. 编译项目
```bash
# 编译整个项目
mvn clean compile

# 编译特定模块
cd core-database
mvn clean compile
```

### 4. 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=UserDaoTest

# 跳过测试编译
mvn test -DskipTests=false -Dmaven.test.skip=false
```

## 🐛 常见问题

### 1. Java版本问题
```bash
# 问题：javac: command not found
# 解决：确保JAVA_HOME设置正确
echo $JAVA_HOME
export JAVA_HOME=/path/to/your/java
```

### 2. Maven依赖下载失败
```bash
# 问题：依赖下载超时
# 解决：配置国内镜像
# 编辑 ~/.m2/settings.xml，添加阿里云镜像
```

### 3. 数据库连接失败
```bash
# 问题：数据库连接被拒绝
# 解决：检查数据库服务是否启动
# MySQL
sudo systemctl status mysql

# PostgreSQL
sudo systemctl status postgresql
```

### 4. 端口占用问题
```bash
# 问题：端口8080被占用
# 解决：查找并杀死占用进程
lsof -i :8080
kill -9 <PID>
```

## 🔍 验证安装

### 1. 创建验证脚本
```bash
#!/bin/bash
# verify-installation.sh

echo "=== VxCore 安装验证 ==="

# 检查Java
echo "检查Java版本..."
java -version
if [ $? -eq 0 ]; then
    echo "✅ Java安装成功"
else
    echo "❌ Java安装失败"
    exit 1
fi

# 检查Maven
echo "检查Maven版本..."
mvn -version
if [ $? -eq 0 ]; then
    echo "✅ Maven安装成功"
else
    echo "❌ Maven安装失败"
    exit 1
fi

# 检查Git
echo "检查Git版本..."
git --version
if [ $? -eq 0 ]; then
    echo "✅ Git安装成功"
else
    echo "❌ Git安装失败"
    exit 1
fi

# 编译项目
echo "编译项目..."
mvn clean compile
if [ $? -eq 0 ]; then
    echo "✅ 项目编译成功"
else
    echo "❌ 项目编译失败"
    exit 1
fi

# 运行测试
echo "运行测试..."
mvn test
if [ $? -eq 0 ]; then
    echo "✅ 测试运行成功"
else
    echo "❌ 测试运行失败"
    exit 1
fi

echo "🎉 所有验证通过！VxCore安装成功！"
```

### 2. 运行验证脚本
```bash
chmod +x verify-installation.sh
./verify-installation.sh
```

## 📚 下一步

- [快速开始](02-quick-start.md) - 运行第一个示例
- [系统架构](04-architecture.md) - 了解架构设计
- [DSL框架](07-dsl-framework.md) - 学习jOOQ DSL

---

**🎯 环境配置完成！现在可以开始使用VxCore了！**
