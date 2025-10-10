package cn.qaiu.db.dsl.core;

import cn.qaiu.db.dsl.core.executor.ExecutorStrategy;
import cn.qaiu.db.dsl.core.executor.ExecutorStrategyRegistry;
import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.util.DatabaseUrlUtil;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 核心jOOQ执行器
 * 基于策略模式实现，支持多种数据库类型
 * 负责将jOOQ Query转换为SQL并通过Vert.x Pool执行
 */
public class JooqExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqExecutor.class);

    private final Pool pool;
    private final DSLContext dslContext;
    private final JDBCType databaseType;
    private final ExecutorStrategy strategy;

    public JooqExecutor(Pool pool) {
        this.pool = pool;
        
        // 获取执行器策略
        this.strategy = ExecutorStrategyRegistry.getInstance().getStrategy(pool);
        this.databaseType = strategy.getSupportedType();
        
        // 使用策略创建DSL上下文
        this.dslContext = strategy.createDSLContext(pool);
        
        LOGGER.info("Initialized JooqExecutor with strategy: {} for database type: {}", 
                strategy.getClass().getSimpleName(), databaseType);
        
        // 异步检测数据库类型并验证策略
        detectDatabaseTypeAsync();
    }

    /**
     * 获取DSL上下文
     */
    public DSLContext dsl() {
        return dslContext;
    }

    /**
     * 获取数据库类型
     */
    public JDBCType getDatabaseType() {
        return databaseType;
    }

    /**
     * 执行SELECT查询，返回RowSet
     */
    public Future<RowSet<io.vertx.sqlclient.Row>> executeQuery(Query query) {
        return strategy.executeQuery(pool, query);
    }

    /**
     * 执行INSERT/UPDATE/DELETE操作，返回影响行数
     */
    public Future<Integer> executeUpdate(Query query) {
        return strategy.executeUpdate(pool, query);
    }

    /**
     * 执行INSERT操作，返回生成的主键
     */
    public Future<Long> executeInsert(Query query) {
        return strategy.executeInsert(pool, query);
    }
    
    /**
     * 执行批量操作
     */
    public Future<int[]> executeBatch(java.util.List<Query> queries) {
        return strategy.executeBatch(pool, queries);
    }

    /**
     * 异步检测数据库类型并验证策略
     */
    private void detectDatabaseTypeAsync() {
        pool.getConnection()
            .onSuccess(conn -> {
                try {
                    JDBCType detectedType = DatabaseUrlUtil.getJDBCType(conn);
                    if (detectedType != null && detectedType != databaseType) {
                        LOGGER.warn("Detected database type {} differs from strategy type {}, using strategy type", 
                                detectedType, databaseType);
                    } else {
                        LOGGER.debug("Database type verification successful: {}", databaseType);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error detecting database type from connection: {}", e.getMessage());
                } finally {
                    conn.close();
                }
            })
            .onFailure(throwable -> {
                LOGGER.warn("Failed to get connection for database type verification: {}", throwable.getMessage());
            });
    }
    
    /**
     * 获取执行器策略
     */
    public ExecutorStrategy getStrategy() {
        return strategy;
    }
}
