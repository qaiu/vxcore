package cn.qaiu.db.dsl.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.pool.JDBCPoolInit;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User DSL 框架使用示例
 * 
 * 展示如何使用新的 JOQ + Vert.x DSL 框架
 * 进行类型安全的数据库操作
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DslExampleVerticle extends AbstractVerticle {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DslExampleVerticle.class);
    
    private UserDao userDao;
    private JooqExecutor executor;
    
    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting DSL Example Verticle...");
        
        try {
            // 初始化数据库连接
            initDatabase();
            
            // 执行示例操作
            runExamples()
                .onSuccess(result -> {
                    LOGGER.info("All examples completed successfully!");
                    startPromise.complete();
                })
                .onFailure(error -> {
                    LOGGER.error("Examples failed", error);
                    startPromise.fail(error);
                });
                
        } catch (Exception e) {
            LOGGER.error("Failed to initialize DSL Example", e);
            startPromise.fail(e);
        }
    }
    
    /**
     * 初始化数据库连接
     */
    private void initDatabase() {
        LOGGER.info("Initializing database connection...");
        
        // 使用现有的 JDBCPoolInit 获取连接池
        JDBCPool pool = JDBCPoolInit.instance().getPool();
        if (pool == null) {
            throw new IllegalStateException("Database pool not initialized");
        }
        
        // 创建执行器和 DAO
        this.executor = new JooqExecutor(pool);
        this.userDao = new UserDao(executor);
        
        LOGGER.info("Database connection initialized successfully");
    }
    
    /**
     * 运行示例操作
     */
    private Future<JsonObject> runExamples() {
        LOGGER.info("Running DSL Examples...");
        
        Promise<JsonObject> promise = Promise.promise();
        
        // 1. 创建用户
        createUsers()
            .compose(users -> {
                LOGGER.info("Created {} users", users.size());
                
                // 2. 查询演示
                return demonstrateQueries();
            })
            .compose(queryResult -> {
                LOGGER.info("Query demonstrations completed");
                
                // 3. 更新演示
                return demonstrateUpdates();
            })
            .compose(updateResult -> {
                LOGGER.info("Update demonstrations completed");
                
                // 4. 统计演示
                return demonstrateStatistics();
            })
            .onSuccess(finalResult -> {
                LOGGER.info("All DSL examples completed successfully!");
                promise.complete(finalResult);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }
    
    /**
     * 示例1：创建用户
     */
    private Future<List<User>> createUsers() {
        LOGGER.info("=== Example 1: Creating Users ===");
        
        return userDao.createUser("alice", "alice@example.com", "password123")
            .compose(alice -> {
                LOGGER.info("Created user: {}", alice);
                return Future.succeededFuture(List.of(alice));
            })
            .recover(error -> {
                if (error.getMessage() != null && error.getMessage().contains("Duplicate")) {
                    LOGGER.info("User already exists, continuing with examples...");
                    return userDao.findByUsername("alice")
                        .map(userOpt -> List.of(userOpt.orElseThrow()));
                }
                return Future.failedFuture(error);
            });
    }
    
    /**
     * 示例2：查询演示
     */
    private Future<JsonObject> demonstrateQueries() {
        LOGGER.info("=== Example 2: Query Demonstrations ===");
        
        return userDao.findByUsername("alice")
            .compose(aliceOpt -> {
                if (aliceOpt.isPresent()) {
                    User alice = aliceOpt.get();
                    LOGGER.info("Found user by username: {}", alice.getUsername());
                    
                    return userDao.findByEmail("alice@example.com")
                        .compose(emailOpt -> {
                            if (emailOpt.isPresent()) {
                                LOGGER.info("Found user by email: {}", emailOpt.get().getEmail());
                            }
                            
                            return userDao.findActiveUsers()
                                .compose(activeUsers -> {
                                    LOGGER.info("Found {} active users", activeUsers.size());
                                    
                                    return userDao.findByAgeRange(18, 65)
                                        .map(ageFiltered -> {
                                            LOGGER.info("Found {} users in age range 18-65", ageFiltered.size());
                                            return new JsonObject().put("queryDemo", "completed");
                                        });
                                });
                        });
                } else {
                    return Future.succeededFuture(new JsonObject().put("queryDemo", "no_users_found"));
                }
            });
    }
    
    /**
     * 示例3：更新演示
     */
    private Future<JsonObject> demonstrateUpdates() {
        LOGGER.info("=== Example 3: Update Demonstrations ===");
        
        return userDao.findByUsername("alice")
            .compose(aliceOpt -> {
                if (aliceOpt.isPresent()) {
                    User alice = aliceOpt.get();
                    
                    return userDao.updatePassword(alice.getId(), "newSecurePassword123")
                        .compose(passwordUpdated -> {
                            LOGGER.info("Password updated: {}", passwordUpdated);
                            
                            return userDao.verifyUserEmail(alice.getId())
                                .compose(emailVerified -> {
                                    LOGGER.info("Email verified: {}", emailVerified);
                                    
                                    return userDao.updateUserStatus(alice.getId(), User.UserStatus.ACTIVE)
                                        .map(statusUpdated -> {
                                            LOGGER.info("Status updated: {}", statusUpdated);
                                            return new JsonObject().put("updateDemo", "completed");
                                        });
                                });
                        });
                } else {
                    LOGGER.warn("No user found for updates");
                    return Future.succeededFuture(new JsonObject().put("updateDemo", "no_user_found"));
                }
            });
    }
    
    /**
     * 示例4：统计演示
     */
    private Future<JsonObject> demonstrateStatistics() {
        LOGGER.info("=== Example 4: Statistics Demonstrations ===");
        
        return userDao.getUserStatistics()
            .compose(stats -> {
                LOGGER.info("User statistics: {}", stats.encodePrettily());
                
                return userDao.findAll()
                    .compose(allUsers -> {
                        LOGGER.info("Total users in database: {}", allUsers.size());
                        
                        return userDao.count(DSL.field("email_verified").eq(true))
                            .map(emailVerifiedCount -> {
                                LOGGER.info("Email verified users: {}", emailVerifiedCount);
                                
                                return new JsonObject()
                                    .put("statisticsDemo", "completed")
                                    .put("totalUsers", allUsers.size())
                                    .put("emailVerifiedUsers", emailVerifiedCount);
                            });
                    });
            });
    }
    
    /**
     * 演示复杂查询
     */
    private Future<Void> demonstrateComplexQueries() {
        LOGGER.info("=== Complex Query Demonstrations ===");
        
        return userDao.findPage(0, 10)
            .compose(usersPage -> {
                LOGGER.info("Page 0 (size 10): found {} users", usersPage.size());
                
                return userDao.findByMinBalance(new BigDecimal("100.00"))
                    .map(richUsers -> {
                        LOGGER.info("Users with balance >= 100: {}", richUsers.size());
                        return null;
                    });
            });
    }
    
    @Override
    public void stop() throws Exception {
        LOGGER.info("Stopping DSL Example Verticle...");
        super.stop();
    }
}
