package cn.qaiu;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import cn.qaiu.db.server.H2ServerHolder;
import cn.qaiu.db.server.DbPoolHolder;
import org.h2.tools.Server;
import cn.qaiu.vx.core.util.VertxHolder;
import cn.qaiu.vx.core.util.ConfigUtil;

/**
 * 启动H2数据库服务器并初始化连接池
 * h2服务启动后 不会立即创建数据库文件，只有在连接时才会创建
 * <br>可通过 -tcpAllowOthers 允许远程连接（注意安全风险 ）
 * <br>可通过 -ifNotExists 避免重复启动时报错
 * <br>可通过 -tcpPort 指定端口，默认9092
 * <br>启动后可通过 JDBC URL 连接：jdbc:h2:tcp://localhost:9092/~/test
 * <br>数据库名需要带路径，~/test 表示用户目录下的 test 数据库
 * <br>Create date 2024-06-10 10:00:00
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class StartH2DatabaseServer {
    public static void main(String[] args) {
        init();
    }

    public static void init() {
        Vertx vertx = Vertx.vertx();

        try {
            // 启动H2 TCP服务
            Server server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
            H2ServerHolder.init(server);
            // 在 H2 Server 启动成功后再把 vertx 放入 Holder
            VertxHolder.init(vertx);
            System.out.println("H2 TCP服务已启动，端口: " + server.getPort());

            // 获取并初始化连接池
            // testQuery(vertx);
        } catch (Exception e) {
            System.err.println("H2 TCP服务启动失败: " + e.getMessage());
            // 仍然把 vertx 注册到 Holder，以便其他组件使用（可根据需要改为退出）
            VertxHolder.init(vertx);
        }
    }

    public static void testQuery(Vertx vertx) {
        Future<Pool> poolFuture = getH2ServerPool(vertx);
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
    }

    public static Future<Pool> getH2ServerPool(Vertx vertx) {
        // 使用 core 模块的 ConfigUtil 异步读取配置（json 格式）
        // 优先从类路径加载，运行时会在 classpath 中找到 resources 下的配置
        return ConfigUtil.readConfig("json", "classpath:h2-database.json", vertx)
                .compose(config -> {
                    // config 已经是 JsonObject
                    JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                            .setJdbcUrl(config.getString("jdbcUrl"))
                            .setUser(config.getString("user"))
                            .setPassword(config.getString("password"));
                    PoolOptions poolOptions = new PoolOptions()
                            .setMaxSize(config.getInteger("maxPoolSize", 5));
                    Pool pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
                    // 注册到 Holder，供其他模块获取
                    DbPoolHolder.init(pool);
                    System.out.println(config);

                    // 添加 JVM 退出钩子，优雅关闭资源（在此处注册一次）
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Shutdown hook: closing DB pool and H2 server and Vertx...");
                        try {
                            DbPoolHolder.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            Server h2 = H2ServerHolder.getH2Server();
                            if (h2 != null) {
                                h2.stop();
                                System.out.println("H2 server stopped.");
                            }
                        } catch (Exception ignored) {
                        }
                        try {
                            VertxHolder.getVertxInstance().close();
                        } catch (Exception ignored) {
                        }
                    }));

                    return Future.succeededFuture(pool);
                });
    }
}