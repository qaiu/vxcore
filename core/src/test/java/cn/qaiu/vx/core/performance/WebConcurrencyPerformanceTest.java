package cn.qaiu.vx.core.performance;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Web 并发性能测试 测试框架在高并发 HTTP 请求下的吞吐量、响应时间和稳定性
 *
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("Web 并发性能测试")
@DisabledIfEnvironmentVariable(
    named = "CI",
    matches = "true",
    disabledReason = "性能测试在CI环境中不稳定，本地可手动运行")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebConcurrencyPerformanceTest {

  private static final int PORT = 18888;
  private static HttpClient client;
  private static String deploymentId;

  @BeforeAll
  static void setUpServer(Vertx vertx, VertxTestContext testContext) {
    HttpClientOptions options =
        new HttpClientOptions()
            .setMaxPoolSize(500)
            .setKeepAlive(true)
            .setTcpKeepAlive(true)
            .setPipelining(true)
            .setConnectTimeout(5000);

    client = vertx.createHttpClient(options);

    vertx
        .deployVerticle(new TestHttpServerVerticle())
        .onSuccess(
            id -> {
              deploymentId = id;
              testContext.completeNow();
            })
        .onFailure(testContext::failNow);
  }

  @AfterAll
  static void tearDown(Vertx vertx, VertxTestContext testContext) {
    if (client != null) {
      client.close();
    }
    if (deploymentId != null) {
      vertx.undeploy(deploymentId).onComplete(ar -> testContext.completeNow());
    } else {
      testContext.completeNow();
    }
  }

  @Test
  @Order(1)
  @DisplayName("基准测试 - 单请求响应时间")
  void testBaselineResponse(VertxTestContext testContext) {
    long startTime = System.nanoTime();

    client
        .request(HttpMethod.GET, PORT, "localhost", "/api/hello")
        .compose(
            req ->
                req.send()
                    .compose(
                        resp -> {
                          assertEquals(200, resp.statusCode());
                          return resp.body();
                        }))
        .onComplete(
            testContext.succeeding(
                body -> {
                  long endTime = System.nanoTime();
                  long responseTime = (endTime - startTime) / 1_000_000;

                  System.out.println("=== 基准测试结果 ===");
                  System.out.println("响应时间: " + responseTime + "ms");
                  assertTrue(responseTime < 500, "单请求响应时间应小于500ms");
                  testContext.completeNow();
                }));
  }

  @Test
  @Order(2)
  @DisplayName("并发测试 - 100并发请求")
  void testLowConcurrency(VertxTestContext testContext) throws InterruptedException {
    runConcurrencyTest(100, testContext);
  }

  @Test
  @Order(3)
  @DisplayName("并发测试 - 500并发请求")
  void testMediumConcurrency(VertxTestContext testContext) throws InterruptedException {
    runConcurrencyTest(500, testContext);
  }

  @Test
  @Order(4)
  @DisplayName("并发测试 - 1000并发请求")
  void testHighConcurrency(VertxTestContext testContext) throws InterruptedException {
    runConcurrencyTest(1000, testContext);
  }

  @Test
  @Order(5)
  @DisplayName("持续压力测试 - 5秒内最大吞吐量")
  void testSustainedLoad(Vertx vertx, VertxTestContext testContext) {
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);
    AtomicLong totalResponseTime = new AtomicLong(0);
    List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

    long testDuration = 5_000;
    long startTime = System.currentTimeMillis();
    AtomicInteger pendingRequests = new AtomicInteger(0);

    long timerId =
        vertx.setPeriodic(
            1,
            id -> {
              if (System.currentTimeMillis() - startTime >= testDuration) {
                vertx.cancelTimer(id);
                return;
              }

              if (pendingRequests.get() < 100) {
                pendingRequests.incrementAndGet();
                long requestStartTime = System.nanoTime();

                client
                    .request(HttpMethod.GET, PORT, "localhost", "/api/hello")
                    .compose(req -> req.send().compose(resp -> resp.body()))
                    .onComplete(
                        ar -> {
                          pendingRequests.decrementAndGet();
                          long responseTime = (System.nanoTime() - requestStartTime) / 1_000_000;

                          if (ar.succeeded()) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet(responseTime);
                            responseTimes.add(responseTime);
                          } else {
                            failCount.incrementAndGet();
                          }
                        });
              }
            });

    vertx.setTimer(
        testDuration + 2000,
        id -> {
          vertx.cancelTimer(timerId);

          int total = successCount.get();
          double avgResponseTime = total > 0 ? (double) totalResponseTime.get() / total : 0;
          double throughput = (total * 1000.0) / (System.currentTimeMillis() - startTime);

          List<Long> sortedTimes = new ArrayList<>(responseTimes);
          Collections.sort(sortedTimes);

          System.out.println("=== 持续压力测试结果 ===");
          System.out.println("成功请求: " + total);
          System.out.println("失败请求: " + failCount.get());
          System.out.println("吞吐量: " + String.format("%.2f", throughput) + " req/s");
          System.out.println("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
          System.out.println("P50: " + getPercentile(sortedTimes, 50) + "ms");
          System.out.println("P95: " + getPercentile(sortedTimes, 95) + "ms");
          System.out.println("P99: " + getPercentile(sortedTimes, 99) + "ms");

          assertTrue(total > 50, "至少应完成50个请求");
          testContext.completeNow();
        });
  }

  @Test
  @Order(6)
  @DisplayName("JSON序列化并发测试")
  void testJsonSerialization(VertxTestContext testContext) throws InterruptedException {
    int concurrency = 200;
    Checkpoint checkpoint = testContext.checkpoint(concurrency);
    AtomicInteger successCount = new AtomicInteger(0);
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < concurrency; i++) {
      client
          .request(HttpMethod.GET, PORT, "localhost", "/api/json")
          .compose(
              req ->
                  req.send()
                      .compose(
                          resp -> {
                            if (resp.statusCode() == 200) successCount.incrementAndGet();
                            return resp.body();
                          }))
          .onComplete(ar -> checkpoint.flag());
    }

    assertTrue(testContext.awaitCompletion(30, TimeUnit.SECONDS));

    System.out.println("=== JSON序列化并发测试结果 ===");
    System.out.println("成功请求: " + successCount.get());
    System.out.println(
        "吞吐量: "
            + String.format("%.2f", concurrency * 1000.0 / (System.currentTimeMillis() - startTime))
            + " req/s");
  }

  private void runConcurrencyTest(int concurrency, VertxTestContext testContext)
      throws InterruptedException {
    Checkpoint checkpoint = testContext.checkpoint(concurrency);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicLong totalResponseTime = new AtomicLong(0);
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < concurrency; i++) {
      long requestStartTime = System.nanoTime();

      client
          .request(HttpMethod.GET, PORT, "localhost", "/api/hello")
          .compose(
              req ->
                  req.send()
                      .compose(
                          resp -> {
                            totalResponseTime.addAndGet(
                                (System.nanoTime() - requestStartTime) / 1_000_000);
                            if (resp.statusCode() == 200) successCount.incrementAndGet();
                            return resp.body();
                          }))
          .onComplete(ar -> checkpoint.flag());
    }

    assertTrue(testContext.awaitCompletion(60, TimeUnit.SECONDS));

    int total = successCount.get();
    double avgResponseTime = total > 0 ? (double) totalResponseTime.get() / total : 0;
    double throughput = (total * 1000.0) / (System.currentTimeMillis() - startTime);

    System.out.println("=== " + concurrency + "并发测试结果 ===");
    System.out.println("成功请求: " + total);
    System.out.println("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
    System.out.println("吞吐量: " + String.format("%.2f", throughput) + " req/s");

    assertTrue(total > concurrency * 0.9, "成功率应大于90%");
  }

  private long getPercentile(List<Long> sortedList, int percentile) {
    if (sortedList.isEmpty()) return 0;
    int index =
        Math.max(
            0,
            Math.min(
                (int) Math.ceil((percentile / 100.0) * sortedList.size()) - 1,
                sortedList.size() - 1));
    return sortedList.get(index);
  }

  static class TestHttpServerVerticle extends AbstractVerticle {
    private HttpServer server;

    @Override
    public void start(Promise<Void> startPromise) {
      Router router = Router.router(vertx);

      router
          .get("/api/hello")
          .handler(
              ctx -> ctx.response().putHeader("content-type", "text/plain").end("Hello World"));

      router
          .get("/api/json")
          .handler(
              ctx -> {
                JsonObject response =
                    new JsonObject()
                        .put("status", "success")
                        .put("code", 200)
                        .put("message", "Test")
                        .put("timestamp", System.currentTimeMillis());
                ctx.response().putHeader("content-type", "application/json").end(response.encode());
              });

      router
          .get("/api/compute")
          .handler(
              ctx -> {
                long result = 0;
                for (int i = 0; i < 10000; i++) result += Math.sqrt(i);
                ctx.json(new JsonObject().put("result", result));
              });

      server = vertx.createHttpServer();
      server
          .requestHandler(router)
          .listen(PORT)
          .onSuccess(s -> startPromise.complete())
          .onFailure(startPromise::fail);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
      if (server != null) server.close().onComplete(ar -> stopPromise.complete());
      else stopPromise.complete();
    }
  }
}
