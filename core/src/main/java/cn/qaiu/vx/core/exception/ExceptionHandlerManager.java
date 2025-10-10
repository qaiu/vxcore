package cn.qaiu.vx.core.exception;

import cn.qaiu.vx.core.annotaions.exception.ExceptionHandler;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异常处理器管理器
 * 负责注册和管理全局及局部异常处理器
 * 
 * @author QAIU
 */
public class ExceptionHandlerManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerManager.class);
    
    // 全局异常处理器
    private final Map<Class<? extends Throwable>, ExceptionHandlerInterface> globalHandlers = new ConcurrentHashMap<>();
    
    // 局部异常处理器 (Controller -> ExceptionType -> Handler)
    private final Map<Class<?>, Map<Class<? extends Throwable>, LocalExceptionHandlerWrapper>> localHandlers = new ConcurrentHashMap<>();
    
    /**
     * 注册全局异常处理器
     */
    public void registerGlobalHandler(Class<? extends Throwable> exceptionType, ExceptionHandlerInterface handler) {
        globalHandlers.put(exceptionType, handler);
        LOGGER.debug("Registered global exception handler for: {}", exceptionType.getSimpleName());
    }
    
    /**
     * 注册局部异常处理器
     */
    public void registerLocalHandler(Class<?> controllerClass, Method handlerMethod) {
        ExceptionHandler annotation = handlerMethod.getAnnotation(ExceptionHandler.class);
        if (annotation == null) {
            return;
        }
        
        Class<? extends Throwable>[] exceptionTypes = annotation.value();
        if (exceptionTypes.length == 0) {
            return;
        }
        
        localHandlers.computeIfAbsent(controllerClass, k -> new ConcurrentHashMap<>());
        Map<Class<? extends Throwable>, LocalExceptionHandlerWrapper> controllerHandlers = localHandlers.get(controllerClass);
        
        for (Class<? extends Throwable> exceptionType : exceptionTypes) {
            LocalExceptionHandlerWrapper wrapper = new LocalExceptionHandlerWrapper(controllerClass, handlerMethod, annotation.priority());
            controllerHandlers.put(exceptionType, wrapper);
            LOGGER.debug("Registered local exception handler for {} in {}: {}", 
                    exceptionType.getSimpleName(), controllerClass.getSimpleName(), handlerMethod.getUsername());
        }
    }
    
    /**
     * 处理异常
     */
    public void handleException(Throwable exception, RoutingContext ctx, Object controllerInstance) {
        LOGGER.debug("Handling exception: {}", exception.getClass().getSimpleName());
        
        // 1. 尝试局部异常处理器
        if (controllerInstance != null) {
            JsonResult<?> result = handleWithLocalHandlers(exception, ctx, controllerInstance);
            if (result != null) {
                sendResponse(ctx, result);
                return;
            }
        }
        
        // 2. 尝试全局异常处理器
        JsonResult<?> result = handleWithGlobalHandlers(exception, ctx);
        if (result != null) {
            sendResponse(ctx, result);
            return;
        }
        
        // 3. 默认异常处理
        handleDefaultException(exception, ctx);
    }
    
    /**
     * 使用局部异常处理器处理异常
     */
    private JsonResult<?> handleWithLocalHandlers(Throwable exception, RoutingContext ctx, Object controllerInstance) {
        Class<?> controllerClass = controllerInstance.getClass();
        Map<Class<? extends Throwable>, LocalExceptionHandlerWrapper> controllerHandlers = localHandlers.get(controllerClass);
        
        if (controllerHandlers == null || controllerHandlers.isEmpty()) {
            return null;
        }
        
        // 查找匹配的异常处理器
        LocalExceptionHandlerWrapper handler = findBestMatch(exception, controllerHandlers);
        if (handler == null) {
            return null;
        }
        
        try {
            return handler.handle(exception, ctx, controllerInstance);
        } catch (Throwable e) {
            LOGGER.error("Error in local exception handler", e);
            return null;
        }
    }
    
    /**
     * 使用全局异常处理器处理异常
     */
    private JsonResult<?> handleWithGlobalHandlers(Throwable exception, RoutingContext ctx) {
        ExceptionHandlerInterface handler = findBestGlobalHandler(exception);
        if (handler == null) {
            return null;
        }
        
        try {
            return handler.handleException(exception, ctx);
        } catch (Throwable e) {
            LOGGER.error("Error in global exception handler", e);
            return JsonResult.error("异常处理器执行失败", 500);
        }
    }
    
    /**
     * 查找最佳匹配的局部异常处理器
     */
    private LocalExceptionHandlerWrapper findBestMatch(Throwable exception, 
            Map<Class<? extends Throwable>, LocalExceptionHandlerWrapper> handlers) {
        
        Class<?> exceptionClass = exception.getClass();
        
        // 1. 精确匹配
        LocalExceptionHandlerWrapper exactMatch = handlers.get(exceptionClass);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 2. 父类匹配
        Class<?> currentClass = exceptionClass.getSuperclass();
        while (currentClass != null && currentClass != Throwable.class) {
            LocalExceptionHandlerWrapper match = handlers.get(currentClass);
            if (match != null) {
                return match;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // 3. 接口匹配
        for (Class<?> interfaceClass : exceptionClass.getInterfaces()) {
            LocalExceptionHandlerWrapper match = handlers.get(interfaceClass);
            if (match != null) {
                return match;
            }
        }
        
        return null;
    }
    
    /**
     * 查找最佳匹配的全局异常处理器
     */
    private ExceptionHandlerInterface findBestGlobalHandler(Throwable exception) {
        Class<?> exceptionClass = exception.getClass();
        
        // 1. 精确匹配
        ExceptionHandlerInterface exactMatch = globalHandlers.get(exceptionClass);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 2. 父类匹配
        Class<?> currentClass = exceptionClass.getSuperclass();
        while (currentClass != null && currentClass != Throwable.class) {
            ExceptionHandlerInterface match = globalHandlers.get(currentClass);
            if (match != null) {
                return match;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // 3. 接口匹配
        for (Class<?> interfaceClass : exceptionClass.getInterfaces()) {
            ExceptionHandlerInterface match = globalHandlers.get(interfaceClass);
            if (match != null) {
                return match;
            }
        }
        
        return null;
    }
    
    /**
     * 默认异常处理
     */
    private void handleDefaultException(Throwable exception, RoutingContext ctx) {
        LOGGER.error("Unhandled exception", exception);
        
        JsonResult<?> result;
        if (exception instanceof BusinessException) {
            result = JsonResult.error("业务错误: " + exception.getMessage(), 400);
        } else if (exception instanceof ValidationException) {
            result = JsonResult.error("参数验证失败: " + exception.getMessage(), 400);
        } else if (exception instanceof SystemException) {
            result = JsonResult.error("系统错误: " + exception.getMessage(), 500);
        } else {
            result = JsonResult.error("服务器内部错误", 500);
        }
        
        sendResponse(ctx, result);
    }
    
    /**
     * 发送响应
     */
    private void sendResponse(RoutingContext ctx, JsonResult<?> result) {
        if (ctx.response().ended()) {
            return;
        }
        
        ctx.response()
                .setStatusCode(result.getCode())
                .putHeader("Content-Type", "application/json")
                .end(result.toJsonObject().encode());
    }
    
    /**
     * 局部异常处理器包装器
     */
    private static class LocalExceptionHandlerWrapper {
        private final Class<?> controllerClass;
        private final Method handlerMethod;
        private final int priority;
        
        public LocalExceptionHandlerWrapper(Class<?> controllerClass, Method handlerMethod, int priority) {
            this.controllerClass = controllerClass;
            this.handlerMethod = handlerMethod;
            this.priority = priority;
        }
        
        public JsonResult<?> handle(Throwable exception, RoutingContext ctx, Object controllerInstance) throws Throwable {
            try {
                Object result = ReflectionUtil.invokeWithArguments(handlerMethod, controllerInstance, exception, ctx);
                if (result instanceof JsonResult) {
                    return (JsonResult<?>) result;
                } else {
                    return JsonResult.error("异常处理器返回类型错误", 500);
                }
            } catch (Throwable e) {
                LOGGER.error("Error invoking local exception handler", e);
                throw e;
            }
        }
        
        public int getPriority() {
            return priority;
        }
    }
}