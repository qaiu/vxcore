# VXCore 开发者指南

## 🎯 概述

本文档面向VXCore项目的维护者和贡献者，详细说明如何利用AI Agent来高效地开发、维护和完善VXCore框架。VXCore是一个基于Vert.x和jOOQ的现代化Java Web框架，采用AI Agent辅助开发模式。

## 🤖 AI Agent开发模式

### 什么是AI Agent开发模式？

AI Agent开发模式是指利用AI助手（如Claude、GPT等）来辅助软件开发的方式。在这种模式下，开发者与AI Agent协作，AI Agent能够：

- **理解项目结构**：分析代码库，理解模块关系和依赖
- **生成代码**：根据需求自动生成高质量的代码
- **重构优化**：识别代码问题并提供优化建议
- **测试验证**：生成测试用例并验证功能
- **文档维护**：自动更新文档和注释

### VXCore中的AI Agent应用

VXCore项目大量使用AI Agent进行开发，主要体现在：

1. **代码生成**：Lambda查询、DAO类、Controller等
2. **测试编写**：单元测试、集成测试、性能测试
3. **文档维护**：API文档、使用指南、架构文档
4. **重构优化**：代码清理、性能优化、架构改进
5. **问题修复**：Bug修复、兼容性处理、安全加固

## 🛠️ 环境准备

### 1. 开发环境要求

```bash
# Java开发环境
java -version  # 需要Java 17+
mvn -version  # 需要Maven 3.8+

# IDE推荐
# IntelliJ IDEA (推荐) 或 VS Code

# Git版本控制
git --version
```

### 2. AI Agent工具配置

#### Claude/ChatGPT使用技巧

```markdown
# 与AI Agent交互的最佳实践

## 1. 提供上下文信息
- 描述项目背景和技术栈
- 提供相关代码片段
- 说明具体需求和约束

## 2. 使用结构化提示
- 明确任务类型（生成、重构、测试、文档）
- 指定目标文件或模块
- 提供验收标准

## 3. 迭代式开发
- 先实现基础功能
- 逐步添加高级特性
- 持续测试和优化
```

#### 项目上下文信息

```markdown
# 提供给AI Agent的项目信息

## 项目概述
- 名称：VXCore
- 类型：Java Web框架
- 技术栈：Vert.x + jOOQ + Maven
- 目标：高性能、类型安全、开发友好

## 核心模块
- core: Web路由、参数绑定、异常处理
- core-database: 数据库操作、Lambda查询、多数据源
- core-example: 使用示例和演示

## 设计原则
- 异步非阻塞
- 类型安全
- 模块化设计
- 开发友好
```

## 🚀 开发工作流

### 1. 新功能开发流程

#### 步骤1：需求分析
```markdown
# 与AI Agent对话示例

开发者：我需要为VXCore添加WebSocket支持功能

AI Agent：请提供以下信息：
1. 具体需求（注解式路由、消息处理等）
2. 目标模块（core还是core-example）
3. 参考实现（Spring WebSocket或其他框架）
4. 验收标准（功能测试、性能要求等）
```

#### 步骤2：架构设计
```markdown
开发者：设计WebSocket注解式路由，类似Spring的@WebSocketHandler

AI Agent：建议的架构设计：
1. 注解定义：@WebSocketHandler, @OnOpen, @OnMessage, @OnClose, @OnError
2. 处理器工厂：WebSocketHandlerFactory
3. 集成点：RouterVerticle
4. 测试策略：单元测试 + 集成测试

请确认这个设计是否符合您的需求？
```

#### 步骤3：代码实现
```markdown
开发者：开始实现WebSocket注解支持

AI Agent：我将为您生成以下文件：
1. core/src/main/java/cn/qaiu/vx/core/annotations/websocket/
2. core/src/main/java/cn/qaiu/vx/core/handlerfactory/WebSocketHandlerFactory.java
3. 相应的测试文件
4. 使用示例

请确认开始实现？
```

#### 步骤4：测试验证
```markdown
开发者：WebSocket功能已实现，需要测试验证

AI Agent：我将生成以下测试：
1. 单元测试：注解解析、处理器注册
2. 集成测试：WebSocket连接、消息收发
3. 性能测试：并发连接、消息吞吐量
4. 示例代码：完整的使用示例

请运行测试并反馈结果。
```

### 2. Bug修复流程

