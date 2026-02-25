package cn.qaiu.vx.core.util;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.annotations.param.PathVariable;
import cn.qaiu.vx.core.annotations.param.RequestBody;
import cn.qaiu.vx.core.annotations.param.RequestParam;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Parameter;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ParamUtil工具类测试")
class ParamUtilTest {

  @Nested
  @DisplayName("MultiMap转换测试")
  class MultiMapConversionTest {

    @Test
    @DisplayName("MultiMap转Map")
    void testMultiMapToMap() {
      MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
      multiMap.set("key1", "value1");
      multiMap.set("key2", "value2");

      Map<String, Object> result = ParamUtil.multiMapToMap(multiMap);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("value1", result.get("key1"));
      assertEquals("value2", result.get("key2"));
    }

    @Test
    @DisplayName("null MultiMap转Map")
    void testNullMultiMapToMap() {
      Map<String, Object> result = ParamUtil.multiMapToMap(null);
      assertNull(result);
    }

    @Test
    @DisplayName("空MultiMap转Map")
    void testEmptyMultiMapToMap() {
      MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
      Map<String, Object> result = ParamUtil.multiMapToMap(multiMap);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("MultiMap转Entity")
    void testMultiMapToEntity() {
      MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
      multiMap.set("id", "1");
      multiMap.set("name", "test");

      TestEntity result = ParamUtil.multiMapToEntity(multiMap, TestEntity.class);

      assertNotNull(result);
      assertEquals("1", result.id);
      assertEquals("test", result.name);
    }

    @Test
    @DisplayName("null MultiMap转Entity")
    void testNullMultiMapToEntity() {
      TestEntity result = ParamUtil.multiMapToEntity(null, TestEntity.class);
      assertNull(result);
    }
  }

  @Nested
  @DisplayName("参数字符串解析测试")
  class ParamsToMapTest {

    @Test
    @DisplayName("标准参数字符串解析")
    void testStandardParamString() {
      String paramString = "key1=value1&key2=value2&key3=value3";
      MultiMap result = ParamUtil.paramsToMap(paramString);

      assertNotNull(result);
      assertEquals("value1", result.get("key1"));
      assertEquals("value2", result.get("key2"));
      assertEquals("value3", result.get("key3"));
    }

