# VXCore 文档中心

欢迎来到 VXCore 项目文档中心！这里包含了项目的完整技术文档、使用指南和最佳实践。

## 🎯 快速导航

### 🚀 新手入门
- [项目概述](01-overview.md) - 项目介绍和核心特性
- [快速开始](02-quick-start.md) - 5分钟快速上手
- [安装配置](03-installation.md) - 环境配置和依赖管理

### 🏗️ 架构设计
- [系统架构](04-architecture.md) - 整体架构设计和设计思想
- [路由注解](08-routing-annotations.md) - 路由注解使用指南
- [代码生成器](12-code-generator.md) - 代码生成器使用指南
- [异常处理](09-exception-handling.md) - 异常处理机制
- [配置管理](10-configuration.md) - 配置管理详解

### 💻 开发指南
- [开发者指南](05-developer-guide.md) - AI Agent辅助开发指南
- [Lambda查询](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - Lambda查询详解
- [多数据源](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 多数据源配置和使用
- [WebSocket指南](WEBSOCKET_GUIDE.md) - WebSocket开发指南
- [反向代理](WEBSOCKET_PROXY_GUIDE.md) - 反向代理配置

### 📊 项目信息
- [项目工作流](29-git-workflow.md) - GitHub CI/CD工作流脚本
- [优化计划](VXCORE_OPTIMIZATION_PLAN.md) - 项目优化实施计划

## 🚀 5分钟快速体验

### 1. 克隆项目
```bash
git clone https://github.com/qaiu/vxcore.git
cd vxcore
```

### 2. 编译项目
```bash
mvn clean compile
```

### 3. 运行示例
```bash
mvn exec:java -Dexec.mainClass="cn.qaiu.example.SimpleRunner"
```

### 4. 访问 API
```bash
curl http://localhost:8080/api/hello?name=VXCore
```

## 🎯 核心特性

### ✨ 主要亮点
- **🚀 高性能**: 基于Vert.x异步非阻塞I/O，支持数万并发连接
- **🔒 类型安全**: 基于jOOQ DSL编译时检查，完全防止SQL注入
- **🌐 Web开发**: 注解式路由、WebSocket、反向代理支持
- **🗄️ 多数据源**: 支持动态数据源切换和事务隔离
- **📈 易于扩展**: 支持Lambda查询、批量操作、SPI扩展

### 🏆 技术栈
- **Java 17+**: 现代Java特性支持
- **Vert.x 4.5+**: 高性能异步框架
- **jOOQ 3.19+**: 类型安全的SQL构建
- **Maven 3.8+**: 现代化构建工具
- **H2/MySQL/PostgreSQL**: 多数据库支持

## 📚 学习路径

### 新手入门 (1-2天)
1. [项目概述](01-overview.md) - 了解框架特性
2. [快速开始](02-quick-start.md) - 创建第一个应用
3. [安装配置](03-installation.md) - 环境搭建

### 进阶开发 (3-5天)
4. [系统架构](04-architecture.md) - 理解架构设计
5. [Lambda查询](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - 掌握数据库操作
6. [多数据源](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 配置多数据源

### 高级特性 (1-2周)
7. [WebSocket指南](WEBSOCKET_GUIDE.md) - 实时通信
8. [反向代理](WEBSOCKET_PROXY_GUIDE.md) - 代理配置
9. [异常处理](09-exception-handling.md) - 错误处理机制

## 🤝 获取帮助

### 🆘 技术支持
- **GitHub Issues**: [提交问题](https://github.com/qaiu/vxcore/issues)
- **讨论区**: [技术讨论](https://github.com/qaiu/vxcore/discussions)
- **邮件支持**: qaiu@qq.com

### 📚 学习资源
- **示例代码**: `core-example/` 目录
- **测试用例**: `core-database/src/test/` 目录
- **配置示例**: `core-example/src/main/resources/` 目录
- **Lambda查询**: `core-database/docs/lambda/` 目录
- **多数据源**: `core-database/docs/MULTI_DATASOURCE_GUIDE.md`

### 🔍 快速查找
- **新手上路**: [快速开始](02-quick-start.md)
- **开发者**: [开发者指南](05-developer-guide.md)
- **Lambda查询**: [Lambda查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md)
- **WebSocket**: [WebSocket指南](WEBSOCKET_GUIDE.md)
- **多数据源**: [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md)
- **反向代理**: [反向代理指南](WEBSOCKET_PROXY_GUIDE.md)
- **CI/CD**: [项目工作流](29-git-workflow.md)

## 📈 项目状态

### ✅ 已完成功能 (v2.0.0)
- **代码生成器**: 根据数据库表结构自动生成三层架构代码
- **Lambda查询增强**: Join、聚合查询、子查询
- **批量操作**: batchInsert、batchUpdate、batchDelete
- **多数据源支持**: 动态切换、事务隔离
- **注解式路由**: 类似Spring MVC的路由注解
- **参数绑定增强**: 方法重载、类型转换
- **异常处理机制**: 全局和局部异常处理
- **WebSocket支持**: 注解式WebSocket路由
- **反向代理**: HTTP/WebSocket代理支持
- **配置元数据**: IDE自动提示和验证
- **SPI扩展**: 第三方数据库驱动扩展

### 🔄 进行中功能
- **Code-gen模板引擎**: 代码生成工具
- **HTML模板引擎**: 视图渲染支持

### 📋 计划功能
- **AOP支持**: 注解式切面编程
- **事件总线**: 注解式事件处理
- **微服务支持**: 服务发现、配置中心

## 📖 完整文档

详细的文档结构请查看 [README](README.md) 文件。

---

**🎯 VXCore - 让Java Web开发更简单、更高效、更现代！**

[开始学习 →](01-overview.md) | [快速开始 →](02-quick-start.md) | [查看GitHub →](https://github.com/qaiu/vxcore)
