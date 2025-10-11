package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.db.util.DatabaseUrlUtil;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 表结构同步器
 * 负责执行表结构的自动同步
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TableStructureSynchronizer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TableStructureSynchronizer.class);
    
    /**
     * 同步所有表结构（自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeAllTables(Pool pool) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> synchronizeAllTables(pool, dbType));
    }
    
    /**
     * 同步所有表结构
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeAllTables(Pool pool, JDBCType dbType) {
        Promise<List<TableStructureComparator.TableDifference>> promise = Promise.promise();
        
        try {
            // 获取所有使用DdlTable注解的类
            Set<Class<?>> ddlTableClasses = ReflectionUtil.getReflections().getTypesAnnotatedWith(DdlTable.class);
            
            if (ddlTableClasses.isEmpty()) {
                LOGGER.warn("No DdlTable annotated classes found");
                promise.complete(new ArrayList<>());
                return promise.future();
            }
            
            List<Future<List<TableStructureComparator.TableDifference>>> futures = ddlTableClasses.stream()
                .map(clz -> synchronizeTable(pool, clz, dbType))
                .toList();
            
            Future.all(futures)
                .onSuccess(result -> {
                    LOGGER.info("Successfully synchronized {} tables", ddlTableClasses.size());
                    // 合并所有差异
                    List<TableStructureComparator.TableDifference> allDifferences = new ArrayList<>();
                    for (Object futureResult : result.list()) {
                        if (futureResult instanceof List<?> list) {
                            @SuppressWarnings("unchecked")
                            List<TableStructureComparator.TableDifference> differences = (List<TableStructureComparator.TableDifference>) list;
                            allDifferences.addAll(differences);
                        }
                    }
                    promise.complete(allDifferences);
                })
                .onFailure(promise::fail);
                
        } catch (Exception e) {
            LOGGER.error("Failed to synchronize tables", e);
            promise.fail(e);
        }
        
        return promise.future();
    }
    
    /**
     * 同步单个表结构（自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTable(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> synchronizeTable(pool, clz, dbType));
    }
    
    /**
     * 同步单个表结构
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTable(Pool pool, Class<?> clz, JDBCType dbType) {
        Promise<List<TableStructureComparator.TableDifference>> promise = Promise.promise();
        
        try {
            // 创建表元数据
            TableMetadata metadata = TableMetadata.fromClass(clz, dbType);
            
            // 检查是否启用自动同步
            if (!metadata.isAutoSync()) {
                LOGGER.debug("Auto sync disabled for table: {}", metadata.getTableName());
                promise.complete(new ArrayList<>());
                return promise.future();
            }
            
            LOGGER.info("Synchronizing table: {} (version: {})", metadata.getTableName(), metadata.getVersion());
            
            // 比较表结构
            TableStructureComparator.compareTableStructure(pool, metadata, dbType)
                .onSuccess(differences -> {
                    if (differences.isEmpty()) {
                        LOGGER.debug("Table {} is already synchronized", metadata.getTableName());
                        promise.complete(differences);
                        return;
                    }
                    
                    LOGGER.info("Found {} differences for table {}", differences.size(), metadata.getTableName());
                    
                    // 执行同步
                    executeSynchronization(pool, differences, metadata.getTableName())
                        .onSuccess(v -> {
                            LOGGER.info("Successfully synchronized table: {}", metadata.getTableName());
                            promise.complete(differences);
                        })
                        .onFailure(promise::fail);
                })
                .onFailure(promise::fail);
                
        } catch (Exception e) {
            LOGGER.error("Failed to synchronize table: {}", clz.getSimpleName(), e);
            promise.fail(e);
        }
        
        return promise.future();
    }
    
    /**
     * 执行同步操作
     */
    private static Future<Void> executeSynchronization(Pool pool, List<TableStructureComparator.TableDifference> differences, String tableName) {
        Promise<Void> promise = Promise.promise();
        
        // 串行执行同步操作，避免H2DB的表锁冲突
        executeDifferencesSequentially(pool, differences, 0, promise);
            
        return promise.future();
    }
    
    /**
     * 串行执行差异修复
     */
    private static void executeDifferencesSequentially(Pool pool, List<TableStructureComparator.TableDifference> differences, 
                                                      int index, Promise<Void> promise) {
        if (index >= differences.size()) {
            LOGGER.info("Successfully executed {} synchronization operations for table {}", 
                       differences.size(), differences.isEmpty() ? "unknown" : differences.get(0).getTableName());
            promise.complete();
            return;
        }
        
        TableStructureComparator.TableDifference diff = differences.get(index);
        executeDifference(pool, diff)
            .onSuccess(result -> {
                // 继续执行下一个差异
                executeDifferencesSequentially(pool, differences, index + 1, promise);
            })
            .onFailure(throwable -> {
                LOGGER.error("Failed to execute difference {} for table {}: {}", 
                           index, diff.getTableName(), throwable.getMessage());
                promise.fail(throwable);
            });
    }
    
    /**
     * 执行单个差异修复
     */
    private static Future<Void> executeDifference(Pool pool, TableStructureComparator.TableDifference difference) {
        Promise<Void> promise = Promise.promise();
        
        String sql = difference.getSqlFix();
        
        // 如果SQL为null，表示不需要执行（比如H2DB主键字段的可空性修改）
        if (sql == null) {
            LOGGER.debug("Skipping SQL execution for {}: {} (no SQL needed)", 
                        difference.getType(), difference.getColumnName());
            promise.complete();
            return promise.future();
        }
        
        LOGGER.info("Executing SQL: {}", sql);
        
        // 检查是否包含多个SQL语句（用分号分隔）
        if (sql.contains(";\n")) {
            // 分割SQL语句
            String[] sqlStatements = sql.split(";\n");
            List<Future<Void>> futures = new ArrayList<>();
            
            for (String statement : sqlStatements) {
                if (!statement.trim().isEmpty()) {
                    futures.add(executeSingleSql(pool, statement.trim()));
                }
            }
            
            Future.all(futures)
                .onSuccess(result -> {
                    LOGGER.debug("Successfully executed all SQL statements for {}: {}", 
                               difference.getType(), difference.getColumnName());
                    promise.complete();
                })
                .onFailure(throwable -> {
                    LOGGER.error("Failed to execute SQL statements for {}: {}", 
                                difference.getType(), throwable.getMessage());
                    promise.fail(throwable);
                });
        } else {
            // 单个SQL语句
            executeSingleSql(pool, sql)
                .onSuccess(result -> {
                    LOGGER.debug("Successfully executed SQL for {}: {}", 
                               difference.getType(), difference.getColumnName());
                    promise.complete();
                })
                .onFailure(throwable -> {
                    LOGGER.error("Failed to execute SQL for {}: {}", 
                                difference.getType(), throwable.getMessage());
                    promise.fail(throwable);
                });
        }
        
        return promise.future();
    }
    
    /**
     * 执行单个SQL语句
     */
    private static Future<Void> executeSingleSql(Pool pool, String sql) {
        Promise<Void> promise = Promise.promise();
        
        pool.query(sql)
            .execute()
            .onSuccess(result -> {
                promise.complete();
            })
            .onFailure(throwable -> {
                // 处理一些常见的错误，如重复键等
                String message = throwable.getMessage();
                if (message != null && (message.contains("Duplicate key name") || 
                                       message.contains("already exists") ||
                                       message.contains("doesn't exist") ||
                                       message.contains("not found") ||
                                       message.contains("Timeout trying to lock table") ||
                                       message.contains("Table") && message.contains("not found") ||
                                       message.contains("Cannot drop last column") ||
                                       message.contains("Column") && message.contains("not found"))) {
                    LOGGER.warn("Ignoring expected error: {}", message);
                    promise.complete();
                } else {
                    promise.fail(throwable);
                }
            });
        
        return promise.future();
    }
    
    /**
     * 强制同步表结构（自动从Pool检测数据库类型，忽略版本检查）
     */
    public static Future<List<TableStructureComparator.TableDifference>> forceSynchronizeTable(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> forceSynchronizeTable(pool, clz, dbType));
    }
    
    /**
     * 强制同步表结构（忽略版本检查）
     */
    public static Future<List<TableStructureComparator.TableDifference>> forceSynchronizeTable(Pool pool, Class<?> clz, JDBCType dbType) {
        Promise<List<TableStructureComparator.TableDifference>> promise = Promise.promise();
        
        try {
            TableMetadata metadata = TableMetadata.fromClass(clz, dbType);
            LOGGER.info("Force synchronizing table: {}", metadata.getTableName());
            
            // 直接比较并同步，忽略版本
            TableStructureComparator.compareTableStructure(pool, metadata, dbType)
                .onSuccess(differences -> {
                    if (differences.isEmpty()) {
                        LOGGER.info("Table {} is already synchronized", metadata.getTableName());
                        promise.complete(differences);
                        return;
                    }
                    
                    executeSynchronization(pool, differences, metadata.getTableName())
                        .onSuccess(v -> promise.complete(differences))
                        .onFailure(promise::fail);
                })
                .onFailure(promise::fail);
                
        } catch (Exception e) {
            LOGGER.error("Failed to force synchronize table: {}", clz.getSimpleName(), e);
            promise.fail(e);
        }
        
        return promise.future();
    }
    
    /**
     * 检查表是否需要同步（自动从Pool检测数据库类型）
     */
    public static Future<Boolean> needsSynchronization(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> needsSynchronization(pool, clz, dbType));
    }
    
    /**
     * 检查表是否需要同步
     */
    public static Future<Boolean> needsSynchronization(Pool pool, Class<?> clz, JDBCType dbType) {
        Promise<Boolean> promise = Promise.promise();
        
        try {
            TableMetadata metadata = TableMetadata.fromClass(clz, dbType);
            
            if (!metadata.isAutoSync()) {
                promise.complete(false);
                return promise.future();
            }
            
            // 检查数据库类型是否匹配
            if (metadata.getDbType() != dbType) {
                LOGGER.debug("Table {} database type mismatch: expected {}, got {}", 
                           metadata.getTableName(), dbType, metadata.getDbType());
                promise.complete(true); // 数据库类型不匹配，需要同步
                return promise.future();
            }
            
            TableStructureComparator.compareTableStructure(pool, metadata, dbType)
                .onSuccess(differences -> {
                    boolean needsSync = !differences.isEmpty();
                    LOGGER.debug("Table {} needs synchronization: {}", 
                               metadata.getTableName(), needsSync);
                    promise.complete(needsSync);
                })
                .onFailure(promise::fail);
                
        } catch (Exception e) {
            LOGGER.error("Failed to check synchronization status for: {}", clz.getSimpleName(), e);
            promise.fail(e);
        }
        
        return promise.future();
    }
    
    /**
     * 从Pool连接中获取数据库类型
     * 优先级：Pool的JDBC类型 > 注解中的类型
     * 
     * @param pool 数据库连接池
     * @return 数据库类型Future
     */
    private static Future<JDBCType> getDatabaseTypeFromPool(Pool pool) {
        Promise<JDBCType> promise = Promise.promise();
        
        pool.getConnection()
            .onSuccess(conn -> {
                try {
                    JDBCType dbType = DatabaseUrlUtil.getJDBCType(conn);
                    if (dbType != null) {
                        LOGGER.debug("Detected database type from Pool: {}", dbType);
                        promise.complete(dbType);
                    } else {
                        LOGGER.warn("Failed to detect database type from Pool, using MySQL as default");
                        promise.complete(JDBCType.MySQL);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error detecting database type from Pool: {}, using MySQL as default", e.getMessage());
                    promise.complete(JDBCType.MySQL);
                } finally {
                    conn.close();
                }
            })
            .onFailure(throwable -> {
                LOGGER.warn("Failed to get connection from Pool: {}, using MySQL as default", throwable.getMessage());
                promise.complete(JDBCType.MySQL);
            });
            
        return promise.future();
    }
}
