package cn.qaiu.vx.core.exception;

import cn.qaiu.vx.core.annotaions.exception.ControllerAdvice;
import cn.qaiu.vx.core.annotaions.exception.ExceptionHandler;
import cn.qaiu.vx.core.annotaions.exception.GlobalExceptionHandler;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.ext.web.RoutingContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异常处理管理器
 * 负责注册和管理全局和局部异常处理器
 * 
 * @author QAIU
 */
public class ExceptionManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionManager.class);
    
    private static final List<ExceptionProcessor> globalProcessors = new ArrayList<>();
    private static final Map<Class<?>, List<ExceptionProcessor>> controllerProcessors = new HashMap<>();
    
    static {
        initializeExceptionHandlers();
    }
    
    /**
     * 初始化异常处理器
     */
    private static void initializeExceptionHandlers() {
        Reflections reflections = ReflectionUtil.getReflections();
        
        // 注册全局异常处理器
        Set<Class<?>> globalHandlerClasses = reflections.getTypesAnnotatedWith(GlobalExceptionHandler.class);
        for (Class<?> handlerClass : globalHandlerClasses) {
            try {
                Object instance = ReflectionUtil.newWithNoParam(handlerClass);
                registerGlobalExceptionHandler(instance);
                LOGGER.info("Registered global exception handler: {}", handlerClass.getSimpleName());
            } catch (Exception e) {
                LOGGER.error("Failed to register global exception handler: {}", handlerClass.getSimpleName(), e);
            }
        }
        
        // 注册控制器异常处理器
        Set<Class<?>> controllerAdviceClasses = reflections.getTypesAnnotatedWith(ControllerAdvice.class);
        for (Class<?> adviceClass : controllerAdviceClasses) {
            try {
                Object instance = ReflectionUtil.newWithNoParam(adviceClass);
                registerControllerExceptionHandler(adviceClass, instance);
                LOGGER.info("Registered controller exception handler: {}", adviceClass.getSimpleName());
            } catch (Exception e) {
                LOGGER.error("Failed to register controller exception handler: {}", adviceClass.getSimpleName(), e);
            }
        }
        
        // 按优先级排序
        globalProcessors.sort(Comparator.comparingInt(ExceptionProcessor::getOrder));
        controllerProcessors.values().forEach(processors -> 
            processors.sort(Comparator.comparingInt(ExceptionProcessor::getOrder)));
        
        LOGGER.info("Exception handling initialized with {} global processors and {} controller processors", 
                   globalProcessors.size(), controllerProcessors.size());
    }
    
    /**
     * 注册全局异常处理器
     */
    private static void registerGlobalExceptionHandler(Object handlerInstance) {
        Method[] methods = handlerInstance.getClass().getMethods();
        GlobalExceptionHandler classAnnotation = handlerInstance.getClass().getAnnotation(GlobalExceptionHandler.class);
        int classOrder = classAnnotation != null ? classAnnotation.order() : 0;
        
        for (Method method : methods) {
            ExceptionHandler methodAnnotation = method.getAnnotation(ExceptionHandler.class);
            if (methodAnnotation != null) {
                Class<? extends Throwable>[] exceptionTypes = methodAnnotation.value();
                int methodOrder = methodAnnotation.priority();
                int finalOrder = methodOrder != 0 ? methodOrder : classOrder;
                
                for (Class<? extends Throwable> exceptionType : exceptionTypes) {
                    ExceptionProcessor processor = new MethodBasedExceptionProcessor(
                        handlerInstance, method, exceptionType, finalOrder);
                    globalProcessors.add(processor);
                }
            }
        }
    }
    
    /**
     * 注册控制器异常处理器
     */
    private static void registerControllerExceptionHandler(Class<?> controllerClass, Object handlerInstance) {
        Method[] methods = handlerInstance.getClass().getMethods();
        ControllerAdvice classAnnotation = controllerClass.getAnnotation(ControllerAdvice.class);
        int classOrder = classAnnotation != null ? classAnnotation.order() : 100;
        
        List<ExceptionProcessor> processors = new ArrayList<>();
        
        for (Method method : methods) {
            ExceptionHandler methodAnnotation = method.getAnnotation(ExceptionHandler.class);
            if (methodAnnotation != null) {
                Class<? extends Throwable>[] exceptionTypes = methodAnnotation.value();
                int methodOrder = methodAnnotation.priority();
                int finalOrder = methodOrder != 0 ? methodOrder : classOrder;
                
                for (Class<? extends Throwable> exceptionType : exceptionTypes) {
                    ExceptionProcessor processor = new MethodBasedExceptionProcessor(
                        handlerInstance, method, exceptionType, finalOrder);
                    processors.add(processor);
                }
            }
        }
        
        if (!processors.isEmpty()) {
            controllerProcessors.put(controllerClass, processors);
        }
    }
    
    /**
     * 处理异常
     * 
     * @param ctx 路由上下文
     * @param throwable 异常对象
     * @return 是否处理了异常
     */
    public static boolean handleException(RoutingContext ctx, Throwable throwable) {
        LOGGER.debug("Handling exception: {}", throwable.getClass().getSimpleName());
        
        // 首先尝试控制器级别的异常处理
        Class<?> controllerClass = getControllerClass(ctx);
        if (controllerClass != null) {
            List<ExceptionProcessor> processors = controllerProcessors.get(controllerClass);
            if (processors != null) {
                for (ExceptionProcessor processor : processors) {
                    if (processor.supports(throwable) && processor.handleException(ctx, throwable)) {
                        LOGGER.debug("Exception handled by controller processor: {}", processor.getClass().getSimpleName());
                        return true;
                    }
                }
            }
        }
        
        // 然后尝试全局异常处理
        for (ExceptionProcessor processor : globalProcessors) {
            if (processor.supports(throwable) && processor.handleException(ctx, throwable)) {
                LOGGER.debug("Exception handled by global processor: {}", processor.getClass().getSimpleName());
                return true;
            }
        }
        
        // 最后使用默认处理
        handleDefaultException(ctx, throwable);
        return true;
    }
    
    /**
     * 获取控制器类
     */
    private static Class<?> getControllerClass(RoutingContext ctx) {
        // 这里可以从路由上下文中获取控制器信息
        // 暂时返回null，实际实现需要根据路由信息获取
        return null;
    }
    
    /**
     * 默认异常处理
     */
    private static void handleDefaultException(RoutingContext ctx, Throwable throwable) {
        LOGGER.error("Unhandled exception: {}", throwable.getMessage(), throwable);
        
        String message = "Internal server error";
        int statusCode = 500;
        
        if (throwable instanceof IllegalArgumentException) {
            message = throwable.getMessage();
            statusCode = 400;
        } else if (throwable instanceof SecurityException) {
            message = "Access denied";
            statusCode = 403;
        } else if (throwable instanceof NoSuchElementException) {
            message = "Resource not found";
            statusCode = 404;
        }
        
        JsonResult<Object> result = JsonResult.error(message, statusCode);
        ctx.response().setStatusCode(statusCode);
        ctx.response().putHeader("Content-Type", "application/json");
        ctx.response().end(result.toJsonObject().encode());
    }
    
    /**
     * 基于方法的异常处理器
     */
    private static class MethodBasedExceptionProcessor implements ExceptionProcessor {
        private final Object handlerInstance;
        private final Method handlerMethod;
        private final Class<? extends Throwable> exceptionType;
        private final int order;
        
        public MethodBasedExceptionProcessor(Object handlerInstance, Method handlerMethod, 
                                          Class<? extends Throwable> exceptionType, int order) {
            this.handlerInstance = handlerInstance;
            this.handlerMethod = handlerMethod;
            this.exceptionType = exceptionType;
            this.order = order;
        }
        
        @Override
        public boolean handleException(RoutingContext ctx, Throwable throwable) {
            try {
                Object result = ReflectionUtil.invokeWithArguments(handlerMethod, handlerInstance, ctx, throwable);
                
                if (result instanceof JsonResult) {
                    JsonResult<?> jsonResult = (JsonResult<?>) result;
                    ctx.response().setStatusCode(jsonResult.getCode());
                    ctx.response().putHeader("Content-Type", "application/json");
                    ctx.response().end(jsonResult.toJsonObject().encode());
                    return true;
                } else if (result instanceof String) {
                    ctx.response().putHeader("Content-Type", "text/plain");
                    ctx.response().end((String) result);
                    return true;
                }
                
                return false;
            } catch (Throwable e) {
                LOGGER.error("Error in exception handler method: {}", handlerMethod.getName(), e);
                return false;
            }
        }
        
        @Override
        public int getOrder() {
            return order;
        }
        
        @Override
        public boolean supports(Throwable throwable) {
            return exceptionType.isAssignableFrom(throwable.getClass());
        }
    }
}
