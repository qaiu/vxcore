# 路径变量自动绑定功能指南

## 概述

VXCore 框架现已支持更灵活的路径变量绑定方式，无需显式指定 `@PathVariable` 注解的值，即可自动根据参数名称进行映射。同时新增了路由自动优先级排序功能。

## 功能特性

### 1. PathVariable 注解自动参数名映射

当 `@PathVariable` 注解不填写 `value` 属性时，框架会自动根据方法参数名称进行映射。

**示例：**

```java
// 旧方式：必须指定注解值
@RouteMapping(value = "/{id}", method = RouteMethod.GET)
public Future<JsonResult<String>> getUserById(@PathVariable("id") Long id) {
    return Future.succeededFuture(JsonResult.data("User ID: " + id));
}

// 新方式：自动根据参数名称映射（需要注解但不需要指定值）
@RouteMapping(value = "/{userId}", method = RouteMethod.GET)
public Future<JsonResult<String>> getUserById(@PathVariable Long userId) {
    return Future.succeededFuture(JsonResult.data("User ID: " + userId));
}
```

### 2. 使用 `:语法` 的路径变量自动绑定（无需注解）

框架现在支持 Vert.x Web 原生的 `:变量名` 语法，并且可以自动绑定到方法参数，**无需任何注解**。

**示例：**

```java
// 单个路径变量 - 无需注解
@RouteMapping(value = "test/:id", method = RouteMethod.GET)
public Future<JsonResult<String>> getById(Long id) {
    return Future.succeededFuture(JsonResult.data("ID: " + id));
}

// 多个路径变量 - 无需注解
@RouteMapping(value = "test/:id/:name", method = RouteMethod.GET)
public Future<JsonResult<String>> getByIdAndName(Long id, String name) {
    return Future.succeededFuture(JsonResult.data("ID: " + id + ", Name: " + name));
}

// 混合使用 {variable} 和 :variable 语法
@RouteMapping(value = "{userId}/detail/:type", method = RouteMethod.GET)
public Future<JsonResult<String>> getUserDetail(Long userId, String type) {
    return Future.succeededFuture(JsonResult.data("User ID: " + userId + ", Type: " + type));
}
```

## 使用要求

### Maven 配置

为了支持无注解的参数名称自动绑定，需要在编译时保留参数名称信息。请确保 `pom.xml` 中包含以下配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>${java.version}</release>
        <!-- 保留参数名称以支持无注解的参数绑定 -->
        <parameters>true</parameters>
    </configuration>
</plugin>
```

**注意**: 如果编译时未启用 `-parameters` 标志，框架会自动使用 Javassist 作为备用方案来获取参数名称。

### 3. 路由自动优先级排序

框架现在支持智能的路由优先级排序。当用户不配置 `order` 属性时，框架会根据路径复杂度自动排序。

**优先级规则**：
- 固定路径段 > 路径变量段
- 路径段数相同时，固定段越多优先级越高
- 具体路径优先匹配，参数路径作为兜底

**评分机制**：
- 每个固定路径段：+100分
- 每个路径变量（`:var` 或 `{var}`）：+10分
- 分数高的路由优先匹配

**示例**：

```java
// 自动优先级排序（无需指定order）
@RouteHandler("/api/users")
public class UserController {

    // 优先级1: /api/users/priority/fixed/path (得分: 300)
    @RouteMapping(value = "priority/fixed/path", method = RouteMethod.GET)
    public Future<JsonResult<String>> fixedPath() {
        return Future.succeededFuture(JsonResult.data("匹配固定路径"));
    }

    // 优先级2: /api/users/priority/fixed/:param (得分: 210)
    @RouteMapping(value = "priority/fixed/:param", method = RouteMethod.GET)
    public Future<JsonResult<String>> partialParam(String param) {
        return Future.succeededFuture(JsonResult.data("匹配部分参数路径: " + param));
    }

    // 优先级3: /api/users/priority/:id/:name (得分: 120)
    @RouteMapping(value = "priority/:id/:name", method = RouteMethod.GET)
    public Future<JsonResult<String>> fullParam(Long id, String name) {
        return Future.succeededFuture(JsonResult.data("匹配全参数路径: " + id + "/" + name));
    }
}
```

**路由匹配顺序**：
```
请求: GET /api/users/priority/fixed/path
匹配: fixedPath() - 完全匹配

