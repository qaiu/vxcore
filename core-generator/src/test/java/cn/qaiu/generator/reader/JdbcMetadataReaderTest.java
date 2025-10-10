package cn.qaiu.generator.reader;

import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDBC 元数据读取器测试
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
public class JdbcMetadataReaderTest {
    
    private JdbcMetadataReader reader;
    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";
    
    @BeforeEach
    void setUp(VertxTestContext testContext) throws Exception {
        // 创建 H2 内存数据库
        Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
        Statement stmt = conn.createStatement();
        
        // 创建测试表
        stmt.execute("DROP TABLE IF EXISTS test_user");
        stmt.execute("""
            CREATE TABLE test_user (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(255),
                age INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);
        
        stmt.close();
        conn.close();
        
        // 创建读取器
        reader = new JdbcMetadataReader(H2_URL, H2_USER, H2_PASSWORD);
        
        testContext.completeNow();
    }
    
    @Test
    void testReadTable(VertxTestContext testContext) {
        reader.readTable("test_user")
                .onSuccess(tableInfo -> {
                    assertNotNull(tableInfo);
                    assertEquals("test_user", tableInfo.getTableName());
                    assertFalse(tableInfo.getColumns().isEmpty());
                    
                    // 检查列信息
                    List<ColumnInfo> columns = tableInfo.getColumns();
                    assertEquals(6, columns.size());
                    
                    // 检查主键
                    ColumnInfo idColumn = columns.stream()
                            .filter(ColumnInfo::isPrimaryKey)
                            .findFirst()
                            .orElse(null);
                    assertNotNull(idColumn);
                    assertEquals("id", idColumn.getColumnName());
                    assertEquals("BIGINT", idColumn.getColumnType());
                    assertEquals("Long", idColumn.getJavaFieldType());
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testReadColumns(VertxTestContext testContext) {
        reader.readColumns("test_user")
                .onSuccess(columns -> {
                    assertNotNull(columns);
                    assertEquals(6, columns.size());
                    
                    // 检查特定列
                    ColumnInfo nameColumn = columns.stream()
                            .filter(col -> "name".equals(col.getColumnName()))
                            .findFirst()
                            .orElse(null);
                    assertNotNull(nameColumn);
                    assertEquals("VARCHAR", nameColumn.getColumnType());
                    assertEquals("String", nameColumn.getJavaFieldType());
                    assertFalse(nameColumn.isNullable());
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testReadAllTables(VertxTestContext testContext) {
        reader.readAllTables(null)
                .onSuccess(tables -> {
                    assertNotNull(tables);
                    assertFalse(tables.isEmpty());
                    
                    // 查找测试表
                    TableInfo testTable = tables.stream()
                            .filter(table -> "test_user".equals(table.getTableName()))
                            .findFirst()
                            .orElse(null);
                    assertNotNull(testTable);
                    
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testTestConnection(VertxTestContext testContext) {
        reader.testConnection()
                .onSuccess(connected -> {
                    assertTrue(connected);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Test
    void testClose(VertxTestContext testContext) {
        reader.close()
                .onSuccess(v -> {
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
}
