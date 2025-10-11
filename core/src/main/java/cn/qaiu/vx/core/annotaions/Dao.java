package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DAO层注解，用于标记数据访问对象
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dao {
    /**
     * DAO名称，默认为空
     * 如果为空，将使用类名首字母小写作为DAO名称
     * 
     * @return DAO名称
     */
    String name() default "";
    
    /**
     * 是否启用缓存，默认false
     * 启用后查询结果会被缓存
     * 
     * @return 是否启用缓存
     */
    boolean cacheable() default false;
}
