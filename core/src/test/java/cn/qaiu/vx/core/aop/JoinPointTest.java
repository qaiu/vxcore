package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JoinPoint 和 ProceedingJoinPoint 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("连接点测试")
public class JoinPointTest {

  private TestService target;
  private Method testMethod;
  private Object[] args;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    target = new TestService();
    testMethod = TestService.class.getDeclaredMethod("process", String.class, Integer.class);
    args = new Object[] {"hello", 42};
  }

  @Test
  @DisplayName("测试获取目标对象")
  void testGetTarget() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);
    assertSame(target, jp.getTarget(), "应该返回目标对象");
  }

  @Test
  @DisplayName("测试获取目标方法")
  void testGetMethod() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);
    assertSame(testMethod, jp.getMethod(), "应该返回目标方法");
  }

  @Test
  @DisplayName("测试获取方法参数")
  void testGetArgs() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);

    Object[] returnedArgs = jp.getArgs();
    assertArrayEquals(args, returnedArgs, "应该返回方法参数");

    // 验证返回的是副本
    returnedArgs[0] = "modified";
    assertArrayEquals(new Object[] {"hello", 42}, jp.getArgs(), "参数数组应该是副本");
  }

  @Test
  @DisplayName("测试获取方法签名")
  void testGetSignature() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);

    String signature = jp.getSignature();
    assertTrue(signature.contains("TestService"), "签名应该包含类名");
    assertTrue(signature.contains("process"), "签名应该包含方法名");
  }

  @Test
  @DisplayName("测试获取简短签名")
  void testGetShortSignature() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);

    String shortSignature = jp.getShortSignature();
    assertEquals("TestService.process", shortSignature, "简短签名格式应该正确");
  }

  @Test
  @DisplayName("测试获取目标类")
  void testGetTargetClass() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);
    assertEquals(TestService.class, jp.getTargetClass(), "应该返回目标类");
  }

  @Test
  @DisplayName("测试 proceed 执行目标方法")
  void testProceed() throws Throwable {
    AtomicBoolean called = new AtomicBoolean(false);

    DefaultJoinPoint jp =
        new DefaultJoinPoint(
            target,
            testMethod,
            args,
            () -> {
              called.set(true);
              return "result";
            });

    Object result = jp.proceed();

    assertTrue(called.get(), "目标方法应该被调用");
    assertEquals("result", result, "应该返回目标方法的返回值");
  }

  @Test
  @DisplayName("测试 proceed 使用新参数")
  void testProceedWithNewArgs() throws Throwable {
    Object[] newArgs = new Object[] {"world", 100};

    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> "result");

    Object result = jp.proceed(newArgs);

    assertArrayEquals(newArgs, jp.getArgs(), "参数应该被更新");
  }

  @Test
  @DisplayName("测试空参数处理")
  void testNullArgs() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, null, () -> null);

    Object[] returnedArgs = jp.getArgs();
    assertNotNull(returnedArgs, "返回的参数数组不应该为null");
    assertEquals(0, returnedArgs.length, "参数数组应该为空");
  }

  @Test
  @DisplayName("测试 toString 方法")
  void testToString() {
    DefaultJoinPoint jp = new DefaultJoinPoint(target, testMethod, args, () -> null);

    String str = jp.toString();
    assertTrue(str.contains("JoinPoint"), "toString 应该包含类型标识");
    assertTrue(str.contains("process"), "toString 应该包含方法名");
  }

  // 测试服务类
  static class TestService {
    public String process(String input, Integer count) {
      return input + ":" + count;
    }
  }
}
