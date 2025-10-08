package cn.qaiu.db.dsl.test;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单实体测试类 - 验证异步获取jdbcType逻辑
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("Simple Entity JDBC Type Detection Test")
public class SimpleEntityJdbcTypeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEntityJdbcTypeTest.class);

    private JDBCPool pool;
    private JooqExecutor executor;

    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(Vertx vertx, VertxTestContext testContext) {
        try {
            // 创建 H2 测试数据库连接 - 使用测试特定的数据库名称避免冲突
            PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
            JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                    .setJdbcUrl("jdbc:h2:mem:testdb_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                    .setUser("sa")
                    .setPassword("");

            pool = JDBCPool.pool(vertx, connectOptions, poolOptions);

            // 创建表
            initTestDatabase(pool)
                    .compose(v -> {
                        // 创建 JooqExecutor - 这里会异步检测数据库类型
                        executor = new JooqExecutor(pool);
                        LOGGER.info("JooqExecutor created, database type detection started");
                        return Future.succeededFuture();
                    })
                    .onSuccess(v -> {
                        LOGGER.info("Test setup completed");
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);

        } catch (Exception e) {
            LOGGER.error("Failed to setup test environment", e);
            testContext.failNow(e);
        }
    }
    
    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown(VertxTestContext testContext) {
        // 清理数据库表数据并关闭连接
        if (pool != null) {
            pool.query("DROP TABLE IF EXISTS test_simple").execute()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info("Test database table dropped");
                    } else {
                        LOGGER.error("Failed to drop table", ar.cause());
                    }
                    // 关闭连接池
                    pool.close();
                    testContext.completeNow();
                });
        } else {
            testContext.completeNow();
        }
    }

    /**
     * 初始化测试数据库
     */
    private Future<Void> initTestDatabase(io.vertx.sqlclient.Pool pool) {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS test_simple (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    description TEXT,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        return pool.query(createTableSQL).execute()
                .onSuccess(result -> LOGGER.info("Test database table created"))
                .onFailure(error -> LOGGER.error("Failed to create test table", error))
                .map(v -> null);
    }

    @Test
    @DisplayName("测试数据库类型检测")
    void testDatabaseTypeDetection(VertxTestContext testContext) {
        // 直接检查数据库类型，不需要等待异步检测
        JDBCType detectedType = executor.getDatabaseType();
        LOGGER.info("Detected database type: {}", detectedType);
        
        testContext.verify(() -> {
            assertNotNull(detectedType, "Database type should not be null");
            assertEquals(JDBCType.H2DB, detectedType, "Should detect H2 database");
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试简单实体创建")
    void testSimpleEntityCreation(VertxTestContext testContext) {
        // 创建一个简单的实体
        SimpleEntity entity = new SimpleEntity();
        entity.setName("测试实体");
        entity.setDescription("这是一个测试描述");
        entity.onCreate();
        
        LOGGER.info("Created simple entity: {}", entity);
        
        testContext.verify(() -> {
            assertNotNull(entity);
            assertEquals("测试实体", entity.getName());
            assertEquals("这是一个测试描述", entity.getDescription());
            assertNotNull(entity.getCreateTime());
            assertNotNull(entity.getUpdateTime());
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试实体JSON序列化")
    void testEntityJsonSerialization(VertxTestContext testContext) {
        SimpleEntity entity = new SimpleEntity();
        entity.setName("JSON测试");
        entity.setDescription("JSON序列化测试");
        entity.onCreate();
        
        JsonObject json = entity.toJson();
        LOGGER.info("Entity JSON: {}", json.encodePrettily());
        
        testContext.verify(() -> {
            assertNotNull(json);
            assertEquals("JSON测试", json.getString("name"));
            assertEquals("JSON序列化测试", json.getString("description"));
            assertNotNull(json.getString("createTime"));
            assertNotNull(json.getString("updateTime"));
        });
        
        // 测试从JSON反序列化
        SimpleEntity deserializedEntity = new SimpleEntity(json);
        LOGGER.info("Deserialized entity: {}", deserializedEntity);
        
        testContext.verify(() -> {
            assertEquals(entity.getName(), deserializedEntity.getName());
            assertEquals(entity.getDescription(), deserializedEntity.getDescription());
        });
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试DSL上下文")
    void testDslContext(VertxTestContext testContext) {
        try {
            var dslContext = executor.dsl();
            LOGGER.info("DSL Context: {}", dslContext);
            
            testContext.verify(() -> {
                assertNotNull(dslContext, "DSL context should not be null");
            });
            
            testContext.completeNow();
        } catch (Exception e) {
            LOGGER.error("Error testing DSL context", e);
            testContext.failNow(e);
        }
    }
}
