package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.aop.annotation.Aspect;
import cn.qaiu.vx.core.aop.annotation.Before;
import cn.qaiu.vx.core.aop.annotation.Order;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AspectRegistry 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("切面注册表测试")
public class AspectRegistryTest {

  private AspectRegistry registry;

  @BeforeEach
  void setUp() {
    registry = AspectRegistry.getInstance();
    registry.clear();
  }

  @AfterEach
  void tearDown() {
    registry.clear();
  }

  @Test
  @DisplayName("测试单例模式")
  void testSingleton() {
    AspectRegistry instance1 = AspectRegistry.getInstance();
    AspectRegistry instance2 = AspectRegistry.getInstance();
    assertSame(instance1, instance2, "应该返回同一个实例");
  }

  @Test
  @DisplayName("测试注册切面类")
  void testRegisterAspectClass() {
    registry.registerAspect(TestAspect.class);

    assertEquals(1, registry.getAspectCount(), "应该有1个注册的切面");
    assertNotNull(registry.getAspect(TestAspect.class), "应该能获取到注册的切面");
  }

  @Test
  @DisplayName("测试注册切面实例")
  void testRegisterAspectInstance() {
    TestAspect aspect = new TestAspect();
    registry.registerAspect(aspect);

    assertEquals(1, registry.getAspectCount(), "应该有1个注册的切面");
    AspectMetadata metadata = registry.getAspect(TestAspect.class);
    assertNotNull(metadata);
    assertSame(aspect, metadata.getAspectInstance(), "应该使用提供的实例");
  }

  @Test
  @DisplayName("测试重复注册切面被忽略")
  void testDuplicateRegistrationIgnored() {
    registry.registerAspect(TestAspect.class);
    registry.registerAspect(TestAspect.class);

    assertEquals(1, registry.getAspectCount(), "重复注册应该被忽略");
  }

  @Test
  @DisplayName("测试切面按优先级排序")
  void testAspectsSortedByOrder() {
    registry.registerAspect(LowPriorityAspect.class);
    registry.registerAspect(HighPriorityAspect.class);
    registry.registerAspect(MediumPriorityAspect.class);

    List<AspectMetadata> sorted = registry.getSortedAspects();

    assertEquals(3, sorted.size());
    assertEquals(HighPriorityAspect.class, sorted.get(0).getAspectClass(), "优先级最高的应该在前");
    assertEquals(MediumPriorityAspect.class, sorted.get(1).getAspectClass());
    assertEquals(LowPriorityAspect.class, sorted.get(2).getAspectClass(), "优先级最低的应该在后");
  }

  @Test
  @DisplayName("测试清空注册表")
  void testClear() {
    registry.registerAspect(TestAspect.class);
    assertEquals(1, registry.getAspectCount());

    registry.clear();

    assertEquals(0, registry.getAspectCount(), "清空后应该没有切面");
    assertFalse(registry.isInitialized(), "清空后应该标记为未初始化");
  }

  @Test
  @DisplayName("测试获取不存在的切面返回null")
  void testGetNonExistentAspect() {
    assertNull(registry.getAspect(TestAspect.class), "不存在的切面应该返回null");
  }

  @Test
  @DisplayName("测试注册非切面类被忽略")
  void testRegisterNonAspectClassIgnored() {
    registry.registerAspect(new NotAnAspect());
    assertEquals(0, registry.getAspectCount(), "非切面类不应该被注册");
  }

  // 测试切面类
  @Aspect
  static class TestAspect {
    @Before("execution(* cn.qaiu..*.*(..))")
    public void beforeAdvice(JoinPoint jp) {}
  }

  @Aspect
  @Order(1)
  static class HighPriorityAspect {}

  @Aspect
  @Order(50)
  static class MediumPriorityAspect {}

  @Aspect
  @Order(100)
  static class LowPriorityAspect {}

  // 非切面类
  static class NotAnAspect {}
}
