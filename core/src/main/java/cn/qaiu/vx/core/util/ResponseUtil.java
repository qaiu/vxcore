package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * HTTP响应工具类
 * 提供统一的HTTP响应处理方法
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ResponseUtil {

    /**
     * 重定向响应
     * 
     * @param response HTTP响应对象
     * @param url 重定向URL
     */
    public static void redirect(HttpServerResponse response, String url) {
        response.putHeader(CONTENT_TYPE, "text/html; charset=utf-8")
                .putHeader(HttpHeaders.LOCATION, url).setStatusCode(302).end();
    }

    /**
     * 重定向响应并完成Promise
     * 
     * @param response HTTP响应对象
     * @param url 重定向URL
     * @param promise 要完成的Promise
     */
    public static void redirect(HttpServerResponse response, String url, Promise<?> promise) {
        redirect(response, url);
        promise.complete();
    }

    /**
     * 发送JSON对象响应
     * 
     * @param ctx 路由上下文
     * @param jsonObject 响应数据
     */
    public static void fireJsonObjectResponse(RoutingContext ctx, JsonObject jsonObject) {
        fireJsonObjectResponse(ctx, jsonObject, 200);
    }

    /**
     * 发送JSON对象响应
     * 
     * @param ctx HTTP响应对象
     * @param jsonObject 响应数据
     */
    public static void fireJsonObjectResponse(HttpServerResponse ctx, JsonObject jsonObject) {
        fireJsonObjectResponse(ctx, jsonObject, 200);
    }

    /**
     * 发送JSON对象响应（指定状态码）
     * 
     * @param ctx 路由上下文
     * @param jsonObject 响应数据
     * @param statusCode HTTP状态码
     */
    public static void fireJsonObjectResponse(RoutingContext ctx, JsonObject jsonObject, int statusCode) {
        ctx.response().putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                .setStatusCode(statusCode)
                .end(jsonObject.encode());
    }

    /**
     * 发送JSON对象响应（指定状态码）
     * 
     * @param ctx HTTP响应对象
     * @param jsonObject 响应数据
     * @param statusCode HTTP状态码
     */
    public static void fireJsonObjectResponse(HttpServerResponse ctx, JsonObject jsonObject, int statusCode) {
        ctx.putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                .setStatusCode(statusCode)
                .end(jsonObject.encode());
    }

    /**
     * 发送JsonResult响应
     * 
     * @param ctx 路由上下文
     * @param jsonResult 响应结果
     * @param <T> 数据类型
     */
    public static <T> void fireJsonResultResponse(RoutingContext ctx, JsonResult<T> jsonResult) {
        fireJsonObjectResponse(ctx, jsonResult.toJsonObject());
    }

    /**
     * 发送JsonResult响应（指定状态码）
     * 
     * @param ctx 路由上下文
     * @param jsonResult 响应结果
     * @param statusCode HTTP状态码
     * @param <T> 数据类型
     */
    public static <T> void fireJsonResultResponse(RoutingContext ctx, JsonResult<T> jsonResult, int statusCode) {
        fireJsonObjectResponse(ctx, jsonResult.toJsonObject(), statusCode);
    }

    /**
     * 发送JsonResult响应
     * 
     * @param ctx HTTP响应对象
     * @param jsonResult 响应结果
     * @param <T> 数据类型
     */
    public static <T> void fireJsonResultResponse(HttpServerResponse ctx, JsonResult<T> jsonResult) {
        fireJsonObjectResponse(ctx, jsonResult.toJsonObject());
    }

    /**
     * 发送文本响应
     * 
     * @param ctx 路由上下文
     * @param text 响应文本
     */
    public static void fireTextResponse(RoutingContext ctx, String text) {
        ctx.response().putHeader(CONTENT_TYPE, "text/html; charset=utf-8").end(text);
    }

    /**
     * 发送错误响应
     * 
     * @param ctx 路由上下文
     * @param statusCode HTTP状态码
     */
    public static void sendError(RoutingContext ctx, int statusCode) {
        ctx.response().setStatusCode(statusCode).end();
    }
}
