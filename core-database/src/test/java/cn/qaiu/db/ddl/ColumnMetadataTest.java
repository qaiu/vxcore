package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnMetadata测试用例
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("ColumnMetadata测试")
public class ColumnMetadataTest {

    private ColumnMetadata columnMetadata;

    @BeforeEach
    void setUp() {
        columnMetadata = new ColumnMetadata();
    }

    /**
     * 测试ColumnMetadata的基本属性设置和获取
     */
    @Test
    @DisplayName("测试ColumnMetadata基本属性")
    public void testColumnMetadataBasicProperties() {
        // 设置属性
        columnMetadata.setName("test_column");
        columnMetadata.setType("VARCHAR");
        columnMetadata.setLength(100);
        columnMetadata.setPrecision(10);
        columnMetadata.setScale(2);
        columnMetadata.setNullable(false);
        columnMetadata.setDefaultValue("test");
        columnMetadata.setDefaultValueIsFunction(false);
        columnMetadata.setAutoIncrement(true);
        columnMetadata.setComment("测试列");
        columnMetadata.setUniqueKey("unique_test");
        columnMetadata.setIndexName("idx_test");
        columnMetadata.setVersion(2);
        columnMetadata.setPrimaryKey(true);

        // 验证属性
        assertEquals("test_column", columnMetadata.getName());
        assertEquals("VARCHAR", columnMetadata.getType());
        assertEquals(100, columnMetadata.getLength());
        assertEquals(10, columnMetadata.getPrecision());
        assertEquals(2, columnMetadata.getScale());
        assertFalse(columnMetadata.isNullable());
        assertEquals("test", columnMetadata.getDefaultValue());
        assertFalse(columnMetadata.isDefaultValueIsFunction());
        assertTrue(columnMetadata.isAutoIncrement());
        assertEquals("测试列", columnMetadata.getComment());
        assertEquals("unique_test", columnMetadata.getUniqueKey());
        assertEquals("idx_test", columnMetadata.getIndexName());
        assertEquals(2, columnMetadata.getVersion());
        assertTrue(columnMetadata.isPrimaryKey());
    }

    /**
     * 测试ColumnMetadata的构造函数
     */
    @Test
    @DisplayName("测试ColumnMetadata构造函数")
    public void testColumnMetadataConstructor() {
        ColumnMetadata metadata = new ColumnMetadata(
            "test_column", "VARCHAR", 100, 10, 2,
            false, "test", false, true, "测试列",
            "unique_test", "idx_test", 2, true
        );

        assertEquals("test_column", metadata.getName());
        assertEquals("VARCHAR", metadata.getType());
        assertEquals(100, metadata.getLength());
        assertEquals(10, metadata.getPrecision());
        assertEquals(2, metadata.getScale());
        assertFalse(metadata.isNullable());
        assertEquals("test", metadata.getDefaultValue());
        assertFalse(metadata.isDefaultValueIsFunction());
        assertTrue(metadata.isAutoIncrement());
        assertEquals("测试列", metadata.getComment());
        assertEquals("unique_test", metadata.getUniqueKey());
        assertEquals("idx_test", metadata.getIndexName());
        assertEquals(2, metadata.getVersion());
        assertTrue(metadata.isPrimaryKey());
    }

