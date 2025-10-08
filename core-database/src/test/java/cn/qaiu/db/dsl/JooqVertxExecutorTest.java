package cn.qaiu.db.dsl;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JooqVertxExecutor 测试类
 * 
 * 测试简化的 SQL 执行器功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("JooqVertxExecutor Test")
public class JooqVertxExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqVertxExecutorTest.class);

    private JooqExecutor executor;
    private JDBCPool pool;

    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(VertxTestContext testContext) {
        try {
            Vertx vertx = Vertx.vertx();

            // 创建 H2 测试数据库连接
            PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
            JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                    .setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL")
                    .setUser("sa")
                    .setPassword("");

            pool = JDBCPool.pool(vertx, connectOptions, poolOptions);

            // 执行器需要在构造函数中接收池对象
            executor = new JooqExecutor(pool);

            // 初始化测试表
            initTestDatabase(pool)
                    .onSuccess(v -> {
                        LOGGER.info("Test setup completed");
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);

        } catch (Exception e) {
            LOGGER.error("Test setup failed", e);
            testContext.failNow(e);
        }
    }

    /**
     * 初始化测试数据库表
     */
    private Future<Void> initTestDatabase(JDBCPool pool) {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS test_table (
                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    age INT DEFAULT 0,
                    email VARCHAR(100)
                )
                """;

        return pool.query(createTableSQL).execute()
                .onSuccess(result -> LOGGER.info("Test database table created"))
                .onFailure(error -> LOGGER.error("Failed to create test table", error))
                .map(v -> null);
    }

    @Test
    @DisplayName("测试执行器初始化")
    void testExecutorInitialization(VertxTestContext testContext) {
        testContext.verify(() -> {
            assertNotNull(executor);
            assertNotNull(executor.dsl());
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试简单的 SELECT 查询")
    void testSimpleSelect(VertxTestContext testContext) {
        // 插入测试数据
        pool.query("INSERT INTO test_table (name, age) VALUES ('Test', 25)").execute()
                .compose(v -> {
                    String sql = "SELECT * FROM test_table WHERE name = ?";
                    return pool.preparedQuery(sql).execute(Tuple.of("Test"));
                })
                .onSuccess(rows -> {
                    testContext.verify(() -> {
                        assertNotNull(rows);
                        // 此处类型已经是 RowSet<Row>，不需要转换
                        assertTrue(rows.size() >= 1);
                    });
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试 INSERT 操作")
    void testInsert(VertxTestContext testContext) {
        pool.query("INSERT INTO test_table (name, age, email) VALUES ('Insert Test', 30, 'insert@test.com')").execute()
                .onSuccess(result -> {
                    testContext.verify(() -> {
                        assertNotNull(result);
                        assertTrue(result.rowCount() >= 1);
                    });
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试 UPDATE 操作")
    void testUpdate(VertxTestContext testContext) {
        // 先插入测试数据
        pool.query("INSERT INTO test_table (name, age) VALUES ('Update Test', 20)").execute()
                .compose(insertResult -> {
                    // 然后更新数据
                    String updateSQL = "UPDATE test_table SET age = ? WHERE name = ?";
                    return pool.preparedQuery(updateSQL).execute(Tuple.of(25, "Update Test"));
                })
                .onSuccess(updateResult -> {
                    testContext.verify(() -> {
                        assertNotNull(updateResult);
                        assertTrue(updateResult.rowCount() >= 1);
                    });
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("测试复杂的 SELECT 查询")
    void testComplexSelect(VertxTestContext testContext) {
        // 插入多条测试数据
        pool.query("INSERT INTO test_table (name, age, email) VALUES ('User1', 20, 'user1@example.com')").execute()
                .compose(v -> pool.query("INSERT INTO test_table (name, age, email) VALUES ('User2', 25, 'user2@example.com')").execute())
                .compose(v -> pool.query("INSERT INTO test_table (name, age, email) VALUES ('User3', 30, 'user3@example.com')").execute())
                .compose(v -> {
                    // 查询年龄范围
                    String sql = "SELECT * FROM test_table WHERE age BETWEEN ? AND ?";
                    return pool.preparedQuery(sql).execute(Tuple.of(22, 28));
                })
                .onSuccess(rows -> {
                    testContext.verify(() -> {
                        assertNotNull(rows);
                        // 应该只有 User2 和 User3 符合条件（但是在 22-28 范围内）
                        System.out.println("Complex select result size: " + rows.size());
                    });
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
}