package cn.qaiu.vx.core.exception;

import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.ext.web.RoutingContext;

/**
 * 全局异常处理器接口
 * 
 * @author QAIU
 */
public interface ExceptionHandlerInterface {
    
    /**
     * 处理异常
     * 
     * @param exception 异常
     * @param ctx 路由上下文
     * @return 处理结果
     */
    JsonResult<?> handleException(Throwable exception, RoutingContext ctx);
}
