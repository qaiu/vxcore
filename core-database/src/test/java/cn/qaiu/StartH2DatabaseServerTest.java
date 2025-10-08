package cn.qaiu;

import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StartH2DatabaseServerTest {

    @BeforeEach
    void init() {
        VertxHolder.init(io.vertx.core.Vertx.vertx());
    }

    @Test
    void testQuery() throws InterruptedException {
        Future<Pool> poolFuture = StartH2DatabaseServer.getH2ServerPool(VertxHolder.getVertxInstance());
        poolFuture.onSuccess(pool -> {
                    System.out.println("连接池大小: " + pool.size());
                    // 查询 H2 服务器时间
                    pool.query("SELECT CURRENT_TIMESTAMP()").execute()
                            .onSuccess(rows -> {
                                if (rows != null && rows.size() > 0) {
                                    Object v = rows.iterator().next().getValue(0);
                                    System.out.println("H2 服务器时间: " + v);
                                } else {
                                    System.err.println("未能获取 H2 服务器时间");
                                }

                                System.out.println("连接池大小: " + pool.size());
                            })
                            .onFailure(err -> System.err.println("查询 H2 服务器时间失败: " + err.getMessage()));

                })
                .onFailure(err -> System.err.println("初始化H2连接池失败: " + err.getMessage()));
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    void getH2ServerPool() {
    }
}