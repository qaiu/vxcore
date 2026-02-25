# VXCore 文档验证问题报告

> 验证日期: 2026-02-06 ~ 2026-02-25
> 验证范围: docs/01, 02, 07, 08, 09, 10, 15
> 验证方式: 从零创建独立项目(vxcore-demo)，按文档示例编写代码并运行

---

## 一、文档与实际 API 不一致

### ISSUE-001: HttpMethod vs RouteMethod

- **涉及文档**: 01-overview.md, 02-quick-start.md
- **文档描述**: `@RouteMapping(value = "/users", method = HttpMethod.GET)`
- **实际 API**: `@RouteMapping(value = "/users", method = RouteMethod.GET)`
- **严重程度**: 高 - 按文档编写代码直接编译失败
- **建议**: 文档中所有 `HttpMethod.GET/POST/PUT/DELETE` 统一改为 `RouteMethod.GET/POST/PUT/DELETE`

### ISSUE-002: @GetRoute/@PathParam 注解不存在

- **涉及文档**: 07-di-framework.md
- **文档描述**: `@GetRoute("/{id}")`, `@PathParam Long id`
- **实际 API**: 框架中不存在这些注解
- **正确写法**: `@RouteMapping(value = "/{id}", method = RouteMethod.GET)`, 参数自动按名称绑定
- **严重程度**: 高

### ISSUE-003: @RequestParam 导入路径

- **涉及文档**: 01-overview.md, 02-quick-start.md
- **文档描述**: `import cn.qaiu.vx.core.annotations.RequestParam`
- **实际包名**: `cn.qaiu.vx.core.annotations.param.RequestParam` (注意 `param` 子包)
- **状态**: 包名拼写已修复（`annotaions` -> `annotations`），导入路径子包问题仍需文档更新

---

## 二、路由前缀重复问题

### ISSUE-004: 路由路径出现双重 /api 前缀 -- **已修复**

- **原因**: 框架 `RouterComponent` 默认 `gatewayPrefix = "api"`，与 `@RouteHandler("/api/...")` 重复
- **修复方式**: 在 `app-dev.yml` 中设置 `custom.gatewayPrefix: /` 覆盖默认值
- **建议**: 文档说明框架默认有 `api` 前缀及如何配置 `gatewayPrefix`

---

## 三、DI 依赖注入验证结果（核心） -- **已修复**

### ISSUE-005: @Inject 字段注入具体类 - 成功 ✅

- **测试**: Controller 中 `@Inject GreetingService` 字段注入
- **结果**: 成功，单例行为正常
- **修复后**: 从 ServiceRegistry 获取实例，保持单例语义

### ISSUE-006: @Inject 构造函数注入具体类 - 成功 ✅

- **测试**: Controller 构造函数标注 `@Inject`，参数为 `CounterService`
- **结果**: 成功

### ISSUE-007: @Inject 接口类型注入 -- **已修复** ✅

- **原问题**: 接口类型注入失败，因为 `resolveService()` 对接口仅走 EventBus 代理
- **修复内容**:
  1. `ServiceRegistry` 新增 `getServiceByType()` 方法，支持按类型（接口或具体类）查找
  2. `ServiceRegistry` 支持注册无接口的 `@Service` 具体类
  3. `RouterHandlerFactory.resolveService()` 优先从 ServiceRegistry 查找已注册服务实例
  4. `ServiceRegistryComponent` 在 `initialize()` 阶段完成服务注册（解决时序问题）
  5. `RouterComponent` 将 ServiceRegistry 传递给 RouterHandlerFactory
- **验证**: `@Inject MessageService messageService`（接口类型）现在成功注入 `MessageServiceImpl` 实例
- **额外验证**: 接口注入和实现类注入返回**同一实例**（单例保证）

### ISSUE-008: Dagger2 在 Controller DI 中的角色说明

- **文档描述**: 大篇幅介绍 Dagger2
- **实际情况**: Dagger2 用于服务类扫描和 ServiceRegistry 管理，Controller DI 通过 RouterHandlerFactory 反射 + ServiceRegistry 实现
- **建议**: 文档区分 Dagger2 的扫描角色和 RouterHandlerFactory 的注入角色

### ISSUE-009: @Service 无接口具体类 -- **已修复** ✅

- **原问题**: `@Service` 标注的不实现接口的类被 ServiceRegistry 跳过
- **修复**: `ServiceRegistry.analyzeServiceClass()` 不再跳过无接口类，为其创建条目
- **验证**: `CalculatorService`（无接口）成功注册并可通过 `@Inject` 注入

---

## 四、序列化与参数绑定问题

### ISSUE-010: LocalDateTime 序列化失败

- **严重程度**: 中
- **建议**: 框架自动注册 `JavaTimeModule`

### ISSUE-011: POST body 需显式 @RequestBody

- **建议**: 文档统一说明 POST/PUT 请求需要 `@RequestBody` 注解

---

## 五、优化建议汇总

