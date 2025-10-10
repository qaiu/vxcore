package cn.qaiu.vx.core.annotaions.exception;

import java.lang.annotation.*;

/**
 * 全局异常处理器注解
 * 标记在类上，表示该类是全局异常处理器
 * 
 * @author QAIU
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalExceptionHandler {
    
    /**
     * 异常处理器的优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    int order() default 0;
}
