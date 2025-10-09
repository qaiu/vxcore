package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * UserDao 重构演示程序
 * 
 * 展示重构后的 UserDao 如何使用 jOOQ DSL 而不是手写 SQL
 */
public class UserDaoRefactorDemo {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoRefactorDemo.class);
    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        try {
            // 配置数据库连接
            SqlConnectOptions connectOptions = new SqlConnectOptions()
                    .setPort(8000)
                    .setHost("localhost")
                    .setDatabase("testdb")
                    .setUser("test")
                    .setPassword("test");

            PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
            Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
            
            // 创建执行器和 DAO
            JooqExecutor executor = new JooqExecutor(pool);
            UserDao userDao = new UserDao(executor);
            
            LOGGER.info("=== 🎯 UserDao 重构演示开始 ===");
            LOGGER.info("✅ UserDao 现在继承 AbstractDao，使用 jOOQ DSL 而不是手写 SQL");
            
            // 运行演示
            runDemo(userDao, executor)
                    .onComplete(result -> {
                        if (result.succeeded()) {
                            LOGGER.info("\n🎉 重构演示成功完成!");
                            LOGGER.info("重构结果: {}", result.result().encodePrettily());
                        } else {
                            LOGGER.error("\n❌ 重构演示失败", result.cause());
                        }
                        
                        // 关闭连接
                        pool.close()
                                .onComplete(v -> {
                                    vertx.close();
                                    LOGGER.info("\n👋 演示结束，连接已关闭");
                                });
                    });
            
        } catch (Exception e) {
            LOGGER.error("演示启动失败", e);
            vertx.close();
        }
    }
    
    private static Future<JsonObject> runDemo(UserDao userDao, JooqExecutor executor) {
        JsonObject result = new JsonObject();
        
        LOGGER.info("\n📊 步骤1: 初始化数据库");
        return initDatabase(executor)
                .compose(v -> {
                    LOGGER.info("└─ 数据库表创建完成");
                    
                    LOGGER.info("\n📊 步骤2: 创建用户 - 使用 AbstractDao.insert()");
                    return userDao.createUser("jooq_user", "jooq@example.com", "password123");
                })
                .compose(createdUser -> {
                    result.put("refactor", "UserDao 重构演示");
                    result.put("user1", new JsonObject()
                            .put("id", createdUser.getId())
                            .put("username", createdUser.getUsername())
                            .put("email", createdUser.getEmail())
                            .put("method", "AbstractDao.insert() - jOOQ DSL"));
                    LOGGER.info("     ✅ 用户创建成功，ID: {}", createdUser.getId());
                    
                    LOGGER.info("\n📊 步骤3: 查询用户 - 使用 jOOQ DSL Condition");
                    return userDao.findByUsername("jooq_user");
                })
                .compose(userOptional -> {
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        result.put("query", new JsonObject()
                                .put("method", "DSL.field().eq() - jOOQ DSL")
                                .put("username", user.getUsername())
                                .put("found", true));
                        LOGGER.info("     ✅ 通过用户名查询成功: {}", user.getUsername());
                        
                        LOGGER.info("\n📊 步骤4: 更新用户 - 使用 AbstractDao.update()");
                        user.setBio("Updated with jOOQ DSL");
                        return userDao.update(user);
                    } else {
                        return Future.failedFuture("User not found");
                    }
                })
                .compose(updatedUser -> {
                    if (updatedUser.isPresent()) {
                        result.put("update", new JsonObject()
                                .put("method", "AbstractDao.update() - jOOQ DSL")
                                .put("bio", updatedUser.get().getBio())
                                .put("success", true));
                        LOGGER.info("     ✅ 用户更新成功: {}", updatedUser.get().getBio());
                        
                        LOGGER.info("\n📊 步骤5: 复杂查询 - 使用 jOOQ DSL Condition");
                        return userDao.findByCondition(
                                DSL.field("username").eq("jooq_user")
                                        .and(DSL.field("status").eq("ACTIVE"))
                        );
                    } else {
                        return Future.failedFuture("Update failed");
                    }
                })
                .compose(users -> {
                    result.put("complexQuery", new JsonObject()
                            .put("method", "DSL.field().eq().and() - jOOQ DSL")
                            .put("results", users.size())
                            .put("success", true));
                    LOGGER.info("     ✅ 复杂查询成功，结果数: {}", users.size());
                    
                    LOGGER.info("\n📊 步骤6: 年龄范围查询 - 使用 jOOQ DSL between()");
                    return userDao.findByAgeRange(20, 30);
                })
                .compose(ageUsers -> {
                    result.put("ageRangeQuery", new JsonObject()
                            .put("method", "DSL.field().between() - jOOQ DSL")
                            .put("results", ageUsers.size())
                            .put("success", true));
                    LOGGER.info("     ✅ 年龄范围查询成功，结果数: {}", ageUsers.size());
                    
                    LOGGER.info("\n📊 步骤7: 余额查询 - 使用 jOOQ DSL ge()");
                    return userDao.findByMinBalance(new BigDecimal("50.00"));
                })
                .compose(balanceUsers -> {
                    result.put("balanceQuery", new JsonObject()
                            .put("method", "DSL.field().ge() - jOOQ DSL")
                            .put("results", balanceUsers.size())
                            .put("success", true));
                    LOGGER.info("     ✅ 余额查询成功，结果数: {}", balanceUsers.size());
                    
                    LOGGER.info("\n📊 步骤8: 统计查询 - 使用 AbstractDao.count()");
                    return userDao.count();
                })
                .compose(totalCount -> {
                    result.put("countQuery", new JsonObject()
                            .put("method", "AbstractDao.count() - jOOQ DSL")
                            .put("totalUsers", totalCount)
                            .put("success", true));
                    LOGGER.info("     ✅ 统计查询成功，总用户数: {}", totalCount);
                    
                    LOGGER.info("\n📊 步骤9: 删除用户 - 使用 AbstractDao.delete()");
                    return userDao.delete(1L); // 假设ID是1
                })
                .compose(deleted -> {
                    result.put("delete", new JsonObject()
                            .put("method", "AbstractDao.delete() - jOOQ DSL")
                            .put("deleted", deleted)
                            .put("success", true));
                    LOGGER.info("     ✅ 用户删除成功: {}", deleted);
                    
                    LOGGER.info("\n🎯 重构对比总结:");
                    LOGGER.info("└─ ❌ 旧版本: 手写 SQL 字符串，容易出错");
                    LOGGER.info("└─ ✅ 新版本: jOOQ DSL，类型安全，编译时检查");
                    LOGGER.info("└─ ❌ 旧版本: 手动参数绑定，容易 SQL 注入");
                    LOGGER.info("└─ ✅ 新版本: jOOQ 自动参数绑定，防 SQL 注入");
                    LOGGER.info("└─ ❌ 旧版本: 重复的 CRUD 代码");
                    LOGGER.info("└─ ✅ 新版本: 继承 AbstractDao，代码复用");
                    
                    return Future.succeededFuture(result);
                });
    }
    
    private static Future<Void> initDatabase(JooqExecutor executor) {
        String createTableSql = "DROP TABLE IF EXISTS dsl_user;" +
                "CREATE TABLE dsl_user (" +
                "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
                "username VARCHAR(255) NOT NULL UNIQUE," +
                "email VARCHAR(255) NOT NULL UNIQUE," +
                "password VARCHAR(255) NOT NULL," +
                "age INTEGER," +
                "bio TEXT," +
                "status VARCHAR(50) NOT NULL," +
                "balance DECIMAL(10, 2) DEFAULT 0.00," +
                "email_verified BOOLEAN DEFAULT FALSE," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
                
        return executor.executeUpdate(executor.dsl().query(createTableSql))
                .onSuccess(v -> LOGGER.info("      ✅ 数据库初始化完成"))
                .onFailure(err -> LOGGER.error("      ❌ 数据库初始化失败: {}", err.getMessage()))
                .mapEmpty();
    }
}
