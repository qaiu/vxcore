package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务实现层注解(XXServiceImpl)
 * <br>Create date 2021/8/25 15:57
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * 服务名称，默认为空
     * 如果为空，将使用类名首字母小写作为服务名称
     * 
     * @return 服务名称
     */
    String name() default "";
}
