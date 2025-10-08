package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;

/**
 * PostgreSQL测试配置
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class PostgreSQLTestConfig {
    
    /**
     * 创建PostgreSQL连接池
     */
    public static JDBCPool createPostgreSQLPool(Vertx vertx) {
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        
        // 从环境变量获取密码，如果没有设置则使用空字符串
        String password = System.getenv("POSTGRES_PASSWORD");
        if (password == null) {
            password = System.getProperty("postgres.password", "testpass");
        }
        
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:postgresql://localhost:5432/testdb")
                .setUser("testuser")
                .setPassword(password);
//                .setDataSourceProvider(new HikariCPDataSourceProvider());

        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 创建PostgreSQL连接池（指定数据库名）
     */
    public static JDBCPool createPostgreSQLPool(Vertx vertx, String databaseName) {
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:postgresql://localhost:5432/" + databaseName)
                .setUser("testuser")
                .setPassword("testpass");
//                .setDataSourceProvider(new HikariCPDataSourceProvider());

        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 检查PostgreSQL连接是否可用
     */
    public static boolean isPostgreSQLAvailable() {
        try {
            // 尝试加载PostgreSQL驱动
            Class.forName("org.postgresql.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
