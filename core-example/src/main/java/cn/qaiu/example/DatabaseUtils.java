package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库工具类
 * 提供数据库连接和初始化功能
 * 
 * @author qaiu
 */
public class DatabaseUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    
    /**
     * 创建H2内存数据库连接
     */
    public static JDBCPool createH2Pool(Vertx vertx) {
        logger.info("创建H2内存数据库连接...");
        
        io.vertx.jdbcclient.JDBCConnectOptions connectOptions = new io.vertx.jdbcclient.JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:lambda_demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .setUser("sa")
                .setPassword("");
        
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10)
                .setIdleTimeout(30)
                .setConnectionTimeout(5000);
        
        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 创建JooqExecutor
     */
    public static JooqExecutor createJooqExecutor(JDBCPool pool) {
        logger.info("创建JooqExecutor...");
        return new JooqExecutor(pool);
    }
    
    /**
     * 创建用户表
     */
    public static io.vertx.core.Future<Void> createUserTable(JDBCPool pool) {
        logger.info("创建用户表...");
        
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS dsl_user (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                age INTEGER DEFAULT 0,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                balance DECIMAL(10,2) DEFAULT 0.00,
                email_verified BOOLEAN DEFAULT FALSE,
                bio TEXT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                version INTEGER DEFAULT 1
            )
            """;
        
        return pool.query(createTableSql).execute()
                .map(v -> {
                    logger.info("✅ 用户表创建成功");
                    return null;
                });
    }
    
    /**
     * 插入演示数据
     */
    public static io.vertx.core.Future<Void> insertDemoData(JDBCPool pool) {
        logger.info("插入演示数据...");
        
        String insertSql = """
            INSERT INTO dsl_user (username, email, password, age, status, balance, email_verified, bio) VALUES
            ('alice', 'alice@example.com', 'password123', 25, 'ACTIVE', 1500.00, true, 'Software Engineer'),
            ('bob', 'bob@example.com', 'password123', 30, 'ACTIVE', 2500.00, true, 'Product Manager'),
            ('charlie', 'charlie@example.com', 'password123', 22, 'ACTIVE', 800.00, false, 'Student'),
            ('diana', 'diana@example.com', 'password123', 28, 'ACTIVE', 3200.00, true, 'Data Scientist'),
            ('eve', 'eve@example.com', 'password123', 35, 'INACTIVE', 500.00, false, 'Designer'),
            ('frank', 'frank@example.com', 'password123', 40, 'ACTIVE', 5000.00, true, 'Tech Lead'),
            ('grace', 'grace@example.com', 'password123', 26, 'ACTIVE', 1200.00, true, 'UX Designer'),
            ('henry', 'henry@example.com', 'password123', 33, 'SUSPENDED', 0.00, false, 'Developer'),
            ('ivy', 'ivy@example.com', 'password123', 29, 'ACTIVE', 2800.00, true, 'Marketing Manager'),
            ('jack', 'jack@example.com', 'password123', 24, 'ACTIVE', 600.00, false, 'Intern')
            """;
        
        return pool.query(insertSql).execute()
                .map(v -> {
                    logger.info("✅ 演示数据插入成功");
                    return null;
                });
    }
    
    /**
     * 清理数据库
     */
    public static io.vertx.core.Future<Void> cleanupDatabase(JDBCPool pool) {
        logger.info("清理数据库...");
        
        String dropTableSql = "DROP TABLE IF EXISTS dsl_user";
        
        return pool.query(dropTableSql).execute()
                .map(v -> {
                    logger.info("✅ 数据库清理完成");
                    return null;
                });
    }
}
