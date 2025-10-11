package cn.qaiu.vx.core.annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器注解，用于标记Web控制器
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    /**
     * 控制器名称，默认为空
     * 如果为空，将使用类名首字母小写作为控制器名称
     * 
     * @return 控制器名称
     */
    String name() default "";
    
    /**
     * 基础路径，默认为空
     * 所有路由方法的前缀路径
     * 
     * @return 基础路径
     */
    String basePath() default "";
    
    /**
     * 是否启用CORS，默认false
     * 跨域资源共享支持
     * 
     * @return 是否启用CORS
     */
    boolean cors() default false;
    
    /**
     * 是否启用认证，默认false
     * 是否需要身份验证
     * 
     * @return 是否启用认证
     */
    boolean authenticated() default false;
}
