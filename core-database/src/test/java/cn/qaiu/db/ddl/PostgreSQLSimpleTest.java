package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.ddl.example.ExampleUser;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL DDL测试 - 简化版本
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("PostgreSQL DDL测试")
public class PostgreSQLSimpleTest {

    private Vertx vertx;
    private JDBCPool pool;

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        vertx = Vertx.vertx();
        
        // 创建PostgreSQL数据库连接
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:postgresql://localhost:5432/testdb")
                .setUser("testuser")
                .setPassword("testpass");

        pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
        
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
     * 测试PostgreSQL列定义生成
     */
    @Test
    @DisplayName("测试PostgreSQL列定义生成")
    public void testPostgreSQLColumnDefinition(VertxTestContext testContext) {
        // 测试PostgreSQL的SERIAL类型
        TableMetadata metadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.PostgreSQL);
        
        testContext.verify(() -> {
            assertTrue(metadata != null, "TableMetadata should not be null");
            
            // 检查自增列的PostgreSQL定义
            metadata.getColumns().values().forEach(column -> {
                if (column.isAutoIncrement()) {
                    String columnDef = column.toColumnDefinition(JDBCType.PostgreSQL);
                    System.out.println("PostgreSQL Column Definition: " + columnDef);
                    
                    // 验证SERIAL类型不包含DEFAULT值
                    assertTrue(!columnDef.contains("DEFAULT"), 
                        "PostgreSQL SERIAL type should not contain DEFAULT: " + columnDef);
                    
                    // 验证SERIAL类型不包含AUTO_INCREMENT
                    assertTrue(!columnDef.contains("AUTO_INCREMENT"), 
                        "PostgreSQL SERIAL type should not contain AUTO_INCREMENT: " + columnDef);
                    
                    // 验证SERIAL类型不包含NOT NULL（因为SERIAL本身就包含NOT NULL）
                    assertTrue(!columnDef.contains("NOT NULL"), 
                        "PostgreSQL SERIAL type should not contain NOT NULL: " + columnDef);
                }
            });
        });
        
        testContext.completeNow();
    }

    /**
     * 测试PostgreSQL建表SQL生成
     */
    @Test
    @DisplayName("测试PostgreSQL建表SQL生成")
    public void testPostgreSQLCreateTableSql(VertxTestContext testContext) {
        TableMetadata metadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.PostgreSQL);
        
        testContext.verify(() -> {
            // 生成PostgreSQL建表SQL
            String createTableSql = generateCreateTableSql(metadata, JDBCType.PostgreSQL);
            System.out.println("PostgreSQL Create Table SQL:");
            System.out.println(createTableSql);
            
            // 验证SQL语法
            assertTrue(createTableSql.contains("CREATE TABLE"), "Should contain CREATE TABLE");
            assertTrue(createTableSql.contains("\"example_user\""), "Should contain table name with quotes");
            
            // 验证SERIAL类型
            assertTrue(createTableSql.contains("SERIAL") || createTableSql.contains("BIGSERIAL"), 
                "Should contain SERIAL or BIGSERIAL for auto-increment columns");
            
            // 验证没有NOT SERIAL这样的错误语法
            assertTrue(!createTableSql.contains("NOT SERIAL"), 
                "Should not contain 'NOT SERIAL' syntax error");
        });
        
        testContext.completeNow();
    }

    /**
     * 生成创建表的SQL（复制自TableStructureComparator）
     */
    private String generateCreateTableSql(TableMetadata metadata, JDBCType dbType) {
        StringBuilder sb = new StringBuilder();
        String quotationMarks = dbType == JDBCType.PostgreSQL ? "\"" : "`";
        
        sb.append("CREATE TABLE ").append(quotationMarks).append(metadata.getTableName()).append(quotationMarks).append(" (\n");
        
        java.util.List<String> columnDefs = new java.util.ArrayList<>();
        for (ColumnMetadata column : metadata.getColumns().values()) {
            columnDefs.add("  " + column.toColumnDefinition(dbType));
        }
        
        sb.append(String.join(",\n", columnDefs));
        sb.append("\n)");
        
        if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL不需要ENGINE和CHARSET，但可以添加注释
            if (metadata.getComment() != null && !metadata.getComment().isEmpty()) {
                sb.append(";\nCOMMENT ON TABLE ").append(quotationMarks).append(metadata.getTableName())
                  .append(quotationMarks).append(" IS '").append(metadata.getComment()).append("'");
            }
        }
        
        return sb.toString();
    }
}
