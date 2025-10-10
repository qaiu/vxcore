package cn.qaiu.vx.core.annotaions.param;

import java.lang.annotation.*;

/**
 * 请求体注解
 * 用于标记从请求体中获取的JSON数据
 * 
 * @author QAIU
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    
    /**
     * 是否必需，默认为true
     */
    boolean required() default true;
}