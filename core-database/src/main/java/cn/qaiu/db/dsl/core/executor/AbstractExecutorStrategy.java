package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.dsl.core.SqlAuditListener;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Query;
// import org.jooq.SQLDialect; // 未使用
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象执行器策略
 * 提供通用的执行器实现
 * 
 * @author QAIU
 */
public abstract class AbstractExecutorStrategy implements ExecutorStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExecutorStrategy.class);
    
    @Override
    public DSLContext createDSLContext(Pool pool) {
        Configuration configuration = new DefaultConfiguration();
        configuration.set(getSQLDialect());
        configuration.set(new SqlAuditListener());
        
        LOGGER.debug("Created DSL context for {} with dialect {}", 
                getSupportedType(), getSQLDialect());
        
        return DSL.using(configuration);
    }
    
    @Override
    public Future<RowSet<io.vertx.sqlclient.Row>> executeQuery(Pool pool, Query query) {
        String sql = query.getSQL();
        Object[] bindValues = query.getBindValues().toArray();
        
        LOGGER.debug("Executing query: {} with bind values: {}", sql, java.util.Arrays.toString(bindValues));
        
        return pool.preparedQuery(sql)
                .execute(io.vertx.sqlclient.Tuple.from(bindValues))
                .onSuccess(result -> LOGGER.debug("Query executed successfully, affected rows: {}", result.size()))
                .onFailure(error -> LOGGER.error("Query execution failed: {}", error.getMessage(), error));
    }
    
    @Override
    public Future<Integer> executeUpdate(Pool pool, Query query) {
        String sql = query.getSQL();
        Object[] bindValues = query.getBindValues().toArray();
        
        LOGGER.debug("Executing update: {} with bind values: {}", sql, java.util.Arrays.toString(bindValues));
        
        return pool.preparedQuery(sql)
                .execute(io.vertx.sqlclient.Tuple.from(bindValues))
                .map(RowSet::rowCount)
                .onSuccess(count -> LOGGER.debug("Update executed successfully, affected rows: {}", count))
                .onFailure(error -> LOGGER.error("Update execution failed: {}", error.getMessage(), error));
    }
    
    @Override
    public Future<Long> executeInsert(Pool pool, Query query) {
        String sql = query.getSQL();
        Object[] bindValues = query.getBindValues().toArray();
        
        System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: SQL=" + sql);
        LOGGER.debug("Executing insert: {} with bind values: {}", sql, java.util.Arrays.toString(bindValues));
        
        return pool.preparedQuery(sql)
                .execute(io.vertx.sqlclient.Tuple.from(bindValues))
                .compose(result -> {
                    int rowCount = result.rowCount();
                    System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: rowCount=" + rowCount);
                    LOGGER.debug("Insert affected rows: {}", rowCount);
                    
                    if (rowCount > 0) {
                        // 插入成功后，查询获取最后插入的ID
                        // H2使用IDENTITY()，MySQL使用LAST_INSERT_ID()
                        System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: calling IDENTITY()");
                        return pool.query("SELECT IDENTITY()").execute()
                                .map(idResult -> {
                                    System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: IDENTITY() size=" + idResult.size());
                                    if (idResult.size() > 0) {
                                        io.vertx.sqlclient.Row row = idResult.iterator().next();
                                        Object id = row.getValue(0);
                                        System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: ID=" + id + ", type=" + (id != null ? id.getClass().getName() : "null"));
                                        if (id instanceof Number) {
                                            Long generatedId = ((Number) id).longValue();
                                            System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: generatedId=" + generatedId);
                                            LOGGER.debug("Retrieved generated ID: {}", generatedId);
                                            return generatedId;
                                        }
                                    }
                                    System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: returning 0");
                                    LOGGER.warn("Could not retrieve generated ID");
                                    return 0L;
                                });
                    } else {
                        System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: rowCount=0, returning 0");
                        LOGGER.warn("Insert affected 0 rows");
                        return Future.succeededFuture(0L);
                    }
                })
                .onSuccess(id -> {
                    System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: Final ID=" + id);
                    LOGGER.debug("Insert executed successfully, generated ID: {}", id);
                })
                .onFailure(error -> {
                    System.out.println("[DEBUG] AbstractExecutorStrategy.executeInsert: FAILED - " + error.getMessage());
                    LOGGER.error("Insert execution failed: {}", error.getMessage(), error);
                });
    }
    
    @Override
    public Future<int[]> executeBatch(Pool pool, java.util.List<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            return Future.succeededFuture(new int[0]);
        }
        
        LOGGER.debug("Executing batch operation with {} queries", queries.size());
        
        return pool.getConnection()
            .compose(connection -> {
                try {
                    // 准备批量执行
                    java.util.List<io.vertx.sqlclient.PreparedQuery<io.vertx.sqlclient.RowSet<io.vertx.sqlclient.Row>>> preparedQueries = 
                        new java.util.ArrayList<>();
                    java.util.List<io.vertx.sqlclient.Tuple> tuples = new java.util.ArrayList<>();
                    
                    for (Query query : queries) {
                        String sql = query.getSQL();
                        Object[] bindValues = query.getBindValues().toArray();
                        
                        preparedQueries.add(connection.preparedQuery(sql));
                        tuples.add(io.vertx.sqlclient.Tuple.from(bindValues));
                    }
                    
                    // 执行批量操作
                    Future<int[]> batchFuture = Future.succeededFuture();
                    int[] results = new int[queries.size()];
                    
                    for (int i = 0; i < preparedQueries.size(); i++) {
                        final int index = i;
                        batchFuture = batchFuture.compose(v -> 
                            preparedQueries.get(index).execute(tuples.get(index))
                                .map(rowSet -> {
                                    results[index] = rowSet.rowCount();
                                    return results;
                                })
                        );
                    }
                    
                    return batchFuture
                        .onSuccess(result -> {
                            LOGGER.debug("Batch operation completed successfully, total affected rows: {}", 
                                       java.util.Arrays.stream(result).sum());
                        })
                        .onFailure(error -> {
                            LOGGER.error("Batch operation failed: {}", error.getMessage(), error);
                        })
                        .onComplete(v -> connection.close());
                        
                } catch (Exception e) {
                    connection.close();
                    return Future.failedFuture(e);
                }
            });
    }
    
    @Override
    public boolean supports(Pool pool) {
        return getPoolType().isAssignableFrom(pool.getClass());
    }
}