#### 步骤1：问题定位
```markdown
开发者：Lambda查询在PostgreSQL上出现类型转换错误

AI Agent：请提供以下信息：
1. 错误堆栈信息
2. 复现步骤
3. 相关代码文件
4. 测试用例

我将分析问题并提供修复方案。
```

#### 步骤2：修复实现
```markdown
AI Agent：问题分析完成，修复方案：
1. 问题原因：PostgreSQL类型映射不匹配
2. 修复方案：增强TypeConverter支持
3. 影响范围：LambdaQueryWrapper、FieldNameConverter
4. 测试验证：PostgreSQL集成测试

开始实施修复？
```

#### 步骤3：回归测试
```markdown
开发者：修复完成，需要回归测试

AI Agent：我将运行以下测试：
1. PostgreSQL Lambda查询测试
2. H2兼容性测试
3. MySQL兼容性测试
4. 性能基准测试

请确认所有测试通过。
```

### 3. 重构优化流程

#### 步骤1：代码分析
```markdown
开发者：需要优化RouterHandlerFactory的性能

AI Agent：代码分析结果：
1. 性能瓶颈：反射调用频繁
2. 优化方向：缓存、预编译、异步处理
3. 风险评估：向后兼容性、测试覆盖
4. 实施计划：分阶段重构

开始实施优化？
```

#### 步骤2：重构实施
```markdown
AI Agent：重构方案：
1. 添加方法缓存：避免重复反射
2. 预编译路由：启动时生成路由表
3. 异步优化：使用Vert.x异步特性
4. 向后兼容：保持API不变

开始实施重构？
```

#### 步骤3：性能验证
```markdown
开发者：重构完成，需要性能验证

AI Agent：性能测试计划：
1. 基准测试：重构前后对比
2. 压力测试：高并发场景
3. 内存测试：内存使用分析
4. 兼容性测试：功能完整性

请运行测试并反馈结果。
```

## 📝 代码规范

### 1. 与AI Agent协作的代码规范

#### 代码注释规范
```java
/**
 * WebSocket处理器工厂
 * 
 * 功能：
 * - 扫描@WebSocketHandler注解的类
 * - 注册WebSocket路由处理器
 * - 支持@OnOpen、@OnMessage、@OnClose、@OnError注解
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @WebSocketHandler("/ws/chat")
 * public class ChatHandler {
 *     @OnMessage
 *     public void onMessage(String message, ServerWebSocket ws) {
 *         ws.writeTextMessage("Echo: " + message);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author AI Agent Generated
 * @since 2.0.0
 */
public class WebSocketHandlerFactory {
    // 实现代码...
}
```

#### 方法命名规范
```java
// 清晰的方法命名，便于AI Agent理解
public class LambdaQueryWrapper<T> {
    
    // 查询条件方法
    public LambdaQueryWrapper<T> eq(SFunction<T, ?> field, Object value) { }
    public LambdaQueryWrapper<T> like(SFunction<T, ?> field, String pattern) { }
    
    // 聚合查询方法
    public LambdaQueryWrapper<T> groupBy(SFunction<T, ?>... fields) { }
    public LambdaQueryWrapper<T> having(Condition condition) { }
    
    // 执行方法
    public Future<List<T>> list() { }
    public Future<T> one() { }
}
```

#### 异常处理规范
```java
/**
 * 异常处理示例
 * 
 * 设计原则：
 * 1. 明确的异常类型
 * 2. 详细的错误信息
 * 3. 适当的日志记录
 * 4. 优雅的降级处理
 */
public class DatabaseException extends BaseException {
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 静态工厂方法，便于AI Agent生成
    public static DatabaseException connectionFailed(String url) {
        return new DatabaseException("数据库连接失败: " + url);
    }
}
```

### 2. 测试代码规范

#### 测试类命名
```java
// 测试类命名：被测试类名 + Test
public class WebSocketHandlerFactoryTest {
    
    // 测试方法命名：test + 功能描述
    @Test
    public void testWebSocketHandlerRegistration() {
        // 测试实现
    }
    
    @Test
    public void testOnMessageAnnotationProcessing() {
        // 测试实现
    }
}
```

