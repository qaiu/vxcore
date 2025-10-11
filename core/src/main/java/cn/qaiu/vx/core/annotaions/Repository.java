package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仓储层注解，用于标记数据仓储对象
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {
    /**
     * 仓储名称，默认为空
     * 如果为空，将使用类名首字母小写作为仓储名称
     * 
     * @return 仓储名称
     */
    String name() default "";
    
    /**
     * 数据源名称，默认为default
     * 指定使用的数据源
     * 
     * @return 数据源名称
     */
    String datasource() default "default";
    
    /**
     * 是否启用事务，默认true
     * 控制是否自动管理事务
     * 
     * @return 是否启用事务
     */
    boolean transactional() default true;
}
