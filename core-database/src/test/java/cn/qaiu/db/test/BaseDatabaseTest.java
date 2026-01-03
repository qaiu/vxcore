package cn.qaiu.db.test;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 数据库测试基类
 * 提供统一的测试隔离和资源管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
public abstract class BaseDatabaseTest {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseDatabaseTest.class);
    
    protected Vertx vertx;
    protected Pool pool;
    protected String testDbName;
    
    @BeforeEach
    void setUpBase(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.testDbName = generateUniqueDbName();
        
        // 创建独立的测试数据库
        this.pool = H2TestConfig.createH2Pool(vertx, testDbName);
        
        // 清理测试表
        H2TestConfig.cleanupTestTables(pool);
        
        LOGGER.info("Test database created: {}", testDbName);
        testContext.completeNow();
    }
    
    @AfterEach
    void tearDownBase(VertxTestContext testContext) {
        // 清理测试资源
        if (pool != null) {
            pool.close().onComplete(ar -> {
                if (ar.succeeded()) {
                    LOGGER.debug("Test database pool closed: {}", testDbName);
                } else {
                    LOGGER.warn("Failed to close test database pool: {}", ar.cause().getMessage());
                }
                testContext.completeNow();
            });
        } else {
            testContext.completeNow();
        }
    }
    
    /**
     * 生成唯一的数据库名称
     */
    protected String generateUniqueDbName() {
        return "testdb_" + UUID.randomUUID().toString().replace("-", "") + 
               "_" + Thread.currentThread().getId();
    }
    
    /**
     * 获取测试数据库名称
     */
    protected String getTestDbName() {
        return testDbName;
    }
    
    /**
     * 获取测试数据库连接池
     */
    protected Pool getPool() {
        return pool;
    }
    
    /**
     * 获取Vertx实例
     */
    protected Vertx getVertx() {
        return vertx;
    }
    
    /**
     * 等待指定时间
     */
    protected void waitFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Thread interrupted while waiting: {}", e.getMessage());
        }
    }
}
