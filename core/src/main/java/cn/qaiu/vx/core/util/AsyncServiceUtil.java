package cn.qaiu.vx.core.util;

import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;

/**
 * 异步服务工具类
 * 
 * @author Xu Haidong
 */
public final class AsyncServiceUtil {

    public static <T> T getAsyncServiceInstance(Class<T> asClazz, Vertx vertx) {
        String address = asClazz.getName();
        return new ServiceProxyBuilder(vertx).setAddress(address).build(asClazz);
    }

    public static <T> T getAsyncServiceInstance(Class<T> asClazz) {
        return getAsyncServiceInstance(asClazz, VertxHolder.getVertxInstance());
    }
}
