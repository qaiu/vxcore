package cn.qaiu.example;

import cn.qaiu.example.config.DatabaseConfig;
import cn.qaiu.example.config.ServerConfig;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.service.UserServiceImpl;
import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单示例应用
 * 
 * @author QAIU
 */
public class SimpleExampleApplication extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExampleApplication.class);

    private Pool pool;
    private JooqExecutor executor;
    private UserDao userDao;
    private UserServiceImpl userService;

    public SimpleExampleApplication() {
    }

    public SimpleExampleApplication(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting SimpleExampleApplication...");

        // 初始化数据库配置
        JsonObject config = new JsonObject()
                .put("url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .put("driver_class", "org.h2.Driver")
                .put("user", "sa")
                .put("password", "")
                .put("max_pool_size", 10);

        // 创建数据库连接池
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(config.getString("url"))
                .setUser(config.getString("user"))
                .setPassword(config.getString("password"));

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(config.getInteger("max_pool_size", 10));

        pool = JDBCPool.pool(vertx, connectOptions, poolOptions);

        // 创建JooqExecutor
        executor = new JooqExecutor(pool);

        // 创建DAO
        userDao = new UserDao(executor);
        userService = new UserServiceImpl(executor);

        // 初始化数据库表
        initializeDatabase()
                .onSuccess(v -> {
                    LOGGER.info("✅ SimpleExampleApplication started successfully");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping SimpleExampleApplication...");
        
        if (pool != null) {
            pool.close()
                    .onSuccess(v -> {
                        LOGGER.info("✅ Database pool closed successfully");
                        stopPromise.complete();
                    })
                    .onFailure(stopPromise::fail);
        } else {
            stopPromise.complete();
        }
    }

    /**
     * 初始化数据库表
     */
    private io.vertx.core.Future<Void> initializeDatabase() {
        return executor.executeUpdate(
                DSL.query("CREATE TABLE IF NOT EXISTS dsl_user (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "age INT DEFAULT 0, " +
                        "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                        "balance DECIMAL(10,2) DEFAULT 0.00, " +
                        "email_verified BOOLEAN DEFAULT FALSE, " +
                        "bio TEXT, " +
                        "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")"))
                .compose(v -> executor.executeUpdate(
                        DSL.query("CREATE TABLE IF NOT EXISTS orders (" +
                                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                                "order_no VARCHAR(50) NOT NULL UNIQUE, " +
                                "user_id BIGINT NOT NULL, " +
                                "product_id BIGINT NOT NULL, " +
                                "quantity INT NOT NULL DEFAULT 1, " +
                                "unit_price DECIMAL(10,2) NOT NULL, " +
                                "total_amount DECIMAL(10,2) NOT NULL, " +
                                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                                "payment_method VARCHAR(20), " +
                                "payment_time DATETIME, " +
                                "shipping_time DATETIME, " +
                                "shipping_address TEXT, " +
                                "remark TEXT, " +
                                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                                ")")))
                .compose(v -> executor.executeUpdate(
                        DSL.query("CREATE TABLE IF NOT EXISTS order_details (" +
                                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                                "order_id BIGINT NOT NULL, " +
                                "product_id BIGINT NOT NULL, " +
                                "product_name VARCHAR(100) NOT NULL, " +
                                "unit_price DECIMAL(10,2) NOT NULL, " +
                                "quantity INT NOT NULL DEFAULT 1, " +
                                "subtotal DECIMAL(10,2) NOT NULL, " +
                                "category VARCHAR(50), " +
                                "description TEXT, " +
                                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                                ")")))
                .compose(v -> executor.executeUpdate(
                        DSL.query("CREATE TABLE IF NOT EXISTS products (" +
                                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                                "name VARCHAR(100) NOT NULL, " +
                                "description TEXT, " +
                                "category VARCHAR(50), " +
                                "price DECIMAL(10,2) NOT NULL, " +
                                "stock INT DEFAULT 0, " +
                                "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                                ")")))
                .compose(v -> {
                    LOGGER.info("✅ Database tables initialized successfully");
                    return io.vertx.core.Future.succeededFuture();
                });
    }

    /**
     * 获取UserDao实例
     */
    public UserDao getUserDao() {
        return userDao;
    }

    /**
     * 获取UserService实例
     */
    public UserServiceImpl getUserService() {
        return userService;
    }

    /**
     * 获取JooqExecutor实例
     */
    public JooqExecutor getExecutor() {
        return executor;
    }

    /**
     * 获取数据库连接池
     */
    public Pool getPool() {
        return pool;
    }
}
