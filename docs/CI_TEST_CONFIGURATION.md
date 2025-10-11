# CI测试配置说明

## 概述

本项目已配置GitHub Actions CI/CD工作流，在CI环境中自动排除MySQL和PostgreSQL相关测试，因为GitHub Actions环境中没有配置这些外部数据库服务。

## 文件变更清单

### 1. GitHub Actions工作流配置

**文件**: `.github/workflows/ci.yml`

- 配置了自动化构建和测试流程
- 支持Java 17和21两个版本的矩阵测试
- 设置`CI=true`环境变量，自动激活测试排除规则
- Maven依赖缓存，加速构建
- 测试报告自动上传

### 2. Maven Profile配置

**文件**: `core-database/pom.xml`

添加了两个Maven Profile：

#### CI Profile
```xml
<profile>
    <id>ci</id>
    <activation>
        <property>
            <name>env.CI</name>
            <value>true</value>
        </property>
    </activation>
    ...
</profile>
```

- 当检测到`CI=true`环境变量时自动激活
- 排除所有`**/MySQL*Test.java`测试
- 排除所有`**/PostgreSQL*Test.java`测试

#### Local Profile
```xml
<profile>
    <id>local</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    ...
</profile>
```

- 本地开发环境默认激活
- 运行所有测试，包括MySQL和PostgreSQL测试

### 3. CI测试脚本

**文件**: `scripts/test-ci-mode.sh`

- 在本地模拟GitHub Actions CI环境
- 自动设置`CI=true`环境变量
- 支持运行所有模块或指定模块的测试
- 彩色输出和测试结果统计

### 4. 配置说明文档

**文件**: `.github/workflows/README.md`

详细说明了：
- GitHub Actions工作流特点
- 测试排除规则
- Maven Profile使用方法
- 本地测试命令示例

## 被排除的测试类

以下测试类在CI环境中会被自动排除：

```
core-database/src/test/java/cn/qaiu/db/ddl/
├── MySQLSimpleTest.java
├── MySQLIntegrationTest.java
├── MySQLTableUpdateTest.java
├── PostgreSQLDdlTest.java
├── PostgreSQLIntegrationTest.java
├── PostgreSQLSimpleTest.java
└── PostgreSQLTestConfig.java
```

## 使用方法

### GitHub Actions（自动）

当代码推送到GitHub时，工作流会自动运行：

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

### 本地开发（运行所有测试）

```bash
# 默认运行所有测试
mvn test

# 或者显式激活local profile
mvn test -Plocal
```

### 本地模拟CI环境

```bash
# 方法1: 使用测试脚本（推荐）
./scripts/test-ci-mode.sh

# 方法2: 设置环境变量
export CI=true
mvn test

# 方法3: 显式激活ci profile
mvn test -Pci
```

### 测试指定模块

```bash
# 测试core模块
./scripts/test-ci-mode.sh core

# 测试core-database模块（CI模式）
./scripts/test-ci-mode.sh core-database

# 测试core-database模块（完整测试）
mvn test -pl core-database
```

## 手动控制测试排除

### 命令行参数方式

```bash
# 排除MySQL测试
mvn test -Dtest='!MySQL*Test'

# 排除PostgreSQL测试
mvn test -Dtest='!PostgreSQL*Test'

# 排除多个测试
mvn test -Dtest='!MySQL*Test,!PostgreSQL*Test'

# 只运行特定测试
mvn test -Dtest='H2*Test'
```

### Profile方式

```bash
# 激活CI profile（排除MySQL和PostgreSQL）
mvn test -Pci

# 激活Local profile（运行所有测试）
mvn test -Plocal
```

## 测试报告

### 本地查看

测试完成后，报告位置：
```
target/surefire-reports/
├── TEST-*.xml          # XML格式测试报告
├── *.txt               # 文本格式测试报告
└── *.html              # HTML格式测试报告（如果配置）
```

### GitHub Actions查看

1. 进入GitHub仓库的Actions页面
2. 选择对应的工作流运行
3. 点击"Artifacts"下载测试报告
4. 报告保留7天

## 环境变量

CI环境会设置以下环境变量：

```bash
CI=true              # 激活ci profile
DB_TYPE=h2          # 指定使用H2数据库
```

## 常见问题

### Q: 为什么要排除MySQL和PostgreSQL测试？

A: GitHub Actions的标准运行环境中没有预装MySQL和PostgreSQL服务。虽然可以通过service容器添加这些数据库，但会：
1. 增加CI运行时间
2. 增加配置复杂度
3. 消耗更多GitHub Actions配额

因此，CI环境只使用H2内存数据库进行测试，而MySQL和PostgreSQL的测试在本地开发环境运行。

### Q: 如何在GitHub Actions中运行MySQL/PostgreSQL测试？

A: 如果需要在CI中运行这些测试，可以修改`.github/workflows/ci.yml`，添加service容器：

```yaml
services:
  mysql:
    image: mysql:8.0
    env:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: testdb
    ports:
      - 3306:3306
    
  postgres:
    image: postgres:15
    env:
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: testdb
    ports:
      - 5432:5432
```

然后移除`CI=true`环境变量，或者创建新的profile。

### Q: 本地如何快速运行CI模式测试？

A: 使用提供的脚本：

```bash
./scripts/test-ci-mode.sh
```

这个脚本会自动设置正确的环境变量并运行测试。

### Q: 测试失败后如何调试？

A: 查看详细的测试报告：

```bash
# 运行测试（显示详细输出）
mvn test -X

# 查看失败的测试报告
cat target/surefire-reports/*.txt

# 运行单个测试类进行调试
mvn test -Dtest=ClassName
```

## 最佳实践

1. **本地开发**: 运行完整测试，确保MySQL和PostgreSQL功能正常
2. **提交前**: 运行CI模式测试，确保不会在CI环境中失败
3. **Pull Request**: GitHub Actions自动运行CI测试
4. **定期**: 在真实的MySQL/PostgreSQL环境中运行完整测试

## 相关文档

- [GitHub Actions工作流说明](.github/workflows/README.md)
- [测试依赖配置](core-database/docs/TEST_DEPENDENCIES.md)
- [测试文档](core-database/docs/TEST_DOCUMENTATION.md)
- [多数据源配置指南](core-database/docs/MULTI_DATASOURCE_GUIDE.md)

## 总结

通过这个配置，项目实现了：

✅ 自动化CI/CD流程  
✅ 智能的测试排除机制  
✅ 本地和CI环境的灵活切换  
✅ 详细的测试报告和文档  
✅ 便捷的测试脚本工具  

这确保了代码质量的同时，也优化了CI资源的使用。

