package cn.qaiu.vx.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

@DisplayName("ReflectionUtil反射工具类测试")
class ReflectionUtilTest {

  @Nested
  @DisplayName("getReflections(String)测试")
  class GetReflectionsStringTest {

    @Test
    @DisplayName("单包路径返回Reflections对象")
    void singlePackage_returnsReflections() {
      Reflections reflections = ReflectionUtil.getReflections("cn.qaiu.vx.core.util");
      assertThat(reflections).isNotNull();
    }

    @Test
    @DisplayName("逗号分隔多包路径返回Reflections对象")
    void commaDelimited_returnsReflections() {
      Reflections reflections =
          ReflectionUtil.getReflections("cn.qaiu.vx.core.util,cn.qaiu.vx.core.annotations");
      assertThat(reflections).isNotNull();
    }

    @Test
    @DisplayName("分号分隔多包路径返回Reflections对象")
    void semicolonDelimited_returnsReflections() {
      Reflections reflections =
          ReflectionUtil.getReflections("cn.qaiu.vx.core.util;cn.qaiu.vx.core.annotations");
      assertThat(reflections).isNotNull();
    }
  }

  @Nested
  @DisplayName("getReflections(List)测试")
  class GetReflectionsListTest {

    @Test
    @DisplayName("包列表返回Reflections对象")
    void packageList_returnsReflections() {
      List<String> packages =
          Arrays.asList("cn.qaiu.vx.core.util", "cn.qaiu.vx.core.annotations");
      Reflections reflections = ReflectionUtil.getReflections(packages);
      assertThat(reflections).isNotNull();
    }

    @Test
    @DisplayName("单元素列表返回Reflections对象")
    void singletonList_returnsReflections() {
      Reflections reflections =
          ReflectionUtil.getReflections(Collections.singletonList("cn.qaiu.vx.core.util"));
      assertThat(reflections).isNotNull();
    }
  }

  @Nested
  @DisplayName("conversion类型转换测试")
  class ConversionTest {

    @Test
    @DisplayName("空字符串返回null")
    void emptyString_returnsNull() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.String");
      assertThat(ReflectionUtil.conversion(ctClass, "", null)).isNull();
    }

