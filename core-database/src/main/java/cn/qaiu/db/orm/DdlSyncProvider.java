package cn.qaiu.db.orm;

import cn.qaiu.db.ddl.EnhancedCreateTable;
import cn.qaiu.vx.core.spi.OrmSyncProvider;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DDL同步提供者实现 实现OrmSyncProvider SPI接口，供core模块在启动时调用
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DdlSyncProvider implements OrmSyncProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdlSyncProvider.class);

  @Override
  public String getName() {
    return "vxcore-ddl-sync-provider";
  }

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public boolean shouldSync(Class<?> mainClass, JsonObject config) {
    if (mainClass == null) {
      return false;
    }

    // 检查主类上是否有 @EnableDdlSync 注解
    EnableDdlSync annotation = mainClass.getAnnotation(EnableDdlSync.class);
    if (annotation == null) {
      LOGGER.debug("@EnableDdlSync annotation not found on main class: {}", mainClass.getName());
      return false;
    }

    // 检查策略是否为NONE
    if (annotation.strategy() == DdlSyncStrategy.NONE) {
      LOGGER.info("DDL sync is disabled (strategy=NONE)");
      return false;
    }

    // 检查是否自动执行
    if (!annotation.autoExecute()) {
      LOGGER.info("DDL sync auto-execute is disabled, skipping automatic sync");
      return false;
    }

    return true;
  }

  @Override
  public Future<OrmSyncResult> sync(Vertx vertx, Class<?> mainClass, JsonObject config) {
    Promise<OrmSyncResult> promise = Promise.promise();

    try {
      EnableDdlSync annotation = mainClass.getAnnotation(EnableDdlSync.class);
      if (annotation == null) {
        promise.complete(OrmSyncResult.skipped("@EnableDdlSync annotation not found"));
        return promise.future();
      }

      DdlSyncStrategy strategy = annotation.strategy();
      String[] entityPackages = annotation.entityPackages();
      boolean showDdl = annotation.showDdl();
      boolean failOnError = annotation.failOnError();

      LOGGER.info("Starting DDL sync with strategy: {}", strategy);

      // 获取连接池
      Pool pool = getPool(config, annotation.dataSource());
      if (pool == null) {
        String msg = "No database pool available for DDL sync";
        if (failOnError) {
          promise.fail(new IllegalStateException(msg));
        } else {
          promise.complete(OrmSyncResult.failure(msg));
        }
        return promise.future();
      }

      // 扫描实体类（复用EnhancedCreateTable的扫描方法，支持指定包路径）
      Set<Class<?>> entityClasses = EnhancedCreateTable.getDdlTableClasses(entityPackages);
      if (entityClasses.isEmpty()) {
        LOGGER.info("No entity classes found with @DdlTable annotation");
        promise.complete(OrmSyncResult.skipped("No entity classes found"));
        return promise.future();
      }

      LOGGER.info("Found {} entity classes with @DdlTable annotation", entityClasses.size());
      if (showDdl) {
        entityClasses.forEach(clz -> LOGGER.info("  - {}", clz.getName()));
      }

      // 根据策略执行同步
      executeSyncByStrategy(pool, entityClasses, strategy, showDdl)
          .onSuccess(
              result -> {
                LOGGER.info("DDL sync completed: {}", result.getMessage());
                promise.complete(result);
              })
          .onFailure(
              err -> {
                LOGGER.error("DDL sync failed", err);
                if (failOnError) {
                  promise.fail(err);
                } else {
                  promise.complete(OrmSyncResult.failure(err.getMessage()));
                }
              });

    } catch (Exception e) {
      LOGGER.error("DDL sync failed with exception", e);
      promise.fail(e);
    }

    return promise.future();
  }

  /** 获取数据库连接池 */
  private Pool getPool(JsonObject config, String dataSourceName) {
    try {
      // 尝试从DataSourceManager获取池
      cn.qaiu.db.datasource.DataSourceManager manager =
          cn.qaiu.db.datasource.DataSourceManager.getInstance();

      if (dataSourceName != null && !dataSourceName.isEmpty()) {
        Object pool = manager.getPool(dataSourceName);
        return pool instanceof Pool ? (Pool) pool : null;
      }

      // 获取默认/主数据源
      return manager.getDefaultPool();
    } catch (Exception e) {
      LOGGER.warn("Failed to get pool from DataSourceManager: {}", e.getMessage());
      return null;
    }
  }

  /** 根据策略执行同步 */
  private Future<OrmSyncResult> executeSyncByStrategy(
      Pool pool, Set<Class<?>> entityClasses, DdlSyncStrategy strategy, boolean showDdl) {
    Promise<OrmSyncResult> promise = Promise.promise();

    switch (strategy) {
      case AUTO:
        // 完整同步：创建表+同步结构
        executeAutoSync(pool, entityClasses, showDdl)
            .onSuccess(promise::complete)
            .onFailure(promise::fail);
        break;

      case CREATE:
        // 仅创建表
        executeCreateOnly(pool, entityClasses, showDdl)
            .onSuccess(promise::complete)
            .onFailure(promise::fail);
        break;

      case UPDATE:
        // 仅更新（不删除字段）
        executeUpdateOnly(pool, entityClasses, showDdl)
            .onSuccess(promise::complete)
            .onFailure(promise::fail);
        break;

      case VALIDATE:
        // 仅验证
        executeValidateOnly(pool, entityClasses)
            .onSuccess(promise::complete)
            .onFailure(promise::fail);
        break;

      case NONE:
        promise.complete(OrmSyncResult.skipped("Strategy is NONE"));
        break;

      default:
        promise.fail(new IllegalArgumentException("Unknown DDL sync strategy: " + strategy));
    }

    return promise.future();
  }

  /** 执行自动同步（创建+更新） */
  private Future<OrmSyncResult> executeAutoSync(
      Pool pool, Set<Class<?>> entityClasses, boolean showDdl) {
    LOGGER.info("Executing AUTO sync (auto-detect database type)");
    // 使用EnhancedCreateTable的自动检测数据库类型版本
    return EnhancedCreateTable.createTableWithStrictMapping(pool, entityClasses.iterator().next())
        .map(v -> OrmSyncResult.success(entityClasses.size(), 0));
  }

  /** 执行仅创建 */
  private Future<OrmSyncResult> executeCreateOnly(
      Pool pool, Set<Class<?>> entityClasses, boolean showDdl) {
    LOGGER.info("Executing CREATE_ONLY sync (auto-detect database type)");
    // 使用EnhancedCreateTable的自动检测数据库类型版本
    return EnhancedCreateTable.createTable(pool, entityClasses.iterator().next())
        .map(v -> OrmSyncResult.success(entityClasses.size(), 0));
  }

  /** 执行仅更新 */
  private Future<OrmSyncResult> executeUpdateOnly(
      Pool pool, Set<Class<?>> entityClasses, boolean showDdl) {
    LOGGER.info("Executing UPDATE_ONLY sync (auto-detect database type)");
    // 使用EnhancedCreateTable的自动检测数据库类型版本
    return EnhancedCreateTable.synchronizeTables(pool)
        .map(diffs -> OrmSyncResult.success(0, diffs.size()));
  }

  /** 执行仅验证 */
  private Future<OrmSyncResult> executeValidateOnly(Pool pool, Set<Class<?>> entityClasses) {
    // 直接使用EnhancedCreateTable的方法（内部会自动检测数据库类型）
    return EnhancedCreateTable.hasTablesNeedingSync(pool)
        .map(
            needsSync -> {
              if (needsSync) {
                return OrmSyncResult.failure(
                    "Schema validation failed: tables need synchronization");
              }
              return OrmSyncResult.success(0, 0);
            });
  }
}
