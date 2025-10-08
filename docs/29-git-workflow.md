# Git工作流

## 🔄 工作流概述

本文档描述了VxCore项目的Git工作流规范，包括分支管理、代码提交流程、代码审查等最佳实践。

## 🌳 分支策略

### 分支类型

#### 1. 主分支 (main)
- **用途**: 生产环境代码，始终保持稳定
- **保护**: 禁止直接推送，只能通过Pull Request合并
- **命名**: `main`

#### 2. 开发分支 (develop)
- **用途**: 集成开发分支，包含最新的开发功能
- **保护**: 需要代码审查才能合并
- **命名**: `develop`

#### 3. 功能分支 (feature)
- **用途**: 开发新功能
- **命名规范**: `feature/功能名称` 或 `feature/issue-编号-功能描述`
- **示例**: `feature/user-authentication`, `feature/issue-123-add-login`

#### 4. 修复分支 (hotfix)
- **用途**: 紧急修复生产环境问题
- **命名规范**: `hotfix/问题描述` 或 `hotfix/issue-编号-问题描述`
- **示例**: `hotfix/memory-leak`, `hotfix/issue-456-sql-injection`

#### 5. 发布分支 (release)
- **用途**: 准备新版本发布
- **命名规范**: `release/版本号`
- **示例**: `release/v1.2.0`

### 分支关系图

```
main (生产环境)
  ↑
develop (开发环境)
  ↑
feature/user-auth (功能开发)
  ↑
feature/login-form (子功能)
```

## 🚀 开发流程

### 1. 功能开发流程

#### 步骤1: 创建功能分支
```bash
# 从develop分支创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# 推送新分支到远程
git push -u origin feature/user-authentication
```

#### 步骤2: 开发功能
```bash
# 进行开发工作
git add .
git commit -m "feat: 添加用户认证功能

- 实现用户登录验证
- 添加JWT token生成
- 完善密码加密逻辑

Closes #123"

# 定期推送代码
git push origin feature/user-authentication
```

#### 步骤3: 创建Pull Request
```bash
# 在GitHub上创建PR，从feature/user-authentication到develop
# 标题: feat: 添加用户认证功能
# 描述: 详细说明功能实现和测试情况
```

#### 步骤4: 代码审查
- 至少需要1个代码审查者
- 所有CI检查必须通过
- 解决审查意见后重新提交

#### 步骤5: 合并到develop
```bash
# 审查通过后，合并PR
# 删除功能分支
git checkout develop
git pull origin develop
git branch -d feature/user-authentication
git push origin --delete feature/user-authentication
```

### 2. 热修复流程

#### 步骤1: 创建热修复分支
```bash
# 从main分支创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-fix
```

#### 步骤2: 修复问题
```bash
# 修复问题
git add .
git commit -m "fix: 修复关键安全漏洞

- 修复SQL注入漏洞
- 加强输入验证
- 更新安全测试用例

Fixes #456"

git push origin hotfix/critical-bug-fix
```

#### 步骤3: 创建PR到main
```bash
# 创建PR从hotfix/critical-bug-fix到main
# 同时创建PR到develop分支
```

#### 步骤4: 发布新版本
```bash
# 合并到main后，创建发布标签
git tag -a v1.2.1 -m "Release version 1.2.1"
git push origin v1.2.1
```

### 3. 发布流程

#### 步骤1: 创建发布分支
```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.3.0
git push origin release/v1.3.0
```

#### 步骤2: 准备发布
```bash
# 更新版本号
# 更新CHANGELOG.md
# 运行完整测试
mvn clean test
mvn clean package

git add .
git commit -m "chore: 准备v1.3.0发布

- 更新版本号到1.3.0
- 更新CHANGELOG.md
- 完善文档"
```

#### 步骤3: 合并到main
```bash
# 创建PR从release/v1.3.0到main
# 审查通过后合并
```

#### 步骤4: 创建发布标签
```bash
git checkout main
git pull origin main
git tag -a v1.3.0 -m "Release version 1.3.0"
git push origin v1.3.0
```

#### 步骤5: 合并回develop
```bash
# 创建PR从release/v1.3.0到develop
# 确保develop包含所有发布内容
```

## 📝 提交规范

### 提交消息格式

```
<类型>(<范围>): <描述>

<详细说明>

<相关Issue>
```

