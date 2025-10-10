package cn.qaiu.vx.core.exception;

import cn.qaiu.vx.core.annotaions.exception.GlobalExceptionHandler;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认全局异常处理器
 * 
 * @author QAIU
 */
@cn.qaiu.vx.core.annotaions.exception.GlobalExceptionHandler(order = 100)
public class DefaultExceptionHandler implements ExceptionHandlerInterface {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    
    @Override
    public JsonResult<?> handleException(Throwable exception, RoutingContext ctx) {
        LOGGER.error("Default exception handler processing: {}", exception.getMessage(), exception);
        
        // 记录异常详情
        logExceptionDetails(exception, ctx);
        
        // 根据异常类型返回不同的错误响应
        if (exception instanceof BusinessException) {
            BusinessException be = (BusinessException) exception;
            return JsonResult.error("业务错误: " + be.getMessage(), 400);
        } else if (exception instanceof ValidationException) {
            ValidationException ve = (ValidationException) exception;
            return JsonResult.error("参数验证失败: " + ve.getMessage(), 400);
        } else if (exception instanceof SystemException) {
            SystemException se = (SystemException) exception;
            return JsonResult.error("系统错误: " + se.getMessage(), 500);
        } else if (exception instanceof IllegalArgumentException) {
            return JsonResult.error("参数错误: " + exception.getMessage(), 400);
        } else if (exception instanceof NullPointerException) {
            return JsonResult.error("系统内部错误: 空指针异常", 500);
        } else if (exception instanceof RuntimeException) {
            return JsonResult.error("运行时错误: " + exception.getMessage(), 500);
        } else {
            return JsonResult.error("服务器内部错误", 500);
        }
    }
    
    /**
     * 记录异常详情
     */
    private void logExceptionDetails(Throwable exception, RoutingContext ctx) {
        LOGGER.error("Exception occurred in request: {} {}", 
                ctx.request().method(), ctx.request().uri());
        LOGGER.error("Exception type: {}", exception.getClass().getSimpleName());
        LOGGER.error("Exception message: {}", exception.getMessage());
        
        if (ctx.request().headers() != null) {
            LOGGER.error("Request headers: {}", ctx.request().headers().names());
        }
        
        if (ctx.request().params() != null) {
            LOGGER.error("Request params: {}", ctx.request().params().names());
        }
        
        // 记录堆栈跟踪
        LOGGER.error("Stack trace:", exception);
    }
}