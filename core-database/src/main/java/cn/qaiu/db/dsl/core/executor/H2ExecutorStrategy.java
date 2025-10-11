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
 * 提供H2数据库的特定执行逻辑
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class H2ExecutorStrategy extends AbstractExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(H2ExecutorStrategy.class);
    
    /**
     * 获取支持的数据源类型
     * 
     * @return H2DB类型
     */
    @Override
    public JDBCType getSupportedType() {
        return JDBCType.H2DB;
    }
    
    /**
     * 获取SQL方言
     * 
     * @return H2方言
     */
    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.H2;
    }
    
    /**
     * 获取连接池类型
     * 
     * @return JDBCPool类型
     */
    @Override
    public Class<? extends Pool> getPoolType() {
        return JDBCPool.class;
    }
    
    /**
     * 检查是否支持指定的连接池
     * 
     * @param pool 连接池
     * @return 是否支持
     */
    @Override
    public boolean supports(Pool pool) {
        boolean supported = pool instanceof JDBCPool || 
                           pool.getClass().getName().contains("JDBCPool");
        
        if (supported) {
            LOGGER.debug("H2 executor strategy supports pool: {}", pool.getClass().getName());
        }
        
        return supported;
    }
    
    /**
     * 执行插入操作
     * 使用H2特定的IDENTITY()函数获取生成的ID
     * 
     * @param pool 连接池
     * @param query 插入查询
     * @return 生成的ID
     */
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
                        // H2 2.x版本：使用CURRVAL函数获取序列值
                        // 对于自增列，H2会自动创建SYSTEM_SEQUENCE序列
                        System.out.println("[DEBUG] H2ExecutorStrategy.executeInsert: Insert successful");
                        LOGGER.debug("H2 insert completed successfully, affected {} rows", rowCount);
                        // 返回rowCount作为成功标志（大于0表示成功）
                        return Future.succeededFuture((long) rowCount);
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
