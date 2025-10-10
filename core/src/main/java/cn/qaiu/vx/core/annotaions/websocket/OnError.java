package cn.qaiu.vx.core.annotaions.websocket;

import java.lang.annotation.*;

/**
 * WebSocket错误事件注解
 * 标记WebSocket发生错误时调用的方法
 * 
 * @author QAIU
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnError {
    
    /**
     * 错误类型
     * 
     * @return 错误类型
     */
    Class<? extends Throwable>[] value() default {Throwable.class};
    
    /**
     * 事件描述
     * 
     * @return 描述信息
     */
    String description() default "";
    
    /**
     * 是否异步处理
     * 
     * @return 是否异步
     */
    boolean async() default false;
}
