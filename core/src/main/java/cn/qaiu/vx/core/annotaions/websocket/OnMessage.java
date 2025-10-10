package cn.qaiu.vx.core.annotaions.websocket;

import java.lang.annotation.*;

/**
 * WebSocket消息接收事件注解
 * 标记WebSocket消息接收时调用的方法
 * 
 * @author QAIU
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {
    
    /**
     * 消息类型
     * 
     * @return 消息类型
     */
    MessageType type() default MessageType.TEXT;
    
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
    
    /**
     * 消息类型枚举
     */
    enum MessageType {
        TEXT,    // 文本消息
        BINARY,  // 二进制消息
        PING,    // Ping消息
        PONG     // Pong消息
    }
}
