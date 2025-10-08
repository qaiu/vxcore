package cn.qaiu.vx.core.interceptor;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static cn.qaiu.vx.core.util.ResponseUtil.sendError;

/**
 * 前置拦截器接口
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface BeforeInterceptor extends Handler<RoutingContext> {
    String IS_NEXT = "RoutingContextIsNext";

    default Handler<RoutingContext> doHandle() {
        return ctx -> {
            // 使用this锁对象
            synchronized (this) {
                ctx.put(IS_NEXT, false);
                handle(ctx); // 调用具体的处理逻辑
                // 确保如果没有调用doNext()并且响应未结束，则返回错误
//                if (!(Boolean) ctx.get(IS_NEXT) && !ctx.response().ended()) {
//                    sendError(ctx, 403);
//                }
            }
        };
    }

    default void doNext(RoutingContext context) {
        // 设置上下文状态为可以继续执行
        // 使用this锁对象
        synchronized (this) {
            context.put(IS_NEXT, true);
            context.next(); // 继续执行下一个处理器
        }
    }

    void handle(RoutingContext context); // 实现具体的拦截处理逻辑
}

