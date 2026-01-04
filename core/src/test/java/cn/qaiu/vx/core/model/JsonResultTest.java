package cn.qaiu.vx.core.model;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JsonResult 单元测试
 *
 * @author qaiu
 */
@DisplayName("JsonResult 测试")
class JsonResultTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTests {

    @Test
    @DisplayName("默认构造函数应创建成功状态的结果")
    void defaultConstructor_ShouldCreateSuccessResult() {
      JsonResult<String> result = new JsonResult<>();

      assertEquals(200, result.getCode());
      assertEquals("success", result.getMsg());
      assertTrue(result.getSuccess());
      assertNull(result.getData());
      assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("带数据的构造函数应正确设置数据")
    void dataConstructor_ShouldSetData() {
      String testData = "test data";
      JsonResult<String> result = new JsonResult<>(testData);

      assertEquals(200, result.getCode());
      assertEquals("success", result.getMsg());
      assertTrue(result.getSuccess());
      assertEquals(testData, result.getData());
    }

    @Test
    @DisplayName("全参数构造函数应正确设置所有字段")
    void fullConstructor_ShouldSetAllFields() {
      JsonResult<String> result = new JsonResult<>(404, "Not Found", false, "error data");

      assertEquals(404, result.getCode());
      assertEquals("Not Found", result.getMsg());
      assertFalse(result.getSuccess());
      assertEquals("error data", result.getData());
    }
  }

  @Nested
  @DisplayName("Setter 方法链式调用测试")
  class SetterChainTests {

    @Test
    @DisplayName("setCode 应返回自身以支持链式调用")
    void setCode_ShouldReturnSelf() {
      JsonResult<String> result = new JsonResult<>();
      JsonResult<String> returned = result.setCode(201);

      assertSame(result, returned);
      assertEquals(201, result.getCode());
    }

    @Test
    @DisplayName("setMsg 应返回自身以支持链式调用")
    void setMsg_ShouldReturnSelf() {
      JsonResult<String> result = new JsonResult<>();
      JsonResult<String> returned = result.setMsg("custom message");

      assertSame(result, returned);
      assertEquals("custom message", result.getMsg());
    }

    @Test
    @DisplayName("setData 应返回自身以支持链式调用")
    void setData_ShouldReturnSelf() {
      JsonResult<String> result = new JsonResult<>();
      JsonResult<String> returned = result.setData("test");

      assertSame(result, returned);
      assertEquals("test", result.getData());
    }

