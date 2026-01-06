package cn.qaiu.vx.core.aop;

/**
 * 可执行连接点接口
 * <p>
 * 扩展自 {@link JoinPoint}，用于环绕通知 {@code @Around}。
 * 提供执行目标方法的能力，允许在方法执行前后添加自定义逻辑。
 * </p>
 *
 * <p>
 * 示例用法:
 * </p>
 * <pre>
 * &#64;Around("execution(* cn.qaiu.*.service.*.*(..))")
 * public Object around(ProceedingJoinPoint pjp) throws Throwable {
 *     // 前置逻辑
 *     long start = System.currentTimeMillis();
 *     
 *     try {
 *         // 执行目标方法
 *         Object result = pjp.proceed();
 *         
 *         // 后置逻辑
 *         log.info("执行耗时: {}ms", System.currentTimeMillis() - start);
 *         return result;
 *     } catch (Throwable e) {
 *         // 异常处理
 *         log.error("方法执行异常", e);
 *         throw e;
 *     }
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * 执行目标方法
     * <p>
     * 使用原始参数执行目标方法。如果不调用此方法，目标方法将不会执行。
     * </p>
     *
     * @return 目标方法的返回值
     * @throws Throwable 目标方法可能抛出的任何异常
     */
    Object proceed() throws Throwable;

    /**
     * 使用指定参数执行目标方法
     * <p>
     * 允许修改传递给目标方法的参数。参数数组的长度和类型必须与目标方法的参数列表匹配。
     * </p>
     *
     * @param args 新的方法参数
     * @return 目标方法的返回值
     * @throws Throwable 目标方法可能抛出的任何异常
     */
    Object proceed(Object[] args) throws Throwable;
}
