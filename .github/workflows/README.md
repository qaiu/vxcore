# GitHub Actions 工作流配置说明

## CI/CD 工作流

项目配置了自动化的CI/CD工作流，在代码提交到GitHub时自动运行测试和构建。

### 工作流特点

1. **多Java版本测试**: 支持Java 17和21两个版本
2. **自动排除外部数据库测试**: CI环境下自动跳过MySQL和PostgreSQL测试
3. **Maven依赖缓存**: 加速构建过程
4. **测试报告上传**: 保留测试结果7天

### 测试排除规则

在CI环境中，以下测试类会被自动排除：
- `**/MySQL*Test.java` - 所有MySQL相关测试
- `**/PostgreSQL*Test.java` - 所有PostgreSQL相关测试

这是因为GitHub Actions环境中没有配置外部数据库服务，只使用H2内存数据库进行测试。

## Maven Profile配置

项目在`core-database/pom.xml`中配置了两个profile：

### 1. CI Profile（自动激活）

当检测到`CI=true`环境变量时自动激活，排除MySQL和PostgreSQL测试。

```bash
# GitHub Actions会自动设置CI=true
mvn test
```

### 2. Local Profile（默认）

本地开发环境默认激活，运行所有测试，包括MySQL和PostgreSQL测试。

```bash
# 本地运行所有测试
mvn test
```

## 本地模拟CI环境

如果需要在本地模拟CI环境运行测试：

```bash
# 设置CI环境变量
export CI=true
mvn clean test

# 或者使用-P参数显式激活ci profile
mvn clean test -Pci
```

## 手动排除测试

如果需要手动排除某些测试：

```bash
# 排除MySQL测试
mvn test -Dtest='!MySQL*Test'

# 排除PostgreSQL测试
mvn test -Dtest='!PostgreSQL*Test'

# 排除两者
mvn test -Dtest='!MySQL*Test,!PostgreSQL*Test'
```

## 只运行特定测试

```bash
# 只运行H2相关测试
mvn test -Dtest='H2*Test'

# 只运行DDL测试（不包括MySQL和PostgreSQL）
mvn test -Dtest='Ddl*Test'
```

## 测试报告位置

测试完成后，报告会保存在：
- Surefire报告: `target/surefire-reports/`
- 测试类: `target/test-classes/`

在GitHub Actions中，这些报告会被上传为artifacts，可以在Actions页面下载查看。

