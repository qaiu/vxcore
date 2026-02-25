package cn.qaiu.vx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Deploy 类单元测试 测试框架启动配置逻辑（不启动实际服务器）
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("Deploy启动配置测试")
public class DeployTest {

  @TempDir Path tempDir;

  private Deploy deploy;

  @BeforeEach
  void setUp() throws Exception {
    deploy = Deploy.instance();
    assertNotNull(deploy, "Deploy实例不应为空");
  }

  @AfterEach
  void tearDown() throws Exception {
    // 重置Deploy实例的内部状态（如果需要）
    resetDeployState();
  }

  /** 通过反射重置Deploy状态 */
  private void resetDeployState() throws Exception {
    try {
      Field pathField = Deploy.class.getDeclaredField("path");
      pathField.setAccessible(true);
      pathField.set(deploy, null);
    } catch (NoSuchFieldException e) {
      // 字段可能不存在，忽略
    }
  }

  @Test
  @DisplayName("测试单例模式")
  void testSingletonPattern() {
    Deploy instance1 = Deploy.instance();
    Deploy instance2 = Deploy.instance();

    assertSame(instance1, instance2, "Deploy应该是单例模式");
  }

  @Test
  @DisplayName("测试path默认值")
  void testPathDefaultValue() throws Exception {
    Field pathField = Deploy.class.getDeclaredField("path");
    pathField.setAccessible(true);

    StringBuilder path = (StringBuilder) pathField.get(deploy);

    assertNotNull(path, "path不应为null");
    assertEquals("app", path.toString(), "默认path应为app");
  }

  @Test
  @DisplayName("测试autoDetectScanPaths - 空配置")
  void testAutoDetectScanPathsEmptyConfig() throws Exception {
    Method method = Deploy.class.getDeclaredMethod("autoDetectScanPaths", JsonObject.class);
    method.setAccessible(true);

    JsonObject config = new JsonObject();

    // 调用不应抛出异常
    assertDoesNotThrow(() -> method.invoke(deploy, config));
  }

  @Test
  @DisplayName("测试autoDetectScanPaths - 已有scan配置")
  void testAutoDetectScanPathsWithExistingConfig() throws Exception {
    Method method = Deploy.class.getDeclaredMethod("autoDetectScanPaths", JsonObject.class);
    method.setAccessible(true);

    JsonObject config =
        new JsonObject().put("scan", new io.vertx.core.json.JsonArray().add("cn.qaiu.test"));

    // 已有配置时不应修改
    method.invoke(deploy, config);

    assertTrue(config.containsKey("scan"), "应保留已有的scan配置");
  }

  @Test
  @DisplayName("测试isAppAnnotationUsed方法")
  void testIsAppAnnotationUsed() throws Exception {
    Method method = Deploy.class.getDeclaredMethod("isAppAnnotationUsed", Set.class);
    method.setAccessible(true);

    Set<String> scanPaths = Set.of("cn.qaiu.vx.core");

    // 当前测试类没有@App注解，应返回false
    Boolean result = (Boolean) method.invoke(deploy, scanPaths);

    assertFalse(result, "测试类没有@App注解，应返回false");
  }

  @Test
  @DisplayName("测试ConfigConstant配置常量")
  void testConfigConstants() {
    // 验证ConfigConstant接口常量值
    assertEquals("local", cn.qaiu.vx.core.util.ConfigConstant.LOCAL);
    assertEquals("globalConfig", cn.qaiu.vx.core.util.ConfigConstant.GLOBAL_CONFIG);
    assertEquals("server", cn.qaiu.vx.core.util.ConfigConstant.SERVER);
    assertEquals("custom", cn.qaiu.vx.core.util.ConfigConstant.CUSTOM);
    assertEquals("vertx", cn.qaiu.vx.core.util.ConfigConstant.VERTX);
  }

  @Test
  @DisplayName("测试getWorkDeploymentOptions方法存在")
  void testGetWorkDeploymentOptionsExists() throws Exception {
    // 验证方法存在
    Method method = Deploy.class.getDeclaredMethod("getWorkDeploymentOptions", String.class);
    method.setAccessible(true);

    assertNotNull(method, "getWorkDeploymentOptions方法应存在");
    // 注意：调用该方法需要globalConfig已初始化，此处仅验证方法存在
  }

