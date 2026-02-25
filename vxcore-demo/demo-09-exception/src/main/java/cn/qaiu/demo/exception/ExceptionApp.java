package cn.qaiu.demo.exception;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@App
public class ExceptionApp {
    private static final Logger log = LoggerFactory.getLogger(ExceptionApp.class);

    public static void main(String[] args) {
        VXCoreApplication.run(args, config -> {
            log.info("Exception handling demo started on port 18085");
        });
    }
}