    /**
     * 测试从字段创建ColumnMetadata
     */
    @Test
    @DisplayName("测试从字段创建ColumnMetadata")
    public void testColumnMetadataFromField() {
        // 创建测试类
        class TestClass {
            @DdlColumn(
                name = "test_column",
                type = "VARCHAR",
                length = 100,
                precision = 10,
                scale = 2,
                nullable = false,
                defaultValue = "test",
                defaultValueIsFunction = false,
                autoIncrement = true,
                comment = "测试列",
                uniqueKey = "unique_test",
                indexName = "idx_test",
                version = 2
            )
            private String testField;
        }

        try {
            Field field = TestClass.class.getDeclaredField("testField");
            TableMetadata tableMetadata = new TableMetadata();
            tableMetadata.setPrimaryKey("testField");
            tableMetadata.setCaseFormat(io.vertx.codegen.format.SnakeCase.INSTANCE);

            ColumnMetadata metadata = ColumnMetadata.fromField(field, tableMetadata);

            assertNotNull(metadata);
            assertEquals("test_column", metadata.getName());
            assertEquals("VARCHAR", metadata.getType());
            assertEquals(100, metadata.getLength());
            assertEquals(10, metadata.getPrecision());
            assertEquals(2, metadata.getScale());
            assertFalse(metadata.isNullable());
            assertEquals("test", metadata.getDefaultValue());
            assertFalse(metadata.isDefaultValueIsFunction());
            assertTrue(metadata.isAutoIncrement());
            assertEquals("测试列", metadata.getComment());
            assertEquals("unique_test", metadata.getUniqueKey());
            assertEquals("idx_test", metadata.getIndexName());
            assertEquals(2, metadata.getVersion());
            assertTrue(metadata.isPrimaryKey());
        } catch (NoSuchFieldException e) {
            fail("测试字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试生成列定义SQL
     */
    @Test
    @DisplayName("测试生成列定义SQL")
    public void testToColumnDefinition() {
        columnMetadata.setName("test_column");
        columnMetadata.setType("VARCHAR");
        columnMetadata.setLength(100);
        columnMetadata.setNullable(false);
        columnMetadata.setDefaultValue("test");
        columnMetadata.setDefaultValueIsFunction(false);
        columnMetadata.setAutoIncrement(false);
        columnMetadata.setPrimaryKey(false);

        String sql = columnMetadata.toColumnDefinition(JDBCType.MySQL);
        assertNotNull(sql);
        assertTrue(sql.contains("`test_column`"));
        assertTrue(sql.contains("VARCHAR(100)"));
        assertTrue(sql.contains("NOT NULL"));
        assertTrue(sql.contains("DEFAULT 'test'"));
    }

    /**
     * 测试生成列定义SQL - 自增字段
     */
    @Test
    @DisplayName("测试生成自增列定义SQL")
    public void testToColumnDefinitionAutoIncrement() {
        columnMetadata.setName("id");
        columnMetadata.setType("INT");
        columnMetadata.setNullable(false);
        columnMetadata.setAutoIncrement(true);
        columnMetadata.setPrimaryKey(true);

        String sql = columnMetadata.toColumnDefinition(JDBCType.MySQL);
        assertNotNull(sql);
        assertTrue(sql.contains("`id`"));
        assertTrue(sql.contains("INT"));
        assertTrue(sql.contains("NOT NULL"));
        assertTrue(sql.contains("AUTO_INCREMENT"));
        assertTrue(sql.contains("PRIMARY KEY"));
    }

    /**
     * 测试生成列定义SQL - PostgreSQL
     */
    @Test
    @DisplayName("测试生成PostgreSQL列定义SQL")
    public void testToColumnDefinitionPostgreSQL() {
        columnMetadata.setName("id");
        columnMetadata.setType("INT");
        columnMetadata.setNullable(false);
        columnMetadata.setAutoIncrement(true);
        columnMetadata.setPrimaryKey(true);

        String sql = columnMetadata.toColumnDefinition(JDBCType.PostgreSQL);
        assertNotNull(sql);
        assertTrue(sql.contains("\"id\""));
        assertTrue(sql.contains("SERIAL"));
        assertTrue(sql.contains("PRIMARY KEY"));
    }

    /**
     * 测试生成列定义SQL - DECIMAL类型
     */
    @Test
    @DisplayName("测试生成DECIMAL列定义SQL")
    public void testToColumnDefinitionDecimal() {
        columnMetadata.setName("price");
        columnMetadata.setType("DECIMAL");
        columnMetadata.setPrecision(10);
        columnMetadata.setScale(2);
        columnMetadata.setNullable(false);
        columnMetadata.setDefaultValue("0.00");
        columnMetadata.setDefaultValueIsFunction(false);

        String sql = columnMetadata.toColumnDefinition(JDBCType.MySQL);
        assertNotNull(sql);
        assertTrue(sql.contains("`price`"));
        assertTrue(sql.contains("DECIMAL(10,2)"));
        assertTrue(sql.contains("NOT NULL"));
        assertTrue(sql.contains("DEFAULT '0.00'"));
    }

    /**
     * 测试生成列定义SQL - 函数默认值
     */
    @Test
    @DisplayName("测试生成函数默认值列定义SQL")
    public void testToColumnDefinitionFunctionDefault() {
        columnMetadata.setName("create_time");
        columnMetadata.setType("TIMESTAMP");
        columnMetadata.setNullable(false);
        columnMetadata.setDefaultValue("CURRENT_TIMESTAMP");
        columnMetadata.setDefaultValueIsFunction(true);

        String sql = columnMetadata.toColumnDefinition(JDBCType.MySQL);
        assertNotNull(sql);
        assertTrue(sql.contains("`create_time`"));
        assertTrue(sql.contains("TIMESTAMP"));
        assertTrue(sql.contains("NOT NULL"));
        assertTrue(sql.contains("DEFAULT CURRENT_TIMESTAMP"));
    }
}
