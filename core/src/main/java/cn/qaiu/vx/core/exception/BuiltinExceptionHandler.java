package cn.qaiu.vx.core.exception;

import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内置异常处理器
 * 提供常见的异常处理逻辑
 * 
 * @author QAIU
 */
public class BuiltinExceptionHandler implements ExceptionHandlerInterface {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltinExceptionHandler.class);
    
    @Override
    public JsonResult<?> handleException(Throwable exception, RoutingContext ctx) {
        LOGGER.error("Handling exception: {}", exception.getMessage(), exception);
        
        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception, ctx);
        } else if (exception instanceof ValidationException) {
            return handleValidationException((ValidationException) exception, ctx);
        } else if (exception instanceof SystemException) {
            return handleSystemException((SystemException) exception, ctx);
        } else if (exception instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception, ctx);
        } else if (exception instanceof NullPointerException) {
            return handleNullPointerException((NullPointerException) exception, ctx);
        } else {
            return handleUnknownException(exception, ctx);
        }
    }
    
    /**
     * 处理业务异常
     */
    private JsonResult<?> handleBusinessException(BusinessException exception, RoutingContext ctx) {
        return JsonResult.error("业务错误: " + exception.getMessage(), 400);
    }
    
    /**
     * 处理验证异常
     */
    private JsonResult<?> handleValidationException(ValidationException exception, RoutingContext ctx) {
        return JsonResult.error("参数验证失败: " + exception.getMessage(), 400);
    }
    
    /**
     * 处理系统异常
     */
    private JsonResult<?> handleSystemException(SystemException exception, RoutingContext ctx) {
        return JsonResult.error("系统错误: " + exception.getMessage(), 500);
    }
    
    /**
     * 处理非法参数异常
     */
    private JsonResult<?> handleIllegalArgumentException(IllegalArgumentException exception, RoutingContext ctx) {
        return JsonResult.error("参数错误: " + exception.getMessage(), 400);
    }
    
    /**
     * 处理空指针异常
     */
    private JsonResult<?> handleNullPointerException(NullPointerException exception, RoutingContext ctx) {
        return JsonResult.error("系统内部错误: 空指针异常", 500);
    }
    
    /**
     * 处理未知异常
     */
    private JsonResult<?> handleUnknownException(Throwable exception, RoutingContext ctx) {
        return JsonResult.error("服务器内部错误", 500);
    }
}