#### 测试数据准备
```java
/**
 * 测试数据准备示例
 * 
 * 原则：
 * 1. 使用Builder模式构建测试数据
 * 2. 提供默认值和自定义选项
 * 3. 支持链式调用
 * 4. 便于AI Agent生成测试用例
 */
public class TestDataBuilder {
    
    public static UserBuilder user() {
        return new UserBuilder();
    }
    
    public static class UserBuilder {
        private String name = "TestUser";
        private String email = "test@example.com";
        private String status = "ACTIVE";
        
        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public User build() {
            return new User(name, email, status);
        }
    }
}
```

## 🔧 工具和脚本

### 1. 开发辅助脚本

#### 代码生成脚本
```bash
#!/bin/bash
# scripts/generate-code.sh

# 使用AI Agent生成代码的脚本
generate_code() {
    local feature=$1
    local module=$2
    
    echo "🤖 使用AI Agent生成 $feature 功能..."
    
    # 调用AI Agent API或使用本地模型
    # 这里需要根据具体的AI Agent工具进行调整
    
    echo "✅ 代码生成完成"
}

# 使用示例
generate_code "WebSocket支持" "core"
```

#### 测试生成脚本
```bash
#!/bin/bash
# scripts/generate-tests.sh

# 生成测试用例
generate_tests() {
    local class_name=$1
    local test_type=$2
    
    echo "🧪 生成 $class_name 的 $test_type 测试..."
    
    # AI Agent生成测试用例
    # 包括单元测试、集成测试、性能测试
    
    echo "✅ 测试生成完成"
}

# 使用示例
generate_tests "WebSocketHandlerFactory" "unit"
```

#### 文档生成脚本
```bash
#!/bin/bash
# scripts/generate-docs.sh

# 生成文档
generate_docs() {
    local feature=$1
    local doc_type=$2
    
    echo "📚 生成 $feature 的 $doc_type 文档..."
    
    # AI Agent生成文档
    # 包括API文档、使用指南、示例代码
    
    echo "✅ 文档生成完成"
}

# 使用示例
generate_docs "WebSocket" "api"
```

### 2. 质量检查脚本

#### 代码质量检查
```bash
#!/bin/bash
# scripts/check-quality.sh

echo "🔍 开始代码质量检查..."

# 1. 编译检查
echo "📦 检查编译..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

# 2. 代码风格检查
echo "🎨 检查代码风格..."
mvn spotless:check -q
if [ $? -ne 0 ]; then
    echo "❌ 代码风格检查失败"
    exit 1
fi

# 3. 静态分析
echo "🔍 运行静态分析..."
mvn spotbugs:check -q
if [ $? -ne 0 ]; then
    echo "❌ 静态分析发现问题"
    exit 1
fi

# 4. 测试覆盖率
echo "📊 检查测试覆盖率..."
mvn jacoco:check -q
if [ $? -ne 0 ]; then
    echo "❌ 测试覆盖率不足"
    exit 1
fi

echo "✅ 代码质量检查通过"
```

#### 性能基准测试
```bash
#!/bin/bash
# scripts/benchmark.sh

echo "⚡ 开始性能基准测试..."

# 1. 编译项目
mvn clean package -DskipTests=true

# 2. 启动服务
java -jar target/vxcore-example-*.jar &
SERVER_PID=$!

# 3. 等待服务启动
sleep 10

# 4. 运行性能测试
echo "🚀 运行HTTP性能测试..."
ab -n 10000 -c 100 http://localhost:8080/api/hello?name=Test

echo "🌐 运行WebSocket性能测试..."
# 使用WebSocket性能测试工具

# 5. 停止服务
kill $SERVER_PID

echo "✅ 性能基准测试完成"
```

## 📊 项目管理

### 1. 任务管理

#### 使用AI Agent进行任务分解
```markdown
# 任务分解示例

## 原始需求
为VXCore添加WebSocket支持

## AI Agent分解结果

### 阶段1：基础功能 (1-2天)
- [ ] 创建WebSocket注解
- [ ] 实现WebSocketHandlerFactory
- [ ] 集成到RouterVerticle
- [ ] 基础单元测试

### 阶段2：高级功能 (2-3天)
- [ ] 支持@OnOpen、@OnMessage、@OnClose、@OnError
- [ ] 消息序列化/反序列化
- [ ] 错误处理机制
- [ ] 集成测试

### 阶段3：优化完善 (1-2天)
- [ ] 性能优化
- [ ] 文档完善
- [ ] 示例代码
- [ ] 最终测试

## 验收标准
- 所有测试通过
- 性能指标达标
- 文档完整
- 示例可运行
```

