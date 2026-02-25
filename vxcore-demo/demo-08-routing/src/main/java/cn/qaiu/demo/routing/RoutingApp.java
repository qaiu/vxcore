package cn.qaiu.demo.routing;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@App
public class RoutingApp {
    private static final Logger log = LoggerFactory.getLogger(RoutingApp.class);

    public static void main(String[] args) {
        VXCoreApplication.run(args, config -> {
            log.info("Routing demo started on port 18084");
            JsonObject server = config.getJsonObject("server");
            if (server != null) {
                int port = server.getInteger("port", 18084);
                log.info("API available at http://localhost:{}/api/routing/", port);
            }
        });
    }
}
