package cn.qaiu.vx.core.util;

import io.vertx.core.Vertx;

import java.util.Objects;

/**
 * 保存vertx实例
 * <br>Create date 2021-04-30 09:22:18
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public final class VertxHolder {

    private static volatile Vertx singletonVertx;

    public static synchronized void init(Vertx vertx) {
        Objects.requireNonNull(vertx, "未初始化Vertx");
        singletonVertx = vertx;
    }

    public static Vertx getVertxInstance() {
        // 从当前Vertx上下文获取 如果获取不到就创建新的Vertx实例
        if (singletonVertx == null) {
            if (Vertx.currentContext() == null) {
                singletonVertx = Vertx.vertx();
            } else {
                singletonVertx = Vertx.currentContext().owner();
            }
        }
        return singletonVertx;
    }
}
