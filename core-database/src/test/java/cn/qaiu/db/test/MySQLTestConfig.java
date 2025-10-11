package cn.qaiu.db.test;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MySQL测试配置工具类
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class MySQLTestConfig {
    
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = MySQLTestConfig.class.getClassLoader().getResourceAsStream("mysql-test.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to load mysql-test.properties: " + e.getMessage());
        }
        return props;
    }
    
    /**
     * 创建MySQL连接池
     */
    public static JDBCPool createMySQLPool(Vertx vertx) {
        Properties props = loadProperties();
        
        if (!props.containsKey("mysql.url")) {
            System.out.println("⚠️ mysql-test.properties not configured properly");
            return null;
        }
        
        PoolOptions poolOptions = new PoolOptions().setMaxSize(Integer.parseInt(props.getProperty("mysql.max_pool_size", "10")));
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(props.getProperty("mysql.url"))
                .setUser(props.getProperty("mysql.user"))
                .setPassword(props.getProperty("mysql.password"));

        return JDBCPool.pool(vertx, connectOptions, poolOptions);
    }
    
    /**
     * 检查MySQL连接是否可用
     */
    public static boolean isMySQLAvailable() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = loadProperties();
            return props.containsKey("mysql.url");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

