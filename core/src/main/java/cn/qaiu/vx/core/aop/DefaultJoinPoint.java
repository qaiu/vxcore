package cn.qaiu.vx.core.aop;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 默认连接点实现
 *
 * <p>实现 {@link ProceedingJoinPoint} 接口，持有方法执行的所有上下文信息。
 *
 * @author qaiu
 * @since 1.0.0
 */
public class DefaultJoinPoint implements ProceedingJoinPoint {

  private final Object target;
  private final Method method;
  private Object[] args;
  private final Callable<Object> targetInvoker;

  /**
   * 构造函数
   *
   * @param target 目标对象
   * @param method 目标方法
   * @param args 方法参数
   * @param targetInvoker 目标方法调用器
   */
  public DefaultJoinPoint(
      Object target, Method method, Object[] args, Callable<Object> targetInvoker) {
    this.target = target;
    this.method = method;
    this.args = args != null ? args.clone() : new Object[0];
    this.targetInvoker = targetInvoker;
  }

  @Override
  public Object getTarget() {
    return target;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public Object[] getArgs() {
    return args.clone();
  }

  @Override
  public Object proceed() throws Throwable {
    try {
      return targetInvoker.call();
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public Object proceed(Object[] args) throws Throwable {
    this.args = args != null ? args.clone() : new Object[0];
    return proceed();
  }

  @Override
  public String toString() {
    return "JoinPoint{"
        + "signature="
        + getSignature()
        + ", args="
        + java.util.Arrays.toString(args)
        + '}';
  }
}
