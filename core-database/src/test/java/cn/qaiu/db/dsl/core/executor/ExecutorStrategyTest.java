package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 执行器策略测试
 * 
 * @author QAIU
 */
@ExtendWith(io.vertx.junit5.VertxExtension.class)
class ExecutorStrategyTest {
    
    private ExecutorStrategyRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = ExecutorStrategyRegistry.getInstance();
    }
    
    @Test
    void testRegistrySupportsBuiltinTypes() {
        // 测试内置数据源类型支持
        assertTrue(registry.supports(JDBCType.MySQL));
        assertTrue(registry.supports(JDBCType.PostgreSQL));
        assertTrue(registry.supports(JDBCType.H2DB));
        
        // 测试获取策略
        ExecutorStrategy mysqlStrategy = registry.getStrategy(JDBCType.MySQL);
        ExecutorStrategy postgresStrategy = registry.getStrategy(JDBCType.PostgreSQL);
        ExecutorStrategy h2Strategy = registry.getStrategy(JDBCType.H2DB);
        
        assertNotNull(mysqlStrategy);
        assertNotNull(postgresStrategy);
        assertNotNull(h2Strategy);
        
        assertEquals(JDBCType.MySQL, mysqlStrategy.getSupportedType());
        assertEquals(JDBCType.PostgreSQL, postgresStrategy.getSupportedType());
        assertEquals(JDBCType.H2DB, h2Strategy.getSupportedType());
    }
    
    @Test
    void testMySQLStrategy() {
        MySQLExecutorStrategy strategy = new MySQLExecutorStrategy();
        
        assertEquals(JDBCType.MySQL, strategy.getSupportedType());
        assertEquals(SQLDialect.MYSQL, strategy.getSQLDialect());
        assertEquals(io.vertx.mysqlclient.MySQLPool.class, strategy.getPoolType());
    }
    
    @Test
    void testPostgreSQLStrategy() {
        PostgreSQLExecutorStrategy strategy = new PostgreSQLExecutorStrategy();
        
        assertEquals(JDBCType.PostgreSQL, strategy.getSupportedType());
        assertEquals(SQLDialect.POSTGRES, strategy.getSQLDialect());
        assertEquals(io.vertx.pgclient.PgPool.class, strategy.getPoolType());
    }
    
    @Test
    void testH2Strategy() {
        H2ExecutorStrategy strategy = new H2ExecutorStrategy();
        
        assertEquals(JDBCType.H2DB, strategy.getSupportedType());
        assertEquals(SQLDialect.H2, strategy.getSQLDialect());
        assertEquals(JDBCPool.class, strategy.getPoolType());
    }
    
    @Test
    void testAbstractExecutorStrategy() {
        // 创建一个测试策略
        TestExecutorStrategy strategy = new TestExecutorStrategy();
        
        // 测试DSL上下文创建
        Vertx vertx = Vertx.vertx();
        try {
            JDBCPool pool = JDBCPool.pool(vertx, 
                io.vertx.core.json.JsonObject.of(
                    "url", "jdbc:h2:mem:test",
                    "driver_class", "org.h2.Driver"
                ));
            
            DSLContext dslContext = strategy.createDSLContext(pool);
            assertNotNull(dslContext);
            assertEquals(SQLDialect.H2, dslContext.configuration().dialect());
            
        } finally {
            vertx.close();
        }
    }
    
    @Test
    void testUnsupportedType() {
        // 测试不支持的数据源类型
        assertThrows(IllegalArgumentException.class, () -> {
            // 假设有一个不支持的类型
            registry.getStrategy(JDBCType.valueOf("UNSUPPORTED"));
        });
    }
    
    /**
     * 测试用的执行器策略
     */
    private static class TestExecutorStrategy extends AbstractExecutorStrategy {
        @Override
        public JDBCType getSupportedType() {
            return JDBCType.H2DB;
        }
        
        @Override
        public SQLDialect getSQLDialect() {
            return SQLDialect.H2;
        }
        
        @Override
        public Class<? extends Pool> getPoolType() {
            return JDBCPool.class;
        }
    }
}
