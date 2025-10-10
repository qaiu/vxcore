package cn.qaiu.vx.core.annotaions.config;

import java.lang.annotation.*;

/**
 * 配置类注解
 * 标记配置类，用于生成配置元数据
 * 
 * @author QAIU
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
    
    /**
     * 配置前缀
     * 
     * @return 配置前缀
     */
    String prefix() default "";
    
    /**
     * 配置类描述
     * 
     * @return 配置类描述
     */
    String description() default "";
    
    /**
     * 是否忽略未知属性
     * 
     * @return 是否忽略未知属性
     */
    boolean ignoreUnknownFields() default true;
    
    /**
     * 是否忽略无效字段
     * 
     * @return 是否忽略无效字段
     */
    boolean ignoreInvalidFields() default false;
}
