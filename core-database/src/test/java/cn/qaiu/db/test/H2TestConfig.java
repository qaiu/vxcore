package cn.qaiu.db.test;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.util.UUID;

/**
 * H2数据库测试配置
 * 确保测试数据隔离和最大兼容性
 * 
 * @author QAIU
 */
public class H2TestConfig {
    
    /**
     * 创建H2内存数据库连接池
     * 每个测试使用独立的数据库实例，避免数据污染
     */
    public static JDBCPool createH2Pool(Vertx vertx) {
        return createH2Pool(vertx, generateUniqueDbName());
    }
    
    /**
     * 创建H2内存数据库连接池（指定数据库名）
     */
    public static JDBCPool createH2Pool(Vertx vertx, String dbName) {
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5)
                .setMaxWaitQueueSize(10);
        
        // H2配置确保最大兼容性
        String jdbcUrl = String.format(
            "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE",
            dbName
        );
        
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(jdbcUrl)
                .setUser("sa")
                .setPassword("");
        
        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 生成唯一的数据库名称，确保测试隔离
     */
    private static String generateUniqueDbName() {
        return "testdb_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 清理测试表，确保测试数据隔离
     */
    public static void cleanupTestTables(Pool pool) {
        if (pool != null) {
            // 清理常见的测试表
            String[] tables = {
                "users", "products", "categories", "orders", "order_items",
                "test_user", "test_product", "test_category", "test_order",
                "example_user", "example_product", "example_category"
            };
            
            for (String table : tables) {
                pool.query("DROP TABLE IF EXISTS " + table)
                    .execute()
                    .onFailure(error -> {
                        // 忽略表不存在的错误
                        if (!error.getMessage().contains("Table") || !error.getMessage().contains("not found")) {
                            System.err.println("Failed to drop table " + table + ": " + error.getMessage());
                        }
                    });
            }
        }
    }
    
    /**
     * 创建测试表的标准DDL
     */
    public static class TestTables {
        
        /**
         * 创建用户测试表
         */
        public static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                age INT,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                balance DECIMAL(10,2) DEFAULT 0.00,
                email_verified BOOLEAN DEFAULT FALSE,
                bio TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        /**
         * 创建产品测试表
         */
        public static final String CREATE_PRODUCTS_TABLE = """
            CREATE TABLE IF NOT EXISTS products (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                product_name VARCHAR(100) NOT NULL,
                product_code VARCHAR(50) NOT NULL UNIQUE,
                category_id BIGINT,
                price DECIMAL(10,2) NOT NULL,
                stock_quantity INT DEFAULT 0,
                description TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        /**
         * 创建分类测试表
         */
        public static final String CREATE_CATEGORIES_TABLE = """
            CREATE TABLE IF NOT EXISTS categories (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                category_name VARCHAR(50) NOT NULL UNIQUE,
                description TEXT,
                parent_id BIGINT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        /**
         * 创建所有测试表
         */
        public static void createAllTestTables(Pool pool) {
            if (pool != null) {
                pool.query(CREATE_USERS_TABLE).execute();
                pool.query(CREATE_PRODUCTS_TABLE).execute();
                pool.query(CREATE_CATEGORIES_TABLE).execute();
            }
        }
    }
}
