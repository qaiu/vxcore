package cn.qaiu.vx.core.aop;

/**
 * 提供声明式 AOP（面向切面编程）支持。
 *
 * <h2>核心组件</h2>
 * <ul>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.Aspect} - 切面类标记注解</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.Before} - 前置通知</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.After} - 后置通知</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.Around} - 环绕通知</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.AfterReturning} - 返回后通知</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.AfterThrowing} - 异常后通知</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.Pointcut} - 切点定义</li>
 *   <li>{@link cn.qaiu.vx.core.aop.annotation.Order} - 切面优先级</li>
 * </ul>
 *
 * <h2>内置切面</h2>
 * <ul>
 *   <li>{@link cn.qaiu.vx.core.aop.Loggable} - 日志记录注解</li>
 *   <li>{@link cn.qaiu.vx.core.aop.Timed} - 方法计时注解</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>
 * // 1. 定义切面
 * &#64;Aspect
 * &#64;Order(10)
 * public class SecurityAspect {
 *
 *     &#64;Before("execution(* cn.qaiu.*.service.*.*(..))")
 *     public void checkPermission(JoinPoint jp) {
 *         // 权限检查逻辑
 *     }
 * }
 *
 * // 2. 使用简化注解
 * &#64;Service
 * public class UserService {
 *
 *     &#64;Loggable(includeArgs = true)
 *     &#64;Timed(slowThreshold = 1000)
 *     public Future&lt;User&gt; findById(Long id) {
 *         // 业务逻辑
 *     }
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
