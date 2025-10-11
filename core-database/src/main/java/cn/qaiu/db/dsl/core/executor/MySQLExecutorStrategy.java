package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL执行器策略
 * 提供MySQL数据库的特定执行逻辑
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class MySQLExecutorStrategy extends AbstractExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLExecutorStrategy.class);
    
    /**
     * 获取支持的数据源类型
     * 
     * @return MySQL类型
     */
    @Override
    public JDBCType getSupportedType() {
        return JDBCType.MySQL;
    }
    
    /**
     * 获取SQL方言
     * 
     * @return MySQL方言
     */
    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.MYSQL;
    }
    
    /**
     * 获取连接池类型
     * 
     * @return MySQLPool类型
     */
    @Override
    public Class<? extends Pool> getPoolType() {
        return MySQLPool.class;
    }
    
    /**
     * 检查是否支持指定的连接池
     * 
     * @param pool 连接池
     * @return 是否支持
     */
    @Override
    public boolean supports(Pool pool) {
        boolean supported = pool instanceof MySQLPool || 
                           pool.getClass().getName().contains("MySQLPool");
        
        if (supported) {
            LOGGER.debug("MySQL executor strategy supports pool: {}", pool.getClass().getName());
        }
        
        return supported;
    }
}
