package cn.qaiu.vx.core.aop;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 方法拦截器接口
 *
 * <p>定义方法拦截的核心逻辑，所有切面通知最终都会通过此接口执行。
 *
 * @author qaiu
 * @since 1.0.0
 */
@FunctionalInterface
public interface MethodInterceptor {

  /**
   * 拦截方法执行
   *
   * @param target 目标对象
   * @param method 目标方法
   * @param args 方法参数
   * @param targetInvoker 原始方法调用器
   * @return 方法返回值（可能被修改）
   * @throws Throwable 执行过程中的异常
   */
  Object intercept(Object target, Method method, Object[] args, Callable<Object> targetInvoker)
      throws Throwable;
}
