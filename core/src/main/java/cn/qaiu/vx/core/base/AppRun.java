package cn.qaiu.vx.core.base;

import io.vertx.core.json.JsonObject;

/**
 * 应用运行接口
 * 定义应用启动时的执行方法
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface AppRun {

    /**
     * 执行方法
     * 应用启动时调用此方法
     * 
     * @param config 启动配置文件
     */
    void execute(JsonObject config);
}
