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
    @DisplayName("Base Entity Tests")
    static class BaseEntityTests extends BaseEntityTest {
    }

    @Nested
    @DisplayName("Entity Mapper Tests")
    static class EntityMapperTests extends EntityMapperTest {
    }

    @Nested
    @DisplayName("JOOQ Vert.x Executor Tests")
    static class JooqVertxExecutorTests extends JooqVertxExecutorTest {
    }

    @Nested
    @DisplayName("User DSL Tests")
    static class UserDslTests extends UserDslTest {
    }
}
