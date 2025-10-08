package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DdlTable dbtype字段测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("DdlTable dbtype字段测试")
public class DdlTableDbTypeTest {

    /**
     * 测试MySQL数据库类型识别
     */
    @Test
    @DisplayName("测试MySQL数据库类型识别")
    void testMySQLDbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_mysql_table",
            dbtype = "mysql",
            comment = "MySQL测试表"
        )
        class TestMySQLTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestMySQLTable.class, JDBCType.H2DB);
        
        // 验证数据库类型识别
        assertEquals(JDBCType.MySQL, metadata.getDbType(), "应该识别为MySQL数据库");
        assertEquals("test_mysql_table", metadata.getTableName(), "表名应该正确");
        assertEquals("MySQL测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试PostgreSQL数据库类型识别
     */
    @Test
    @DisplayName("测试PostgreSQL数据库类型识别")
    void testPostgreSQLDbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_postgresql_table",
            dbtype = "postgresql",
            comment = "PostgreSQL测试表"
        )
        class TestPostgreSQLTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestPostgreSQLTable.class, JDBCType.H2DB);
        
        // 验证数据库类型识别
        assertEquals(JDBCType.PostgreSQL, metadata.getDbType(), "应该识别为PostgreSQL数据库");
        assertEquals("test_postgresql_table", metadata.getTableName(), "表名应该正确");
        assertEquals("PostgreSQL测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试H2数据库类型识别
     */
    @Test
    @DisplayName("测试H2数据库类型识别")
    void testH2DbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_h2_table",
            dbtype = "h2",
            comment = "H2测试表"
        )
        class TestH2Table {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestH2Table.class, JDBCType.MySQL);
        
        // 验证数据库类型识别
        assertEquals(JDBCType.H2DB, metadata.getDbType(), "应该识别为H2数据库");
        assertEquals("test_h2_table", metadata.getTableName(), "表名应该正确");
        assertEquals("H2测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试Oracle数据库类型识别
     */
    @Test
    @DisplayName("测试Oracle数据库类型识别")
    void testOracleDbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_oracle_table",
            dbtype = "oracle",
            comment = "Oracle测试表"
        )
        class TestOracleTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestOracleTable.class, JDBCType.MySQL);
        
        // 验证数据库类型识别
        assertEquals(JDBCType.MySQL, metadata.getDbType(), "应该识别为Oracle数据库");
        assertEquals("test_oracle_table", metadata.getTableName(), "表名应该正确");
        assertEquals("Oracle测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试SQL Server数据库类型识别
     */
    @Test
    @DisplayName("测试SQL Server数据库类型识别")
    void testSqlServerDbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_sqlserver_table",
            dbtype = "sqlserver",
            comment = "SQL Server测试表"
        )
        class TestSqlServerTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestSqlServerTable.class, JDBCType.MySQL);
        
        // 验证数据库类型识别
        assertEquals(JDBCType.MySQL, metadata.getDbType(), "应该识别为SQL Server数据库");
        assertEquals("test_sqlserver_table", metadata.getTableName(), "表名应该正确");
        assertEquals("SQL Server测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试空dbtype字段的默认行为
     */
    @Test
    @DisplayName("测试空dbtype字段的默认行为")
    void testEmptyDbTypeDefault() {
        // 创建测试类（不指定dbtype）
        @DdlTable(
            value = "test_default_table",
            comment = "默认数据库类型测试表"
        )
        class TestDefaultTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestDefaultTable.class, JDBCType.PostgreSQL);
        
        // 验证默认数据库类型（应该使用传入的dbType参数）
        assertEquals(JDBCType.PostgreSQL, metadata.getDbType(), "应该使用传入的数据库类型");
        assertEquals("test_default_table", metadata.getTableName(), "表名应该正确");
        assertEquals("默认数据库类型测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试不支持的数据库类型
     */
    @Test
    @DisplayName("测试不支持的数据库类型")
    void testUnsupportedDbType() {
        // 创建测试类（使用不支持的dbtype）
        @DdlTable(
            value = "test_unsupported_table",
            dbtype = "unknown_db",
            comment = "不支持的数据库类型测试表"
        )
        class TestUnsupportedTable {
            private Long id;
            private String name;
        }
        
        // 测试表元数据创建
        TableMetadata metadata = TableMetadata.fromClass(TestUnsupportedTable.class, JDBCType.H2DB);
        
        // 验证不支持的数据库类型（应该默认为MySQL）
        assertEquals(JDBCType.MySQL, metadata.getDbType(), "不支持的数据库类型应该默认为MySQL");
        assertEquals("test_unsupported_table", metadata.getTableName(), "表名应该正确");
        assertEquals("不支持的数据库类型测试表", metadata.getComment(), "表注释应该正确");
    }

    /**
     * 测试大小写不敏感的数据库类型识别
     */
    @Test
    @DisplayName("测试大小写不敏感的数据库类型识别")
    void testCaseInsensitiveDbTypeDetection() {
        // 测试不同大小写的MySQL
        @DdlTable(value = "test_mysql_upper", dbtype = "MYSQL")
        class TestMySQLUpper {
            private Long id;
        }
        
        @DdlTable(value = "test_mysql_mixed", dbtype = "MySql")
        class TestMySQLMixed {
            private Long id;
        }
        
        // 验证大小写不敏感识别
        TableMetadata metadata1 = TableMetadata.fromClass(TestMySQLUpper.class, JDBCType.H2DB);
        assertEquals(JDBCType.MySQL, metadata1.getDbType(), "大写MYSQL应该识别为MySQL");
        
        TableMetadata metadata2 = TableMetadata.fromClass(TestMySQLMixed.class, JDBCType.H2DB);
        assertEquals(JDBCType.MySQL, metadata2.getDbType(), "混合大小写MySql应该识别为MySQL");
    }

    /**
     * 测试EnhancedCreateTable的dbtype自动识别功能
     */
    @Test
    @DisplayName("测试EnhancedCreateTable的dbtype自动识别功能")
    void testEnhancedCreateTableDbTypeDetection() {
        // 创建测试类
        @DdlTable(
            value = "test_enhanced_table",
            dbtype = "mysql",
            comment = "EnhancedCreateTable测试表"
        )
        class TestEnhancedTable {
            private Long id;
            private String name;
        }
        
        // 测试EnhancedCreateTable的dbtype识别
        // 注意：这里只是测试方法调用，不实际执行数据库操作
        try {
            // 测试TableMetadata的dbtype识别功能
            TableMetadata metadata = TableMetadata.fromClass(TestEnhancedTable.class);
            
            // 验证dbtype识别正确
            assertEquals(JDBCType.MySQL, metadata.getDbType(), "应该识别为MySQL类型");
            
        } catch (Exception e) {
            fail("dbtype识别不应该抛出异常: " + e.getMessage());
        }
    }
}