#### 类型说明
- **feat**: 新功能
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码格式调整
- **refactor**: 代码重构
- **test**: 测试相关
- **chore**: 构建过程或辅助工具的变动

#### 示例
```bash
# 新功能
git commit -m "feat(auth): 添加OAuth2登录支持

- 集成Google OAuth2
- 添加用户信息同步
- 完善错误处理

Closes #123"

# 修复bug
git commit -m "fix(dao): 修复批量插入内存泄漏

- 优化批量操作内存使用
- 添加资源自动释放
- 完善异常处理

Fixes #456"

# 文档更新
git commit -m "docs: 更新API文档

- 添加新接口说明
- 完善示例代码
- 修正参数描述"
```

### 提交频率
- **小步提交**: 每完成一个小功能就提交
- **原子提交**: 每次提交只做一件事
- **清晰描述**: 提交消息要清楚说明做了什么

## 🔍 代码审查规范

### 审查检查项

#### 1. 代码质量
- [ ] 代码逻辑正确
- [ ] 变量命名清晰
- [ ] 注释充分
- [ ] 无重复代码

#### 2. 性能考虑
- [ ] 无性能瓶颈
- [ ] 内存使用合理
- [ ] 数据库查询优化

#### 3. 安全性
- [ ] 无安全漏洞
- [ ] 输入验证充分
- [ ] 权限控制正确

#### 4. 测试覆盖
- [ ] 单元测试充分
- [ ] 集成测试通过
- [ ] 边界条件测试

### 审查流程
1. **自动检查**: CI/CD自动运行测试和代码检查
2. **人工审查**: 至少1个审查者进行代码审查
3. **修改完善**: 根据审查意见修改代码
4. **重新审查**: 修改后重新提交审查
5. **合并代码**: 审查通过后合并到目标分支

## 🛠️ CI/CD集成

### GitHub Actions工作流

#### 1. 代码检查工作流
```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
```

#### 2. 代码质量检查
```yaml
# .github/workflows/code-quality.yml
name: Code Quality

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  sonar:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 0
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache SonarQube packages
      uses: actions/cache@v1
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
        restore-keys: ${{ runner.os }}-sonar
    
    - name: SonarQube Scan
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### 本地预提交检查

#### 1. 安装pre-commit
```bash
# 安装pre-commit
pip install pre-commit

# 安装hooks
pre-commit install
```

#### 2. 配置.pre-commit-config.yaml
```yaml
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
      - id: check-yaml
      - id: check-added-large-files

  - repo: https://github.com/diffplug/spotless
    rev: 6.19.0
    hooks:
      - id: spotless-maven
        args: [check]
```

## 📊 分支管理工具

### 1. Git Flow
```bash
# 安装git-flow
# macOS
brew install git-flow

# Ubuntu/Debian
sudo apt install git-flow

# 初始化git-flow
git flow init
```

### 2. 常用命令
```bash
# 开始新功能
git flow feature start user-auth

# 完成功能
git flow feature finish user-auth

# 开始热修复
git flow hotfix start critical-fix

# 完成热修复
git flow hotfix finish critical-fix
```

## 🚨 紧急情况处理

### 1. 生产环境紧急修复
```bash
# 1. 立即创建热修复分支
git checkout main
git checkout -b hotfix/emergency-fix

# 2. 快速修复问题
# 3. 提交修复
git commit -m "hotfix: 紧急修复生产环境问题"

# 4. 直接推送到main（紧急情况）
git push origin hotfix/emergency-fix:main

# 5. 事后补充PR和文档
```

### 2. 回滚策略
```bash
# 回滚到上一个版本
git checkout main
git reset --hard HEAD~1
git push origin main --force

# 创建回滚标签
git tag -a rollback-v1.2.0 -m "Rollback to v1.2.0"
git push origin rollback-v1.2.0
```

## 📚 最佳实践

### 1. 分支命名
- 使用小写字母和连字符
- 包含功能描述或issue编号
- 保持简洁明了

### 2. 提交频率
- 频繁提交，小步快跑
- 每个提交都是可工作的
- 提交前运行测试

### 3. 代码审查
- 及时响应审查请求
- 建设性的反馈意见
- 尊重不同的编程风格

### 4. 文档更新
- 代码变更时同步更新文档
- 保持README和API文档最新
- 记录重要的设计决策

## 🔗 相关链接

- [Git官方文档](https://git-scm.com/doc)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)

---

**🎯 遵循这些工作流规范，让团队协作更高效！**
