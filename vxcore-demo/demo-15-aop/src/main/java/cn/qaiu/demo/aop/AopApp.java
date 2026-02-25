package cn.qaiu.demo.aop;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@App
public class AopApp {
    private static final Logger log = LoggerFactory.getLogger(AopApp.class);

    public static void main(String[] args) {
        VXCoreApplication.run(args, config -> {
            log.info("AOP demo started on port 18087");
        });
    }
}
