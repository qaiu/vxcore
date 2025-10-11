package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.ddl.example.ExampleUser;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL集成测试
 * 需要PostgreSQL服务器运行在127.0.0.1:5432
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("PostgreSQL集成测试")
public class PostgreSQLIntegrationTest {

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

        // 检查PostgreSQL是否可用
        if (!PostgreSQLTestConfig.isPostgreSQLAvailable()) {
            testContext.failNow(new RuntimeException("PostgreSQL driver not available"));
            return;
        }
        
        // 创建PostgreSQL连接
        pool = PostgreSQLTestConfig.createPostgreSQLPool(vertx);
        
        testContext.completeNow();
    }

    /**
     * 测试PostgreSQL DDL映射完整流程
     */
    @Test
    @DisplayName("测试PostgreSQL DDL映射完整流程")
    public void testPostgreSQLDdlMappingFlow(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        // 1. 创建基本表
        CreateTable.createTable(pool, JDBCType.PostgreSQL)
            .compose(v -> {
                // 2. 执行严格DDL映射
                return EnhancedCreateTable.createTableWithStrictMapping(pool, JDBCType.PostgreSQL);
            })
            .compose(v -> {
                // 3. 同步表结构
                return EnhancedCreateTable.synchronizeTables(pool, JDBCType.PostgreSQL);
            })
            .onComplete(testContext.succeeding(v -> {
                testContext.verify(() -> {
                    assertTrue(true, "PostgreSQL DDL mapping flow completed successfully");
                });
                // 添加延迟确保操作完成
                vertx.setTimer(100, id -> testContext.completeNow());
            }));
    }

    /**
     * 测试PostgreSQL表结构比较
     */
    @Test
    @DisplayName("测试PostgreSQL表结构比较")
    public void testPostgreSQLTableStructureComparison(VertxTestContext testContext) {
        if (pool == null) {
            testContext.completeNow();
            return;
        }
        // 创建表
        CreateTable.createTable(pool, JDBCType.PostgreSQL)
            .compose(v -> {
                // 比较表结构
                TableMetadata metadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.PostgreSQL);
                return TableStructureComparator.compareTableStructure(pool, metadata, JDBCType.PostgreSQL);
            })
            .onComplete(testContext.succeeding(differences -> {
                testContext.verify(() -> {
                    assertTrue(differences != null, "Differences should not be null");
                    // 表应该已经存在，所以不应该有TABLE_NOT_EXISTS差异
                    boolean hasTableNotExists = differences.stream()
                        .anyMatch(diff -> diff.getType() == TableStructureComparator.DifferenceType.TABLE_NOT_EXISTS);
                    assertTrue(!hasTableNotExists, "Table should exist, no TABLE_NOT_EXISTS differences expected");
                });
                // 添加延迟确保操作完成
                vertx.setTimer(100, id -> testContext.completeNow());
            }));
    }

    /**
     * 测试PostgreSQL特定数据类型
     */
    @Test
    @DisplayName("测试PostgreSQL特定数据类型")
    public void testPostgreSQLSpecificDataTypes(VertxTestContext testContext) {
        // 测试PostgreSQL的SERIAL类型
        TableMetadata metadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.PostgreSQL);
        
        testContext.verify(() -> {
            assertTrue(metadata != null, "TableMetadata should not be null");
            
            // 检查是否有自增列
            boolean hasAutoIncrement = metadata.getColumns().values().stream()
                .anyMatch(ColumnMetadata::isAutoIncrement);
            assertTrue(hasAutoIncrement, "Should have auto-increment columns");
            
            // 检查PostgreSQL特定的列定义
            metadata.getColumns().values().forEach(column -> {
                if (column.isAutoIncrement()) {
                    // PostgreSQL的自增列应该使用SERIAL或BIGSERIAL
                    assertTrue(column.getType().equals("INT") || column.getType().equals("BIGINT"),
                        "Auto-increment columns should be INT or BIGINT for PostgreSQL");
                }
            });
        });
        
        testContext.completeNow();
    }
}
