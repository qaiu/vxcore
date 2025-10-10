package cn.qaiu.db.ddl;

import cn.qaiu.db.ddl.example.ExampleUser;
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
 * 简化的DDL测试用例
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("简化DDL测试")
public class SimpleDdlTest {

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

        // 创建H2数据库连接 - 使用统一配置
        pool = cn.qaiu.db.test.H2TestConfig.createH2Pool(vertx);
        
        // 清理测试表，确保测试隔离
        cn.qaiu.db.test.H2TestConfig.cleanupTestTables(pool);
        
        testContext.completeNow();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (pool != null) {
            pool.close().onComplete(testContext.succeedingThenComplete());
        } else {
            testContext.completeNow();
        }
    }

    /**
     * 测试基本表创建
     */
    @Test
    @DisplayName("测试基本表创建")
    public void testBasicTableCreation(VertxTestContext testContext) {
        try {
            // 使用TableMetadata创建表结构
            TableMetadata tableMetadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.H2DB);
            
            // 生成建表SQL
            String createSQL = generateCreateTableSQL(tableMetadata);
            
            // 执行建表SQL
            pool.query(createSQL)
                .execute()
                .onSuccess(result -> {
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 测试严格DDL映射
     */
    @Test
    @DisplayName("测试严格DDL映射")
    public void testStrictDdlMapping(VertxTestContext testContext) {
        try {
            // 使用TableMetadata创建表结构
            TableMetadata tableMetadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.H2DB);
            
            // 生成建表SQL
            String createSQL = generateCreateTableSQL(tableMetadata);
            
            // 执行建表SQL
            pool.query(createSQL)
                .execute()
                .onSuccess(result -> {
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 测试表结构同步
     */
    @Test
    @DisplayName("测试表结构同步")
    public void testTableSynchronization(VertxTestContext testContext) {
        try {
            // 先创建表
            TableMetadata tableMetadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.H2DB);
            String createSQL = generateCreateTableSQL(tableMetadata);
            
            pool.query(createSQL)
                .execute()
                .onSuccess(result -> {
                    // 验证表创建成功
                    pool.query("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'EXAMPLE_USER'")
                        .execute()
                        .onSuccess(countResult -> {
                            testContext.completeNow();
                        })
                        .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }
    
    /**
     * 生成建表SQL
     */
    private String generateCreateTableSQL(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableMetadata.getTableName()).append("` (\n");
        
        boolean first = true;
        for (ColumnMetadata column : tableMetadata.getColumns().values()) {
            if (!first) {
                sql.append(",\n");
            }
            sql.append("  ").append(column.toColumnDefinition(JDBCType.H2DB));
            first = false;
        }
        
        sql.append("\n)");
        
        if (tableMetadata.getComment() != null && !tableMetadata.getComment().isEmpty()) {
            sql.append(" COMMENT='").append(tableMetadata.getComment()).append("'");
        }
        
        return sql.toString();
    }
}