#### 进度跟踪
```markdown
# 进度跟踪模板

## 项目：VXCore WebSocket支持
## 开始时间：2024-01-01
## 预计完成：2024-01-10

### 当前状态
- 总体进度：60%
- 当前阶段：阶段2 - 高级功能
- 下一个里程碑：集成测试完成

### 已完成任务
- [x] 创建WebSocket注解
- [x] 实现WebSocketHandlerFactory
- [x] 集成到RouterVerticle
- [x] 基础单元测试

### 进行中任务
- [ ] 支持@OnMessage注解
- [ ] 消息序列化机制
- [ ] 错误处理实现

### 待办任务
- [ ] 性能优化
- [ ] 文档编写
- [ ] 示例代码
- [ ] 最终测试

### 风险和问题
- 风险：WebSocket连接管理复杂度
- 问题：消息序列化性能
- 解决方案：使用Vert.x原生序列化
```

### 2. 版本管理

#### 语义化版本
```markdown
# 版本号规范

## 格式：主版本.次版本.修订版本

### 主版本 (Major)
- 不兼容的API更改
- 重大功能变更
- 架构重构

### 次版本 (Minor)
- 向后兼容的功能新增
- 新特性添加
- 性能改进

### 修订版本 (Patch)
- 向后兼容的Bug修复
- 安全补丁
- 文档更新

## 示例
- v1.0.0: 初始版本
- v1.1.0: 添加WebSocket支持
- v1.1.1: 修复WebSocket连接问题
- v2.0.0: 重构路由系统
```

#### 发布流程
```bash
#!/bin/bash
# scripts/release.sh

# 发布流程脚本
release() {
    local version=$1
    
    echo "🚀 开始发布版本 $version..."
    
    # 1. 更新版本号
    mvn versions:set -DnewVersion=$version
    
    # 2. 运行完整测试
    mvn clean test
    
    # 3. 生成文档
    mvn site
    
    # 4. 打包发布
    mvn clean package -DskipTests=false
    
    # 5. 创建Git标签
    git tag -a v$version -m "Release version $version"
    git push origin v$version
    
    # 6. 推送到Maven中央仓库
    mvn deploy
    
    echo "✅ 版本 $version 发布完成"
}

# 使用示例
release "1.2.0"
```

## 🔍 调试和排错

### 1. 常见问题排查

#### 编译问题
```markdown
# 编译问题排查指南

## 问题：找不到符号
## 解决方案：
1. 检查import语句
2. 确认依赖关系
3. 清理并重新编译
4. 检查IDE配置

## 问题：类型不匹配
## 解决方案：
1. 检查泛型参数
2. 确认类型转换
3. 查看错误堆栈
4. 使用AI Agent分析

## 问题：循环依赖
## 解决方案：
1. 重构模块结构
2. 使用接口解耦
3. 调整依赖方向
4. 使用依赖注入
```

#### 运行时问题
```markdown
# 运行时问题排查指南

## 问题：NullPointerException
## 解决方案：
1. 检查空值判断
2. 使用Optional
3. 添加日志记录
4. 使用AI Agent分析

## 问题：内存泄漏
## 解决方案：
1. 检查资源释放
2. 使用try-with-resources
3. 分析内存使用
4. 优化对象生命周期

## 问题：性能问题
## 解决方案：
1. 使用性能分析工具
2. 优化算法复杂度
3. 减少对象创建
4. 使用缓存机制
```

### 2. 调试工具

#### 日志配置
```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/vxcore.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/vxcore.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

#### 性能监控
```java
/**
 * 性能监控工具
 * 
 * 功能：
 * - 方法执行时间统计
 * - 内存使用监控
 * - 数据库查询性能
 * - HTTP请求响应时间
 */
