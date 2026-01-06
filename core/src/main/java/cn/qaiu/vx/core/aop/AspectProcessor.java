package cn.qaiu.vx.core.aop;

import cn.qaiu.vx.core.aop.AspectMetadata.AdviceMetadata;
import cn.qaiu.vx.core.aop.AspectMetadata.AdviceType;
import io.vertx.core.Future;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 切面处理器
 * <p>
 * 使用 Byte Buddy 实现动态代理，将切面逻辑织入目标类。
 * 支持两种模式：
 * <ul>
 *   <li>代理模式：创建目标类的子类代理</li>
 *   <li>重定义模式：使用 Java Agent 重新定义已加载的类（需要 byte-buddy-agent）</li>
 * </ul>
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
public class AspectProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectProcessor.class);

    private static final AspectProcessor INSTANCE = new AspectProcessor();

    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    private final Map<Method, InterceptorChain> interceptorChainCache = new ConcurrentHashMap<>();
    private volatile boolean agentInstalled = false;

    private AspectProcessor() {
    }

    public static AspectProcessor getInstance() {
        return INSTANCE;
    }

    /**
     * 安装 Byte Buddy Agent
     * <p>
     * 必须在任何类重定义操作之前调用。
     * 可以在应用启动时调用，或通过 -javaagent 参数启动。
     * </p>
     */
    public synchronized void installAgent() {
        if (agentInstalled) {
            return;
        }
        try {
            ByteBuddyAgent.install();
            agentInstalled = true;
            LOGGER.info("Byte Buddy Agent 安装成功");
        } catch (Exception e) {
            LOGGER.warn("Byte Buddy Agent 安装失败，将使用代理模式: {}", e.getMessage());
        }
    }

    /**
     * 检查 Agent 是否已安装
     *
     * @return 如果已安装返回 true
     */
    public boolean isAgentInstalled() {
        return agentInstalled;
    }

    /**
     * 为目标类创建代理对象
     *
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> targetClass) {
        return (T) proxyCache.computeIfAbsent(targetClass, this::doCreateProxy);
    }

    /**
     * 为目标实例创建代理
     *
     * @param target 目标实例
     * @param <T>    目标类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T target) {
        Class<?> targetClass = target.getClass();
        
        // 检查是否需要代理
        if (!needsProxy(targetClass)) {
            return target;
        }

        try {
            DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new GeneralInterceptor(target)))
                    .make();

            Class<?> proxyClass = unloaded
                    .load(targetClass.getClassLoader())
                    .getLoaded();

            return (T) proxyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("创建代理失败: {}", targetClass.getName(), e);
            return target;
        }
    }

    /**
     * 检查类是否需要代理
     */
    private boolean needsProxy(Class<?> targetClass) {
        List<AspectMetadata> aspects = AspectRegistry.getInstance().getSortedAspects();
        for (AspectMetadata aspect : aspects) {
            for (Method method : targetClass.getDeclaredMethods()) {
                if (!aspect.getMatchingAdvices(method).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object doCreateProxy(Class<?> targetClass) {
        try {
            DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(GeneralInterceptor.class))
                    .make();

            Class<?> proxyClass = unloaded
                    .load(targetClass.getClassLoader())
                    .getLoaded();

            return proxyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("创建代理失败: {}", targetClass.getName(), e);
            throw new RuntimeException("创建代理失败", e);
        }
    }

    /**
     * 使用 Agent 重定义类（运行时织入）
     * <p>
     * 需要先调用 {@link #installAgent()} 安装 Agent。
     * </p>
     *
     * @param targetClass 目标类
     */
    public void redefineClass(Class<?> targetClass) {
        if (!agentInstalled) {
            LOGGER.warn("Agent 未安装，无法重定义类: {}", targetClass.getName());
            return;
        }

        try {
            new ByteBuddy()
                    .redefine(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(GeneralInterceptor.class))
                    .make()
                    .load(targetClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            LOGGER.debug("重定义类成功: {}", targetClass.getName());
        } catch (Exception e) {
            LOGGER.error("重定义类失败: {}", targetClass.getName(), e);
        }
    }

    /**
     * 获取或创建方法的拦截器链
     */
    public InterceptorChain getInterceptorChain(Method method) {
        return interceptorChainCache.computeIfAbsent(method, this::buildInterceptorChain);
    }

    /**
     * 构建方法的拦截器链
     */
    private InterceptorChain buildInterceptorChain(Method method) {
        InterceptorChain chain = new InterceptorChain();
        List<AspectMetadata> aspects = AspectRegistry.getInstance().getSortedAspects();

        for (AspectMetadata aspect : aspects) {
            List<AdviceMetadata> advices = aspect.getMatchingAdvices(method);
            for (AdviceMetadata advice : advices) {
                chain.addInterceptor(createInterceptor(aspect, advice));
            }
        }

        return chain;
    }

    /**
     * 根据通知元数据创建拦截器
     */
    private MethodInterceptor createInterceptor(AspectMetadata aspect, AdviceMetadata advice) {
        return (target, method, args, targetInvoker) -> {
            Object aspectInstance = aspect.getAspectInstance();
            Method adviceMethod = advice.getAdviceMethod();
            JoinPoint joinPoint = new DefaultJoinPoint(target, method, args, targetInvoker);

            switch (advice.getType()) {
                case BEFORE:
                    invokeAdvice(aspectInstance, adviceMethod, joinPoint, null, null);
                    return targetInvoker.call();

                case AFTER:
                    try {
                        Object result = targetInvoker.call();
                        invokeAdvice(aspectInstance, adviceMethod, joinPoint, null, null);
                        return result;
                    } catch (Throwable e) {
                        invokeAdvice(aspectInstance, adviceMethod, joinPoint, null, null);
                        throw e;
                    }

                case AROUND:
                    return invokeAroundAdvice(aspectInstance, adviceMethod, (ProceedingJoinPoint) joinPoint);

                case AFTER_RETURNING:
                    Object result = targetInvoker.call();
                    // 处理异步结果
                    if (result instanceof Future) {
                        return ((Future<?>) result).map(r -> {
                            invokeAdvice(aspectInstance, adviceMethod, joinPoint, r, null);
                            return r;
                        });
                    }
                    invokeAdvice(aspectInstance, adviceMethod, joinPoint, result, null);
                    return result;

                case AFTER_THROWING:
                    try {
                        return targetInvoker.call();
                    } catch (Throwable e) {
                        invokeAdvice(aspectInstance, adviceMethod, joinPoint, null, e);
                        throw e;
                    }

                default:
                    return targetInvoker.call();
            }
        };
    }

    /**
     * 调用通知方法
     */
    private void invokeAdvice(Object aspectInstance, Method adviceMethod, 
                             JoinPoint joinPoint, Object result, Throwable exception) {
        try {
            Class<?>[] paramTypes = adviceMethod.getParameterTypes();
            Object[] adviceArgs = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                if (JoinPoint.class.isAssignableFrom(paramTypes[i])) {
                    adviceArgs[i] = joinPoint;
                } else if (Throwable.class.isAssignableFrom(paramTypes[i]) && exception != null) {
                    adviceArgs[i] = exception;
                } else if (result != null && paramTypes[i].isInstance(result)) {
                    adviceArgs[i] = result;
                } else {
                    adviceArgs[i] = null;
                }
            }

            adviceMethod.invoke(aspectInstance, adviceArgs);
        } catch (Exception e) {
            LOGGER.error("调用通知方法失败: {}", adviceMethod.getName(), e);
        }
    }

    /**
     * 调用环绕通知
     */
    private Object invokeAroundAdvice(Object aspectInstance, Method adviceMethod, 
                                      ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Class<?>[] paramTypes = adviceMethod.getParameterTypes();
            Object[] adviceArgs = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                if (ProceedingJoinPoint.class.isAssignableFrom(paramTypes[i])) {
                    adviceArgs[i] = joinPoint;
                } else if (JoinPoint.class.isAssignableFrom(paramTypes[i])) {
                    adviceArgs[i] = joinPoint;
                } else {
                    adviceArgs[i] = null;
                }
            }

            return adviceMethod.invoke(aspectInstance, adviceArgs);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * 清空代理缓存
     */
    public void clearCache() {
        proxyCache.clear();
        interceptorChainCache.clear();
    }

    /**
     * 通用拦截器
     * <p>
     * 由 Byte Buddy 代理类使用，负责调用拦截器链
     * </p>
     */
    public static class GeneralInterceptor {

        private final Object target;

        public GeneralInterceptor() {
            this.target = null;
        }

        public GeneralInterceptor(Object target) {
            this.target = target;
        }

        @RuntimeType
        public Object intercept(@This Object self,
                               @Origin Method method,
                               @AllArguments Object[] args,
                               @SuperCall Callable<Object> superCall) throws Throwable {
            
            Object actualTarget = target != null ? target : self;
            InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);

            if (chain.isEmpty()) {
                return superCall.call();
            }

            return chain.intercept(actualTarget, method, args, superCall);
        }
    }
}
