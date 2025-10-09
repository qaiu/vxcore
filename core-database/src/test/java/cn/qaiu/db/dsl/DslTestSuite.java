package cn.qaiu.db.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

/**
 * DSL 框架测试套件
 * 
 * 使用嵌套测试类组织所有 DSL 相关测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("DSL Framework Test Suite")
public class DslTestSuite {

    @Nested
    @DisplayName("JOOQ Vert.x Executor Tests")
    static class JooqVertxExecutorTests extends JooqVertxExecutorTest {
    }
}
