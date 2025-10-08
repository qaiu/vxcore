package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TableMetadata测试用例
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("TableMetadata测试")
public class TableMetadataTest {

    private TableMetadata tableMetadata;

    @BeforeEach
    void setUp() {
        tableMetadata = new TableMetadata();
    }

    /**
     * 测试TableMetadata的基本属性设置和获取
     */
    @Test
    @DisplayName("测试TableMetadata基本属性")
    public void testTableMetadataBasicProperties() {
        // 设置属性
        tableMetadata.setTableName("test_table");
        tableMetadata.setPrimaryKey("id");
        tableMetadata.setVersion(2);
        tableMetadata.setAutoSync(true);
        tableMetadata.setComment("测试表");
        tableMetadata.setCharset("utf8mb4");
        tableMetadata.setCollate("utf8mb4_unicode_ci");
        tableMetadata.setEngine("InnoDB");
        tableMetadata.setCaseFormat(io.vertx.codegen.format.SnakeCase.INSTANCE);
        tableMetadata.setDbType(JDBCType.MySQL);

        // 验证属性
        assertEquals("test_table", tableMetadata.getTableName());
        assertEquals("id", tableMetadata.getPrimaryKey());
        assertEquals(2, tableMetadata.getVersion());
        assertTrue(tableMetadata.isAutoSync());
        assertEquals("测试表", tableMetadata.getComment());
        assertEquals("utf8mb4", tableMetadata.getCharset());
        assertEquals("utf8mb4_unicode_ci", tableMetadata.getCollate());
        assertEquals("InnoDB", tableMetadata.getEngine());
        assertEquals(io.vertx.codegen.format.SnakeCase.INSTANCE, tableMetadata.getCaseFormat());
        assertEquals(JDBCType.MySQL, tableMetadata.getDbType());
    }

    /**
     * 测试TableMetadata的构造函数
     */
    @Test
    @DisplayName("测试TableMetadata构造函数")
    public void testTableMetadataConstructor() {
        TableMetadata metadata = new TableMetadata(
            "test_table", "id", 2, true, "测试表",
            "utf8mb4", "utf8mb4_unicode_ci", "InnoDB",
            io.vertx.codegen.format.SnakeCase.INSTANCE, JDBCType.MySQL
        );

        assertEquals("test_table", metadata.getTableName());
        assertEquals("id", metadata.getPrimaryKey());
        assertEquals(2, metadata.getVersion());
        assertTrue(metadata.isAutoSync());
        assertEquals("测试表", metadata.getComment());
        assertEquals("utf8mb4", metadata.getCharset());
        assertEquals("utf8mb4_unicode_ci", metadata.getCollate());
        assertEquals("InnoDB", metadata.getEngine());
        assertEquals(io.vertx.codegen.format.SnakeCase.INSTANCE, metadata.getCaseFormat());
        assertEquals(JDBCType.MySQL, metadata.getDbType());
    }

    /**
     * 测试添加和获取列
     */
    @Test
    @DisplayName("测试添加和获取列")
    public void testAddAndGetColumn() {
        ColumnMetadata column1 = new ColumnMetadata();
        column1.setName("id");
        column1.setType("INT");
        column1.setPrimaryKey(true);

        ColumnMetadata column2 = new ColumnMetadata();
        column2.setName("name");
        column2.setType("VARCHAR");
        column2.setLength(100);

        // 添加列
        tableMetadata.addColumn(column1);
        tableMetadata.addColumn(column2);

        // 验证列
        Map<String, ColumnMetadata> columns = tableMetadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("id"));
        assertTrue(columns.containsKey("name"));

