package cn.qaiu.vx.core.exception;

import io.vertx.ext.web.RoutingContext;

/**
 * 异常处理器接口
 * 
 * @author qaiu
 */
public interface ExceptionHandler {
    
    /**
     * 处理异常
     * 
     * @param throwable 异常
     * @param ctx 路由上下文
     * @return 是否处理了异常
     */
    boolean handle(Throwable throwable, RoutingContext ctx);
    
    /**
     * 获取支持的异常类型
     * 
     * @return 异常类型
     */
    Class<? extends Throwable> getSupportedExceptionType();
    
    /**
     * 获取优先级，数字越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}
