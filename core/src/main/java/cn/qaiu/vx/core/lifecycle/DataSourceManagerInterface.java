package cn.qaiu.vx.core.lifecycle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 数据源管理器接口
 * 定义数据源管理的基本操作
 * 使用抽象类型避免与具体数据库实现耦合
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface DataSourceManagerInterface {
    
    /**
     * 注册数据源
     * 
     * @param name 数据源名称
     * @param config 数据源配置
     * @return Future<Void>
     */
    Future<Void> registerDataSource(String name, JsonObject config);
    
    /**
     * 初始化所有数据源
     * 
     * @param vertx Vertx实例
     * @param config 全局配置
     * @return Future<Void>
     */
    Future<Void> initializeDataSources(Vertx vertx, JsonObject config);
    
    /**
     * 初始化所有已注册的数据源
     * 
     * @return Future<Void>
     */
    Future<Void> initializeAllDataSources();
    
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
    List<String> getDataSourceNames();
}