package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ColumnInfo 单元测试
 *
 * @author test
 */
@DisplayName("ColumnInfo 测试")
class ColumnInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      ColumnInfo info = new ColumnInfo();
      assertNull(info.getColumnName());
      assertNull(info.getColumnType());
      assertTrue(info.isNullable());
      assertFalse(info.isPrimaryKey());
      assertFalse(info.isUnique());
      assertFalse(info.isAutoIncrement());
    }

    @Test
    @DisplayName("双参数构造函数")
    void testTwoArgConstructor() {
      ColumnInfo info = new ColumnInfo("user_name", "varchar");
      assertEquals("user_name", info.getColumnName());
      assertEquals("varchar", info.getColumnType());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      ColumnInfo info = new ColumnInfo()
          .setColumnName("id")
          .setColumnType("bigint")
          .setDataType(java.sql.Types.BIGINT)
          .setTypeName("BIGINT")
          .setColumnSize(20)
          .setDecimalDigits(0)
          .setNullable(false)
          .setPrimaryKey(true)
          .setUnique(true)
          .setAutoIncrement(true)
          .setDefaultValue("0")
          .setComment("主键ID")
          .setDescription("自增主键");

      assertEquals("id", info.getColumnName());
      assertEquals("bigint", info.getColumnType());
      assertEquals(java.sql.Types.BIGINT, info.getDataType());
      assertEquals("BIGINT", info.getTypeName());
      assertEquals(20, info.getColumnSize());
      assertEquals(0, info.getDecimalDigits());
      assertFalse(info.isNullable());
      assertTrue(info.isPrimaryKey());
      assertTrue(info.isUnique());
      assertTrue(info.isAutoIncrement());
      assertEquals("0", info.getDefaultValue());
      assertEquals("主键ID", info.getComment());
      assertEquals("自增主键", info.getDescription());
    }

    @Test
    @DisplayName("setLength 影响 columnSize")
    void testSetLength() {
      ColumnInfo info = new ColumnInfo().setLength(255);
      assertEquals(255, info.getColumnSize());
    }
  }

  @Nested
  @DisplayName("getJavaFieldName 测试")
  class JavaFieldNameTest {

    @ParameterizedTest
    @CsvSource({
        "user_name, userName",
        "USER_NAME, userName",
        "id, id",
        "ID, id",
        "created_at, createdAt",
        "first_name_last, firstNameLast"
    })
    @DisplayName("下划线转驼峰")
    void testUnderscoreToCamel(String columnName, String expectedFieldName) {
      ColumnInfo info = new ColumnInfo(columnName, "varchar");
      assertEquals(expectedFieldName, info.getJavaFieldName());
    }

    @Test
    @DisplayName("空列名返回空")
    void testEmptyColumnName() {
      ColumnInfo info = new ColumnInfo("", "varchar");
      assertEquals("", info.getJavaFieldName());
    }

    @Test
    @DisplayName("null 列名返回 null")
    void testNullColumnName() {
      ColumnInfo info = new ColumnInfo();
      assertNull(info.getJavaFieldName());
    }
  }

  @Nested
  @DisplayName("getJavaFieldType 测试")
  class JavaFieldTypeTest {

    @ParameterizedTest
    @CsvSource({
        "int, Integer",
        "integer, Integer",
        "int4, Integer",
        "bigint, Long",
        "int8, Long",
        "smallint, Short",
        "int2, Short",
        "tinyint, Byte",
        "decimal, BigDecimal",
        "numeric, BigDecimal",
        "float, Float",
        "real, Float",
        "double, Double",
        "boolean, Boolean",
        "bool, Boolean",
        "bit, Boolean",
        "varchar, String",
        "char, String",
        "text, String",
        "date, LocalDate",
        "time, LocalTime",
        "timestamp, LocalDateTime",
        "datetime, LocalDateTime",
        "blob, byte[]",
        "binary, byte[]"
    })
    @DisplayName("SQL类型映射到Java类型")
    void testSqlToJavaTypeMapping(String sqlType, String expectedJavaType) {
      ColumnInfo info = new ColumnInfo("col", sqlType);
      assertEquals(expectedJavaType, info.getJavaFieldType());
    }

    @Test
    @DisplayName("未知类型默认为 String")
    void testUnknownTypeDefaultsToString() {
      ColumnInfo info = new ColumnInfo("col", "unknowntype");
      assertEquals("String", info.getJavaFieldType());
    }

    @Test
    @DisplayName("null 类型返回 String")
    void testNullTypeReturnsString() {
      ColumnInfo info = new ColumnInfo();
      assertEquals("String", info.getJavaFieldType());
    }
  }

  @Nested
  @DisplayName("类型检测方法测试")
  class TypeCheckTest {

    @ParameterizedTest
    @ValueSource(strings = {"int", "bigint", "decimal", "float", "double", "numeric", "money", "real"})
    @DisplayName("数值类型检测")
    void testIsNumericType(String type) {
      ColumnInfo info = new ColumnInfo("col", type);
      assertTrue(info.isNumericType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"char", "varchar", "text", "string"})
    @DisplayName("字符串类型检测")
    void testIsStringType(String type) {
      ColumnInfo info = new ColumnInfo("col", type);
      assertTrue(info.isStringType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"date", "time", "timestamp", "datetime"})
    @DisplayName("日期时间类型检测")
    void testIsDateTimeType(String type) {
      ColumnInfo info = new ColumnInfo("col", type);
      assertTrue(info.isDateTimeType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"blob", "binary", "varbinary"})
    @DisplayName("二进制类型检测")
    void testIsBinaryType(String type) {
      ColumnInfo info = new ColumnInfo("col", type);
      assertTrue(info.isBinaryType());
    }

    @Test
    @DisplayName("null 类型返回 false")
    void testNullTypeReturnsFalse() {
      ColumnInfo info = new ColumnInfo();
      assertFalse(info.isNumericType());
      assertFalse(info.isStringType());
      assertFalse(info.isDateTimeType());
      assertFalse(info.isBinaryType());
    }
  }

  @Nested
  @DisplayName("getColumnComment 测试")
  class ColumnCommentTest {

    @Test
    @DisplayName("优先返回 comment")
    void testCommentFirst() {
      ColumnInfo info = new ColumnInfo()
          .setColumnName("col")
          .setComment("注释")
          .setDescription("描述");
      assertEquals("注释", info.getColumnComment());
    }

    @Test
    @DisplayName("comment 为空时返回 description")
    void testDescriptionWhenCommentEmpty() {
      ColumnInfo info = new ColumnInfo()
          .setColumnName("col")
          .setComment("")
          .setDescription("描述");
      assertEquals("描述", info.getColumnComment());
    }

    @Test
    @DisplayName("comment 和 description 都为空时返回 columnName")
    void testColumnNameWhenBothEmpty() {
      ColumnInfo info = new ColumnInfo("col_name", "varchar");
      assertEquals("col_name", info.getColumnComment());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含所有字段信息")
    void testToString() {
      ColumnInfo info = new ColumnInfo("id", "bigint")
          .setPrimaryKey(true)
          .setAutoIncrement(true);
      
      String str = info.toString();
      
      assertTrue(str.contains("columnName='id'"));
      assertTrue(str.contains("columnType='bigint'"));
      assertTrue(str.contains("primaryKey=true"));
      assertTrue(str.contains("autoIncrement=true"));
    }
  }
}
