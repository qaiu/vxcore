package cn.qaiu.vx.core.util;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * FutureUtils 工具类单元测试
 *
 * @author test
 */
@DisplayName("FutureUtils 工具类测试")
class FutureUtilsTest {

  @Nested
  @DisplayName("getResult(Future) 测试")
  class GetResultFromFutureTest {

    @Test
    @DisplayName("获取成功的 Future 结果")
    void testGetSuccessfulFutureResult() {
      Future<String> future = Future.succeededFuture("Hello");
      String result = FutureUtils.getResult(future);
      assertEquals("Hello", result);
    }

    @Test
    @DisplayName("获取空值 Future 结果")
    void testGetNullFutureResult() {
      Future<String> future = Future.succeededFuture(null);
      String result = FutureUtils.getResult(future);
      assertNull(result);
    }

    @Test
    @DisplayName("获取整数类型 Future 结果")
    void testGetIntegerFutureResult() {
      Future<Integer> future = Future.succeededFuture(42);
      Integer result = FutureUtils.getResult(future);
      assertEquals(42, result);
    }

    @Test
    @DisplayName("获取复杂对象 Future 结果")
    void testGetObjectFutureResult() {
      TestData data = new TestData("test", 123);
      Future<TestData> future = Future.succeededFuture(data);
      TestData result = FutureUtils.getResult(future);
      assertSame(data, result);
    }

    @Test
    @DisplayName("获取失败的 Future 结果 - 抛出 RuntimeException")
    void testGetFailedFutureResult() {
      Future<String> future = Future.failedFuture(new IllegalStateException("Test error"));
      
      RuntimeException exception = assertThrows(RuntimeException.class, 
          () -> FutureUtils.getResult(future));
      assertTrue(exception.getCause() instanceof java.util.concurrent.ExecutionException);
    }
  }

  @Nested
  @DisplayName("getResult(Promise) 测试")
  class GetResultFromPromiseTest {

    @Test
    @DisplayName("获取成功完成的 Promise 结果")
    void testGetSuccessfulPromiseResult() {
      Promise<String> promise = Promise.promise();
      promise.complete("World");
      String result = FutureUtils.getResult(promise);
      assertEquals("World", result);
    }

    @Test
    @DisplayName("获取空值 Promise 结果")
    void testGetNullPromiseResult() {
      Promise<String> promise = Promise.promise();
      promise.complete(null);
      String result = FutureUtils.getResult(promise);
      assertNull(result);
    }

    @Test
    @DisplayName("获取整数类型 Promise 结果")
    void testGetIntegerPromiseResult() {
      Promise<Integer> promise = Promise.promise();
      promise.complete(100);
      Integer result = FutureUtils.getResult(promise);
      assertEquals(100, result);
    }

    @Test
    @DisplayName("获取复杂对象 Promise 结果")
    void testGetObjectPromiseResult() {
      TestData data = new TestData("promise", 456);
      Promise<TestData> promise = Promise.promise();
      promise.complete(data);
      TestData result = FutureUtils.getResult(promise);
      assertSame(data, result);
    }

    @Test
    @DisplayName("获取失败的 Promise 结果 - 抛出异常")
    void testGetFailedPromiseResult() {
      Promise<String> promise = Promise.promise();
      promise.fail(new RuntimeException("Promise failed"));
      
      assertThrows(java.util.concurrent.CompletionException.class, 
          () -> FutureUtils.getResult(promise));
    }
  }

  @Nested
  @DisplayName("类型安全测试")
  class TypeSafetyTest {

    @Test
    @DisplayName("不同泛型类型的 Future")
    void testDifferentFutureTypes() {
      Future<String> stringFuture = Future.succeededFuture("string");
      Future<Integer> intFuture = Future.succeededFuture(123);
      Future<Boolean> boolFuture = Future.succeededFuture(true);

      assertEquals("string", FutureUtils.getResult(stringFuture));
      assertEquals(123, FutureUtils.getResult(intFuture));
      assertTrue(FutureUtils.getResult(boolFuture));
    }

    @Test
    @DisplayName("不同泛型类型的 Promise")
    void testDifferentPromiseTypes() {
      Promise<String> stringPromise = Promise.promise();
      Promise<Double> doublePromise = Promise.promise();
      
      stringPromise.complete("test");
      doublePromise.complete(3.14);

      assertEquals("test", FutureUtils.getResult(stringPromise));
      assertEquals(3.14, FutureUtils.getResult(doublePromise));
    }
  }

  // 测试用数据类
  private static class TestData {
    private final String name;
    private final int value;

    TestData(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
