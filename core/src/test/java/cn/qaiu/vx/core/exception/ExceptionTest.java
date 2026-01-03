package cn.qaiu.vx.core.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 异常类单元测试
 *
 * @author QAIU
 */
@DisplayName("异常类测试")
class ExceptionTest {

  @Nested
  @DisplayName("BaseException 测试")
  class BaseExceptionTest {

    @Test
    @DisplayName("测试基本构造函数")
    void testBasicConstructor() {
      BaseException exception = new BaseException(500, "服务器错误");

      assertEquals(500, exception.getCode());
      assertEquals("服务器错误", exception.getMessage());
      assertNull(exception.getCause());
    }

    @Test
    @DisplayName("测试带Cause的构造函数")
    void testConstructorWithCause() {
      Throwable cause = new RuntimeException("原始错误");
      BaseException exception = new BaseException(500, "服务器错误", cause);

      assertEquals(500, exception.getCode());
      assertEquals("服务器错误", exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试异常继承RuntimeException")
    void testExtendsRuntimeException() {
      BaseException exception = new BaseException(400, "错误");
      assertTrue(exception instanceof RuntimeException);
    }
  }

  @Nested
  @DisplayName("BusinessException 测试")
  class BusinessExceptionTest {

    @Test
    @DisplayName("测试单参数构造函数 - 默认code为400")
    void testSingleParamConstructor() {
      BusinessException exception = new BusinessException("业务错误");

      assertEquals(400, exception.getCode());
      assertEquals("业务错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试双参数构造函数")
    void testTwoParamConstructor() {
      BusinessException exception = new BusinessException(403, "禁止访问");

      assertEquals(403, exception.getCode());
      assertEquals("禁止访问", exception.getMessage());
    }

    @Test
    @DisplayName("测试带Cause的构造函数 - 默认code")
    void testConstructorWithCauseDefaultCode() {
      Throwable cause = new IllegalArgumentException("参数错误");
      BusinessException exception = new BusinessException("业务异常", cause);

      assertEquals(400, exception.getCode());
      assertEquals("业务异常", exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试带Cause的构造函数 - 自定义code")
    void testConstructorWithCauseCustomCode() {
      Throwable cause = new IllegalArgumentException("参数错误");
      BusinessException exception = new BusinessException(409, "冲突", cause);

      assertEquals(409, exception.getCode());
      assertEquals("冲突", exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试继承自BaseException")
    void testExtendsBaseException() {
      BusinessException exception = new BusinessException("测试");
      assertTrue(exception instanceof BaseException);
    }
  }

  @Nested
  @DisplayName("SystemException 测试")
  class SystemExceptionTest {

    @Test
    @DisplayName("测试单参数构造函数 - 默认code为500")
    void testSingleParamConstructor() {
      SystemException exception = new SystemException("系统错误");

      assertEquals(500, exception.getCode());
      assertEquals("系统错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试带Cause的构造函数")
    void testConstructorWithCause() {
      Throwable cause = new RuntimeException("内部错误");
      SystemException exception = new SystemException("系统异常", cause);

      assertEquals(500, exception.getCode());
      assertEquals("系统异常", exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试继承自BaseException")
    void testExtendsBaseException() {
      SystemException exception = new SystemException("测试");
      assertTrue(exception instanceof BaseException);
    }
  }

  @Nested
  @DisplayName("ValidationException 测试")
  class ValidationExceptionTest {

    @Test
    @DisplayName("测试单参数构造函数 - 默认code为422")
    void testSingleParamConstructor() {
      ValidationException exception = new ValidationException("验证失败");

      assertEquals(422, exception.getCode());
      assertEquals("验证失败", exception.getMessage());
    }

    @Test
    @DisplayName("测试带Cause的构造函数")
    void testConstructorWithCause() {
      Throwable cause = new IllegalArgumentException("无效参数");
      ValidationException exception = new ValidationException("参数验证失败", cause);

      assertEquals(422, exception.getCode());
      assertEquals("参数验证失败", exception.getMessage());
      assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试继承自BaseException")
    void testExtendsBaseException() {
      ValidationException exception = new ValidationException("测试");
      assertTrue(exception instanceof BaseException);
    }
  }

  @Nested
  @DisplayName("异常场景测试")
  class ExceptionScenarioTest {

    @Test
    @DisplayName("测试异常可以被正确抛出和捕获")
    void testExceptionThrowAndCatch() {
      assertThrows(
          BusinessException.class,
          () -> {
            throw new BusinessException("测试异常");
          });
    }

    @Test
    @DisplayName("测试异常链传递")
    void testExceptionChain() {
      ValidationException original = new ValidationException("原始验证错误");
      BusinessException wrapped = new BusinessException("包装后的业务错误", original);

      assertEquals(original, wrapped.getCause());
      assertTrue(wrapped.getCause() instanceof ValidationException);
    }

    @Test
    @DisplayName("测试不同异常类型的code区分")
    void testExceptionCodeDistinction() {
      BusinessException business = new BusinessException("业务");
      SystemException system = new SystemException("系统");
      ValidationException validation = new ValidationException("验证");

      assertEquals(400, business.getCode());
      assertEquals(500, system.getCode());
      assertEquals(422, validation.getCode());

      // 确保code不同
      assertNotEquals(business.getCode(), system.getCode());
      assertNotEquals(business.getCode(), validation.getCode());
      assertNotEquals(system.getCode(), validation.getCode());
    }
  }
}
