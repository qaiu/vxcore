package cn.qaiu.db.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源注解
 * 用于标识方法或类使用的数据源
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    
    /**
     * 数据源名称
     * 对应配置文件中的数据源配置
     */
    String value() default "default";
}
