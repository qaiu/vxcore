package cn.qaiu.db.orm;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DdlSyncProvider 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("DDL同步提供者测试")
public class DdlSyncProviderTest {

  private DdlSyncProvider provider;

  @BeforeEach
  void setUp() {
    provider = new DdlSyncProvider();
  }

  @Test
  @DisplayName("测试提供者名称")
  void testGetName() {
    assertEquals("vxcore-ddl-sync-provider", provider.getName());
  }

  @Test
  @DisplayName("测试提供者优先级")
  void testGetPriority() {
    assertEquals(10, provider.getPriority());
  }

  @Test
  @DisplayName("测试shouldSync - null主类")
  void testShouldSyncNullMainClass() {
    assertFalse(provider.shouldSync(null, new JsonObject()));
  }

  @Test
  @DisplayName("测试shouldSync - 无注解的类")
  void testShouldSyncNoAnnotation() {
    assertFalse(provider.shouldSync(NoAnnotationClass.class, new JsonObject()));
  }

  @Test
  @DisplayName("测试shouldSync - 有EnableDdlSync注解")
  void testShouldSyncWithAnnotation() {
    assertTrue(provider.shouldSync(EnabledClass.class, new JsonObject()));
  }

  @Test
  @DisplayName("测试shouldSync - 策略为NONE")
  void testShouldSyncStrategyNone() {
    assertFalse(provider.shouldSync(DisabledStrategyClass.class, new JsonObject()));
  }

  @Test
  @DisplayName("测试shouldSync - autoExecute为false")
  void testShouldSyncAutoExecuteFalse() {
    assertFalse(provider.shouldSync(NoAutoExecuteClass.class, new JsonObject()));
  }

  @Test
  @DisplayName("测试DdlSyncStrategy枚举")
  void testDdlSyncStrategy() {
    assertEquals(5, DdlSyncStrategy.values().length);
    assertNotNull(DdlSyncStrategy.AUTO);
    assertNotNull(DdlSyncStrategy.CREATE);
    assertNotNull(DdlSyncStrategy.UPDATE);
    assertNotNull(DdlSyncStrategy.VALIDATE);
    assertNotNull(DdlSyncStrategy.NONE);
  }

  @Test
  @DisplayName("测试EnableDdlSync注解默认值")
  void testEnableDdlSyncDefaults() {
    EnableDdlSync annotation = EnabledClass.class.getAnnotation(EnableDdlSync.class);
    assertNotNull(annotation);
    assertEquals(DdlSyncStrategy.AUTO, annotation.strategy());
    assertEquals(0, annotation.entityPackages().length);
    assertTrue(annotation.autoExecute());
    assertTrue(annotation.failOnError(), "failOnError默认应为true");
    assertFalse(annotation.showDdl());
    assertEquals("", annotation.dataSource());
  }

  @Test
  @DisplayName("测试EnableDdlSync自定义值")
  void testEnableDdlSyncCustomValues() {
    EnableDdlSync annotation = CustomConfigClass.class.getAnnotation(EnableDdlSync.class);
    assertNotNull(annotation);
    assertEquals(DdlSyncStrategy.CREATE, annotation.strategy());
    assertArrayEquals(new String[] {"cn.qaiu.entity"}, annotation.entityPackages());
    assertTrue(annotation.failOnError());
    assertTrue(annotation.showDdl());
    assertEquals("secondary", annotation.dataSource());
  }

  // ============ 测试用辅助类 ============

  // 无注解的类
  static class NoAnnotationClass {}

  // 有EnableDdlSync注解的类
  @EnableDdlSync
  static class EnabledClass {}

  // 策略为NONE的类
  @EnableDdlSync(strategy = DdlSyncStrategy.NONE)
  static class DisabledStrategyClass {}

  // autoExecute为false的类
  @EnableDdlSync(autoExecute = false)
  static class NoAutoExecuteClass {}

  // 自定义配置的类
  @EnableDdlSync(
      strategy = DdlSyncStrategy.CREATE,
      entityPackages = {"cn.qaiu.entity"},
      failOnError = true,
      showDdl = true,
      dataSource = "secondary")
  static class CustomConfigClass {}
}
