# VXCore 文档中心

欢迎来到 VXCore 项目文档中心！这里包含了项目的完整技术文档、使用指南和最佳实践。

## 📚 文档结构

### 🚀 快速开始
- [项目概述](01-overview.md) - 项目介绍和核心特性
- [快速开始](02-quick-start.md) - 5分钟快速上手
- [安装配置](03-installation.md) - 环境配置和依赖管理

### 🏗️ 架构设计
- [系统架构](04-architecture.md) - 整体架构设计
- [核心组件](05-components.md) - 核心组件详解
- [数据流设计](06-data-flow.md) - 数据流转和生命周期

### 💻 开发指南
- [DSL框架](07-dsl-framework.md) - jOOQ DSL框架使用指南
- [Lambda查询](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md) - Lambda查询详解
- [多数据源](../core-database/docs/MULTI_DATASOURCE_GUIDE.md) - 多数据源配置和使用
- [路由注解](08-routing-annotations.md) - 路由注解使用指南

### 🌐 Web开发
- [WebSocket指南](WEBSOCKET_GUIDE.md) - WebSocket开发指南
- [反向代理](WEBSOCKET_PROXY_GUIDE.md) - 反向代理配置
- [异常处理](09-exception-handling.md) - 异常处理机制
- [配置管理](10-configuration.md) - 配置管理详解

### 🔧 高级特性
- [异步编程](11-async-programming.md) - Vert.x异步编程模式
- [事务管理](12-transaction-management.md) - 事务处理最佳实践
- [性能优化](13-performance-optimization.md) - 性能调优指南
- [SPI扩展](14-spi-extension.md) - SPI扩展机制

### 🧪 测试指南
- [测试环境](15-testing-environment.md) - 测试环境搭建
- [单元测试](16-unit-testing.md) - 单元测试编写指南
- [集成测试](17-integration-testing.md) - 集成测试策略
- [测试最佳实践](18-testing-best-practices.md) - 测试最佳实践

### 📊 运维部署
- [部署指南](19-deployment.md) - 生产环境部署
- [监控日志](20-monitoring-logging.md) - 监控和日志配置
- [故障排查](21-troubleshooting.md) - 常见问题排查
- [性能监控](22-performance-monitoring.md) - 性能监控指标

### 📖 API参考
- [API文档](23-api-reference.md) - 完整API参考
- [配置参数](24-configuration.md) - 配置参数详解
- [注解参考](25-annotations.md) - 注解使用说明

### 🔄 更新记录
- [版本历史](26-version-history.md) - 版本更新记录
- [迁移指南](27-migration-guide.md) - 版本迁移指南
- [路线图](28-roadmap.md) - 未来发展规划
- [Git工作流](29-git-workflow.md) - Git工作流规范
- [优化计划](VXCORE_OPTIMIZATION_PLAN.md) - 项目优化实施计划

## 🎯 核心特性

### ✨ 主要亮点
- **🚀 高性能**: 基于Vert.x异步非阻塞I/O，支持数万并发连接
- **🔒 类型安全**: 基于jOOQ DSL编译时检查，完全防止SQL注入
- **🌐 Web开发**: 注解式路由、WebSocket、反向代理支持
- **🗄️ 多数据源**: 支持动态数据源切换和事务隔离
- **📈 易于扩展**: 支持Lambda查询、批量操作、SPI扩展
- **🔧 开发友好**: 丰富的注解、配置元数据、IDE支持

### 🏆 技术栈
- **Java 17+**: 现代Java特性支持
- **Vert.x 4.5+**: 高性能异步框架
- **jOOQ 3.19+**: 类型安全的SQL构建
- **Maven 3.8+**: 现代化构建工具
- **H2/MySQL/PostgreSQL**: 多数据库支持

### 📊 性能指标
- **并发处理**: 50,000+ QPS HTTP请求
- **WebSocket**: 10,000+ 并发连接
- **数据库查询**: 10,000+ QPS
- **批量操作**: 1000条记录 < 100ms
- **响应时间**: 微秒级延迟

## 📞 获取帮助

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
- **Lambda查询**: [Lambda查询指南](../core-database/docs/lambda/LAMBDA_QUERY_GUIDE.md)
- **WebSocket**: [WebSocket指南](WEBSOCKET_GUIDE.md)
- **多数据源**: [多数据源指南](../core-database/docs/MULTI_DATASOURCE_GUIDE.md)
- **反向代理**: [反向代理指南](WEBSOCKET_PROXY_GUIDE.md)

## 🤝 贡献指南

我们欢迎社区贡献！请查看：
- [贡献指南](CONTRIBUTING.md)
- [代码规范](CODE_STYLE.md)
- [提交流程](PULL_REQUEST_PROCESS.md)

### 📋 贡献要求
- **代码规范**: 遵循阿里巴巴Java开发规范
- **测试覆盖**: 新功能测试覆盖率 > 80%
- **文档完善**: 新功能必须包含使用文档
- **向后兼容**: 保持API向后兼容性

## 📈 项目状态

### ✅ 已完成功能 (v2.0.0)
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

---

**🎯 VXCore - 让Java Web开发更简单、更高效、更现代！**
