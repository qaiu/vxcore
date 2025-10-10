package cn.qaiu.vx.core.annotaions.websocket;

import java.lang.annotation.*;

/**
 * WebSocket连接关闭事件注解
 * 标记WebSocket连接关闭时调用的方法
 * 
 * @author QAIU
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnClose {
    
    /**
     * 事件描述
     * 
     * @return 描述信息
     */
    String value() default "";
    
    /**
     * 是否异步处理
     * 
     * @return 是否异步
     */
    boolean async() default false;
}
