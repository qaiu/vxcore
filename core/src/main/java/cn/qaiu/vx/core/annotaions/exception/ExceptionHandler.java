package cn.qaiu.vx.core.annotaions.exception;

import java.lang.annotation.*;

/**
 * 异常处理器注解
 * 用于在Controller中定义局部异常处理
 * 
 * @author qaiu
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    
    /**
     * 要处理的异常类型
     */
    Class<? extends Throwable>[] value() default {};
    
    /**
     * 优先级，数字越小优先级越高
     */
    int priority() default 100;
}