package cn.qaiu.vx.core.annotaions.exception;

import java.lang.annotation.*;

/**
 * 控制器异常处理器注解
 * 标记在控制器类上，表示该类包含局部异常处理方法
 * 
 * @author QAIU
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerAdvice {
    
    /**
     * 异常处理器的优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    int order() default 100;
}
