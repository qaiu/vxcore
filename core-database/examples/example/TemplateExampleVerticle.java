package cn.qaiu.db.dsl.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.template.JooqTemplateExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.jooq.Condition;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * jOOQ DSL + 模板API混合使用示例
 * 展示如何结合jOOQ DSL的类型安全和模板API的便利性
 */
public class TemplateExampleVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateExampleVerticle.class);
    
    private Pool pool;
    private JooqTemplateExecutor templateExecutor;
    private JooqUserDao userDao;

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting TemplateExample Verticle...");
        
        setupDatabase()
            .compose(v -> runTemplateExamples())
            .onSuccess(result -> {
                LOGGER.info("TemplateExample completed successfully");
                LOGGER.info("Results: {}", result.encodePrettily());
                startPromise.complete();
            })
            .onFailure(throwable -> {
                LOGGER.error("TemplateExample failed", throwable);
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
            
            // 创建jOOQ和模板执行器
            JooqExecutor jooqExecutor = new JooqExecutor(pool);
            templateExecutor = new JooqTemplateExecutor(pool);
            userDao = new JooqUserDao(jooqExecutor);
            
            // 创建表结构
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255),
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
    
    private Future<JsonObject> runTemplateExamples() {
        Promise<JsonObject> promise = Promise.promise();
        
        JsonObject examplesResult = new JsonObject();
        
        // 先插入一些测试数据
        User user1 = new User();
        user1.setUsername("template_user1");
        user1.setEmail("template1@example.com");
        user1.setPassword("pwd123");
        user1.setBio("Template Example User 1");
        user1.setStatus(User.UserStatus.ACTIVE);
        
        userDao.insert(user1)
            .compose(insertedUser -> {
                if (insertedUser.isPresent()) {
                    examplesResult.put("templateInsertExample", "Success: User inserted with ID " + insertedUser.get().getId());
                    LOGGER.info("Template insert example: {}", insertedUser.get());
                    return Future.succeededFuture(insertedUser.get().getId());
                } else {
                    return Future.failedFuture("Template insert failed");
                }
            })
            
            // 示例1：使用模板API查询用户
            .compose(userId -> {
                String templateSql = "SELECT * FROM users WHERE username = :username";
                Map<String, Object> params = JooqTemplateExecutor.buildQueryParams(
                    "username", "template_user1"
                );
                
                return templateExecutor.query(templateSql, params)
                    .map(results -> {
                        if (!results.isEmpty()) {
                            examplesResult.put("templateQueryExample", "Success: Found " + results.size() + " users using template API");
                            examplesResult.put("templateQueryResult", results);
                            LOGGER.info("Template query example: Found {} users", results.size());
                        } else {
                            examplesResult.put("templateQueryExample", "No users found");
                        }
                        return userId;
                    });
            })
            
            // 示例2：使用模板API单条查询
            .compose(userId -> {
                String templateSql = "SELECT username, email FROM users WHERE id = :userId LIMIT 1";
                Map<String, Object> params = JooqTemplateExecutor.buildQueryParams(
                    "userId", userId
                );
                
                return templateExecutor.queryForOne(templateSql, params)
                    .map(result -> {
                        if (result.isPresent()) {
                            examplesResult.put("templateQueryOneExample", "Success: Found user details");
                            examplesResult.put("templateQueryOneResult", result.get());
                            LOGGER.info("Template query one example: {}", result.get());
                        } else {
                            examplesResult.put("templateQueryOneExample", "User not found");
                        }
                        return userId;
                    });
            })
            
            // 示例3：混合使用jOOQ DSL和模板API
            .compose(userId -> {
                // 使用jOOQ DSL构建复杂查询
                Condition condition = DSL.field("user_status").eq("ACTIVE")
                        .and(DSL.field("username").like("%template%"));
                
                // 将jOOQ查询转换为模板信息
                Query jooqQuery = templateExecutor.getJooqExecutor().dsl()
                        .selectFrom("users")
                        .where(condition);
                
                JooqTemplateExecutor.TemplateQueryInfo templateInfo = templateExecutor.toTemplateInfo(jooqQuery);
                
                return templateExecutor.query(templateInfo.getSqlTemplate(), templateInfo.getParameters())
                    .map(results -> {
                        examplesResult.put("mixedQueryExample", "Success: Mixed jOOQ DSL + Template API");
                        examplesResult.put("mixedQueryTemplate", templateInfo.getSqlTemplate());
                        examplesResult.put("mixedQueryParams", templateInfo.getParameters());
                        examplesResult.put("mixedQueryResults", results);
                        LOGGER.info("Mixed query example: {} results", results.size());
                        LOGGER.info("Generated SQL template: {}", templateInfo.getSqlTemplate());
                        LOGGER.info("Generated params: {}", templateInfo.getParameters());
                        return userId;
                    });
            })
            
            // 示例4：复杂查询模板
            .compose(userId -> {
                String complexTemplateSql = """
                    SELECT u.*, 
                           CASE 
                               WHEN u.email_verified = true THEN 'VERIFIED' 
                               ELSE 'PENDING' 
                           END as verification_status
                    FROM users u
                    WHERE u.create_time >= '2024-01-01'
                    ORDER BY u.create_time DESC
                    LIMIT :limit
                """;
                
                Map<String, Object> params = JooqTemplateExecutor.buildQueryParams(
                    "limit", 10
                );
                
                return templateExecutor.query(complexTemplateSql, params)
                    .map(results -> {
                        examplesResult.put("complexTemplateExample", "Success: Complex query with template");
                        examplesResult.put("complexTemplateResults", results);
                        LOGGER.info("Complex template example: {} results", results.size());
                        return userId;
                    });
            })
            
            // 完成所有示例
            .onSuccess(finalUserId -> {
                examplesResult.put("overallTemplateResult", "All template examples completed successfully");
                promise.complete(examplesResult);
            })
            .onFailure(throwable -> {
                LOGGER.error("Template example execution failed", throwable);
                examplesResult.put("templateError", throwable.getMessage());
                promise.complete(examplesResult);
            });
            
        return promise.future();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping TemplateExample Verticle...");
        
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
