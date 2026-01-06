package cn.qaiu.vx.core.aop;

import java.lang.reflect.Method;

/**
 * 连接点接口
 * <p>
 * 表示程序执行过程中的一个点，通常是方法执行。
 * 提供对当前执行上下文的访问，包括目标对象、方法签名、方法参数等。
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
public interface JoinPoint {

    /**
     * 获取目标对象
     *
     * @return 被拦截方法所属的对象实例
     */
    Object getTarget();

    /**
     * 获取目标方法
     *
     * @return 被拦截的方法
     */
    Method getMethod();

    /**
     * 获取方法参数
     *
     * @return 方法调用时传入的参数数组
     */
    Object[] getArgs();

    /**
     * 获取方法签名字符串
     *
     * @return 方法签名的字符串表示
     */
    default String getSignature() {
        Method method = getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    /**
     * 获取简短的方法签名
     *
     * @return 简短的方法签名（类名.方法名）
     */
    default String getShortSignature() {
        Method method = getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    /**
     * 获取目标类
     *
     * @return 目标对象的类
     */
    default Class<?> getTargetClass() {
        return getTarget().getClass();
    }
}
