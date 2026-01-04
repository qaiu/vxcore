package cn.qaiu.vx.core.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * 枚举类单元测试
 *
 * @author qaiu
 */
@DisplayName("枚举类测试")
class EnumTest {

  @Nested
  @DisplayName("MIMEType 测试")
  class MIMETypeTest {

    @Test
    @DisplayName("NULL 类型值应为空字符串")
    void nullTypeShouldBeEmpty() {
      assertEquals("", MIMEType.NULL.getValue());
    }

    @Test
    @DisplayName("ALL 类型值应为 */*")
    void allTypeShouldBeWildcard() {
      assertEquals("*/*", MIMEType.ALL.getValue());
    }

    @Test
    @DisplayName("JSON 类型值应正确")
    void jsonTypeShouldBeCorrect() {
      assertEquals("application/json", MIMEType.APPLICATION_JSON.getValue());
    }

    @Test
    @DisplayName("HTML 类型值应正确")
    void htmlTypeShouldBeCorrect() {
      assertEquals("text/html", MIMEType.TEXT_HTML.getValue());
    }

    @Test
    @DisplayName("表单类型值应正确")
    void formUrlEncodedTypeShouldBeCorrect() {
      assertEquals(
          "application/x-www-form-urlencoded", MIMEType.APPLICATION_X_WWW_FORM_URLENCODED.getValue());
    }

    @Test
    @DisplayName("multipart 表单类型值应正确")
    void multipartFormDataTypeShouldBeCorrect() {
      assertEquals("multipart/form-data", MIMEType.MULTIPART_FORM_DATA.getValue());
    }

    @ParameterizedTest
    @EnumSource(MIMEType.class)
    @DisplayName("所有 MIMEType 枚举值都应有非空的 getValue()")
    void allEnumsShouldHaveNonNullValue(MIMEType type) {
      assertNotNull(type.getValue());
    }

    @Test
    @DisplayName("应能通过 valueOf 获取枚举值")
    void shouldGetEnumByValueOf() {
      assertEquals(MIMEType.APPLICATION_JSON, MIMEType.valueOf("APPLICATION_JSON"));
      assertEquals(MIMEType.TEXT_PLAIN, MIMEType.valueOf("TEXT_PLAIN"));
    }

    @Test
    @DisplayName("values() 应返回所有枚举值")
    void valuesShouldReturnAllEnums() {
      MIMEType[] values = MIMEType.values();
      assertTrue(values.length > 0);
      // 检查包含常用类型
      boolean hasJson = false;
      boolean hasHtml = false;
      for (MIMEType type : values) {
        if (type == MIMEType.APPLICATION_JSON) hasJson = true;
        if (type == MIMEType.TEXT_HTML) hasHtml = true;
      }
      assertTrue(hasJson);
      assertTrue(hasHtml);
    }
  }

  @Nested
  @DisplayName("RouteMethod 测试")
  class RouteMethodTest {

    @Test
    @DisplayName("应包含所有标准 HTTP 方法")
    void shouldContainAllHttpMethods() {
      assertNotNull(RouteMethod.GET);
      assertNotNull(RouteMethod.POST);
      assertNotNull(RouteMethod.PUT);
      assertNotNull(RouteMethod.DELETE);
      assertNotNull(RouteMethod.PATCH);
      assertNotNull(RouteMethod.HEAD);
      assertNotNull(RouteMethod.OPTIONS);
    }

    @Test
    @DisplayName("应包含 ROUTE 通用方法")
    void shouldContainRouteMethod() {
      assertNotNull(RouteMethod.ROUTE);
    }

    @Test
    @DisplayName("应能通过 valueOf 获取枚举值")
    void shouldGetEnumByValueOf() {
      assertEquals(RouteMethod.GET, RouteMethod.valueOf("GET"));
      assertEquals(RouteMethod.POST, RouteMethod.valueOf("POST"));
      assertEquals(RouteMethod.PUT, RouteMethod.valueOf("PUT"));
      assertEquals(RouteMethod.DELETE, RouteMethod.valueOf("DELETE"));
    }

    @ParameterizedTest
    @EnumSource(RouteMethod.class)
    @DisplayName("所有 RouteMethod 枚举都应有有效的 name()")
    void allEnumsShouldHaveValidName(RouteMethod method) {
      assertNotNull(method.name());
      assertFalse(method.name().isEmpty());
    }

    @Test
    @DisplayName("values() 应返回所有枚举值")
    void valuesShouldReturnAllEnums() {
      RouteMethod[] values = RouteMethod.values();
      assertEquals(10, values.length); // OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, PATCH, ROUTE
    }

    @Test
    @DisplayName("枚举值的序号应正确")
    void ordinalShouldBeCorrect() {
      assertEquals(0, RouteMethod.OPTIONS.ordinal());
      assertEquals(1, RouteMethod.GET.ordinal());
      assertEquals(3, RouteMethod.POST.ordinal());
    }
  }
}
