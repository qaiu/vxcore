package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SQLDialect;

/**
 * 执行器策略接口
 * 定义不同数据源类型的执行器行为
 * 
 * @author QAIU
 */
public interface ExecutorStrategy {
    
    /**
     * 获取支持的数据源类型
     * 
     * @return 数据源类型
     */
    JDBCType getSupportedType();
    
    /**
     * 获取SQL方言
     * 
     * @return SQL方言
     */
    SQLDialect getSQLDialect();
    
    /**
     * 创建DSL上下文
     * 
     * @param pool 连接池
     * @return DSL上下文
     */
    DSLContext createDSLContext(Pool pool);
    
    /**
     * 执行查询
     * 
     * @param pool 连接池
     * @param query jOOQ查询
     * @return 查询结果
     */
    Future<RowSet<io.vertx.sqlclient.Row>> executeQuery(Pool pool, Query query);
    
    /**
     * 执行更新
     * 
     * @param pool 连接池
     * @param query jOOQ查询
     * @return 影响行数
     */
    Future<Integer> executeUpdate(Pool pool, Query query);
    
    /**
     * 执行插入
     * 
     * @param pool 连接池
     * @param query jOOQ查询
     * @return 插入的ID
     */
    Future<Long> executeInsert(Pool pool, Query query);
    
    /**
     * 执行批量操作
     * 
     * @param pool 连接池
     * @param queries jOOQ查询列表
     * @return 每个查询的影响行数数组
     */
    Future<int[]> executeBatch(Pool pool, java.util.List<Query> queries);
    
    /**
     * 获取连接池类型
     * 
     * @return 连接池类型
     */
    Class<? extends Pool> getPoolType();
    
    /**
     * 验证连接池是否支持
     * 
     * @param pool 连接池
     * @return 是否支持
     */
    boolean supports(Pool pool);
}
