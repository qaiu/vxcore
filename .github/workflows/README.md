# VXCore GitHub 工作流说明

本目录包含了 VXCore 项目的所有 GitHub Actions 工作流配置，用于自动化构建、测试、代码质量检查和发布。

## 📋 工作流概览

### 1. CI/CD 主流程 (`ci.yml`)

**触发条件**：
- Push 到 `main` 或 `develop` 分支
- Pull Request 到 `main` 或 `develop` 分支
- 每天凌晨2点自动运行性能测试

**主要功能**：
- 代码质量检查
- 单元测试（支持 Java 17 和 21）
- 集成测试（支持 H2、MySQL、PostgreSQL）
- 性能测试
- 构建和打包
- 自动发布

**测试矩阵**：
- Java 版本：17, 21
- 数据库：H2, MySQL, PostgreSQL
- 模块：core, core-database, core-generator, core-example

### 2. 性能测试 (`performance.yml`)

**触发条件**：
- 每天凌晨3点自动运行
- 手动触发（支持选择测试类型）

**测试类型**：
- 单元性能测试
- 集成性能测试
- 压力测试
- 内存测试
- 基准测试

**配置选项**：
- 测试类型：all, unit, integration, stress, memory
- Java 版本：17, 21

### 3. 代码质量检查 (`code-quality.yml`)

**触发条件**：
- Push 到 `main` 或 `develop` 分支
- Pull Request 到 `main` 或 `develop` 分支
- 每周一凌晨1点自动运行

**检查项目**：
- 代码格式检查（Spotless）
- 静态代码分析（SpotBugs, PMD, Checkstyle）
- 依赖安全检查（OWASP）
- 代码覆盖率检查（JaCoCo）
- 代码重复检查（CPD）

### 4. 发布流程 (`release.yml`)

**触发条件**：
- 推送标签（格式：`v*`）
- 手动触发

**发布步骤**：
- 构建和测试
- 发布到 Maven Central
- 创建 GitHub Release
- 发布到 Docker Hub
- 发送通知

## 🚀 使用方法

### 运行所有测试

```bash
# 推送代码到 main 分支
git push origin main

# 或创建 Pull Request
gh pr create --title "Feature: 新功能" --body "描述"
```

### 手动触发性能测试

1. 进入 GitHub Actions 页面
2. 选择 "Performance Tests" 工作流
3. 点击 "Run workflow"
4. 选择测试类型和 Java 版本

### 发布新版本

```bash
# 创建并推送标签
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 或使用 GitHub CLI
gh release create v1.0.0 --title "VXCore v1.0.0" --notes "发布说明"
```

## 📊 测试报告

### 查看测试结果

1. 进入 GitHub Actions 页面
2. 选择对应的工作流运行
3. 查看 "Artifacts" 部分下载测试报告

### 测试报告类型

- **单元测试报告**：`test-reports-{module}-java-{version}`
- **集成测试报告**：`integration-test-reports-{database}-java-{version}`
- **性能测试报告**：`performance-test-reports`
- **代码质量报告**：`quality-summary-report`
- **覆盖率报告**：`coverage-reports`

## 🔧 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `CI` | CI 环境标识 | `true` |
| `DB_TYPE` | 数据库类型 | `h2` |
| `MAVEN_OPTS` | Maven 选项 | `-Xmx2048m -XX:+UseG1GC` |

### 密钥配置

需要在 GitHub 仓库设置中配置以下密钥：

| 密钥名 | 说明 | 用途 |
|--------|------|------|
| `OSSRH_USERNAME` | Maven Central 用户名 | 发布到 Maven Central |
| `OSSRH_TOKEN` | Maven Central 令牌 | 发布到 Maven Central |
| `DOCKER_USERNAME` | Docker Hub 用户名 | 发布 Docker 镜像 |
| `DOCKER_TOKEN` | Docker Hub 令牌 | 发布 Docker 镜像 |
| `SLACK_WEBHOOK` | Slack Webhook URL | 发送通知 |

## 📈 监控和告警

### 测试状态监控

- 所有测试失败时会自动创建 Issue
- 性能测试失败时会发送 Slack 通知
- 代码质量检查失败时会评论 PR

### 性能基准

- 启动时间：< 5秒
- 内存使用：< 512MB
- 并发处理：> 1000 QPS
- 测试覆盖率：> 80%

## 🛠️ 本地开发

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl core

# 运行性能测试
mvn test -Dtest=*PerformanceTest

# 运行集成测试
mvn verify -pl core-example
```

### 代码质量检查

```bash
# 检查代码格式
mvn spotless:check

# 修复代码格式
mvn spotless:apply

# 运行静态分析
mvn spotbugs:check
mvn pmd:check
mvn checkstyle:check

# 生成覆盖率报告
mvn jacoco:report
```

### Docker 开发环境

```bash
# 启动开发环境
docker-compose up -d

# 查看日志
docker-compose logs -f vxcore-app

# 停止环境
docker-compose down
```

## 🔍 故障排除

### 常见问题

1. **测试失败**
   - 检查数据库连接配置
   - 确认测试环境变量设置
   - 查看详细错误日志

2. **构建失败**
   - 检查 Maven 依赖版本
   - 确认 Java 版本兼容性
   - 查看构建日志

3. **发布失败**
   - 检查 Maven Central 凭据
   - 确认版本号格式
   - 查看发布日志

### 调试技巧

1. **查看详细日志**
   ```bash
   # 启用调试模式
   mvn test -X
   ```

2. **本地复现问题**
   ```bash
   # 使用相同的环境变量
   export CI=true
   export DB_TYPE=h2
   mvn test
   ```

3. **检查依赖版本**
   ```bash
   # 查看依赖树
   mvn dependency:tree
   
   # 检查依赖更新
   mvn versions:display-dependency-updates
   ```

## 📚 相关文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Maven 文档](https://maven.apache.org/guides/)
- [Docker 文档](https://docs.docker.com/)
- [VXCore 项目文档](../docs/)

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request
5. 等待 CI 检查通过
6. 代码审查
7. 合并到主分支

---

**注意**：所有工作流都经过优化，支持并行执行和缓存，以提高构建效率。如有问题，请查看 GitHub Actions 日志或联系维护者。