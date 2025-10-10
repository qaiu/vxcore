package cn.qaiu.vx.core.annotaions.websocket;

import java.lang.annotation.*;

/**
 * WebSocket处理器注解
 * 标记WebSocket处理器类，类似Spring的@WebSocketHandler
 * 
 * @author QAIU
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketHandler {
    
    /**
     * WebSocket路径
     * 
     * @return WebSocket路径
     */
    String value() default "";
    
    /**
     * 是否启用
     * 
     * @return 是否启用
     */
    boolean enabled() default true;
    
    /**
     * 处理器描述
     * 
     * @return 描述信息
     */
    String description() default "";
    
    /**
     * 注册顺序
     * 
     * @return 注册顺序
     */
    int order() default 0;
}
