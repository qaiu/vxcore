package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jOOQ DSL框架示例 Verticle
 * 演示如何使用新的jOOQ DSL框架进行数据库操作
 */
public class JooqExampleVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqExampleVerticle.class);
    
    private Pool pool;
    private JooqExecutor executor;
    private JooqUserDao userDao;

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting JooqExample Verticle...");
        
        setupDatabase()
            .compose(v -> runExamples())
            .onSuccess(result -> {
                LOGGER.info("JooqExample completed successfully");
                LOGGER.info("Final result: {}", result.encodePrettily());
                startPromise.complete();
            })
            .onFailure(throwable -> {
                LOGGER.error("JooqExample failed", throwable);
                startPromise.fail(throwable);
            });
    }
    
    private Future<Void> setupDatabase() {
        Promise<Void> promise = Promise.promise();
        
        try {
            // 创建数据库连接配置（使用H2内存数据库进行示例）
            SqlConnectOptions connectOptions = new SqlConnectOptions()
                    .setHost("localhost")
                    .setPort(9092)
                    .setDatabase("h2memdb")
                    .setUser("sa")
                    .setPassword("")
                    .setCachePreparedStatements(true);

            PoolOptions poolOptions = new PoolOptions()
                    .setMaxSize(5);

            // 创建连接池
            pool = Pool.pool(vertx, connectOptions, poolOptions);
            executor = new JooqExecutor(pool);
            userDao = new JooqUserDao(executor);
            
            // 创建表结构
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password_hash VARCHAR(255),
                    email_verified BOOLEAN DEFAULT FALSE,
                    bio TEXT,
                    user_status VARCHAR(20) DEFAULT 'ACTIVE',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            pool.query(createTableSql)
                .execute()
                .onSuccess(result -> {
                    LOGGER.info("Database table created successfully");
                    promise.complete();
                })
                .onFailure(throwable -> {
                    LOGGER.error("Failed to create database table", throwable);
                    promise.fail(throwable);
                });
                
        } catch (Exception e) {
            LOGGER.error("Failed to setup database", e);
            promise.fail(e);
        }
        
        return promise.future();
    }
    
    private Future<JsonObject> runExamples() {
        Promise<JsonObject> promise = Promise.promise();
        
        JsonObject examplesResult = new JsonObject();
        
        // 示例1：插入用户
        User user1 = new User();
        user1.setUsername("john_doe");
        user1.setEmail("john@example.com");
        user1.setPassword("password123");
        user1.setBio("Software Developer");
        user1.setStatus(User.UserStatus.ACTIVE);
        
        userDao.insert(user1)
            .compose(insertedUser -> {
                if (insertedUser.isPresent()) {
                    examplesResult.put("insertExample", "Success: User inserted with ID " + insertedUser.get().getId());
                    LOGGER.info("1. Insert example completed: {}", insertedUser.get());
                    return Future.succeededFuture(insertedUser.get().getId());
                } else {
                    examplesResult.put("insertExample", "Failed: No user inserted");
                    return Future.failedFuture("Insert failed");
                }
            })
            
            // 示例2：插入第二个用户
            .compose(userId -> {
                User user2 = new User();
                user2.setUsername("jane_smith");
                user2.setEmail("jane@example.com");
                user2.setPassword("password456");
                user2.setEmailVerified(true);
                user2.setBio("Product Manager");
                user2.setStatus(User.UserStatus.ACTIVE);
                
                return userDao.insert(user2)
                    .map(insertedUser2 -> {
                        if (insertedUser2.isPresent()) {
                            examplesResult.put("insertExample2", "Success: Second user inserted with ID " + insertedUser2.get().getId());
                            LOGGER.info("2. Second insert example completed: {}", insertedUser2.get());
                            return insertedUser2.get().getId();
                        } else {
                            examplesResult.put("insertExample2", "Failed: Second user not inserted");
                            return userId;
                        }
                    });
            })
            
            // 示例3：根据用户名查找
            .compose(userId -> {
                return userDao.findByUsername("john_doe")
                    .map(userOpt -> {
                        if (userOpt.isPresent()) {
                            examplesResult.put("findByUsernameExample", "Success: User found " + userOpt.get().toString());
                            LOGGER.info("3. Find by username example: {}", userOpt.get());
                        } else {
                            examplesResult.put("findByUsernameExample", "Failed: User not found");
                        }
                        return userId;
                    });
            })
            
            // 示例4：根据邮箱查找
            .compose(userId -> {
                return userDao.findByEmail("jane@example.com")
                    .map(userOpt -> {
                        if (userOpt.isPresent()) {
                            examplesResult.put("findByEmailExample", "Success: User found " + userOpt.get().toString());
                            LOGGER.info("4. Find by email example: {}", userOpt.get());
                        } else {
                            examplesResult.put("findByEmailExample", "Failed: User not found");
                        }
                        return userId;
                    });
            })
            
            // 示例5：查找所有用户
            .compose(userId -> {
                return userDao.findAll()
                    .map(users -> {
                        examplesResult.put("findAllExample", "Success: Found " + users.size() + " users");
                        examplesResult.put("findAllUsers", users.stream().map(User::toString).toList());
                        LOGGER.info("5. Find all example: Found {} users", users.size());
                        return userId;
                    });
            })
            
            // 示例6：验证登录
            .compose(userId -> {
                return userDao.authenticate("john_doe", "password123")
                    .map(authUser -> {
                        if (authUser.isPresent()) {
                            examplesResult.put("authenticationExample", "Success: Authentication passed for " + authUser.get().getUsername());
                            LOGGER.info("6. Authentication example: User {} authenticated successfully", authUser.get().getUsername());
                        } else {
                            examplesResult.put("authenticationExample", "Failed: Authentication failed");
                        }
                        return userId;
                    });
            })
            
            // 示例7：统计用户数量
            .compose(userId -> {
                return userDao.count()
                    .map(userCount -> {
                        examplesResult.put("countExample", "Success: Total users count = " + userCount);
                        LOGGER.info("7. Count example: Total users = {}", userCount);
                        return userId;
                    });
            })
            
            // 完成所有示例
            .onSuccess(finalUserId -> {
                examplesResult.put("overallResult", "All examples completed successfully");
                promise.complete(examplesResult);
            })
            .onFailure(throwable -> {
                LOGGER.error("Example execution failed", throwable);
                examplesResult.put("error", throwable.getMessage());
                promise.complete(examplesResult);
            });
            
        return promise.future();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping JooqExample Verticle...");
        
        if (pool != null) {
            pool.close()
                .onSuccess(v -> {
                    LOGGER.info("Database pool closed successfully");
                    stopPromise.complete();
                })
                .onFailure(throwable -> {
                    LOGGER.error("Failed to close database pool", throwable);
                    stopPromise.complete();
                });
        } else {
            stopPromise.complete();
        }
    }
}
