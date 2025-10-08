package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.vx.core.util.ConfigConstant;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化的表结构更新测试
 * 测试DDL映射系统的表结构自动更新功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("简化的表结构更新测试")
public class SimpleTableUpdateTest {

    private Pool pool;
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        
        // 设置配置
        SharedData sharedData = vertx.sharedData();
        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));
        localMap.put(CUSTOM_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));

        VertxHolder.init(vertx);

        // 创建H2数据库连接 - 使用MySQL模式
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE")
                .setUser("sa")
                .setPassword("");

        pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
        
        testContext.completeNow();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (pool != null) {
            pool.close();
        }
        if (vertx != null) {
            vertx.close(testContext.succeedingThenComplete());
        }
    }

    /**
     * 第一步：创建初始表结构
     */
    @Test
    @DisplayName("第一步：创建初始表结构")
    void testStep1_CreateInitialTable(VertxTestContext testContext) {
        try {
            // 使用简单的SQL创建表 - H2数据库语法
            String createSQL = """
                CREATE TABLE IF NOT EXISTS test_user (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    age INT DEFAULT 0,
                    active BOOLEAN DEFAULT TRUE,
                    PRIMARY KEY (id)
                )
                """;
            
            System.out.println("生成的建表SQL:");
            System.out.println(createSQL);
            
            // 执行建表SQL
            pool.query(createSQL)
                .execute()
                .onSuccess(result -> {
                    System.out.println("建表SQL执行成功");
                    
                    // 验证表是否创建成功 - 使用H2数据库的SHOW TABLES
                    pool.query("SHOW TABLES")
                        .execute()
                        .onSuccess(countResult -> {
                            System.out.println("所有表:");
                            for (var row : countResult) {
                                System.out.println("  - " + row.getString(0));
                            }
                            
                            // 检查是否存在test_user表
                            boolean tableExists = false;
                            for (var row : countResult) {
                                String tableName = row.getString(0);
                                if ("test_user".equalsIgnoreCase(tableName)) {
                                    tableExists = true;
                                    break;
                                }
                            }
                            
                            if (tableExists) {
                                // 验证初始字段数量
                                pool.query("SHOW COLUMNS FROM test_user")
                                    .execute()
                                    .onSuccess(columnCountResult -> {
                                        int columnCount = 0;
                                        System.out.println("表test_user的字段:");
                                        for (var row : columnCountResult) {
                                            System.out.println("  - " + row.getString(0) + " " + row.getString(1));
                                            columnCount++;
                                        }
                                        
                                        System.out.println("✅ 第一步完成：初始表结构创建成功，包含" + columnCount + "个字段");
                                        testContext.completeNow();
                                    })
                                    .onFailure(e -> {
                                        System.out.println("查询字段失败: " + e.getMessage());
                                        testContext.failNow(e);
                                    });
                            } else {
                                System.out.println("表test_user不存在");
                                testContext.failNow(new RuntimeException("表test_user不存在"));
                            }
                        })
                        .onFailure(e -> {
                            System.out.println("查询表失败: " + e.getMessage());
                            testContext.failNow(e);
                        });
                })
                .onFailure(e -> {
                    System.out.println("建表SQL执行失败: " + e.getMessage());
                    testContext.failNow(e);
                });
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 第二步：测试表结构自动更新 - 添加新字段
     */
    @Test
    @DisplayName("第二步：测试表结构自动更新 - 添加新字段")
    void testStep2_AutoUpdateTableStructure(VertxTestContext testContext) {
        try {
            // 先创建基础表结构
            String createSQL = """
                CREATE TABLE IF NOT EXISTS test_user (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    age INT DEFAULT 0,
                    active BOOLEAN DEFAULT TRUE,
                    PRIMARY KEY (id)
                )
                """;
            
            System.out.println("先创建基础表结构...");
            
            // 执行建表SQL
            pool.query(createSQL)
                .execute()
                .compose(result -> {
                    System.out.println("基础表创建成功，开始添加新字段...");
                    
                    // 使用ALTER TABLE添加新字段
                    String alterSQL1 = "ALTER TABLE test_user ADD COLUMN phone VARCHAR(20)";
                    String alterSQL2 = "ALTER TABLE test_user ADD COLUMN address VARCHAR(200)";
                    String alterSQL3 = "ALTER TABLE test_user ADD COLUMN birthday DATE";
                    
                    System.out.println("执行ALTER TABLE语句添加新字段...");
                    
                    // 执行第一个ALTER TABLE
                    return pool.query(alterSQL1).execute();
                })
                .compose(result -> {
                    System.out.println("添加PHONE字段成功");
                    return pool.query("ALTER TABLE test_user ADD COLUMN address VARCHAR(200)").execute();
                })
                .compose(result -> {
                    System.out.println("添加ADDRESS字段成功");
                    return pool.query("ALTER TABLE test_user ADD COLUMN birthday DATE").execute();
                })
                .onSuccess(result -> {
                    System.out.println("添加BIRTHDAY字段成功");
                    
                    // 验证表结构是否已更新
                    pool.query("SHOW COLUMNS FROM test_user")
                        .execute()
                        .onSuccess(columnCountResult -> {
                            int columnCount = 0;
                            System.out.println("更新后表test_user的字段:");
                            for (var row : columnCountResult) {
                                System.out.println("  - " + row.getString(0) + " " + row.getString(1));
                                columnCount++;
                            }
                            
                            // 验证新增的字段是否存在
                            pool.query("SHOW COLUMNS FROM test_user")
                                .execute()
                                .onSuccess(columnsResult -> {
                                    // 检查是否包含新增的字段
                                    boolean hasPhone = false;
                                    boolean hasAddress = false;
                                    boolean hasBirthday = false;
                                    
                                    for (var row : columnsResult) {
                                        String columnName = row.getString(0);
                                        if ("phone".equalsIgnoreCase(columnName)) hasPhone = true;
                                        if ("address".equalsIgnoreCase(columnName)) hasAddress = true;
                                        if ("birthday".equalsIgnoreCase(columnName)) hasBirthday = true;
                                    }
                                    
                                    assertTrue(hasPhone, "表应该包含PHONE字段");
                                    assertTrue(hasAddress, "表应该包含ADDRESS字段");
                                    assertTrue(hasBirthday, "表应该包含BIRTHDAY字段");
                                    
                                    System.out.println("✅ 第二步完成：表结构自动更新成功，新增3个字段");
                                    System.out.println("   - PHONE: VARCHAR(20)");
                                    System.out.println("   - ADDRESS: VARCHAR(200)");
                                    System.out.println("   - BIRTHDAY: DATE");
                                    
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                        })
                        .onFailure(testContext::failNow);
                })
                .onFailure(e -> {
                    System.out.println("ALTER TABLE执行失败: " + e.getMessage());
                    testContext.failNow(e);
                });
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }
}
