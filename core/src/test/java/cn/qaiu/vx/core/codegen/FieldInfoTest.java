package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * FieldInfo 单元测试
 *
 * @author test
 */
@DisplayName("FieldInfo 测试")
class FieldInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      FieldInfo info = new FieldInfo();
      assertNull(info.getFieldName());
      assertNull(info.getFieldType());
      assertTrue(info.isNullable());
      assertFalse(info.isPrimaryKey());
      assertFalse(info.isUnique());
      assertEquals(0, info.getLength());
    }

    @Test
    @DisplayName("双参数构造函数")
    void testTwoArgConstructor() {
      FieldInfo info = new FieldInfo("userName", "String");
      assertEquals("userName", info.getFieldName());
      assertEquals("String", info.getFieldType());
    }

    @Test
    @DisplayName("三参数构造函数")
    void testThreeArgConstructor() {
      FieldInfo info = new FieldInfo("userName", "String", "user_name");
      assertEquals("userName", info.getFieldName());
      assertEquals("String", info.getFieldType());
      assertEquals("user_name", info.getColumnName());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      FieldInfo info =
          new FieldInfo()
              .setFieldName("userId")
              .setFieldType("Long")
              .setColumnName("user_id")
              .setColumnType("bigint")
              .setDescription("用户ID")
              .setPrimaryKey(true)
              .setNullable(false)
              .setUnique(true)
              .setLength(20)
              .setPrecision(10)
              .setScale(2)
              .setDefaultValue("0")
              .setComment("主键");

      assertEquals("userId", info.getFieldName());
      assertEquals("Long", info.getFieldType());
      assertEquals("user_id", info.getColumnName());
      assertEquals("bigint", info.getColumnType());
      assertEquals("用户ID", info.getDescription());
      assertTrue(info.isPrimaryKey());
      assertFalse(info.isNullable());
      assertTrue(info.isUnique());
      assertEquals(20, info.getLength());
      assertEquals(10, info.getPrecision());
      assertEquals(2, info.getScale());
      assertEquals("0", info.getDefaultValue());
      assertEquals("主键", info.getComment());
    }
  }

  @Nested
  @DisplayName("getCapitalizedFieldName 测试")
  class CapitalizedFieldNameTest {

    @ParameterizedTest
    @CsvSource({"userName, UserName", "id, Id", "a, A", "ABC, ABC"})
    @DisplayName("首字母大写转换")
    void testCapitalize(String fieldName, String expected) {
      FieldInfo info = new FieldInfo(fieldName, "String");
      assertEquals(expected, info.getCapitalizedFieldName());
    }

    @Test
    @DisplayName("空字段名返回空")
    void testEmptyFieldName() {
      FieldInfo info = new FieldInfo("", "String");
      assertEquals("", info.getCapitalizedFieldName());
    }

    @Test
    @DisplayName("null 字段名返回 null")
    void testNullFieldName() {
      FieldInfo info = new FieldInfo();
      assertNull(info.getCapitalizedFieldName());
    }
  }

  @Nested
  @DisplayName("Getter/Setter 方法名测试")
  class GetterSetterNameTest {

    @Test
    @DisplayName("普通字段的 Getter 名")
    void testNormalGetterName() {
      FieldInfo info = new FieldInfo("userName", "String");
      assertEquals("getUserName", info.getGetterName());
    }

    @Test
    @DisplayName("boolean 字段的 Getter 名使用 is 前缀")
    void testBooleanGetterName() {
      FieldInfo info = new FieldInfo("active", "boolean");
      assertEquals("isActive", info.getGetterName());
    }

    @Test
    @DisplayName("Boolean 包装类的 Getter 名使用 get 前缀")
    void testBooleanWrapperGetterName() {
      FieldInfo info = new FieldInfo("active", "Boolean");
      assertEquals("getActive", info.getGetterName());
    }

    @Test
    @DisplayName("Setter 名")
    void testSetterName() {
      FieldInfo info = new FieldInfo("userName", "String");
      assertEquals("setUserName", info.getSetterName());
    }

    @Test
    @DisplayName("空字段名返回空")
    void testEmptyFieldNameForGetterSetter() {
      FieldInfo info = new FieldInfo("", "String");
      assertEquals("", info.getGetterName());
      assertEquals("set", info.getSetterName());
    }
  }

  @Nested
  @DisplayName("getSimpleFieldType 测试")
  class SimpleFieldTypeTest {

    @ParameterizedTest
    @CsvSource({
      "java.lang.String, String",
      "java.util.List, List",
      "java.time.LocalDateTime, LocalDateTime",
      "String, String",
      "int, int"
    })
    @DisplayName("提取简单类型名")
    void testSimpleType(String fullType, String expected) {
      FieldInfo info = new FieldInfo("field", fullType);
      assertEquals(expected, info.getSimpleFieldType());
    }

    @Test
    @DisplayName("null 类型返回 null")
    void testNullType() {
      FieldInfo info = new FieldInfo();
      assertNull(info.getSimpleFieldType());
    }
  }

  @Nested
  @DisplayName("类型检测测试")
  class TypeDetectionTest {

    @ParameterizedTest
    @ValueSource(strings = {"int", "long", "double", "float", "boolean", "char", "byte", "short"})
    @DisplayName("基本类型检测")
    void testIsPrimitiveType(String type) {
      FieldInfo info = new FieldInfo("field", type);
      assertTrue(info.isPrimitiveType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"Integer", "Long", "Double", "Float", "Boolean", "Character", "Byte", "Short"})
    @DisplayName("包装类型检测")
    void testIsWrapperType(String type) {
      FieldInfo info = new FieldInfo("field", type);
      assertTrue(info.isWrapperType());
    }

    @Test
    @DisplayName("字符串类型检测")
    void testIsStringType() {
      FieldInfo info = new FieldInfo("name", "String");
      assertTrue(info.isStringType());

      FieldInfo info2 = new FieldInfo("name", "java.lang.String");
      assertTrue(info2.isStringType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LocalDate", "LocalTime", "LocalDateTime", "Date", "Timestamp"})
    @DisplayName("日期时间类型检测")
    void testIsDateTimeType(String type) {
      FieldInfo info = new FieldInfo("field", type);
      assertTrue(info.isDateTimeType());
    }

    @Test
    @DisplayName("null 类型检测返回 false")
    void testNullTypeDetection() {
      FieldInfo info = new FieldInfo();
      assertFalse(info.isPrimitiveType());
      assertFalse(info.isWrapperType());
      assertFalse(info.isStringType());
      assertFalse(info.isDateTimeType());
    }
  }

  @Nested
  @DisplayName("getFieldDefaultValue 测试")
  class FieldDefaultValueTest {

    @Test
    @DisplayName("有默认值时返回默认值")
    void testExplicitDefaultValue() {
      FieldInfo info = new FieldInfo("count", "int").setDefaultValue("10");
      assertEquals("10", info.getFieldDefaultValue());
    }

    @Test
    @DisplayName("boolean 基本类型默认值")
    void testBooleanPrimitiveDefault() {
      FieldInfo info = new FieldInfo("active", "boolean");
      assertEquals("false", info.getFieldDefaultValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"int", "long", "double", "float", "char", "byte", "short"})
    @DisplayName("数值基本类型默认值为 0")
    void testNumericPrimitiveDefault(String type) {
      FieldInfo info = new FieldInfo("value", type);
      assertEquals("0", info.getFieldDefaultValue());
    }

    @Test
    @DisplayName("引用类型默认值为 null")
    void testReferenceTypeDefault() {
      FieldInfo info = new FieldInfo("name", "String");
      assertEquals("null", info.getFieldDefaultValue());
    }
  }

  @Nested
  @DisplayName("getFieldComment 测试")
  class FieldCommentTest {

    @Test
    @DisplayName("优先返回 comment")
    void testCommentFirst() {
      FieldInfo info = new FieldInfo("field", "String").setComment("注释").setDescription("描述");
      assertEquals("注释", info.getFieldComment());
    }

    @Test
    @DisplayName("comment 为空时返回 description")
    void testDescriptionWhenCommentEmpty() {
      FieldInfo info = new FieldInfo("field", "String").setComment("").setDescription("描述");
      assertEquals("描述", info.getFieldComment());
    }

    @Test
    @DisplayName("都为空时返回 fieldName")
    void testFieldNameWhenBothEmpty() {
      FieldInfo info = new FieldInfo("userName", "String");
      assertEquals("userName", info.getFieldComment());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含所有字段信息")
    void testToString() {
      FieldInfo info = new FieldInfo("userId", "Long").setColumnName("user_id").setPrimaryKey(true);

      String str = info.toString();

      assertTrue(str.contains("fieldName='userId'"));
      assertTrue(str.contains("fieldType='Long'"));
      assertTrue(str.contains("columnName='user_id'"));
      assertTrue(str.contains("primaryKey=true"));
    }
  }

  @Nested
  @DisplayName("占位 Setter 测试")
  class PlaceholderSetterTest {

    @Test
    @DisplayName("setGetterName 返回 this")
    void testSetGetterName() {
      FieldInfo info = new FieldInfo();
      assertSame(info, info.setGetterName("getTest"));
    }

    @Test
    @DisplayName("setSetterName 返回 this")
    void testSetSetterName() {
      FieldInfo info = new FieldInfo();
      assertSame(info, info.setSetterName("setTest"));
    }
  }
}
