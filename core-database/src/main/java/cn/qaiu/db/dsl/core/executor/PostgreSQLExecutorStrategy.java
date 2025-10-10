package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL执行器策略
 * 
 * @author QAIU
 */
public class PostgreSQLExecutorStrategy extends AbstractExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLExecutorStrategy.class);
    
    @Override
    public JDBCType getSupportedType() {
        return JDBCType.PostgreSQL;
    }
    
    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.POSTGRES;
    }
    
    @Override
    public Class<? extends Pool> getPoolType() {
        return PgPool.class;
    }
    
    @Override
    public boolean supports(Pool pool) {
        boolean supported = pool instanceof PgPool || 
                           pool.getClass().getName().contains("PgPool");
        
        if (supported) {
            LOGGER.debug("PostgreSQL executor strategy supports pool: {}", pool.getClass().getName());
        }
        
        return supported;
    }
}
