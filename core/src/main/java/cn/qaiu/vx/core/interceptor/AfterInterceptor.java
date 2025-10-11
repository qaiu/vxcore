package cn.qaiu.vx.core.interceptor;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 后置拦截器接口
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface AfterInterceptor {

    /**
     * 处理后置拦截逻辑
     * 
     * @param ctx 路由上下文
     * @param responseData 响应数据
     */
    void handle(RoutingContext ctx, JsonObject responseData);

}
