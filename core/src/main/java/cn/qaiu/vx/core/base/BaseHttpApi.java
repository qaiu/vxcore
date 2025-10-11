package cn.qaiu.vx.core.base;

import cn.qaiu.vx.core.interceptor.AfterInterceptor;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.util.CommonUtil;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.reflections.Reflections;

import java.util.Set;

import static cn.qaiu.vx.core.util.ResponseUtil.*;

/**
 * 统一响应处理
 * <br>Create date 2021-05-06 09:20:37
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface BaseHttpApi {

    // 需要扫描注册的Router路径
    Reflections reflections = ReflectionUtil.getReflections();

    /**
     * 发送JsonObject响应
     * 
     * @param ctx 路由上下文
     * @param jsonObject 响应数据
     */
    default void doFireJsonObjectResponse(RoutingContext ctx, JsonObject jsonObject) {
        if (!ctx.response().ended()) {
            fireJsonObjectResponse(ctx, jsonObject);
        }
        handleAfterInterceptor(ctx, jsonObject);
    }


    /**
     * 发送JsonResult响应
     * 
     * @param ctx 路由上下文
     * @param jsonResult 响应结果
     * @param <T> 响应数据类型
     */
    default <T> void doFireJsonResultResponse(RoutingContext ctx, JsonResult<T> jsonResult) {
        if (!ctx.response().ended()) {
            fireJsonResultResponse(ctx, jsonResult);
        }
        handleAfterInterceptor(ctx, jsonResult.toJsonObject());
    }

    /**
     * 发送JsonObject响应（指定状态码）
     * 
     * @param ctx 路由上下文
     * @param jsonObject 响应数据
     * @param statusCode HTTP状态码
     */
    default void doFireJsonObjectResponse(RoutingContext ctx, JsonObject jsonObject, int statusCode) {
        if (!ctx.response().ended()) {
            fireJsonObjectResponse(ctx, jsonObject, statusCode);
        }
        handleAfterInterceptor(ctx, jsonObject);
    }


    /**
     * 发送JsonResult响应（指定状态码）
     * 
     * @param ctx 路由上下文
     * @param jsonResult 响应结果
     * @param statusCode HTTP状态码
     * @param <T> 响应数据类型
     */
    default <T> void doFireJsonResultResponse(RoutingContext ctx, JsonResult<T> jsonResult, int statusCode) {
        if (!ctx.response().ended()) {
            fireJsonResultResponse(ctx, jsonResult, statusCode);
        }
        handleAfterInterceptor(ctx, jsonResult.toJsonObject());
    }

    /**
     * 获取后置拦截器集合
     * 
     * @return 后置拦截器集合
     */
    default Set<AfterInterceptor> getAfterInterceptor() {

        Set<Class<? extends AfterInterceptor>> afterInterceptorClassSet =
                reflections.getSubTypesOf(AfterInterceptor.class);
        if (afterInterceptorClassSet == null) {
            return null;
        }
        return CommonUtil.sortClassSet(afterInterceptorClassSet);
    }

    /**
     * 处理后置拦截器
     * 
     * @param ctx 路由上下文
     * @param jsonObject 响应数据
     */
    default void handleAfterInterceptor(RoutingContext ctx, JsonObject jsonObject) {
        Set<AfterInterceptor> afterInterceptor = getAfterInterceptor();
        if (afterInterceptor != null) {
            afterInterceptor.forEach(ai -> ai.handle(ctx, jsonObject));
        }
        if (!ctx.response().ended()) {
            fireTextResponse(ctx, "handleAfterInterceptor: response not end");
        }
    }

}
