package cn.qaiu.example.util;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.example.entity.Product;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自动建表管理器 基于 @DdlTable 注解实现自动ORM建表和索引
 *
 * @author QAIU
 */
public class AutoTableManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoTableManager.class);

  private final JooqExecutor jooqExecutor;

  public AutoTableManager(JooqExecutor jooqExecutor) {
    this.jooqExecutor = jooqExecutor;
  }

  /** 创建所有表 */
  public Future<Void> createAllTables() {
    List<Class<?>> entityClasses = Arrays.asList(User.class, Product.class);

    List<Future<Void>> futures =
        entityClasses.stream().map(this::createTable).collect(java.util.stream.Collectors.toList());

    return Future.all(futures).mapEmpty();
  }

  /** 创建单个表 */
  private Future<Void> createTable(Class<?> entityClass) {
    String tableName = getTableName(entityClass);
    LOGGER.info("Creating table: {}", tableName);

    // 获取Pool - 从DataSourceManager获取
    cn.qaiu.db.datasource.DataSourceManager manager =
        cn.qaiu.db.datasource.DataSourceManager.getInstance();
    io.vertx.sqlclient.Pool pool = manager.getDefaultPool();

    if (pool == null) {
      LOGGER.error("Cannot get default pool for table creation");
      return io.vertx.core.Future.failedFuture("Cannot get default pool");
    }

    // 使用DDL映射系统自动创建表
    return cn.qaiu.db.ddl.EnhancedCreateTable.createTableWithStrictMapping(pool, entityClass)
        .onSuccess(v -> LOGGER.info("✅ Table created successfully: {}", tableName))
        .onFailure(err -> LOGGER.error("❌ Failed to create table: {}", tableName, err));
  }

  /** 获取表名 */
  private String getTableName(Class<?> entityClass) {
    // 从 @DdlTable 注解获取表名
    cn.qaiu.db.ddl.DdlTable ddlTable = entityClass.getAnnotation(cn.qaiu.db.ddl.DdlTable.class);
    if (ddlTable != null && !ddlTable.value().isEmpty()) {
      return ddlTable.value();
    }
    return entityClass.getSimpleName().toLowerCase(Locale.ROOT) + "s";
  }
}
