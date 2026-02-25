package cn.qaiu.vx.core.performance;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 框架核心组件并发性能测试 测试路由创建、JSON处理、EventBus等核心组件的并发性能
 *
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("框架核心组件并发性能测试")
@DisabledIfEnvironmentVariable(
    named = "CI",
    matches = "true",
    disabledReason = "性能测试在CI环境中不稳定，本地可手动运行")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FrameworkComponentPerformanceTest {

  private static final int THREAD_COUNT = 10;
  private static final int OPERATIONS_PER_THREAD = 1000;

  @BeforeAll
  static void setUp(Vertx vertx) {
    VertxHolder.init(vertx);
  }

  @Test
  @Order(1)
  @DisplayName("Router创建并发性能测试")
  void testRouterCreationConcurrency(Vertx vertx, VertxTestContext testContext) {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicLong totalTime = new AtomicLong(0);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.submit(
          () -> {
            try {
              long threadStartTime = System.nanoTime();

              for (int j = 0; j < 100; j++) {
                Router router = Router.router(vertx);
                router.get("/test").handler(ctx -> ctx.response().end("OK"));
                router.post("/test").handler(ctx -> ctx.response().end("OK"));
                successCount.incrementAndGet();
              }

              totalTime.addAndGet(System.nanoTime() - threadStartTime);
            } catch (Exception e) {
              System.err.println("Router creation error: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    try {
      assertTrue(latch.await(30, TimeUnit.SECONDS));

      long endTime = System.currentTimeMillis();
      int total = successCount.get();
      double avgTimePerOp = total > 0 ? (double) totalTime.get() / total / 1_000_000 : 0;
      double throughput = total * 1000.0 / (endTime - startTime);

      System.out.println("=== Router创建并发性能测试结果 ===");
      System.out.println("线程数: " + THREAD_COUNT);
      System.out.println("成功操作: " + total);
      System.out.println("总耗时: " + (endTime - startTime) + "ms");
      System.out.println("平均每操作: " + String.format("%.3f", avgTimePerOp) + "ms");
      System.out.println("吞吐量: " + String.format("%.2f", throughput) + " ops/s");

      assertTrue(total > 0, "应有成功的操作");

      testContext.completeNow();
    } catch (InterruptedException e) {
      testContext.failNow(e);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  @Order(2)
  @DisplayName("JSON序列化并发性能测试")
  void testJsonSerializationConcurrency(VertxTestContext testContext) {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicLong totalTime = new AtomicLong(0);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.submit(
          () -> {
            try {
              long threadStartTime = System.nanoTime();

              for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                JsonObject json =
                    new JsonObject()
                        .put("id", j)
                        .put("name", "test_" + j)
                        .put("timestamp", System.currentTimeMillis())
                        .put(
                            "nested",
                            new JsonObject()
                                .put("field1", "value1")
                                .put("field2", 123)
                                .put("field3", true));

                String encoded = json.encode();
                JsonObject decoded = new JsonObject(encoded);

                if (decoded.getInteger("id") == j) {
                  successCount.incrementAndGet();
                }
              }

              totalTime.addAndGet(System.nanoTime() - threadStartTime);
            } catch (Exception e) {
              System.err.println("JSON error: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    try {
      assertTrue(latch.await(30, TimeUnit.SECONDS));

      long endTime = System.currentTimeMillis();
      int total = successCount.get();
      double avgTimePerOp = total > 0 ? (double) totalTime.get() / total : 0;
      double throughput = total * 1000.0 / (endTime - startTime);

      System.out.println("=== JSON序列化并发性能测试结果 ===");
      System.out.println("线程数: " + THREAD_COUNT);
      System.out.println("每线程操作数: " + OPERATIONS_PER_THREAD);
      System.out.println("成功操作: " + total);
      System.out.println("总耗时: " + (endTime - startTime) + "ms");
      System.out.println("平均每操作: " + String.format("%.0f", avgTimePerOp) + "ns");
      System.out.println("吞吐量: " + String.format("%.2f", throughput) + " ops/s");

      int expectedTotal = THREAD_COUNT * OPERATIONS_PER_THREAD;
      assertEquals(expectedTotal, total, "所有操作都应成功");
      assertTrue(avgTimePerOp < 500000, "每操作应小于500微秒");

      testContext.completeNow();
    } catch (InterruptedException e) {
      testContext.failNow(e);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  @Order(3)
  @DisplayName("EventBus消息发送并发性能测试")
  void testEventBusConcurrency(Vertx vertx, VertxTestContext testContext) {
    String address = "test.performance.address";
    AtomicInteger receivedCount = new AtomicInteger(0);

    vertx
        .eventBus()
        .<JsonObject>consumer(
            address,
            msg -> {
              receivedCount.incrementAndGet();
              msg.reply(new JsonObject().put("status", "ok"));
            });

    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicLong totalTime = new AtomicLong(0);

    long startTime = System.currentTimeMillis();
    int messagesPerThread = 100;

    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.submit(
          () -> {
            try {
              long threadStartTime = System.nanoTime();
              CountDownLatch messageLatch = new CountDownLatch(messagesPerThread);

              for (int j = 0; j < messagesPerThread; j++) {
                JsonObject message = new JsonObject().put("id", j).put("data", "test message");

                vertx
                    .eventBus()
                    .<JsonObject>request(address, message)
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            successCount.incrementAndGet();
                          }
                          messageLatch.countDown();
                        });
              }

              messageLatch.await(10, TimeUnit.SECONDS);
              totalTime.addAndGet(System.nanoTime() - threadStartTime);
            } catch (Exception e) {
              System.err.println("EventBus error: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    try {
      assertTrue(latch.await(30, TimeUnit.SECONDS));

      long endTime = System.currentTimeMillis();
      int total = successCount.get();
      double avgTimePerOp = total > 0 ? (double) totalTime.get() / total / 1_000_000 : 0;
      double throughput = total * 1000.0 / (endTime - startTime);

      System.out.println("=== EventBus并发性能测试结果 ===");
      System.out.println("线程数: " + THREAD_COUNT);
      System.out.println("每线程消息数: " + messagesPerThread);
      System.out.println("成功发送: " + total);
      System.out.println("接收消息数: " + receivedCount.get());
      System.out.println("总耗时: " + (endTime - startTime) + "ms");
      System.out.println("平均每消息: " + String.format("%.3f", avgTimePerOp) + "ms");
      System.out.println("吞吐量: " + String.format("%.2f", throughput) + " msg/s");

      assertTrue(total > 0, "应有成功发送的消息");
      assertEquals(total, receivedCount.get(), "发送和接收数应一致");

      testContext.completeNow();
    } catch (InterruptedException e) {
      testContext.failNow(e);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  @Order(4)
  @DisplayName("高负载压力测试")
  void testHighLoadStress(VertxTestContext testContext) {
    final int stressThreadCount = 20;
    final int stressOperationsPerThread = 500;

    ExecutorService executor = Executors.newFixedThreadPool(stressThreadCount);
    CountDownLatch latch = new CountDownLatch(stressThreadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicLong totalTime = new AtomicLong(0);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < stressThreadCount; i++) {
      executor.submit(
          () -> {
            try {
              long threadStartTime = System.nanoTime();

              for (int j = 0; j < stressOperationsPerThread; j++) {
                JsonObject json =
                    new JsonObject()
                        .put("operation", j)
                        .put("thread", Thread.currentThread().getId())
                        .put("timestamp", System.currentTimeMillis());

                String encoded = json.encode();
                JsonObject decoded = new JsonObject(encoded);

                if (decoded.containsKey("operation")) {
                  successCount.incrementAndGet();
                }
              }

              totalTime.addAndGet(System.nanoTime() - threadStartTime);
            } catch (Exception e) {
              System.err.println("Stress test error: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    try {
      assertTrue(latch.await(60, TimeUnit.SECONDS));

      long endTime = System.currentTimeMillis();
      int total = successCount.get();
      int expectedTotal = stressThreadCount * stressOperationsPerThread;
      double throughput = total * 1000.0 / (endTime - startTime);

      System.out.println("=== 高负载压力测试结果 ===");
      System.out.println("线程数: " + stressThreadCount);
      System.out.println("每线程操作数: " + stressOperationsPerThread);
      System.out.println("总操作数: " + expectedTotal);
      System.out.println("成功操作: " + total);
      System.out.println("总耗时: " + (endTime - startTime) + "ms");
      System.out.println("吞吐量: " + String.format("%.2f", throughput) + " ops/s");

      assertEquals(expectedTotal, total, "所有操作都应成功");
      assertTrue(throughput > 1000, "吞吐量应大于1000 ops/s");

      testContext.completeNow();
    } catch (InterruptedException e) {
      testContext.failNow(e);
    } finally {
      executor.shutdown();
    }
  }
}