请求: GET /api/users/priority/fixed/abc
匹配: partialParam("abc") - 固定段匹配

请求: GET /api/users/priority/123/test
匹配: fullParam(123, "test") - 参数段匹配
```

**显式优先级**：用户仍可使用 `order` 属性显式指定优先级，显式优先级始终优先于自动计算的优先级。

```java
// 即使路径简单，也会因为 order=100 而优先注册
@RouteMapping(value = ":id", method = RouteMethod.GET, order = 100)
public Future<JsonResult<String>> highPriority(Long id) {
    return Future.succeededFuture(JsonResult.data("高优先级路由"));
}
```

### 路径变量语法对比

| 语法格式 | 示例 | 是否需要注解 | 说明 |
|---------|------|------------|------|
| `{变量名}` | `/{id}` | 推荐使用 `@PathVariable` | Spring 风格，推荐使用注解 |
| `:变量名` | `/:id` | 无需注解 | Vert.x 原生风格，自动绑定 |

## 完整示例

以下是在 `UserController` 中添加的测试用例：

```java
@RouteHandler("/api/users")
public class UserController {

    // ========== 参数绑定测试 ==========

    /**
     * 测试1: PathVariable注解不填写字段名时自动根据参数名称映射
     */
    @RouteMapping(value = "test1/:userId", method = RouteMethod.GET, order = 100)
    public Future<JsonResult<String>> testPathVariableWithoutValue(Long userId) {
        return Future.succeededFuture(JsonResult.data("userId: " + userId));
    }

    /**
     * 测试2: 使用 :语法 的路径变量自动绑定（无注解）
     */
    @RouteMapping(value = "test2/:id", method = RouteMethod.GET, order = 100)
    public Future<JsonResult<String>> testColonSyntaxAutoBinding(Long id) {
        return Future.succeededFuture(JsonResult.data("id: " + id));
    }

    /**
     * 测试3: 多个路径变量自动绑定（无注解）
     */
    @RouteMapping(value = "test3/:id/:name", method = RouteMethod.GET, order = 100)
    public Future<JsonResult<String>> testMultipleColonSyntax(Long id, String name) {
        return Future.succeededFuture(JsonResult.data("id: " + id + ", name: " + name));
    }

    /**
     * 测试4: 混合使用 {variable} 和 :variable 语法
     */
    @RouteMapping(value = "test4/{userId}/detail/:type", method = RouteMethod.GET, order = 100)
    public Future<JsonResult<String>> testMixedSyntax(Long userId, String type) {
        return Future.succeededFuture(JsonResult.data("userId: " + userId + ", type: " + type));
    }

    // ========== 路由优先级自动排序测试 ==========

    /**
     * 测试5: 固定路径（最高优先级）
     */
    @RouteMapping(value = "priority/fixed/path", method = RouteMethod.GET)
    public Future<JsonResult<String>> testPriorityFixedPath() {
        return Future.succeededFuture(JsonResult.data("匹配固定路径"));
    }

    /**
     * 测试6: 部分参数路径（中等优先级）
     */
    @RouteMapping(value = "priority/fixed/:param", method = RouteMethod.GET)
    public Future<JsonResult<String>> testPriorityPartialParam(String param) {
        return Future.succeededFuture(JsonResult.data("匹配部分参数: " + param));
    }

    /**
     * 测试7: 全参数路径（最低优先级）
     */
    @RouteMapping(value = "priority/:id/:name", method = RouteMethod.GET)
    public Future<JsonResult<String>> testPriorityFullParam(Long id, String name) {
        return Future.succeededFuture(JsonResult.data("匹配全参数: " + id + "/" + name));
    }
}
```

## 测试 API 端点

启动应用后，可以通过以下 curl 命令测试：

```bash
# ========== 参数绑定测试 ==========

# 测试1: PathVariable无注解值
curl http://localhost:8080/api/users/test1/123

# 测试2: 冒号语法自动绑定
curl http://localhost:8080/api/users/test2/456

# 测试3: 多参数自动绑定
curl http://localhost:8080/api/users/test3/789/张三

# 测试4: 混合语法
curl http://localhost:8080/api/users/test4/111/detail/full

