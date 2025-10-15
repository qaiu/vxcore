package cn.qaiu.db.datasource;

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
     * @return DataSourceManager实例
     */
    public static cn.qaiu.db.datasource.DataSourceManager createDataSourceManager() {
        return cn.qaiu.db.datasource.DataSourceManager.getInstance();
    }
    
    /**
     * 创建数据源管理器实例（单例模式）
     * 
     * @return DataSourceManager实例
     */
    public static cn.qaiu.db.datasource.DataSourceManager getInstance() {
        return cn.qaiu.db.datasource.DataSourceManager.getInstance();
    }
}