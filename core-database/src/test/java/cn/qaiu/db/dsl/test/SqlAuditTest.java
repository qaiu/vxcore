package cn.qaiu.db.dsl.test;

import cn.qaiu.db.dsl.core.SqlAuditStatistics;
import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.example.UserDao;
import cn.qaiu.db.dsl.example.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.PoolOptions;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL审计功能测试
 * 
 * 验证SQL审计监听器和统计功能是否正常工作
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
public class SqlAuditTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlAuditTest.class);
    
    private JDBCPool pool;
    private JooqExecutor executor;
    private UserDao userDao;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 重置统计信息
        SqlAuditStatistics.resetStatistics();
        
        // 创建 H2 测试数据库连接 - 使用随机数据库名避免冲突
        String dbName = "testdb_" + System.currentTimeMillis();
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL")
                .setUser("sa")
                .setPassword("");
        
        pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
        
        // 创建JooqExecutor和UserDao
        executor = new JooqExecutor(pool);
        userDao = new UserDao(executor);
        
        // 创建表
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS dsl_user (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                age INT DEFAULT 0,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                balance DECIMAL(10,2) DEFAULT 0.00,
                email_verified BOOLEAN DEFAULT FALSE,
                bio TEXT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        pool.query(createTableSQL).execute()
            .onSuccess(v -> {
                LOGGER.info("Test database table created");
                testContext.completeNow();
            })
            .onFailure(testContext::failNow);
    }
    
    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (executor != null) {
            // 清理测试数据
            executor.executeQuery(DSL.query("DELETE FROM dsl_user"))
                    .onSuccess(v -> {
                        LOGGER.debug("Test data cleaned up");
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }
    
    @Test
    void testSqlAuditBasicOperations(VertxTestContext testContext) {
        // 执行一些基本操作
        userDao.createUser("testuser", "test@example.com", "password123")
            .compose(user -> {
                LOGGER.info("User created: {}", user.getId());
                return userDao.findById(user.getId());
            })
            .compose(userOpt -> {
                assertTrue(userOpt.isPresent());
                User user = userOpt.get();
                assertEquals("testuser", user.getUsername());
                
                // 更新用户
                user.setPassword("newpassword123");
                return userDao.update(user);
            })
            .compose(updatedUser -> {
                LOGGER.info("User updated: {}", updatedUser.isPresent() ? updatedUser.get().getId() : "null");
                
                // 查询用户
                return userDao.findByUsername("testuser");
            })
            .compose(userOpt -> {
                assertTrue(userOpt.isPresent());
                User user = userOpt.get();
                assertEquals("newpassword123", user.getPassword());
                
                // 删除用户
                return userDao.delete(user.getId());
            })
            .compose(deleted -> {
                LOGGER.info("User deleted: {}", deleted);
                
                // 打印SQL审计统计信息
                SqlAuditStatistics.printAllStatistics();
                
                testContext.completeNow();
                return null;
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testSqlAuditStatistics(VertxTestContext testContext) {
        // 执行多个操作来生成统计信息
        userDao.createUser("user1", "user1@example.com", "pass1")
            .compose(v -> userDao.createUser("user2", "user2@example.com", "pass2"))
            .compose(v -> userDao.createUser("user3", "user3@example.com", "pass3"))
            .compose(v -> userDao.findAll())
            .compose(users -> {
                LOGGER.info("Found {} users", users.size());
                assertEquals(3, users.size());
                
                // 检查统计信息
                var stats = SqlAuditStatistics.getAllStatistics();
                assertFalse(stats.isEmpty(), "应该有SQL执行统计信息");
                
                LOGGER.info("SQL统计信息数量: {}", stats.size());
                
                // 打印详细统计
                SqlAuditStatistics.printAllStatistics();
                
                // 导出JSON格式
                String jsonStats = SqlAuditStatistics.exportStatisticsAsJson();
                LOGGER.info("JSON统计信息: {}", jsonStats);
                assertNotNull(jsonStats);
                assertTrue(jsonStats.contains("statistics"));
                
                testContext.completeNow();
                return null;
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testSlowQueryDetection(VertxTestContext testContext) {
        // 执行一些操作
        userDao.createUser("slowuser", "slow@example.com", "password")
            .compose(v -> userDao.findByUsername("slowuser"))
            .compose(userOpt -> {
                assertTrue(userOpt.isPresent());
                
                // 打印慢查询统计（阈值1ms，应该能检测到一些查询）
                SqlAuditStatistics.printSlowQueries(1);
                
                testContext.completeNow();
                return null;
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testErrorQueryDetection(VertxTestContext testContext) {
        // 执行一些正常操作
        userDao.createUser("erroruser", "error@example.com", "password")
            .compose(v -> {
                // 打印错误查询统计
                SqlAuditStatistics.printErrorQueries();
                
                testContext.completeNow();
                return null;
            })
            .onFailure(testContext::failNow);
    }
}