public class PerformanceMonitor {
    
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    public static <T> T monitor(String operation, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("操作 {} 执行时间: {}ms", operation, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("操作 {} 执行失败，耗时: {}ms", operation, duration, e);
            throw e;
        }
    }
}
```

## 📚 学习资源

### 1. 技术文档

#### 核心框架文档
- [Vert.x官方文档](https://vertx.io/docs/)
- [jOOQ用户手册](https://www.jooq.org/doc/latest/manual/)
- [Maven用户指南](https://maven.apache.org/guides/)

#### AI Agent工具文档
- [Claude使用指南](https://claude.ai/docs)
- [ChatGPT最佳实践](https://platform.openai.com/docs/guides)
- [GitHub Copilot文档](https://docs.github.com/en/copilot)

### 2. 最佳实践

#### 代码质量
- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Effective Java](https://www.oreilly.com/library/view/effective-java/9780134686097/)

#### 测试策略
- [JUnit 5用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers文档](https://www.testcontainers.org/)
- [Mockito文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

#### 性能优化
- [Java性能调优指南](https://docs.oracle.com/en/java/javase/17/gctuning/)
- [Vert.x性能调优](https://vertx.io/docs/vertx-core/java/#_performance)
- [jOOQ性能最佳实践](https://www.jooq.org/doc/latest/manual/sql-execution/performance/)

## 🤝 贡献指南

### 1. 贡献流程

#### 提交代码
```bash
# 1. Fork项目
git clone https://github.com/your-username/vxcore.git
cd vxcore

# 2. 创建功能分支
git checkout -b feature/your-feature

# 3. 开发功能
# ... 使用AI Agent辅助开发 ...

# 4. 运行测试
mvn clean test

# 5. 提交代码
git add .
git commit -m "feat: 添加新功能"

# 6. 推送分支
git push origin feature/your-feature

# 7. 创建Pull Request
```

#### 代码审查
```markdown
# 代码审查检查清单

## 功能完整性
- [ ] 功能实现完整
- [ ] 测试用例充分
- [ ] 文档更新及时
- [ ] 示例代码正确

## 代码质量
- [ ] 代码风格一致
- [ ] 命名规范清晰
- [ ] 注释充分准确
- [ ] 无重复代码

## 性能和安全
- [ ] 性能指标达标
- [ ] 无安全漏洞
- [ ] 内存使用合理
- [ ] 异常处理完善

## 兼容性
- [ ] 向后兼容
- [ ] 多数据库支持
- [ ] 跨平台兼容
- [ ] 版本兼容
```

### 2. 社区参与

#### 问题报告
```markdown
# Bug报告模板

## 问题描述
简要描述遇到的问题

## 复现步骤
1. 第一步
2. 第二步
3. 第三步

## 预期行为
描述期望的正确行为

## 实际行为
描述实际发生的行为

## 环境信息
- Java版本：
- Maven版本：
- 操作系统：
- 数据库版本：

## 相关代码
```java
// 相关的代码片段
```

## 错误堆栈
```
错误堆栈信息
```
```

#### 功能请求
```markdown
# 功能请求模板

## 功能描述
详细描述希望添加的功能

## 使用场景
说明这个功能的使用场景和价值

## 实现建议
如果有实现想法，请提供建议

## 相关Issue
关联的相关Issue或讨论

## 优先级
- [ ] 高优先级
- [ ] 中优先级
- [ ] 低优先级
```

## 🎯 总结

VXCore项目采用AI Agent辅助开发模式，这种模式带来了以下优势：

### 开发效率提升
- **快速原型**：AI Agent能够快速生成基础代码框架
- **智能补全**：自动生成测试用例、文档和示例
- **问题诊断**：快速定位和解决代码问题
- **重构优化**：自动识别优化机会并提供改进方案

### 代码质量保证
- **一致性**：AI Agent遵循项目编码规范
- **完整性**：自动生成完整的测试用例
- **文档化**：自动更新相关文档
- **最佳实践**：应用行业最佳实践

### 知识传承
- **经验积累**：AI Agent学习项目开发经验
- **模式复用**：复用成功的开发模式
- **知识共享**：团队成员共享AI Agent的知识
- **持续改进**：基于反馈持续优化

### 使用建议

1. **明确需求**：与AI Agent交互时，提供清晰的需求描述
2. **迭代开发**：采用小步快跑的方式，逐步完善功能
3. **测试驱动**：先写测试，再写实现，确保质量
4. **文档同步**：及时更新文档，保持项目文档的时效性
5. **持续学习**：不断学习AI Agent的使用技巧和最佳实践

通过遵循本指南，您将能够高效地使用AI Agent来维护和完善VXCore项目，提升开发效率和代码质量。

---

**🎯 让AI Agent成为您的开发伙伴，共同构建更好的VXCore！**