    @Test
    @DisplayName("null字符串返回null")
    void nullString_returnsNull() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.String");
      assertThat(ReflectionUtil.conversion(ctClass, null, null)).isNull();
    }

    @Test
    @DisplayName("转换为String类型")
    void convertToString() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.String");
      Object result = ReflectionUtil.conversion(ctClass, "hello", null);
      assertThat(result).isEqualTo("hello");
    }

    @Test
    @DisplayName("转换为Boolean包装类型")
    void convertToBoolean() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Boolean");
      assertThat(ReflectionUtil.conversion(ctClass, "true", null)).isEqualTo(true);
      assertThat(ReflectionUtil.conversion(ctClass, "false", null)).isEqualTo(false);
    }

    @Test
    @DisplayName("转换为boolean原始类型")
    void convertToPrimitiveBoolean() throws Exception {
      CtClass ctClass = CtClass.booleanType;
      assertThat(ReflectionUtil.conversion(ctClass, "true", null)).isEqualTo(true);
    }

    @Test
    @DisplayName("转换为Integer包装类型")
    void convertToInteger() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Integer");
      assertThat(ReflectionUtil.conversion(ctClass, "42", null)).isEqualTo(42);
    }

    @Test
    @DisplayName("转换为int原始类型")
    void convertToPrimitiveInt() throws Exception {
      CtClass ctClass = CtClass.intType;
      assertThat(ReflectionUtil.conversion(ctClass, "100", null)).isEqualTo(100);
    }

    @Test
    @DisplayName("转换为Long包装类型")
    void convertToLong() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Long");
      assertThat(ReflectionUtil.conversion(ctClass, "999", null)).isEqualTo(999L);
    }

    @Test
    @DisplayName("转换为long原始类型")
    void convertToPrimitiveLong() throws Exception {
      CtClass ctClass = CtClass.longType;
      assertThat(ReflectionUtil.conversion(ctClass, "999", null)).isEqualTo(999L);
    }

    @Test
    @DisplayName("转换为Double包装类型")
    void convertToDouble() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Double");
      assertThat(ReflectionUtil.conversion(ctClass, "3.14", null)).isEqualTo(3.14);
    }

    @Test
    @DisplayName("转换为double原始类型")
    void convertToPrimitiveDouble() throws Exception {
      CtClass ctClass = CtClass.doubleType;
      assertThat(ReflectionUtil.conversion(ctClass, "2.71", null)).isEqualTo(2.71);
    }

    @Test
    @DisplayName("转换为Float包装类型")
    void convertToFloat() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Float");
      assertThat((Float) ReflectionUtil.conversion(ctClass, "1.5", null)).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("转换为float原始类型")
    void convertToPrimitiveFloat() throws Exception {
      CtClass ctClass = CtClass.floatType;
      assertThat((Float) ReflectionUtil.conversion(ctClass, "1.5", null)).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("转换为Short包装类型")
    void convertToShort() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Short");
      assertThat(ReflectionUtil.conversion(ctClass, "10", null)).isEqualTo((short) 10);
    }

    @Test
    @DisplayName("转换为short原始类型")
    void convertToPrimitiveShort() throws Exception {
      CtClass ctClass = CtClass.shortType;
      assertThat(ReflectionUtil.conversion(ctClass, "10", null)).isEqualTo((short) 10);
    }

    @Test
    @DisplayName("转换为Byte包装类型")
    void convertToByte() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Byte");
      assertThat(ReflectionUtil.conversion(ctClass, "5", null)).isEqualTo((byte) 5);
    }

    @Test
    @DisplayName("转换为byte原始类型")
    void convertToPrimitiveByte() throws Exception {
      CtClass ctClass = CtClass.byteType;
      assertThat(ReflectionUtil.conversion(ctClass, "5", null)).isEqualTo((byte) 5);
    }

    @Test
    @DisplayName("转换为Character包装类型")
    void convertToCharacter() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Character");
      assertThat(ReflectionUtil.conversion(ctClass, "A", null)).isEqualTo('A');
    }

    @Test
    @DisplayName("转换为char原始类型")
    void convertToPrimitiveChar() throws Exception {
      CtClass ctClass = CtClass.charType;
      assertThat(ReflectionUtil.conversion(ctClass, "Z", null)).isEqualTo('Z');
    }

    @Test
    @DisplayName("转换为Date类型（使用默认格式）")
    void convertToDate_defaultFormat() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.util.Date");
      Object result = ReflectionUtil.conversion(ctClass, "2024-01-15", null);
      assertThat(result).isInstanceOf(Date.class);
    }

    @Test
    @DisplayName("转换为Date类型（使用自定义格式）")
    void convertToDate_customFormat() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.util.Date");
      Object result = ReflectionUtil.conversion(ctClass, "15/01/2024", "dd/MM/yyyy");
      assertThat(result).isInstanceOf(Date.class);
    }

    @Test
    @DisplayName("Date转换失败时抛出RuntimeException")
    void convertToDate_invalidFormat_throwsRuntimeException() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.util.Date");
      assertThatThrownBy(() -> ReflectionUtil.conversion(ctClass, "not-a-date", null))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("不支持的类型抛出RuntimeException")
    void unsupportedType_throwsRuntimeException() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.util.ArrayList");
      assertThatThrownBy(() -> ReflectionUtil.conversion(ctClass, "someValue", null))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("无法将String类型");
    }
  }

  @Nested
  @DisplayName("isBasicType测试")
  class IsBasicTypeTest {

    @Test
    @DisplayName("原始类型返回true")
    void primitiveTypes_returnTrue() throws Exception {
      assertThat(ReflectionUtil.isBasicType(CtClass.intType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.longType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.doubleType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.booleanType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.floatType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.charType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.shortType)).isTrue();
      assertThat(ReflectionUtil.isBasicType(CtClass.byteType)).isTrue();
    }

    @Test
    @DisplayName("包装类型返回true")
    void wrapperTypes_returnTrue() throws Exception {
      ClassPool pool = ClassPool.getDefault();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Boolean"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Integer"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Long"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Double"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Float"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Short"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Byte"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.Character"))).isTrue();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.lang.String"))).isTrue();
    }

    @Test
    @DisplayName("Date类型返回true")
    void dateType_returnsTrue() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.util.Date");
      assertThat(ReflectionUtil.isBasicType(ctClass)).isTrue();
    }

    @Test
    @DisplayName("非基本类型返回false")
    void nonBasicType_returnsFalse() throws Exception {
      ClassPool pool = ClassPool.getDefault();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.util.ArrayList"))).isFalse();
      assertThat(ReflectionUtil.isBasicType(pool.get("java.util.HashMap"))).isFalse();
    }
  }

  @Nested
  @DisplayName("isBasicTypeArray测试")
  class IsBasicTypeArrayTest {

    @Test
    @DisplayName("非数组类型返回false")
    void nonArrayType_returnsFalse() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.String");
      assertThat(ReflectionUtil.isBasicTypeArray(ctClass)).isFalse();
    }

    @Test
    @DisplayName("对象数组（非基本类型数组）返回false")
    void objectArray_returnsFalse() throws Exception {
      CtClass ctClass = ClassPool.getDefault().get("java.lang.Object[]");
      assertThat(ReflectionUtil.isBasicTypeArray(ctClass)).isFalse();
    }
  }

  @Nested
  @DisplayName("newWithNoParam测试")
  class NewWithNoParamTest {

    @Test
    @DisplayName("通过无参构造创建对象")
    void createsInstance_withNoArgConstructor() throws Exception {
      StringBuilder result = ReflectionUtil.newWithNoParam(StringBuilder.class);
      assertThat(result).isNotNull();
      assertThat(result).isInstanceOf(StringBuilder.class);
    }
  }

  @Nested
  @DisplayName("invokeWithArguments测试")
  class InvokeWithArgumentsTest {

    @Test
    @DisplayName("反射调用有参方法")
    void invokesMethodWithArguments() throws Throwable {
      StringBuilder sb = new StringBuilder();
      Method appendMethod = StringBuilder.class.getMethod("append", String.class);
      ReflectionUtil.invokeWithArguments(appendMethod, sb, "Hello");
      assertThat(sb.toString()).isEqualTo("Hello");
    }
  }

  @Nested
  @DisplayName("invoke无参方法测试")
  class InvokeTest {

    @Test
    @DisplayName("反射调用无参方法")
    void invokesMethodWithNoArguments() throws Throwable {
      StringBuilder sb = new StringBuilder("Hello World");
      Method lengthMethod = StringBuilder.class.getMethod("length");
      Object result = ReflectionUtil.invoke(lengthMethod, sb);
      assertThat(result).isEqualTo(11);
    }
  }
}