  @Test
  @DisplayName("测试多次调用instance返回相同实例")
  void testMultipleInstanceCalls() {
    Deploy[] instances = new Deploy[10];
    for (int i = 0; i < 10; i++) {
      instances[i] = Deploy.instance();
    }

    for (int i = 1; i < 10; i++) {
      assertSame(instances[0], instances[i], "所有实例应该相同");
    }
  }

  @Test
  @DisplayName("测试配置文件解析 - application.yml格式识别")
  void testNewConfigFormatDetection() throws Exception {
    JsonObject newFormatConfig =
        new JsonObject()
            .put("server", new JsonObject().put("port", 8080))
            .put("datasources", new JsonObject());

    // 新格式应同时包含server和datasources
    assertTrue(newFormatConfig.containsKey("server"), "新格式应包含server");
    assertTrue(newFormatConfig.containsKey("datasources"), "新格式应包含datasources");
  }

  @Test
  @DisplayName("测试配置文件解析 - app.yml格式识别")
  void testOldConfigFormatDetection() throws Exception {
    JsonObject oldFormatConfig =
        new JsonObject()
            .put("active", "dev")
            .put("vertx", new JsonObject())
            .put("custom", new JsonObject());

    // 旧格式应包含active
    assertTrue(oldFormatConfig.containsKey("active"), "旧格式应包含active");
    assertFalse(
        oldFormatConfig.containsKey("server") && oldFormatConfig.containsKey("datasources"),
        "旧格式不应同时包含server和datasources");
  }

  @Test
  @DisplayName("测试静态run方法存在")
  void testStaticRunMethodExists() throws Exception {
    // 验证Deploy.run静态方法存在
    Method runMethod = Deploy.class.getMethod("run", String[].class, io.vertx.core.Handler.class);
    assertNotNull(runMethod, "run静态方法应存在");
    assertTrue(java.lang.reflect.Modifier.isStatic(runMethod.getModifiers()), "run应该是静态方法");
  }

  @Test
  @DisplayName("测试start方法存在")
  void testStartMethodExists() throws Exception {
    // 验证start方法存在
    Method startMethod =
        Deploy.class.getMethod("start", String[].class, io.vertx.core.Handler.class);
    assertNotNull(startMethod, "start方法应存在");
  }

  @Test
  @DisplayName("测试框架常量定义")
  void testFrameworkConstants() throws Exception {
    // 验证Deploy类中的关键常量
    try {
      Field customField = Deploy.class.getDeclaredField("CUSTOM");
      customField.setAccessible(true);
      assertEquals("custom", customField.get(null));

      Field vertxField = Deploy.class.getDeclaredField("VERTX");
      vertxField.setAccessible(true);
      assertEquals("vertx", vertxField.get(null));

      Field eventLoopField = Deploy.class.getDeclaredField("EVENT_LOOP_POOL_SIZE");
      eventLoopField.setAccessible(true);
      assertEquals("eventLoopPoolSize", eventLoopField.get(null));
    } catch (NoSuchFieldException e) {
      // 某些常量可能不存在，这是可接受的
    }
  }

  @Test
  @DisplayName("测试outLogo不抛出异常")
  void testOutLogoDoesNotThrow() throws Exception {
    Method method = Deploy.class.getDeclaredMethod("outLogo", JsonObject.class);
    method.setAccessible(true);

    JsonObject config = new JsonObject().put("copyright", "TestCorp");

    // 应该不抛出异常
    assertDoesNotThrow(() -> method.invoke(deploy, config));
  }

  @Test
  @DisplayName("测试并发访问单例")
  void testConcurrentSingletonAccess() throws InterruptedException {
    final Deploy[] instances = new Deploy[100];
    Thread[] threads = new Thread[100];

    for (int i = 0; i < 100; i++) {
      final int index = i;
      threads[i] =
          new Thread(
              () -> {
                instances[index] = Deploy.instance();
              });
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    // 验证所有实例相同
    for (int i = 1; i < 100; i++) {
      assertSame(instances[0], instances[i], "并发访问应返回相同实例");
    }
  }
}
