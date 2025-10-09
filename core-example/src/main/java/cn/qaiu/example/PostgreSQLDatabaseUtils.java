package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL数据库工具类
 * 提供PostgreSQL连接和初始化功能
 * 
 * @author qaiu
 */
public class PostgreSQLDatabaseUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLDatabaseUtils.class);
    
    /**
     * 创建PostgreSQL连接
     */
    public static PgPool createPostgreSQLPool(Vertx vertx) {
        logger.info("创建PostgreSQL连接...");
        
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost("your-postgres-host")
                .setPort(5432)
                .setDatabase("your-database")
                .setUser("your-username")
                .setPassword("your-password")
                .setSslMode(io.vertx.pgclient.SslMode.REQUIRE);
        
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10)
                .setIdleTimeout(30)
                .setConnectionTimeout(5000);
        
        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 创建JooqExecutor
     */
    public static JooqExecutor createJooqExecutor(PgPool pool) {
        logger.info("创建JooqExecutor...");
        return new JooqExecutor(pool);
    }
    
    /**
     * 创建用户表
     */
    public static io.vertx.core.Future<Void> createUserTable(PgPool pool) {
        logger.info("创建用户表...");
        
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS dsl_user (
                id BIGSERIAL PRIMARY KEY,
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
    public static io.vertx.core.Future<Void> insertDemoData(PgPool pool) {
        logger.info("插入演示数据...");
        
        // 先清空表数据
        String clearTableSql = "DELETE FROM dsl_user";
        
        return pool.query(clearTableSql).execute()
                .compose(v -> {
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
                    
                    return pool.query(insertSql).execute();
                })
                .map(v -> {
                    logger.info("✅ 演示数据插入成功");
                    return null;
                });
    }
    
    /**
     * 清理数据库
     */
    public static io.vertx.core.Future<Void> cleanupDatabase(PgPool pool) {
        logger.info("清理数据库...");
        
        String dropTableSql = "DROP TABLE IF EXISTS dsl_user";
        
        return pool.query(dropTableSql).execute()
                .map(v -> {
                    logger.info("✅ 数据库清理完成");
                    return null;
                });
    }
    
    /**
     * 查询所有用户数据（用于验证）
     */
    public static io.vertx.core.Future<Void> queryAllUsers(PgPool pool) {
        logger.info("查询所有用户数据...");
        
        String querySql = "SELECT id, username, email, age, status, balance, email_verified FROM dsl_user ORDER BY id";
        
        return pool.query(querySql).execute()
                .map(rows -> {
                    logger.info("📊 数据库中的用户数据:");
                    rows.forEach(row -> {
                        logger.info("   - ID: {}, 用户名: {}, 邮箱: {}, 年龄: {}, 状态: {}, 余额: ${}, 验证: {}", 
                                row.getLong("id"),
                                row.getString("username"),
                                row.getString("email"),
                                row.getInteger("age"),
                                row.getString("status"),
                                row.getBigDecimal("balance"),
                                row.getBoolean("email_verified"));
                    });
                    return null;
                });
    }
}
