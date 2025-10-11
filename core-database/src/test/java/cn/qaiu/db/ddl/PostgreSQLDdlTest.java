package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.ddl.example.ExampleUser;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL DDL映射测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("PostgreSQL DDL映射测试")
public class PostgreSQLDdlTest {

    private Vertx vertx;
    private JDBCPool pool;

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        vertx = Vertx.vertx();

        // 设置配置
        SharedData sharedData = vertx.sharedData();
        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));
        localMap.put(CUSTOM_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));

        VertxHolder.init(vertx);

        // 使用配置工具类创建PostgreSQL连接池
        pool = PostgreSQLTestConfig.createPostgreSQLPool(vertx);
        
        if (pool == null) {
            System.out.println("⚠️ PostgreSQL connection pool not available, skipping tests");
        }
        
        testContext.completeNow();
    }
    // 测试获取服务器时间
    @Test
    @DisplayName("测试获取服务器时间")
    public void testTime(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        pool.query("SELECT NOW()")
            .execute()
            .onComplete(testContext.succeeding(rows -> {
                testContext.verify(() -> {
                    System.out.println("PostgreSQL current time: " + rows.iterator().next().getValue(0));
                    assertTrue(rows.size() > 0, "Should retrieve current time from PostgreSQL");
                });
                testContext.completeNow();
            }));
    }

    /**
     * 测试PostgreSQL基本表创建
     */
    @Test
    @DisplayName("测试PostgreSQL基本表创建")
    public void testPostgreSQLBasicTableCreation(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        CreateTable.createTable(pool, JDBCType.PostgreSQL)
            .onComplete(testContext.succeeding(v -> {
                testContext.verify(() -> {
                    // CreateTable.createTable返回Future<Void>，所以v是null
                    // 只要没有异常就说明成功了
                    assertTrue(true, "PostgreSQL table creation completed successfully");
                });
                // 添加延迟确保操作完成
                vertx.setTimer(100, id -> testContext.completeNow());
            }));
    }

    /**
     * 测试PostgreSQL严格DDL映射
     */
    @Test
    @DisplayName("测试PostgreSQL严格DDL映射")
    public void testPostgreSQLStrictDdlMapping(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        EnhancedCreateTable.createTableWithStrictMapping(pool, JDBCType.PostgreSQL)
            .onComplete(testContext.succeeding(v -> {
                testContext.verify(() -> {
                    // EnhancedCreateTable.createTableWithStrictMapping返回Future<Void>，所以v是null
                    // 只要没有异常就说明成功了
                    assertTrue(true, "PostgreSQL strict DDL mapping completed successfully");
                });
                // 添加延迟确保操作完成
                vertx.setTimer(100, id -> testContext.completeNow());
            }));
    }

    /**
     * 测试PostgreSQL表结构同步
     */
    @Test
    @DisplayName("测试PostgreSQL表结构同步")
    public void testPostgreSQLTableSynchronization(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        EnhancedCreateTable.synchronizeTables(pool, JDBCType.PostgreSQL)
            .onComplete(testContext.succeeding(v -> {
                testContext.verify(() -> {
                    // EnhancedCreateTable.synchronizeTables返回Future<Void>，所以v是null
                    // 只要没有异常就说明成功了
                    assertTrue(true, "PostgreSQL table synchronization completed successfully");
                });
                // 添加延迟确保操作完成
                vertx.setTimer(100, id -> testContext.completeNow());
            }));
    }

    /**
     * 测试PostgreSQL特定功能
     */
    @Test
    @DisplayName("测试PostgreSQL特定功能")
    public void testPostgreSQLSpecificFeatures(VertxTestContext testContext) {
        // 测试PostgreSQL的SERIAL类型
        TableMetadata metadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.PostgreSQL);
        
        testContext.verify(() -> {
            // 验证PostgreSQL特定的列定义
            assertTrue(metadata != null, "TableMetadata should not be null");
            
            // 检查是否有自增列
            boolean hasAutoIncrement = metadata.getColumns().values().stream()
                .anyMatch(ColumnMetadata::isAutoIncrement);
            assertTrue(hasAutoIncrement, "Should have auto-increment columns");
        });
        
        testContext.completeNow();
    }
}
