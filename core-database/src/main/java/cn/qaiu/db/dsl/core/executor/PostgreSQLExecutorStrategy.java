package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.util.DatabaseUrlUtil;
import io.vertx.sqlclient.Pool;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL执行器策略 提供PostgreSQL数据库的特定执行逻辑
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class PostgreSQLExecutorStrategy extends AbstractExecutorStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLExecutorStrategy.class);

  /**
   * 获取支持的数据源类型
   *
   * @return PostgreSQL类型
   */
  @Override
  public JDBCType getSupportedType() {
    return JDBCType.PostgreSQL;
  }

  /**
   * 获取SQL方言
   *
   * @return PostgreSQL方言
   */
  @Override
  public SQLDialect getSQLDialect() {
    return SQLDialect.POSTGRES;
  }

  /**
   * 获取连接池类型
   * 使用通用Pool类型，避免依赖已废弃的PgPool
   *
   * @return Pool类型
   */
  @Override
  public Class<? extends Pool> getPoolType() {
    return Pool.class;
  }

  /**
   * 检查是否支持指定的连接池
   * 通过检查连接池的数据库类型来判断，而非类名
   *
   * @param pool 连接池
   * @return 是否支持
   */
  @Override
  public boolean supports(Pool pool) {
    // 优先通过URL检测数据库类型
    try {
      JDBCType jdbcType = DatabaseUrlUtil.getJDBCTypeFromPool(pool).toCompletionStage().toCompletableFuture().get();
      boolean supported = jdbcType == JDBCType.PostgreSQL;
      if (supported) {
        LOGGER.debug("PostgreSQL executor strategy supports pool (detected by URL): {}", pool.getClass().getName());
      }
      return supported;
    } catch (Exception e) {
      LOGGER.debug("Failed to detect database type from pool URL, falling back to class name check");
    }

    // 后备：通过类名检测
    String className = pool.getClass().getName().toLowerCase();
    boolean supported = className.contains("pg") || className.contains("postgres");

    if (supported) {
      LOGGER.debug("PostgreSQL executor strategy supports pool (detected by class name): {}", pool.getClass().getName());
    }

    return supported;
  }
}
