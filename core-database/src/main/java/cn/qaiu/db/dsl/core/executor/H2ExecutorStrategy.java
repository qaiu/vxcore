package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2执行器策略
 * 
 * @author QAIU
 */
public class H2ExecutorStrategy extends AbstractExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(H2ExecutorStrategy.class);
    
    @Override
    public JDBCType getSupportedType() {
        return JDBCType.H2DB;
    }
    
    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.H2;
    }
    
    @Override
    public Class<? extends Pool> getPoolType() {
        return JDBCPool.class;
    }
    
    @Override
    public boolean supports(Pool pool) {
        boolean supported = pool instanceof JDBCPool || 
                           pool.getClass().getName().contains("JDBCPool");
        
        if (supported) {
            LOGGER.debug("H2 executor strategy supports pool: {}", pool.getClass().getName());
        }
        
        return supported;
    }
}
