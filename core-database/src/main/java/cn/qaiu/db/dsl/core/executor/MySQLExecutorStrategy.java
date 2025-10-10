package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL执行器策略
 * 
 * @author QAIU
 */
public class MySQLExecutorStrategy extends AbstractExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLExecutorStrategy.class);
    
    @Override
    public JDBCType getSupportedType() {
        return JDBCType.MySQL;
    }
    
    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.MYSQL;
    }
    
    @Override
    public Class<? extends Pool> getPoolType() {
        return MySQLPool.class;
    }
    
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
