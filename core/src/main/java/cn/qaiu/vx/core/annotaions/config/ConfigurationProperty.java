package cn.qaiu.vx.core.annotaions.config;

import java.lang.annotation.*;

/**
 * 配置属性注解
 * 用于标记配置属性，提供元数据信息
 * 
 * @author QAIU
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperty {
    
    /**
     * 配置键名
     * 
     * @return 配置键名
     */
    String value() default "";
    
    /**
     * 配置描述
     * 
     * @return 配置描述
     */
    String description() default "";
    
    /**
     * 默认值
     * 
     * @return 默认值
     */
    String defaultValue() default "";
    
    /**
     * 是否必需
     * 
     * @return 是否必需
     */
    boolean required() default false;
    
    /**
     * 配置类型
     * 
     * @return 配置类型
     */
    PropertyType type() default PropertyType.STRING;
    
    /**
     * 允许的值（用于枚举类型）
     * 
     * @return 允许的值
     */
    String[] allowedValues() default {};
    
    /**
     * 最小值（用于数值类型）
     * 
     * @return 最小值
     */
    double minValue() default Double.MIN_VALUE;
    
    /**
     * 最大值（用于数值类型）
     * 
     * @return 最大值
     */
    double maxValue() default Double.MAX_VALUE;
    
    /**
     * 配置组
     * 
     * @return 配置组
     */
    String group() default "default";
    
    /**
     * 配置属性类型枚举
     */
    enum PropertyType {
        STRING, INTEGER, LONG, DOUBLE, BOOLEAN, ARRAY, OBJECT, ENUM
    }
}
