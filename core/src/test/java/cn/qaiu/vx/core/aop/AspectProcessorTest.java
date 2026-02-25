package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.aop.annotation.After;
import cn.qaiu.vx.core.aop.annotation.Aspect;
import cn.qaiu.vx.core.aop.annotation.Before;
import cn.qaiu.vx.core.aop.annotation.Order;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

/**
 * AspectProcessor 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("切面处理器测试")
public class AspectProcessorTest {

  private AspectProcessor processor;

  @BeforeEach
  void setUp() {
    processor = AspectProcessor.getInstance();
    AspectRegistry.getInstance().clear();
    processor.clearCache();
  }

  @AfterEach
  void tearDown() {
    AspectRegistry.getInstance().clear();
    processor.clearCache();
  }

  @Test
  @DisplayName("测试单例模式")
  void testSingleton() {
    AspectProcessor instance1 = AspectProcessor.getInstance();
    AspectProcessor instance2 = AspectProcessor.getInstance();
    assertSame(instance1, instance2, "应该返回同一个实例");
  }

  @Test
  @DisplayName("测试安装 Agent")
  void testInstallAgent() {
    // 尝试安装 Agent（在测试环境中可能成功或失败）
    processor.installAgent();
    // 不抛出异常就算成功
  }

  @Test
  @DisplayName("测试创建代理 - 无切面时返回原对象")
  void testCreateProxyWithoutAspects() {
    TargetService target = new TargetService();
    TargetService proxy = processor.createProxy(target);

    // 没有注册切面时，应该返回原对象
    assertSame(target, proxy, "没有匹配的切面时应该返回原对象");
  }

  @Test
  @DisplayName("测试获取拦截器链")
  void testGetInterceptorChain() throws NoSuchMethodException {
    // 注册一个切面
    AspectRegistry.getInstance().registerAspect(TestLoggingAspect.class);

    var method = TargetService.class.getDeclaredMethod("doSomething");
    InterceptorChain chain = processor.getInterceptorChain(method);

    assertNotNull(chain, "应该返回拦截器链");
  }

  @Test
  @DisplayName("测试清空缓存")
  void testClearCache() {
    // 这个测试主要验证不会抛出异常
    processor.clearCache();
  }

  @Test
  @DisplayName("测试拦截器链缓存")
  void testInterceptorChainCache() throws NoSuchMethodException {
    AspectRegistry.getInstance().registerAspect(TestLoggingAspect.class);

    var method = TargetService.class.getDeclaredMethod("doSomething");

    InterceptorChain chain1 = processor.getInterceptorChain(method);
    InterceptorChain chain2 = processor.getInterceptorChain(method);

    assertSame(chain1, chain2, "相同方法应该返回缓存的拦截器链");
  }

  // 测试目标类
  static class TargetService {
    public String doSomething() {
      return "done";
    }

    @Loggable
    public String loggableMethod() {
      return "logged";
    }
  }

  // 测试切面
  @Aspect
  @Order(10)
  public static class TestLoggingAspect {
    public static final List<String> logs = new ArrayList<>();

    @Before("execution(* cn.qaiu.vx.core.aop.AspectProcessorTest.TargetService.*(..))")
    public void logBefore(JoinPoint jp) {
      logs.add("before:" + jp.getShortSignature());
    }

    @After("execution(* cn.qaiu.vx.core.aop.AspectProcessorTest.TargetService.*(..))")
    public void logAfter(JoinPoint jp) {
      logs.add("after:" + jp.getShortSignature());
    }
  }
}
