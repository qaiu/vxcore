package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用启动类注解
 * 用于标记应用的主启动类，可以配置扫描包路径
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface App {
    
    /**
     * 基础扫描包路径
     * 如果配置了此属性，将使用此路径作为扫描包，优先级高于自动检测
     * 
     * @return 扫描包路径，支持多个包用逗号分隔
     */
    String baseScanPackage() default "";
    
    /**
     * 应用名称
     * 
     * @return 应用名称
     */
    String name() default "";
    
    /**
     * 应用版本
     * 
     * @return 应用版本
     */
    String version() default "1.0.0";
    
    /**
     * 应用描述
     * 
     * @return 应用描述
     */
    String description() default "";
}