    @Test
    @DisplayName("setSuccess 应返回自身以支持链式调用")
    void setSuccess_ShouldReturnSelf() {
      JsonResult<String> result = new JsonResult<>();
      JsonResult<String> returned = result.setSuccess(false);

      assertSame(result, returned);
      assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("setTimestamp 应返回自身以支持链式调用")
    void setTimestamp_ShouldReturnSelf() {
      JsonResult<String> result = new JsonResult<>();
      long timestamp = 1234567890L;
      JsonResult<String> returned = result.setTimestamp(timestamp);

      assertSame(result, returned);
      assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    @DisplayName("链式调用应正确设置多个字段")
    void chainedCalls_ShouldSetMultipleFields() {
      JsonResult<String> result =
          new JsonResult<String>()
              .setCode(201)
              .setMsg("Created")
              .setSuccess(true)
              .setData("new resource");

      assertEquals(201, result.getCode());
      assertEquals("Created", result.getMsg());
      assertTrue(result.getSuccess());
      assertEquals("new resource", result.getData());
    }
  }

  @Nested
  @DisplayName("静态工厂方法测试")
  class StaticFactoryMethodTests {

    @Test
    @DisplayName("error() 应创建默认错误结果")
    void error_ShouldCreateDefaultErrorResult() {
      JsonResult<String> result = JsonResult.error();

      assertEquals(500, result.getCode());
      assertEquals("failed", result.getMsg());
      assertFalse(result.getSuccess());
      assertNull(result.getData());
    }

    @Test
    @DisplayName("error(msg) 应创建带消息的错误结果")
    void errorWithMsg_ShouldCreateErrorWithMessage() {
      JsonResult<String> result = JsonResult.error("Custom error");

      assertEquals(500, result.getCode());
      assertEquals("Custom error", result.getMsg());
      assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("error(msg) 当消息为空时应使用默认消息")
    void errorWithEmptyMsg_ShouldUseDefaultMessage() {
      JsonResult<String> result = JsonResult.error("");

      assertEquals("failed", result.getMsg());
    }

    @Test
    @DisplayName("error(msg) 当消息为null时应使用默认消息")
    void errorWithNullMsg_ShouldUseDefaultMessage() {
      JsonResult<String> result = JsonResult.error(null);

      assertEquals("failed", result.getMsg());
    }

    @Test
    @DisplayName("error(msg, code) 应创建带消息和状态码的错误结果")
    void errorWithMsgAndCode_ShouldCreateErrorWithMessageAndCode() {
      JsonResult<String> result = JsonResult.error("Not Found", 404);

      assertEquals(404, result.getCode());
      assertEquals("Not Found", result.getMsg());
      assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("error(msg, code) 当消息为空时应使用默认消息")
    void errorWithEmptyMsgAndCode_ShouldUseDefaultMessage() {
      JsonResult<String> result = JsonResult.error("", 404);

      assertEquals(404, result.getCode());
      assertEquals("failed", result.getMsg());
    }

    @Test
    @DisplayName("data(data) 应创建带数据的成功结果")
    void dataOnly_ShouldCreateSuccessWithData() {
      String testData = "test data";
      JsonResult<String> result = JsonResult.data(testData);

      assertEquals(200, result.getCode());
      assertEquals("success", result.getMsg());
      assertTrue(result.getSuccess());
      assertEquals(testData, result.getData());
    }

    @Test
    @DisplayName("data(msg, data) 应创建带消息和数据的成功结果")
    void dataWithMsg_ShouldCreateSuccessWithMessageAndData() {
      String testData = "test data";
      JsonResult<String> result = JsonResult.data("Custom message", testData);

      assertEquals(200, result.getCode());
      assertEquals("Custom message", result.getMsg());
      assertTrue(result.getSuccess());
      assertEquals(testData, result.getData());
    }

    @Test
    @DisplayName("data(msg, data) 当消息为空时应使用默认消息")
    void dataWithEmptyMsg_ShouldUseDefaultMessage() {
      JsonResult<String> result = JsonResult.data("", "data");

      assertEquals("success", result.getMsg());
    }

    @Test
    @DisplayName("success() 应创建默认成功结果")
    void success_ShouldCreateDefaultSuccessResult() {
      JsonResult<String> result = JsonResult.success();

      assertEquals(200, result.getCode());
      assertEquals("success", result.getMsg());
      assertTrue(result.getSuccess());
      assertNull(result.getData());
    }

    @Test
    @DisplayName("success(msg) 应创建带消息的成功结果")
    void successWithMsg_ShouldCreateSuccessWithMessage() {
      JsonResult<String> result = JsonResult.success("Operation completed");

      assertEquals(200, result.getCode());
      assertEquals("Operation completed", result.getMsg());
      assertTrue(result.getSuccess());
    }

    @Test
    @DisplayName("success(msg) 当消息为空时应使用默认消息")
    void successWithEmptyMsg_ShouldUseDefaultMessage() {
      JsonResult<String> result = JsonResult.success("");

      assertEquals("success", result.getMsg());
    }
  }

  @Nested
  @DisplayName("JSON 转换测试")
  class JsonConversionTests {

    @Test
    @DisplayName("toJsonObject 应正确转换为 JsonObject")
    void toJsonObject_ShouldConvertCorrectly() {
      JsonResult<String> result = JsonResult.data("test data");

      JsonObject json = result.toJsonObject();

      assertEquals(200, json.getInteger("code"));
      assertEquals("success", json.getString("msg"));
      assertTrue(json.getBoolean("success"));
      assertEquals("test data", json.getString("data"));
      assertNotNull(json.getLong("timestamp"));
    }

    @Test
    @DisplayName("toJsonResult 应正确从 JsonObject 转换")
    void toJsonResult_ShouldConvertFromJsonObject() {
      JsonObject json = new JsonObject();
      json.put("code", 201);
      json.put("msg", "Created");
      json.put("success", true);
      json.put("data", "new item");
      json.put("timestamp", System.currentTimeMillis());

      JsonResult<?> result = JsonResult.toJsonResult(json);

      assertEquals(201, result.getCode());
      assertEquals("Created", result.getMsg());
      assertTrue(result.getSuccess());
    }
  }

  @Nested
  @DisplayName("泛型测试")
  class GenericTests {

    @Test
    @DisplayName("应支持不同类型的数据")
    void shouldSupportDifferentDataTypes() {
      // String
      JsonResult<String> stringResult = JsonResult.data("string data");
      assertEquals("string data", stringResult.getData());

      // Integer
      JsonResult<Integer> intResult = JsonResult.data(42);
      assertEquals(42, intResult.getData());

      // Custom object
      TestObject testObj = new TestObject("name", 100);
      JsonResult<TestObject> objResult = JsonResult.data(testObj);
      assertEquals(testObj, objResult.getData());
    }
  }

  // 测试用的简单对象
  static class TestObject {
    String name;
    int value;

    TestObject(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
