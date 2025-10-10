package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import org.jooq.Query;
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
    
    @Override
    public Future<Long> executeInsert(Pool pool, Query query) {
        String sql = query.getSQL();
        Object[] bindValues = query.getBindValues().toArray();
        
        System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: SQL=" + sql);
        LOGGER.debug("Executing H2 insert: {} with bind values: {}", sql, java.util.Arrays.toString(bindValues));
        
        return pool.preparedQuery(sql)
                .execute(io.vertx.sqlclient.Tuple.from(bindValues))
                .compose(result -> {
                    int rowCount = result.rowCount();
                    System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: rowCount=" + rowCount);
                    LOGGER.debug("Insert affected rows: {}", rowCount);
                    
                    if (rowCount > 0) {
                        // H2使用IDENTITY()函数获取最后插入的ID
                        System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: calling IDENTITY()");
                        return pool.query("CALL IDENTITY()").execute()
                                .map(idResult -> {
                                    System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: IDENTITY() result size=" + idResult.size());
                                    if (idResult.size() > 0) {
                                        io.vertx.sqlclient.Row row = idResult.iterator().next();
                                        Object id = row.getValue(0);
                                        System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: ID value=" + id + ", type=" + (id != null ? id.getClass().getName() : "null"));
                                        if (id instanceof Number) {
                                            Long generatedId = ((Number) id).longValue();
                                            System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: generatedId=" + generatedId);
                                            LOGGER.debug("Retrieved H2 generated ID: {}", generatedId);
                                            return generatedId;
                                        }
                                    }
                                    System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: Could not retrieve ID, returning 0");
                                    LOGGER.warn("Could not retrieve H2 generated ID");
                                    return 0L;
                                });
                    } else {
                        System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: Insert affected 0 rows");
                        LOGGER.warn("Insert affected 0 rows");
                        return Future.succeededFuture(0L);
                    }
                })
                .onSuccess(id -> {
                    System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: Final result ID=" + id);
                    LOGGER.debug("H2 insert executed successfully, generated ID: {}", id);
                })
                .onFailure(error -> {
                    System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: FAILED - " + error.getMessage());
                    LOGGER.error("H2 insert execution failed: {}", error.getMessage(), error);
                });
    }
}
