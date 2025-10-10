package cn.qaiu.db.datasource;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;

/**
 * 数据源提供者接口
 * 支持SPI机制扩展不同类型的数据源
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface DataSourceProvider {
    
    /**
     * 获取数据源类型名称
     * 
     * @return 数据源类型名称，如 "mysql", "postgresql", "h2", "mongodb" 等
     */
    String getType();
    
    /**
     * 创建数据源连接池
     * 
     * @param config 数据源配置
     * @return 连接池Future
     */
    Future<Pool> createPool(DataSourceConfig config);
    
    /**
     * 检查数据源是否可用
     * 
     * @param config 数据源配置
     * @return 检查结果Future
     */
    Future<Boolean> isAvailable(DataSourceConfig config);
    
    /**
     * 关闭数据源
     * 
     * @param pool 连接池
     * @return 关闭结果Future
     */
    Future<Void> close(Pool pool);
}
