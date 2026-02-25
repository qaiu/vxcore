package cn.qaiu.demo.exception.handler;

import cn.qaiu.vx.core.annotations.exception.ControllerAdvice;
import cn.qaiu.vx.core.annotations.exception.ExceptionHandler;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class DemoExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DemoExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public JsonResult<?> handleRuntimeException(RuntimeException e, RoutingContext ctx) {
        log.warn("Caught RuntimeException: {}", e.getMessage());
        return JsonResult.error("Runtime error: " + e.getMessage(), 500);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResult<?> handleIllegalArg(IllegalArgumentException e, RoutingContext ctx) {
        log.warn("Caught IllegalArgumentException: {}", e.getMessage());
        return JsonResult.error("Bad request: " + e.getMessage(), 400);
    }
}
