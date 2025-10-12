package cn.qaiu.db.datasource;

import cn.qaiu.vx.core.lifecycle.DataSourceManager;
import io.vertx.core.Vertx;

/**
 * 数据源管理器工厂
 * 负责创建和配置DataSourceManager实例
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceManagerFactory {
    
    /**
     * 创建数据源管理器实例
     * 
     * @param vertx Vertx实例
     * @return DataSourceManager实例
     */
    public static cn.qaiu.vx.core.lifecycle.DataSourceManager createDataSourceManager(Vertx vertx) {
        return new DataSourceManager(vertx);
    }
    
    /**
     * 创建数据源管理器实例（单例模式）
     * 
     * @param vertx Vertx实例
     * @return DataSourceManager实例
     */
    public static cn.qaiu.vx.core.lifecycle.DataSourceManager getInstance(Vertx vertx) {
        return DataSourceManager.getInstance(vertx);
    }
}