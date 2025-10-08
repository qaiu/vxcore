# VXCore

一个基于 Vert.x 的强大 DSL 框架，集成了 jOOQ 用于数据库操作。

## 项目简介

VXCore 是一个现代化的 Java 框架，专为构建高性能、响应式的应用程序而设计。它结合了 Vert.x 的异步编程模型和 jOOQ 的类型安全数据库操作，提供了简洁而强大的 DSL（领域特定语言）来简化数据库交互。

## 主要特性

- 🚀 **高性能异步**: 基于 Vert.x 的响应式编程模型
- 🔒 **类型安全**: 使用 jOOQ 提供编译时类型检查
- 📝 **简洁 DSL**: 直观的领域特定语言，简化数据库操作
- 🗄️ **多数据库支持**: 支持 H2、MySQL、PostgreSQL
- 🧪 **完整测试**: 包含全面的单元测试和集成测试
- 📦 **模块化设计**: 清晰的模块分离，易于扩展

## 项目结构

```
vxcore/
├── core/                    # 核心框架模块
│   ├── src/main/java/      # 核心 Java 源码
│   └── pom.xml             # 核心模块配置
├── core-database/          # 数据库操作模块
│   ├── src/main/java/      # 数据库相关源码
│   ├── src/test/java/      # 测试代码
│   ├── examples/           # 使用示例
│   ├── docs/               # 文档
│   └── pom.xml             # 数据库模块配置
└── pom.xml                 # 根项目配置
```

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- 数据库（H2/MySQL/PostgreSQL）

### 安装

1. 克隆项目：
```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

2. 编译项目：
```bash
mvn clean compile
```

3. 运行测试：
```bash
mvn test
```

### 基本使用

#### 1. 配置数据库连接

```java
// H2 数据库配置
JsonObject config = new JsonObject()
    .put("url", "jdbc:h2:mem:testdb")
    .put("username", "sa")
    .put("password", "");
```

#### 2. 创建实体类

```java
@Table("users")
public class User extends BaseEntity {
    @Id
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    // getters and setters...
}
```

#### 3. 使用 DSL 进行数据库操作

```java
// 创建用户
User user = new User();
user.setUsername("john_doe");
user.setEmail("john@example.com");

userDao.create(user)
    .onSuccess(createdUser -> {
        System.out.println("用户创建成功: " + createdUser.getId());
    })
    .onFailure(throwable -> {
        System.err.println("创建失败: " + throwable.getMessage());
    });

// 查询用户
userDao.findByUsername("john_doe")
    .onSuccess(user -> {
        System.out.println("找到用户: " + user.getEmail());
    });
```

## 支持的数据库

### H2
- 用于开发和测试
- 内存数据库，无需额外配置

### MySQL
- 生产环境推荐
- 支持完整的 SQL 特性

### PostgreSQL
- 企业级应用
- 支持高级数据类型和功能

## 示例代码

查看 `core-database/examples/` 目录获取更多使用示例：

- `DemoRunner.java` - 基本使用示例
- `DslExampleVerticle.java` - DSL 使用示例
- `JooqExampleVerticle.java` - jOOQ 集成示例

## 测试

项目包含完整的测试套件：

```bash
# 运行所有测试
mvn test

# 运行特定数据库测试
mvn test -Dtest=*H2*
mvn test -Dtest=*MySQL*
mvn test -Dtest=*PostgreSQL*
```

## 文档

详细文档位于 `core-database/docs/` 目录：

- `ARCHITECTURE_FINAL.md` - 架构设计
- `DSL_FRAMEWORK_SUMMARY.md` - DSL 框架总结
- `TEST_SETUP_GUIDE.md` - 测试环境配置指南

## 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 作者: QAIU
- 邮箱: qaiu@qaiu.top
- 网站: https://qaiu.top
- GitHub: https://github.com/qaiu

## 更新日志

### v1.0.0
- 初始版本发布
- 支持 H2、MySQL、PostgreSQL
- 完整的 DSL 框架
- 集成 jOOQ 支持
- 全面的测试覆盖