| 编号 | 类别 | 建议 | 优先级 | 状态 |
|------|------|------|--------|------|
| OPT-001 | 文档 | 统一 HttpMethod -> RouteMethod，修正所有代码示例 | P0 | 待处理 |
| OPT-002 | 文档 | 删除不存在的 @GetRoute/@PathParam，使用 @RouteMapping | P0 | 待处理 |
| OPT-003 | 文档 | 说明路由前缀机制，避免 /api 重复 | P0 | 待处理 |
| OPT-004 | 文档 | DI 章节说明: Dagger2 扫描 vs RouterHandlerFactory 注入 | P1 | 待处理 |
| OPT-005 | 文档 | 说明接口注入的 ServiceRegistry 机制 | P1 | 待处理 |
| OPT-006 | 框架 | ~~resolveService() 支持查找接口实现类注入~~ | - | **已修复** |
| OPT-007 | 框架 | 自动注册 Jackson JavaTimeModule | P1 | 待处理 |
| OPT-008 | 文档 | 明确 @RequestBody 的使用场景 | P1 | 待处理 |
| OPT-009 | 框架 | ~~修正 annotations 包名拼写错误~~ | - | **已修复** |
| OPT-010 | 框架 | ~~ServiceRegistry 支持无接口具体类注册~~ | - | **已修复** |

---

## 六、全量验证结果

| 功能 | 文档 | 模块 | 结果 |
|------|------|------|------|
| @App + VXCoreApplication.run() 启动 | 01/02 | demo-01/02 | ✅ 通过 |
| @RouteHandler + @RouteMapping 路由注册 | 01/02/08 | demo-01/02/08 | ✅ 通过 |
| GET 请求参数自动绑定 | 01/02/08 | demo-01/02/08 | ✅ 通过 |
| @RequestParam 参数绑定 | 08 | demo-08 | ✅ 通过 |
| 路径参数 /:id 自动绑定 | 02/08 | demo-02/08 | ✅ 通过 |
| {id} 路径变量转换 | 08 | demo-08 | ✅ 通过 |
| POST @RequestBody JSON 绑定 | 08 | demo-08 | ✅ 通过 |
| PUT/DELETE HTTP 方法 | 08 | demo-08 | ✅ 通过 |
| @Inject 字段注入具体类 | 07 | demo-07 | ✅ 通过 |
| @Inject 构造函数注入具体类 | 07 | demo-07 | ✅ 通过 |
| @Inject 接口注入 | 07 | demo-07 | ✅ 通过 (**修复后**) |
| Controller 内 Service 单例行为 | 07 | demo-07 | ✅ 通过 |
| @Service 注解扫描（含无接口类） | 07/15 | demo-07/15 | ✅ 通过 (**修复后**) |
| DI + @Service 全链路验证 | 07/15 | demo-15 | ✅ 通过 |
| 异常处理 RuntimeException | 09 | demo-09 | ✅ 通过 |
| 异常处理 NullPointerException | 09 | demo-09 | ✅ 通过 |
| 异常处理 IllegalArgumentException | 09 | demo-09 | ✅ 通过 |
| Future.failedFuture 异步异常 | 09 | demo-09 | ✅ 通过 |
| YAML 配置加载与合并 | 10 | demo-10 | ✅ 通过 |
| SharedData 配置访问 | 10 | demo-10 | ✅ 通过 |
| 自定义配置节读取 | 10 | demo-10 | ✅ 通过 |
| AOP @Aspect 切面扫描 | 15 | demo-15 | ✅ 启动正常 |
| AOP + DI 联合验证 | 15 | demo-15 | ✅ 通过 |

---

## 七、框架核心修改记录

### 修改 1: ServiceRegistry 增强
- **文件**: `core/src/main/java/cn/qaiu/vx/core/registry/ServiceRegistry.java`
- **变更**: 
  - 新增 `servicesByType` Map，建立类型到实例的索引
  - 新增 `getServiceByType(Class<?>)` 方法，支持精确匹配和 assignable 兼容查找
  - `analyzeServiceClass()` 不再跳过无接口的 `@Service` 类
  - `registerService()` 同时注册到名称索引和类型索引

### 修改 2: RouterHandlerFactory DI 改进
- **文件**: `core/src/main/java/cn/qaiu/vx/core/handlerfactory/RouterHandlerFactory.java`
- **变更**:
  - 新增 `serviceRegistry` 字段和 `setServiceRegistry()` setter
  - `resolveService()` 重构：优先从 ServiceRegistry 查找 -> 接口 fallback EventBus -> 具体类 fallback 反射

### 修改 3: RouterComponent 集成 ServiceRegistry
- **文件**: `core/src/main/java/cn/qaiu/vx/core/lifecycle/RouterComponent.java`
- **变更**: 在 `initialize()` 中从 FrameworkLifecycleManager 获取 ServiceRegistry 并注入到 RouterHandlerFactory

### 修改 4: ServiceRegistryComponent 时序修复
- **文件**: `core/src/main/java/cn/qaiu/vx/core/lifecycle/ServiceRegistryComponent.java`
- **变更**: 在 `initialize()` 阶段完成服务注册（而非 `start()` 阶段），确保 RouterComponent 创建 Controller 时服务已可用

---

## 八、未验证的文档

| 文档 | 原因 |
|------|------|
| 06-security.md | 需要 JWT 密钥配置和安全策略设置 |
| 12-code-generator.md | 需要数据库连接 |
| 13-no-arg-constructor-dao.md | 需要数据库连接 |
