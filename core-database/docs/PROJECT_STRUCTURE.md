# Core-Database 项目结构

## 📁 目录结构

```
core-database/
├── src/                          # 源代码目录
│   ├── main/java/               # 主要源码
│   │   └── cn/qaiu/db/
│   │       ├── ddl/            # DDL相关功能
│   │       ├── dsl/            # DSL框架核心
│   │       ├── pool/           # 连接池管理
│   │       ├── server/         # 数据库服务器
│   │       └── util/           # 工具类
│   └── resources/              # 资源文件
│       ├── h2-database.json
│       ├── h2db.txt
│       └── jooq-codegen.xml
├── docs/                        # 文档目录
│   ├── README.md               # 项目说明
│   ├── PROJECT_STRUCTURE.md   # 项目结构说明
│   ├── ARCHITECTURE_FINAL.md  # 架构文档
│   ├── DSL_FRAMEWORK_SUMMARY.md # DSL框架总结
│   ├── JOOQ_DEEP_OPTIMIZATION_SUMMARY.md # jOOQ优化总结
│   ├── CLEAN_JOOQ_FRAMEWORK.md # 清理jOOQ框架文档
│   ├── TEST_*.md              # 测试相关文档
│   └── *.md                   # 其他文档
├── examples/                    # 示例代码
│   └── example/               # DSL示例
│       ├── User.java          # 用户实体示例
│       ├── UserDao.java       # 用户DAO示例
│       └── UserStatus.java    # 用户状态枚举
├── src/test/java/cn/qaiu/db/dsl/test/ # 测试代码
│   ├── SqlAuditTest.java      # SQL审计测试
│   ├── EnhancedTypeMapperTest.java # 类型映射器测试
│   └── ...                    # 其他测试
├── scripts/                     # 脚本目录
│   ├── *.sh                   # Shell脚本
│   ├── debug-scripts/         # 调试脚本
│   │   ├── README.md
│   │   └── *.java            # 调试用Java文件
│   └── logs/                  # 日志文件
│       └── *.log
├── target/                     # 构建输出目录
└── pom.xml                     # Maven配置文件
```

## 🎯 目录说明

### 📚 src/ - 源代码
- **main/java/**: 主要源代码，包含所有核心功能
- **resources/**: 配置文件和资源文件

### 📖 docs/ - 文档
- 项目说明文档
- 架构设计文档
- 测试文档
- 优化报告
- 使用指南

### 🔧 examples/ - 示例代码
- DSL框架使用示例
- 实体类示例
- DAO示例
- 最佳实践示例

### 🧪 tests/ - 测试代码
- 单元测试
- 集成测试
- 性能测试
- 功能验证测试

### 📜 scripts/ - 脚本和工具
- **Shell脚本**: 构建、测试、部署脚本
- **debug-scripts/**: 调试和开发辅助脚本
- **logs/**: 日志文件

## 🚀 使用指南

### 开发环境设置
```bash
# 运行测试
./scripts/run-tests.sh

# 运行MySQL测试
./scripts/run-mysql-tests.sh

# 运行PostgreSQL测试
./scripts/run-postgresql-tests.sh

# 检查测试环境
./scripts/check-test-env.sh

# 验证测试依赖
./scripts/verify-test-deps.sh
```

### 查看文档
```bash
# 查看项目说明
cat docs/README.md

# 查看架构文档
cat docs/ARCHITECTURE_FINAL.md

# 查看DSL框架说明
cat docs/DSL_FRAMEWORK_SUMMARY.md
```

### 运行示例
```bash
# 查看示例代码
ls examples/

# 运行示例测试
mvn test -Dtest="*Example*"
```

## 📋 开发规范

### 代码组织
- **源码**: 放在 `src/main/java/` 下
- **文档**: 放在 `docs/` 下
- **示例**: 放在 `examples/` 下
- **测试**: 放在 `tests/` 下
- **脚本**: 放在 `scripts/` 下

### 文件命名
- **Java文件**: 使用PascalCase (如: `UserDao.java`)
- **文档文件**: 使用UPPER_CASE (如: `README.md`)
- **脚本文件**: 使用kebab-case (如: `run-tests.sh`)

### 目录结构
- 保持目录结构清晰
- 相关功能放在同一目录下
- 避免在源码目录中放置非源码文件

## 🔄 维护说明

### 添加新功能
1. 在 `src/main/java/` 下添加源码
2. 在 `tests/` 下添加测试
3. 在 `docs/` 下更新文档
4. 在 `examples/` 下添加示例（如需要）

### 添加新脚本
1. 在 `scripts/` 下添加脚本
2. 确保脚本有执行权限
3. 在 `docs/` 下更新使用说明

### 添加新文档
1. 在 `docs/` 下添加文档
2. 使用清晰的命名
3. 更新 `docs/PROJECT_STRUCTURE.md`

## 📊 项目统计

- **源码文件**: 约50个Java文件
- **测试文件**: 约30个测试文件
- **文档文件**: 约15个Markdown文件
- **脚本文件**: 约5个Shell脚本
- **示例文件**: 约10个示例文件

## 🎉 优化成果

通过重新组织项目结构，实现了：

1. **清晰分离**: 源码、文档、示例、测试、脚本各司其职
2. **易于维护**: 相关文件集中管理，便于查找和维护
3. **规范统一**: 统一的命名规范和目录结构
4. **便于使用**: 清晰的目录结构便于新开发者理解项目
5. **专业标准**: 符合企业级项目的目录组织标准