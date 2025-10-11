package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 组件注解，用于标记业务组件
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    /**
     * 组件名称，默认为空
     * 如果为空，将使用类名首字母小写作为组件名称
     * 
     * @return 组件名称
     */
    String name() default "";
    
    /**
     * 组件优先级，数值越小优先级越高
     * 用于控制组件的初始化顺序
     * 
     * @return 优先级数值
     */
    int priority() default 0;
    
    /**
     * 是否懒加载，默认false
     * true表示延迟初始化，false表示立即初始化
     * 
     * @return 是否懒加载
     */
    boolean lazy() default false;
}
