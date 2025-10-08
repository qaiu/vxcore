package cn.qaiu.db.server;

import io.vertx.sqlclient.Pool;

import java.util.Objects;

/**
 * 数据库连接池 Holder
 */
public final class DbPoolHolder {

    private static volatile Pool pool;

    public static synchronized void init(Pool p) {
        Objects.requireNonNull(p, "未初始化数据库连接池");
        pool = p;
    }

    public static Pool getPool() {
        Objects.requireNonNull(pool, "等待数据库连接池初始化");
        return pool;
    }

    public static synchronized void close() {
        if (pool != null) {
            try {
                pool.close();
            } catch (Exception ignored) {
            }
            pool = null;
        }
    }
}

