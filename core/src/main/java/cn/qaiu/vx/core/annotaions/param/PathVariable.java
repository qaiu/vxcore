package cn.qaiu.vx.core.annotaions.param;

import java.lang.annotation.*;

/**
 * 路径变量注解
 * 用于标记从URL路径中获取的变量
 * 
 * @author QAIU
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {
    
    /**
     * 变量名称，默认为方法参数名
     */
    String value() default "";
    
    /**
     * 是否必需，默认为true
     */
    boolean required() default true;
}