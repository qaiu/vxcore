# VXCore 项目工作流

## 🎯 工作流概述

本文档描述了VXCore项目的开发工作流，包括代码提交验证、测试执行、构建打包和推送到中央仓库的完整流程。

## 🚀 快速开始

### 环境准备
```bash
# 确保Java 17+已安装
java -version

# 确保Maven 3.8+已安装
mvn -version

# 克隆项目
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

### 本地开发流程
```bash
# 1. 拉取最新代码
git pull origin main

# 2. 创建功能分支
git checkout -b feature/your-feature-name

# 3. 进行开发
# ... 编写代码 ...

# 4. 运行测试验证
mvn clean test

# 5. 提交代码
git add .
git commit -m "feat: 添加新功能"

# 6. 推送到远程仓库
git push origin feature/your-feature-name
```

## 🔧 提交验证流程

### 1. 代码质量检查

#### 编译检查
```bash
# 编译所有模块
mvn clean compile

# 检查编译错误
mvn compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请修复错误后重试"
    exit 1
fi
echo "✅ 编译成功"
```

#### 代码风格检查
```bash
# 运行代码格式化
mvn spotless:apply

# 检查代码风格
mvn spotless:check
if [ $? -ne 0 ]; then
    echo "❌ 代码风格检查失败，请运行 mvn spotless:apply"
    exit 1
fi
echo "✅ 代码风格检查通过"
```

#### 静态代码分析
```bash
# 运行SpotBugs静态分析
mvn spotbugs:check
if [ $? -ne 0 ]; then
    echo "❌ 静态代码分析发现问题，请查看target/spotbugsXml.xml"
    exit 1
fi
echo "✅ 静态代码分析通过"
```

### 2. 单元测试验证

#### 运行所有测试
```bash
# 运行所有单元测试
mvn clean test

# 检查测试结果
if [ $? -ne 0 ]; then
    echo "❌ 单元测试失败，请查看测试报告"
    exit 1
fi
echo "✅ 所有单元测试通过"
```

#### 测试覆盖率检查
```bash
# 生成测试覆盖率报告
mvn jacoco:report

# 检查覆盖率阈值
mvn jacoco:check
if [ $? -ne 0 ]; then
    echo "❌ 测试覆盖率不足，请查看target/site/jacoco/index.html"
    exit 1
fi
echo "✅ 测试覆盖率达标"
```

### 3. 集成测试验证

#### 数据库集成测试
```bash
# 运行H2数据库测试
mvn test -Dtest="*H2*"

# 运行PostgreSQL测试（需要本地PostgreSQL）
mvn test -Dtest="*PostgreSQL*"

# 运行MySQL测试（需要本地MySQL）
mvn test -Dtest="*MySQL*"
```

#### Web服务集成测试
```bash
# 启动示例服务进行集成测试
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner" &
SERVER_PID=$!

# 等待服务启动
sleep 10

# 测试HTTP接口
curl -f http://localhost:8080/api/hello?name=Test
if [ $? -ne 0 ]; then
    echo "❌ HTTP接口测试失败"
    kill $SERVER_PID
    exit 1
fi

# 测试WebSocket接口
# 这里可以使用WebSocket客户端工具进行测试

# 停止服务
kill $SERVER_PID
echo "✅ 集成测试通过"
```

## 📦 构建打包流程

### 1. 完整构建
```bash
# 清理并构建所有模块
mvn clean package -DskipTests=false

# 检查构建结果
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi
echo "✅ 构建成功"
```

### 2. 生成文档
```bash
# 生成JavaDoc文档
mvn javadoc:javadoc

# 生成项目文档
mvn site

# 检查文档生成
if [ ! -d "target/site" ]; then
    echo "❌ 文档生成失败"
    exit 1
fi
echo "✅ 文档生成成功"
```

### 3. 打包发布
```bash
# 创建发布包
mvn clean package -DskipTests=false -P release