# ========== 路由优先级测试 ==========

# 测试5: 匹配固定路径
curl http://localhost:8080/api/users/priority/fixed/path
# 预期输出: "匹配固定路径"

# 测试6: 匹配部分参数路径
curl http://localhost:8080/api/users/priority/fixed/abc
# 预期输出: "匹配部分参数: abc"

# 测试7: 匹配全参数路径
curl http://localhost:8080/api/users/priority/123/test
# 预期输出: "匹配全参数: 123/test"
```

## 类型转换

框架支持自动类型转换，支持的类型包括：
- String
- Integer / int
- Long / long
- Double / double
- Float / float
- Boolean / boolean
- Character / char
- Short / short
- Byte / byte
- Enum 枚举类型

## 注意事项

1. **参数名称要求**：使用无注解绑定时，参数名称必须与路径变量名称完全一致（区分大小写）
2. **编译配置**：
   - 推荐启用 `-parameters` 编译器标志以获得最佳性能
   - 如未启用，框架会自动使用 Javassist 作为备用方案
3. **推荐实践**：虽然支持无注解绑定，但在复杂项目中仍建议使用 `@PathVariable` 注解以提高代码可读性
4. **路径变量格式**：
   - `{variable}` 格式会自动转换为 `:variable` 格式
   - 两种格式可以混合使用
   - 推荐在新项目中统一使用一种格式
5. **路由优先级**：
   - 显式指定的 `order` 优先级始终优先于自动计算
   - 自动优先级仅在 `order=0`（默认值）时生效
   - 路由注册顺序：高 order 值 → 低 order 值 → 自动优先级排序
6. **日志输出**：路由注册时会在日志中显示完整的路径格式（包括 `:变量`），方便调试

## 实现原理

### 参数名称获取

1. **Java反射（优先）**：通过 `-parameters` 编译器标志保留参数名称信息
2. **Javassist（备用）**：当 Java 反射无法获取参数名称时，自动使用 Javassist 从字节码中提取参数信息
3. **双重保障**：确保在任何编译配置下都能正常工作

### 自动绑定流程

1. `ParamUtil.getParameterValue()` 检查是否有 `@PathVariable` 注解
2. 如果有注解但未指定值，使用参数名称作为变量名
3. 如果没有注解，尝试按参数名称从路径参数中获取值
4. `ParamUtil.convertValue()` 自动将字符串转换为目标类型

### 路由优先级排序

1. `getRouteMethods()` 方法使用自定义的 Comparator 对路由进行排序
2. 优先按照用户指定的 `order` 值排序（降序）
3. 当 `order` 值相同时，调用 `calculatePathPriority()` 计算路径复杂度
4. `calculatePathPriority()` 根据固定段和参数段的数量计算分数
5. 分数高的路由优先注册到 Vert.x Router

### 路径变量转换

1. `convertPathVariables()` 将 `{variable}` 格式转换为 `:variable` 格式
2. 只在实际发生转换时记录 DEBUG 日志，避免日志冗余
3. 转换后的路径用于日志输出和路由注册

## 相关文件

- 路径变量注解：[PathVariable.java](../core/src/main/java/cn/qaiu/vx/core/annotations/param/PathVariable.java)
- 参数工具类：[ParamUtil.java](../core/src/main/java/cn/qaiu/vx/core/util/ParamUtil.java)
- 参数匹配器：[ParameterMatcher.java](../core/src/main/java/cn/qaiu/vx/core/util/ParameterMatcher.java)
- 路由处理器工厂：[RouterHandlerFactory.java](../core/src/main/java/cn/qaiu/vx/core/handlerfactory/RouterHandlerFactory.java)
- 示例控制器：[UserController.java](../core-example/src/main/java/cn/qaiu/example/controller/UserController.java)

## 更新日志

- **v1.1.0+**: 新增路径变量自动绑定功能
  - 支持 `@PathVariable` 注解自动参数名映射
  - 支持 `:语法` 的路径变量无注解自动绑定
  - 添加编译器参数名称保留配置
  - 新增 Javassist 备用参数名获取方案
  - 实现路由自动优先级排序
  - 优化路径变量转换日志输出
  - 添加路径复杂度评分机制