        assertEquals(column1, tableMetadata.getColumn("id"));
        assertEquals(column2, tableMetadata.getColumn("name"));
        assertNull(tableMetadata.getColumn("non_existent"));
    }

    /**
     * 测试从类创建TableMetadata - DdlTable注解
     */
    @Test
    @DisplayName("测试从DdlTable注解类创建TableMetadata")
    public void testFromClassWithDdlTable() {
        @DdlTable(
            value = "test_table",
            keyFields = "id",
            version = 2,
            autoSync = true,
            comment = "测试表",
            charset = "utf8mb4",
            collate = "utf8mb4_unicode_ci",
            engine = "InnoDB"
        )
        class TestClass {
            @DdlColumn(name = "id", type = "INT", autoIncrement = true, nullable = false)
            private Integer id;

            @DdlColumn(name = "name", type = "VARCHAR", length = 100, nullable = false)
            private String name;
        }

        TableMetadata metadata = TableMetadata.fromClass(TestClass.class, JDBCType.MySQL);

        assertNotNull(metadata);
        assertEquals("test_table", metadata.getTableName());
        assertEquals("id", metadata.getPrimaryKey());
        assertEquals(2, metadata.getVersion());
        assertTrue(metadata.isAutoSync());
        assertEquals("测试表", metadata.getComment());
        assertEquals("utf8mb4", metadata.getCharset());
        assertEquals("utf8mb4_unicode_ci", metadata.getCollate());
        assertEquals("InnoDB", metadata.getEngine());
        assertEquals(JDBCType.MySQL, metadata.getDbType());

        // 验证列
        Map<String, ColumnMetadata> columns = metadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("id"));
        assertTrue(columns.containsKey("name"));
    }

    /**
     * 测试从类创建TableMetadata - Table注解（兼容性）
     */
    @Test
    @DisplayName("测试从Table注解类创建TableMetadata")
    public void testFromClassWithTable() {
        @Table(value = "test_table", keyFields = "id")
        class TestClass {
            @Constraint(notNull = true, autoIncrement = true)
            private Integer id;

            @Constraint(notNull = true)
            private String name;
        }

        TableMetadata metadata = TableMetadata.fromClass(TestClass.class, JDBCType.MySQL);

        assertNotNull(metadata);
        assertEquals("test_table", metadata.getTableName());
        assertEquals("id", metadata.getPrimaryKey());
        assertEquals(1, metadata.getVersion()); // 默认版本
        assertTrue(metadata.isAutoSync()); // 默认启用自动同步
        assertEquals("utf8mb4", metadata.getCharset()); // 默认字符集
        assertEquals("utf8mb4_unicode_ci", metadata.getCollate()); // 默认排序规则
        assertEquals("InnoDB", metadata.getEngine()); // 默认引擎
        assertEquals(JDBCType.MySQL, metadata.getDbType());

        // 验证列
        Map<String, ColumnMetadata> columns = metadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("id"));
        assertTrue(columns.containsKey("name"));
    }

    /**
     * 测试从类创建TableMetadata - 无注解类
     */
    @Test
    @DisplayName("测试从无注解类创建TableMetadata")
    public void testFromClassWithoutAnnotation() {
        class TestClass {
            private Integer id;
            private String name;
        }

        TableMetadata metadata = TableMetadata.fromClass(TestClass.class, JDBCType.MySQL);

        assertNotNull(metadata);
        assertEquals("test_class", metadata.getTableName()); // 类名转下划线
        assertEquals("id", metadata.getPrimaryKey()); // 默认主键
        assertEquals(1, metadata.getVersion()); // 默认版本
        assertTrue(metadata.isAutoSync()); // 默认启用自动同步
        assertEquals("utf8mb4", metadata.getCharset()); // 默认字符集
        assertEquals("utf8mb4_unicode_ci", metadata.getCollate()); // 默认排序规则
        assertEquals("InnoDB", metadata.getEngine()); // 默认引擎
        assertEquals(JDBCType.MySQL, metadata.getDbType());

        // 验证列
        Map<String, ColumnMetadata> columns = metadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("id"));
        assertTrue(columns.containsKey("name"));
    }

    /**
     * 测试从类创建TableMetadata - 忽略字段
     */
    @Test
    @DisplayName("测试从类创建TableMetadata时忽略字段")
    public void testFromClassIgnoreFields() {
        class TestClass {
            private Integer id;
            private String name;
            @DdlIgnore
            private String ignoredField;
            @TableGenIgnore
            private String anotherIgnoredField;
            private String serialVersionUID; // 应该被忽略
        }

        TableMetadata metadata = TableMetadata.fromClass(TestClass.class, JDBCType.MySQL);

        assertNotNull(metadata);
        
        // 验证列（应该只有id和name）
        Map<String, ColumnMetadata> columns = metadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("id"));
        assertTrue(columns.containsKey("name"));
        assertFalse(columns.containsKey("ignoredField"));
        assertFalse(columns.containsKey("anotherIgnoredField"));
        assertFalse(columns.containsKey("serialVersionUID"));
    }

    /**
     * 测试从类创建TableMetadata - RowMapped注解
     */
    @Test
    @DisplayName("测试从RowMapped注解类创建TableMetadata")
    public void testFromClassWithRowMapped() {
        @io.vertx.sqlclient.templates.annotations.RowMapped(formatter = io.vertx.codegen.format.CamelCase.class)
        class TestClass {
            private Integer userId;
            private String userName;
        }

        TableMetadata metadata = TableMetadata.fromClass(TestClass.class, JDBCType.MySQL);

        assertNotNull(metadata);
        assertEquals(io.vertx.codegen.format.CamelCase.INSTANCE, metadata.getCaseFormat());
        
        // 验证列名转换
        Map<String, ColumnMetadata> columns = metadata.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("userId"));
        assertTrue(columns.containsKey("userName"));
    }
}
