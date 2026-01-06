package cn.qaiu.vx.core.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 拦截器链
 * <p>
 * 管理和执行一系列方法拦截器，按顺序调用每个拦截器形成责任链。
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
public class InterceptorChain implements MethodInterceptor {

    private final List<MethodInterceptor> interceptors;

    public InterceptorChain() {
        this.interceptors = new ArrayList<>();
    }

    public InterceptorChain(List<MethodInterceptor> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 要添加的拦截器
     * @return this，支持链式调用
     */
    public InterceptorChain addInterceptor(MethodInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    /**
     * 添加多个拦截器
     *
     * @param interceptors 要添加的拦截器列表
     * @return this，支持链式调用
     */
    public InterceptorChain addInterceptors(List<MethodInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
        return this;
    }

    /**
     * 获取所有拦截器
     *
     * @return 拦截器的不可变列表
     */
    public List<MethodInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    /**
     * 是否为空
     *
     * @return 如果没有拦截器返回true
     */
    public boolean isEmpty() {
        return interceptors.isEmpty();
    }

    /**
     * 获取拦截器数量
     *
     * @return 拦截器数量
     */
    public int size() {
        return interceptors.size();
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, Callable<Object> targetInvoker) throws Throwable {
        if (interceptors.isEmpty()) {
            return targetInvoker.call();
        }
        return new ChainExecution(target, method, args, targetInvoker, interceptors).proceed();
    }

    /**
     * 链式执行器
     * <p>
     * 负责按顺序执行拦截器链中的每个拦截器
     * </p>
     */
    private static class ChainExecution {
        private final Object target;
        private final Method method;
        private final Object[] args;
        private final Callable<Object> targetInvoker;
        private final List<MethodInterceptor> interceptors;
        private int currentIndex = 0;

        ChainExecution(Object target, Method method, Object[] args, 
                      Callable<Object> targetInvoker, List<MethodInterceptor> interceptors) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.targetInvoker = targetInvoker;
            this.interceptors = interceptors;
        }

        Object proceed() throws Throwable {
            if (currentIndex >= interceptors.size()) {
                // 所有拦截器都执行完了，调用目标方法
                return targetInvoker.call();
            }

            MethodInterceptor interceptor = interceptors.get(currentIndex++);
            // 创建一个新的 Callable，用于调用链中的下一个拦截器
            Callable<Object> next = () -> {
                try {
                    return proceed();
                } catch (Exception e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
            return interceptor.intercept(target, method, args, next);
        }
    }
}
