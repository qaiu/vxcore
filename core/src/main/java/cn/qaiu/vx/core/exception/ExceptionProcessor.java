package cn.qaiu.vx.core.exception;

import io.vertx.ext.web.RoutingContext;

/**
 * 异常处理器接口
 * 
 * @author QAIU
 */
public interface ExceptionProcessor {
    
    /**
     * 处理异常
     * 
     * @param ctx 路由上下文
     * @param throwable 异常对象
     * @return 是否处理了异常，true表示已处理，false表示继续寻找其他处理器
     */
    boolean handleException(RoutingContext ctx, Throwable throwable);
    
    /**
     * 获取处理器优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    int getOrder();
    
    /**
     * 检查是否支持处理指定类型的异常
     * 
     * @param throwable 异常对象
     * @return 是否支持
     */
    boolean supports(Throwable throwable);
}
