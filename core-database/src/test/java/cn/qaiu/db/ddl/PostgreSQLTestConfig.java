package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PostgreSQL测试配置
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class PostgreSQLTestConfig {
    
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = PostgreSQLTestConfig.class.getClassLoader().getResourceAsStream("postgresql-test.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to load postgresql-test.properties: " + e.getMessage());
        }
        return props;
    }
    
    /**
     * 创建PostgreSQL连接池
     */
    public static JDBCPool createPostgreSQLPool(Vertx vertx) {
        Properties props = loadProperties();
        
        if (!props.containsKey("postgresql.url")) {
            System.out.println("⚠️ postgresql-test.properties not configured properly");
            return null;
        }
        
        PoolOptions poolOptions = new PoolOptions().setMaxSize(Integer.parseInt(props.getProperty("postgresql.max_pool_size", "10")));
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(props.getProperty("postgresql.url"))
                .setUser(props.getProperty("postgresql.user"))
                .setPassword(props.getProperty("postgresql.password"));

        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 创建PostgreSQL连接池（指定数据库名）
     */
    public static JDBCPool createPostgreSQLPool(Vertx vertx, String databaseName) {
        Properties props = loadProperties();
        
        if (!props.containsKey("postgresql.url")) {
            System.out.println("⚠️ postgresql-test.properties not configured properly");
            return null;
        }
        
        PoolOptions poolOptions = new PoolOptions().setMaxSize(Integer.parseInt(props.getProperty("postgresql.max_pool_size", "10")));
        
        // 替换数据库名
        String url = props.getProperty("postgresql.url");
        String baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
        String newUrl = baseUrl + databaseName + "?" + url.substring(url.indexOf('?') + 1);
        
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(newUrl)
                .setUser(props.getProperty("postgresql.user"))
                .setPassword(props.getProperty("postgresql.password"));

        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 检查PostgreSQL连接是否可用
     */
    public static boolean isPostgreSQLAvailable() {
        try {
            // 尝试加载PostgreSQL驱动
            Class.forName("org.postgresql.Driver");
            // 检查配置文件是否存在
            Properties props = loadProperties();
            return props.containsKey("postgresql.url");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
