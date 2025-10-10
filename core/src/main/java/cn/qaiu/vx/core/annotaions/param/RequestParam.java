package cn.qaiu.vx.core.annotaions.param;

import java.lang.annotation.*;

/**
 * 请求参数注解
 * 用于标记从请求参数中获取的值
 * 
 * @author QAIU
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    
    /**
     * 参数名称，默认为方法参数名
     */
    String value() default "";
    
    /**
     * 是否必需，默认为false
     */
    boolean required() default false;
    
    /**
     * 默认值
     */
    String defaultValue() default "";
}