    @Test
    @DisplayName("null参数字符串")
    void testNullParamString() {
      MultiMap result = ParamUtil.paramsToMap(null);
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("空参数字符串")
    void testEmptyParamString() {
      MultiMap result = ParamUtil.paramsToMap("");
      assertNotNull(result);
      // 空字符串分割后会生成一个元素
      assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("无值参数")
    void testParamWithoutValue() {
      String paramString = "key1&key2=value2";
      MultiMap result = ParamUtil.paramsToMap(paramString);

      assertNotNull(result);
      assertEquals("", result.get("key1"));
      assertEquals("value2", result.get("key2"));
    }

    @Test
    @DisplayName("特殊字符参数")
    void testSpecialCharParams() {
      String paramString = "key1=value%20with%20space&key2=123";
      MultiMap result = ParamUtil.paramsToMap(paramString);

      assertNotNull(result);
      assertEquals("value%20with%20space", result.get("key1"));
    }
  }

  @Nested
  @DisplayName("值转换测试")
  class ConvertValueTest {

    @ParameterizedTest
    @CsvSource({
      "123, java.lang.String, 123",
      "456, java.lang.Integer, 456",
      "789, java.lang.Long, 789",
      "3.14, java.lang.Double, 3.14",
      "2.71, java.lang.Float, 2.71",
      "true, java.lang.Boolean, true",
      "X, java.lang.Character, X"
    })
    @DisplayName("基本类型转换")
    void testBasicTypeConversion(String value, String typeName, String expected) throws
        ClassNotFoundException {
      Class<?> targetType = Class.forName(typeName);
      Object result = ParamUtil.convertValue(value, targetType);

      assertNotNull(result);
      assertEquals(expected, result.toString());
    }

    @Test
    @DisplayName("String转换")
    void testStringConversion() {
      Object result = ParamUtil.convertValue("hello", String.class);
      assertEquals("hello", result);
    }

    @Test
    @DisplayName("Integer转换")
    void testIntegerConversion() {
      Object result = ParamUtil.convertValue("123", Integer.class);
      assertEquals(123, result);
    }

    @Test
    @DisplayName("int基本类型转换")
    void testIntConversion() {
      Object result = ParamUtil.convertValue("456", int.class);
      assertEquals(456, result);
    }

    @Test
    @DisplayName("Long转换")
    void testLongConversion() {
      Object result = ParamUtil.convertValue("789", Long.class);
      assertEquals(789L, result);
    }

    @Test
    @DisplayName("long基本类型转换")
    void testLongPrimitiveConversion() {
      Object result = ParamUtil.convertValue("1000", long.class);
      assertEquals(1000L, result);
    }

    @Test
    @DisplayName("Double转换")
    void testDoubleConversion() {
      Object result = ParamUtil.convertValue("3.14", Double.class);
      assertEquals(3.14, result);
    }

    @Test
    @DisplayName("double基本类型转换")
    void testDoublePrimitiveConversion() {
      Object result = ParamUtil.convertValue("2.71", double.class);
      assertEquals(2.71, result);
    }

    @Test
    @DisplayName("Float转换")
    void testFloatConversion() {
      Object result = ParamUtil.convertValue("1.5", Float.class);
      assertEquals(1.5f, result);
    }

    @Test
    @DisplayName("float基本类型转换")
    void testFloatPrimitiveConversion() {
      Object result = ParamUtil.convertValue("2.5", float.class);
      assertEquals(2.5f, result);
    }

    @Test
    @DisplayName("Boolean转换")
    void testBooleanConversion() {
      Object result = ParamUtil.convertValue("true", Boolean.class);
      assertEquals(true, result);
    }

    @Test
    @DisplayName("boolean基本类型转换")
    void testBooleanPrimitiveConversion() {
      Object result = ParamUtil.convertValue("false", boolean.class);
      assertEquals(false, result);
    }

    @Test
    @DisplayName("Character转换")
    void testCharacterConversion() {
      Object result = ParamUtil.convertValue("A", Character.class);
      assertEquals('A', result);
    }

    @Test
    @DisplayName("char基本类型转换")
    void testCharPrimitiveConversion() {
      Object result = ParamUtil.convertValue("X", char.class);
      assertEquals('X', result);
    }

    @Test
    @DisplayName("Short转换")
    void testShortConversion() {
      Object result = ParamUtil.convertValue("123", Short.class);
      assertEquals((short) 123, result);
    }

    @Test
    @DisplayName("short基本类型转换")
    void testShortPrimitiveConversion() {
      Object result = ParamUtil.convertValue("456", short.class);
      assertEquals((short) 456, result);
    }

    @Test
    @DisplayName("Byte转换")
    void testByteConversion() {
      Object result = ParamUtil.convertValue("127", Byte.class);
      assertEquals((byte) 127, result);
    }

    @Test
    @DisplayName("byte基本类型转换")
    void testBytePrimitiveConversion() {
      Object result = ParamUtil.convertValue("100", byte.class);
      assertEquals((byte) 100, result);
    }

    @Test
    @DisplayName("Enum转换")
    void testEnumConversion() {
      Object result = ParamUtil.convertValue("RED", TestColor.class);
      assertEquals(TestColor.RED, result);
    }

    @Test
    @DisplayName("null值转换")
    void testNullConversion() {
      Object result = ParamUtil.convertValue(null, String.class);
      assertNull(result);
    }

    @Test
    @DisplayName("空字符串转换")
    void testEmptyStringConversion() {
      Object result = ParamUtil.convertValue("", String.class);
      assertNull(result);
    }

    @Test
    @DisplayName("无效Integer转换")
    void testInvalidIntegerConversion() {
      assertThrows(
          IllegalArgumentException.class,
          () -> ParamUtil.convertValue("abc", Integer.class));
    }

    @Test
    @DisplayName("不支持的类型转换")
    void testUnsupportedTypeConversion() {
      assertThrows(
          IllegalArgumentException.class,
          () -> ParamUtil.convertValue("test", UUID.class));
    }
  }

  @Nested
  @DisplayName("方法选择测试")
  class MethodSelectionTest {

    @Test
    @DisplayName("获取候选方法")
    void testGetCandidateMethods() throws NoSuchMethodException {
      java.lang.reflect.Method[] methods = TestMethodClass.class.getDeclaredMethods();
      List<java.lang.reflect.Method> candidates =
          ParamUtil.getCandidateMethods(methods, "testMethod");

      assertEquals(1, candidates.size());
      assertEquals("testMethod", candidates.get(0).getName());
    }

    @Test
    @DisplayName("选择单个候选方法")
    void testSelectSingleCandidate() {
      List<java.lang.reflect.Method> candidates = new ArrayList<>();
      try {
        candidates.add(TestMethodClass.class.getDeclaredMethod("testMethod", String.class));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }

      Map<String, String> pathParams = new HashMap<>();
      MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
      java.lang.reflect.Method result =
          ParamUtil.selectBestMatch(candidates, pathParams, queryParams, false);

      assertNotNull(result);
      assertEquals("testMethod", result.getName());
    }

    @Test
    @DisplayName("空候选方法列表")
    void testEmptyCandidateList() {
      List<java.lang.reflect.Method> candidates = new ArrayList<>();
      Map<String, String> pathParams = new HashMap<>();
      MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();

      java.lang.reflect.Method result =
          ParamUtil.selectBestMatch(candidates, pathParams, queryParams, false);

      assertNull(result);
    }
  }

  // Helper classes
  static class TestEntity {
    public String id;
    public String name;
  }

  enum TestColor {
    RED,
    GREEN,
    BLUE
  }

  static class TestMethodClass {
    public void testMethod(String param) {}
  }
}
