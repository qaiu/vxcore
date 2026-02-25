package cn.qaiu.demo.config;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@App
public class ConfigApp {
    private static final Logger log = LoggerFactory.getLogger(ConfigApp.class);

    public static void main(String[] args) {
        VXCoreApplication.run(args, config -> {
            log.info("Config demo started on port 18086");
        });
    }
}
