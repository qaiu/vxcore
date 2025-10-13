package cn.qaiu.vx.core.lifecycle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 数据源提供者接口
 * 使用SPI模式解耦模块依赖，允许core-database模块提供具体实现
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface DataSourceProvider {
    
    /**
     * 获取提供者名称
     * 
     * @return 提供者名称
     */
    String getName();
    
    /**
     * 检查是否支持指定的数据源类型
     * 
     * @param type 数据源类型（如：h2, mysql, postgresql等）
     * @return 是否支持
     */
    boolean supports(String type);
    
    /**
     * 创建数据源管理器
     * 
     * @param vertx Vertx实例
     * @return 数据源管理器实例
     */
    DataSourceManagerInterface createDataSourceManager(Vertx vertx);
    
    /**
     * 初始化数据源
     * 
     * @param vertx Vertx实例
     * @param config 数据源配置
     * @return Future<Void>
     */
    Future<Void> initializeDataSources(Vertx vertx, JsonObject config);
    
    /**
     * 关闭所有数据源
     * 
     * @return Future<Void>
     */
    Future<Void> closeAllDataSources();
    
    /**
     * 获取数据源连接池
     * 
     * @param name 数据源名称
     * @return 连接池对象
     */
    Object getPool(String name);
    
    /**
     * 获取所有数据源名称
     * 
     * @return 数据源名称列表
     */
    java.util.List<String> getDataSourceNames();
}
