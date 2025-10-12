package cn.qaiu.vx.core.lifecycle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 生命周期组件接口
 * 定义框架组件的生命周期管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface LifecycleComponent {
    
    /**
     * 初始化组件
     * 
     * @param vertx Vertx实例
     * @param config 全局配置
     * @return 初始化结果
     */
    Future<Void> initialize(Vertx vertx, JsonObject config);
    
    /**
     * 启动组件
     * 
     * @return 启动结果
     */
    default Future<Void> start() {
        return Future.succeededFuture();
    }
    
    /**
     * 停止组件
     * 
     * @return 停止结果
     */
    default Future<Void> stop() {
        return Future.succeededFuture();
    }
    
    /**
     * 获取组件名称
     * 
     * @return 组件名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取组件优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}