# 检查发布包
ls -la target/*.jar
if [ $? -ne 0 ]; then
    echo "❌ 发布包生成失败"
    exit 1
fi
echo "✅ 发布包生成成功"
```

## 🚀 推送到中央仓库

### 1. 本地验证脚本

创建 `scripts/validate-before-push.sh`：
```bash
#!/bin/bash
set -e

echo "🔍 开始提交前验证..."

# 1. 编译检查
echo "📦 检查编译..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译通过"

# 2. 代码风格检查
echo "🎨 检查代码风格..."
mvn spotless:check -q
if [ $? -ne 0 ]; then
    echo "❌ 代码风格检查失败，请运行: mvn spotless:apply"
    exit 1
fi
echo "✅ 代码风格检查通过"

# 3. 静态分析
echo "🔍 运行静态分析..."
mvn spotbugs:check -q
if [ $? -ne 0 ]; then
    echo "❌ 静态分析发现问题"
    exit 1
fi
echo "✅ 静态分析通过"

# 4. 单元测试
echo "🧪 运行单元测试..."
mvn test -q
if [ $? -ne 0 ]; then
    echo "❌ 单元测试失败"
    exit 1
fi
echo "✅ 单元测试通过"

# 5. 测试覆盖率
echo "📊 检查测试覆盖率..."
mvn jacoco:check -q
if [ $? -ne 0 ]; then
    echo "❌ 测试覆盖率不足"
    exit 1
fi
echo "✅ 测试覆盖率达标"

# 6. 构建验证
echo "🏗️ 验证构建..."
mvn package -q -DskipTests=true
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi
echo "✅ 构建验证通过"

echo "🎉 所有验证通过，可以安全推送！"
```

### 2. Git Hook配置

#### 安装pre-commit hook
```bash
# 创建hooks目录
mkdir -p .git/hooks

# 创建pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# 运行验证脚本
./scripts/validate-before-push.sh
EOF

# 设置执行权限
chmod +x .git/hooks/pre-commit
```

#### 安装pre-push hook
```bash
# 创建pre-push hook
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash
# 推送前运行完整验证
echo "🚀 准备推送到远程仓库..."
./scripts/validate-before-push.sh

# 检查是否有未提交的更改
if ! git diff-index --quiet HEAD --; then
    echo "❌ 有未提交的更改，请先提交"
    exit 1
fi

echo "✅ 可以安全推送到远程仓库"
EOF

# 设置执行权限
chmod +x .git/hooks/pre-push
```

### 3. 推送流程

#### 标准推送流程
```bash
# 1. 确保在正确的分支
git checkout main
git pull origin main

# 2. 运行验证脚本
./scripts/validate-before-push.sh

# 3. 推送到远程仓库
git push origin main

# 4. 验证推送结果
git log --oneline -1
echo "✅ 代码已成功推送到远程仓库"
```

#### 功能分支推送流程
```bash
# 1. 创建功能分支
git checkout -b feature/new-feature

# 2. 开发功能
# ... 编写代码 ...

# 3. 提交更改
git add .
git commit -m "feat: 添加新功能"

# 4. 运行验证
./scripts/validate-before-push.sh

# 5. 推送功能分支
git push origin feature/new-feature

# 6. 创建Pull Request
echo "请访问GitHub创建Pull Request: https://github.com/qaiu/vxcore/compare/main...feature/new-feature"
```

## 🔄 CI/CD集成

### GitHub Actions工作流脚本

#### 1. 主CI工作流 (.github/workflows/ci.yml)
```yaml
name: VXCore CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  MAVEN_OPTS: -Xmx1024m

jobs:
  # 编译检查
  compile:
    name: Compile Check
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Compile project
      run: mvn clean compile -q
      
    - name: Check for compilation errors
      run: |
        if [ $? -ne 0 ]; then
          echo "❌ Compilation failed"
          exit 1
        fi
        echo "✅ Compilation successful"

  # 代码质量检查
  quality:
    name: Code Quality
    runs-on: ubuntu-latest
    needs: compile
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Check code style
      run: mvn spotless:check
      
    - name: Run static analysis
      run: mvn spotbugs:check
      
    - name: Upload SpotBugs results
      uses: actions/upload-artifact@v3
      if: failure()
      with:
        name: spotbugs-results
        path: target/spotbugsXml.xml

  # 单元测试
  test:
    name: Unit Tests
    runs-on: ubuntu-latest
    needs: compile
    
    strategy:
      matrix:
        java-version: [17, 21]
        
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK ${{ matrix.java-version }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ matrix.java-version }}
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run unit tests
      run: mvn clean test
      
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests (Java ${{ matrix.java-version }})
        path: target/surefire-reports/*.xml
        reporter: java-junit
        
    - name: Generate coverage report
      run: mvn jacoco:report
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella
        fail_ci_if_error: false

  # 集成测试
  integration-test:
    name: Integration Tests
    runs-on: ubuntu-latest
    needs: [compile, test]
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: testdb
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
          
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run PostgreSQL tests
      run: mvn test -Dtest="*PostgreSQL*" -Dspring.profiles.active=test
      env:
        POSTGRES_URL: jdbc:postgresql://localhost:5432/testdb
        POSTGRES_USER: postgres
        POSTGRES_PASSWORD: postgres
        
    - name: Run H2 tests
      run: mvn test -Dtest="*H2*"
      
    - name: Start example service
      run: |
        mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner" &
        sleep 15
        
    - name: Test HTTP endpoints
      run: |
        curl -f http://localhost:8080/api/hello?name=Test
        curl -f http://localhost:8080/api/users
        
    - name: Stop example service
      run: pkill -f "cn.qaiu.example.SimpleRunner"

  # 构建验证
  build:
    name: Build Verification
    runs-on: ubuntu-latest
    needs: [compile, test, integration-test]
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Build project
      run: mvn clean package -DskipTests=true
      
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: vxcore-jars
        path: target/*.jar
        retention-days: 7
        
    - name: Generate site documentation
      run: mvn site
      
    - name: Upload site documentation
      uses: actions/upload-artifact@v3
      with:
        name: site-docs
        path: target/site
        retention-days: 7

  # 安全扫描
  security:
    name: Security Scan
    runs-on: ubuntu-latest
    needs: compile
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run OWASP dependency check
      run: mvn org.owasp:dependency-check-maven:check
      
    - name: Upload security report
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: security-report
        path: target/dependency-check-report.html
```

#### 2. 代码质量检查工作流 (.github/workflows/code-quality.yml)
```yaml
name: Code Quality Check

on:
  pull_request:
    branches: [ main, develop ]
  schedule:
    - cron: '0 2 * * 1'  # 每周一凌晨2点运行

jobs:
  code-quality:
    name: Code Quality Analysis
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        fetch-depth: 0
        
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run Spotless check
      run: mvn spotless:check
      
    - name: Run SpotBugs analysis
      run: mvn spotbugs:check
      
    - name: Run PMD analysis
      run: mvn pmd:check
      
    - name: Run Checkstyle
      run: mvn checkstyle:check
      
    - name: Upload quality reports
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: quality-reports
        path: |
          target/spotbugsXml.xml
          target/pmd.xml
          target/checkstyle-result.xml
        retention-days: 30

  sonarcloud:
    name: SonarCloud Analysis
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        fetch-depth: 0
        
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run tests with coverage
      run: mvn clean test jacoco:report
      
    - name: SonarCloud Scan
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

#### 3. 发布工作流 (.github/workflows/release.yml)
```yaml
name: Release

on:
  push:
    tags:
      - 'v*.*.*'
  workflow_dispatch:
    inputs:
      version:
        description: 'Release version'
        required: true
        default: '1.0.0'

env:
  MAVEN_OPTS: -Xmx1024m

jobs:
  release:
    name: Create Release
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Set version
      run: |
        if [ "${{ github.event_name }}" = "workflow_dispatch" ]; then
          echo "VERSION=${{ github.event.inputs.version }}" >> $GITHUB_ENV
        else
          echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_ENV
        fi
        
    - name: Update version in pom.xml
      run: mvn versions:set -DnewVersion=${{ env.VERSION }}
      
    - name: Run full test suite
      run: mvn clean test
      
    - name: Build project
      run: mvn clean package -DskipTests=false
      
    - name: Generate changelog
      id: changelog
      run: |
        if [ -f CHANGELOG.md ]; then
          echo "CHANGELOG<<EOF" >> $GITHUB_OUTPUT
          cat CHANGELOG.md >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT
        else
          echo "CHANGELOG=No changelog available" >> $GITHUB_OUTPUT
        fi
        
    - name: Create Release
      uses: actions/create-release@v1
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        tag_name: v${{ env.VERSION }}
        release_name: VXCore v${{ env.VERSION }}
        body: |
          ## VXCore v${{ env.VERSION }}
          
          ### Changes
          ${{ steps.changelog.outputs.CHANGELOG }}
          
          ### Downloads
          - JAR files are attached to this release
          - Documentation is available in the site artifacts
        draft: false
        prerelease: false
        
    - name: Upload Release Assets
      uses: actions/upload-release-asset@v1
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        upload_url: ${{ steps.create_release.outputs.upload_url }}
        asset_path: target/vxcore-core-${{ env.VERSION }}.jar
        asset_name: vxcore-core-${{ env.VERSION }}.jar
        asset_content_type: application/java-archive
        
    - name: Upload Database Module
      uses: actions/upload-release-asset@v1
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        upload_url: ${{ steps.create_release.outputs.upload_url }}
        asset_path: target/vxcore-database-${{ env.VERSION }}.jar
        asset_name: vxcore-database-${{ env.VERSION }}.jar
        asset_content_type: application/java-archive
        
    - name: Upload Example Module
      uses: actions/upload-release-asset@v1
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        upload_url: ${{ steps.create_release.outputs.upload_url }}
        asset_path: target/vxcore-example-${{ env.VERSION }}.jar
        asset_name: vxcore-example-${{ env.VERSION }}.jar
        asset_content_type: application/java-archive

  publish:
    name: Publish to Maven Central
    runs-on: ubuntu-latest
    needs: release
    if: github.event_name == 'push' && startsWith(github.ref, 'refs/tags/v')
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Set version
      run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_ENV
      
    - name: Update version in pom.xml
      run: mvn versions:set -DnewVersion=${{ env.VERSION }}
      
    - name: Configure GPG
      run: |
        echo "${{ secrets.GPG_PRIVATE_KEY }}" | base64 -d > private.key
        gpg --import private.key
        gpg --list-secret-keys
        
    - name: Publish to Maven Central
      run: mvn clean deploy -P release
      env:
        MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
        GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

#### 4. 性能测试工作流 (.github/workflows/performance.yml)
```yaml
name: Performance Tests

on:
  schedule:
    - cron: '0 3 * * 0'  # 每周日凌晨3点运行
  workflow_dispatch:

jobs:
  performance-test:
    name: Performance Benchmark
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Build project
      run: mvn clean package -DskipTests=true
      
    - name: Start application
      run: |
        java -jar target/vxcore-example-*.jar &
        sleep 15
        
    - name: Install Apache Bench
      run: sudo apt-get update && sudo apt-get install -y apache2-utils
        
    - name: Run HTTP performance test
      run: |
        ab -n 10000 -c 100 http://localhost:8080/api/hello?name=Test > http-performance.txt
        
    - name: Run database performance test
      run: |
        ab -n 5000 -c 50 http://localhost:8080/api/users > db-performance.txt
        
    - name: Stop application
      run: pkill -f "vxcore-example"
      
    - name: Upload performance results
      uses: actions/upload-artifact@v3
      with:
        name: performance-results
        path: |
          http-performance.txt
          db-performance.txt
        retention-days: 30
        
    - name: Comment performance results
      if: github.event_name == 'workflow_dispatch'
      uses: actions/github-script@v6
      with:
        script: |
          const fs = require('fs');
          const httpPerf = fs.readFileSync('http-performance.txt', 'utf8');
          const dbPerf = fs.readFileSync('db-performance.txt', 'utf8');
          
          github.rest.issues.createComment({
            issue_number: context.issue.number,
            owner: context.repo.owner,
            repo: context.repo.repo,
            body: `## Performance Test Results
            
            ### HTTP Performance
            \`\`\`
            ${httpPerf}
            \`\`\`
            
            ### Database Performance
            \`\`\`
            ${dbPerf}
            \`\`\`
            `
          });
```

#### 5. 依赖更新工作流 (.github/workflows/dependency-update.yml)
```yaml
name: Dependency Update

on:
  schedule:
    - cron: '0 1 * * 1'  # 每周一凌晨1点运行
  workflow_dispatch:

jobs:
  dependency-update:
    name: Update Dependencies
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Check for dependency updates
      run: mvn versions:display-dependency-updates
      
    - name: Check for plugin updates
      run: mvn versions:display-plugin-updates
      
    - name: Update dependencies
      run: mvn versions:use-latest-versions
      
    - name: Create Pull Request
      uses: peter-evans/create-pull-request@v5
      with:
        token: ${{ secrets.GITHUB_TOKEN }}
        commit-message: 'chore: update dependencies to latest versions'
        title: 'chore: update dependencies'
        body: |
          ## Dependency Updates
          
          This PR updates project dependencies to their latest versions.
          
          ### Changes
          - Updated Maven dependencies
          - Updated Maven plugins
          - Updated Java version if applicable
          
          ### Testing
          - [ ] All tests pass
          - [ ] No breaking changes detected
          - [ ] Performance benchmarks maintained
        branch: dependency-update
        delete-branch: true
```

## 📊 质量指标

### 1. 代码质量指标
- **编译成功率**: 100%
- **单元测试覆盖率**: > 80%
- **集成测试覆盖率**: > 70%
- **代码重复率**: < 5%
- **圈复杂度**: < 10

### 2. 性能指标
- **构建时间**: < 5分钟
- **测试执行时间**: < 3分钟
- **内存使用**: < 512MB
- **启动时间**: < 10秒

### 3. 安全指标
- **安全漏洞**: 0个
- **依赖漏洞**: 0个
- **代码扫描**: 通过

## 🛠️ 开发工具配置

### 1. IDE配置

#### IntelliJ IDEA
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <option name="name" value="java" />
          <option name="value" value="java.*" />
          <option name="name" value="javax" />
          <option name="value" value="javax.*" />
          <option name="name" value="org" />
          <option name="value" value="org.*" />
          <option name="name" value="com" />
          <option name="value" value="com.*" />
          <option name="name" value="cn" />
          <option name="value" value="cn.*" />
          <option name="name" value="BLANK_LINE" />
          <option name="value" value="" />
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

#### VS Code
```json
// .vscode/settings.json
{
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.format.settings.profile": "GoogleStyle",
  "java.saveActions.organizeImports": true,
  "java.compile.nullAnalysis.mode": "automatic"
}
```

### 2. Maven配置

#### settings.xml
```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <profiles>
    <profile>
      <id>vxcore</id>
      <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>vxcore</activeProfile>
  </activeProfiles>
</settings>
```

## 🚨 故障排除

### 1. 常见问题

#### 编译失败
```bash
# 清理并重新编译
mvn clean compile

# 检查Java版本
java -version
mvn -version

# 检查Maven配置
mvn help:effective-settings
```

#### 测试失败
```bash
# 运行单个测试
mvn test -Dtest=TestClassName

# 跳过测试
mvn package -DskipTests=true

# 查看测试报告
open target/surefire-reports/index.html
```

#### 推送失败
```bash
# 检查远程仓库配置
git remote -v

# 检查分支状态
git status
git branch -a

# 强制推送（谨慎使用）
git push origin main --force
```

### 2. 性能优化

#### 构建优化
```bash
# 并行构建
mvn -T 4 clean package

# 跳过非必要插件
mvn clean package -DskipTests=true -Dmaven.javadoc.skip=true

# 使用本地仓库缓存
mvn -o clean package
```

#### 测试优化
```bash
# 并行测试
mvn test -T 4

# 跳过集成测试
mvn test -DskipITs=true

# 使用测试配置文件
mvn test -P test-profile
```

## 📚 最佳实践

### 1. 提交规范
- **原子提交**: 每次提交只做一件事
- **清晰描述**: 提交消息要清楚说明做了什么
- **类型前缀**: 使用feat、fix、docs等前缀
- **关联Issue**: 在提交消息中关联相关Issue

### 2. 测试策略
- **测试驱动**: 先写测试，再写实现
- **边界测试**: 测试边界条件和异常情况
- **集成测试**: 确保各模块协同工作
- **性能测试**: 定期进行性能基准测试

### 3. 代码质量
- **代码审查**: 所有代码都要经过审查
- **静态分析**: 使用工具进行代码质量检查
- **文档更新**: 代码变更时同步更新文档
- **版本控制**: 使用语义化版本号

## 🔗 相关资源

- [Maven官方文档](https://maven.apache.org/guides/)
- [JUnit 5用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [GitHub Actions文档](https://docs.github.com/en/actions)
- [SpotBugs用户指南](https://spotbugs.github.io/spotbugs/)

---

**🎯 遵循这个工作流，确保代码质量和项目稳定性！**
