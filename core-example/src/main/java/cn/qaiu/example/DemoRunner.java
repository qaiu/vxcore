package cn.qaiu.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.template.JooqTemplateExecutor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.jooq.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * 演示程序运行器
 * 展示完整的 jOOQ DSL 框架功能
 */
public class DemoRunner {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunner.class);
    
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
            
            // 创建执行器
            JooqExecutor jooqExecutor = new JooqExecutor(pool);
            JooqTemplateExecutor templateExecutor = new JooqTemplateExecutor(pool);
            JooqUserDao userDao = new JooqUserDao(jooqExecutor);
            
            LOGGER.info("=== 🎯 jOOQ DSL 框架演示开始 ===\n");
            
            // 演示流程
            runDemo(jooqExecutor, templateExecutor, userDao)
                    .onComplete(result -> {
                        if (result.succeeded()) {
                            LOGGER.info("\n🎉 演示成功完成!");
                            LOGGER.info("框架展示结果: {}", result.result().encodePrettily());
                        } else {
                            LOGGER.error("\n❌ 演示失败", result.cause());
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
    
    private static Future<JsonObject> runDemo(JooqExecutor jooqExecutor, 
                                            JooqTemplateExecutor templateExecutor, 
                                            JooqUserDao userDao) {
        
        JsonObject result = new JsonObject();
        LocalDateTime now = LocalDateTime.now();
        
        LOGGER.info("📊 步骤1: 初始化数据库");
        LOGGER.info("└─ 创建测试表: dsl_user");
        
        return initDatabase(jooqExecutor)
                .compose(v -> {
                    LOGGER.info("\n📊 步骤2: 基础 CRUD 操作");
                    
                    // 插入第一个用户
                    User user1 = new User();
                    user1.setUsername("jooq_user_1");
                    user1.setEmail("jooq1@example.com");
                    user1.setPassword("password123");
                    user1.setBio("jOOQ DSL 框架测试用户1");
                    user1.setStatus(User.UserStatus.ACTIVE);
                    
                    LOGGER.info("└─ 插入用户1: {}({})", user1.getUsername(), user1.getId());
                    
                    return userDao.insert(user1)
                            .map(insertedUser -> {
                                if (insertedUser.isPresent()) {
                                    User user = insertedUser.get();
                                    result.put("demo", "jOOQ DSL 框架演示");
                                    result.put("user1", new JsonObject()
                                            .put("id", user.getId())
                                            .put("username", user.getUsername())
                                            .put("email", user.getEmail())
                                            .put("status", user.getStatus().name()));
                                    LOGGER.info("     ✅ 用户插入成功，ID: {}", user.getId());
                                    return user.getId();
                                } else {
                                    throw new RuntimeException("Failed to insert user1");
                                }
                            });
                })
                .compose(userId -> {
                    LOGGER.info("\n📊 步骤3: 高级查询功能");
                    
                    // 使用真正的 jOOQ DSL 构建查询
                    return userDao.findActiveUsers()
                            .map(activeUsers -> {
                                result.put("query", new JsonObject()
                                        .put("activeUsersCount", activeUsers.size())
                                        .put("demo", "真正的 jOOQ DSL 查询"));
                                LOGGER.info("└─ jOOQ DSL 查询活跃用户: {} 个", activeUsers.size());
                                return activeUsers.size();
                            });
                })
                .compose(count -> {
                    LOGGER.info("\n📊 步骤4: 模板 API 集成");
                    
                    String sqlTemplate = "SELECT COUNT(*) as total FROM dsl_user WHERE status = :status";
                    java.util.Map<String, Object> params = JooqTemplateExecutor.buildQueryParams("status", "ACTIVE");
                    
                    return templateExecutor.query(sqlTemplate, params)
                            .map(templateResults -> {
                                int templateCount = 0;
                                if (!templateResults.isEmpty()) {
                                    templateCount = templateResults.get(0).getInteger("total");
                                }
                                
                                result.put("template", new JsonObject()
                                        .put("templateQueryCount", templateCount)
                                        .put("demo", "SQL 模板与 jOOQ DSL 集成"));
                                LOGGER.info("└─ SQL 模板查询结果: {} 个活跃用户", templateCount);
                                return templateCount;
                            });
                })
                .compose(count -> {
                    LOGGER.info("\n📊 步骤5: 性能展示");
                    LocalDateTime endTime = LocalDateTime.now();
                    long durationMs = java.time.Duration.between(now, endTime).toMillis();
                    
                    result.put("performance", new JsonObject()
                            .put("duration", durationMs + "ms")
                            .put("demo", "异步非阻塞性能展示"));
                            
                    LOGGER.info("└─ 异步非阻塞总耗时: {} ms", durationMs);
                    return Future.succeededFuture(result);
                });
    }
    
    private static Future<Void> initDatabase(JooqExecutor jooqExecutor) {
        String createTableSql = "DROP TABLE IF EXISTS dsl_user;" +
                "CREATE TABLE dsl_user (" +
                "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
                "username VARCHAR(255) NOT NULL UNIQUE," +
                "email VARCHAR(255) NOT NULL UNIQUE," +
                "password VARCHAR(255) NOT NULL," +
                "bio TEXT," +
                "status VARCHAR(50) NOT NULL," +
                "balance DECIMAL(10, 2) DEFAULT 0.00," +
                "email_verified BOOLEAN DEFAULT FALSE," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
                
        Query createTableQuery = jooqExecutor.dsl().query(createTableSql);
        return jooqExecutor.executeUpdate(createTableQuery)
                .onSuccess(v -> LOGGER.info("      ✅ 数据库初始化完成"))
                .onFailure(err -> LOGGER.error("      ❌ 数据库初始化失败: {}", err.getMessage()))
                .mapEmpty();
    }
}
