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
 * 增强的建表工具
 * 支持严格的DDL映射和自动同步
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class EnhancedCreateTable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedCreateTable.class);
    
    /**
     * 创建表（兼容原有功能）
     */
    public static Future<Void> createTable(Pool pool, JDBCType type) {
        return CreateTable.createTable(pool, type);
    }
    
    /**
     * 创建表（自动从Pool检测数据库类型）
     */
    public static Future<Void> createTable(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> CreateTable.createTable(pool, dbType));
    }
    
    /**
     * 创建表并启用严格DDL映射（自动从Pool检测数据库类型）
     */
    public static Future<Void> createTableWithStrictMapping(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> createTableWithStrictMapping(pool, dbType));
    }
    
    /**
     * 同步表结构（自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> syncTableStructure(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> synchronizeTable(pool, clz, dbType));
    }
    
    /**
     * 生成表结构报告（自动从Pool检测数据库类型）
     */
    public static Future<String> generateTableStructureReport(Pool pool, Class<?> clz) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> generateTableStructureReport(pool, dbType));
    }
    
    /**
     * 创建表并启用严格DDL映射
     */
    public static Future<Void> createTableWithStrictMapping(Pool pool, JDBCType type) {
        Promise<Void> promise = Promise.promise();
        
        // 首先执行原有的建表逻辑
        createTable(pool, type)
            .onSuccess(v -> {
                LOGGER.info("Basic table creation completed, starting strict DDL synchronization...");
                
                // 然后执行严格的DDL同步
                TableStructureSynchronizer.synchronizeAllTables(pool, type)
                    .onSuccess(syncResult -> {
                        LOGGER.info("Strict DDL synchronization completed successfully");
                        promise.complete();
                    })
                    .onFailure(promise::fail);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }
    
    /**
     * 仅执行严格DDL同步（不创建新表，自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTables(Pool pool) {
        return TableStructureSynchronizer.synchronizeAllTables(pool);
    }
    
    /**
     * 仅执行严格DDL同步（不创建新表）
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTables(Pool pool, JDBCType type) {
        return TableStructureSynchronizer.synchronizeAllTables(pool, type);
    }
    
    /**
     * 同步指定的表（自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTable(Pool pool, Class<?> clz) {
        return TableStructureSynchronizer.synchronizeTable(pool, clz);
    }
    
    /**
     * 同步指定的表
     */
    public static Future<List<TableStructureComparator.TableDifference>> synchronizeTable(Pool pool, Class<?> clz, JDBCType type) {
        return TableStructureSynchronizer.synchronizeTable(pool, clz, type);
    }
    
    /**
     * 强制同步指定的表（自动从Pool检测数据库类型）
     */
    public static Future<List<TableStructureComparator.TableDifference>> forceSynchronizeTable(Pool pool, Class<?> clz) {
        return TableStructureSynchronizer.forceSynchronizeTable(pool, clz);
    }
    
    /**
     * 强制同步指定的表
     */
    public static Future<List<TableStructureComparator.TableDifference>> forceSynchronizeTable(Pool pool, Class<?> clz, JDBCType type) {
        return TableStructureSynchronizer.forceSynchronizeTable(pool, clz, type);
    }
    
    /**
     * 检查表是否需要同步（自动从Pool检测数据库类型）
     */
    public static Future<Boolean> needsSynchronization(Pool pool, Class<?> clz) {
        return TableStructureSynchronizer.needsSynchronization(pool, clz);
    }
    
    /**
     * 检查表是否需要同步
     */
    public static Future<Boolean> needsSynchronization(Pool pool, Class<?> clz, JDBCType type) {
        return TableStructureSynchronizer.needsSynchronization(pool, clz, type);
    }
    
    /**
     * 获取所有使用DdlTable注解的类
     */
    public static Set<Class<?>> getDdlTableClasses() {
        return ReflectionUtil.getReflections().getTypesAnnotatedWith(DdlTable.class);
    }
    
    /**
     * 获取所有使用Table注解的类（兼容原有）
     */
    public static Set<Class<?>> getTableClasses() {
        return ReflectionUtil.getReflections().getTypesAnnotatedWith(Table.class);
    }
    
    /**
     * 检查是否有表需要同步（自动从Pool检测数据库类型）
     */
    public static Future<Boolean> hasTablesNeedingSync(Pool pool) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> hasTablesNeedingSync(pool, dbType));
    }
    
    /**
     * 检查是否有表需要同步
     */
    public static Future<Boolean> hasTablesNeedingSync(Pool pool, JDBCType type) {
        Promise<Boolean> promise = Promise.promise();
        
        Set<Class<?>> ddlTableClasses = getDdlTableClasses();
        if (ddlTableClasses.isEmpty()) {
            promise.complete(false);
            return promise.future();
        }
        
        List<Future<Boolean>> futures = ddlTableClasses.stream()
            .map(clz -> needsSynchronization(pool, clz, type))
            .toList();
        
        Future.all(futures)
            .onSuccess(results -> {
                boolean needsSync = results.list().stream().anyMatch(b -> (Boolean) b);
                promise.complete(needsSync);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }
    
    /**
     * 生成表结构报告（自动从Pool检测数据库类型）
     */
    public static Future<String> generateTableStructureReport(Pool pool) {
        return getDatabaseTypeFromPool(pool)
            .compose(dbType -> generateTableStructureReport(pool, dbType));
    }
    
    /**
     * 生成表结构报告
     */
    public static Future<String> generateTableStructureReport(Pool pool, JDBCType type) {
        Promise<String> promise = Promise.promise();
        
        StringBuilder report = new StringBuilder();
        report.append("=== Table Structure Report ===\n\n");
        
        Set<Class<?>> ddlTableClasses = getDdlTableClasses();
        Set<Class<?>> tableClasses = getTableClasses();
        
        report.append("DdlTable annotated classes: ").append(ddlTableClasses.size()).append("\n");
        report.append("Table annotated classes: ").append(tableClasses.size()).append("\n\n");
        
        List<Future<Void>> futures = new ArrayList<>();
        
        for (Class<?> clz : ddlTableClasses) {
            Future<Void> future = needsSynchronization(pool, clz, type)
                .onSuccess(needsSync -> {
                    report.append("Table: ").append(clz.getSimpleName())
                          .append(" - Needs Sync: ").append(needsSync).append("\n");
                })
                .map(v -> null); // 转换为Void
            futures.add(future);
        }
        
        Future.all(futures)
            .onSuccess(v -> {
                report.append("\n=== End of Report ===");
                promise.complete(report.toString());
            })
            .onFailure(promise::fail);
            
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
    
    /**
     * 解析数据库类型字符串（已过时，保留用于向后兼容）
     * @deprecated 现在优先使用Pool的数据库类型自动检测
     * @param dbtypeStr 数据库类型字符串
     * @return JDBCType枚举值
     */
    @Deprecated(since = "0.1.9", forRemoval = true)
    private static JDBCType parseDbType(String dbtypeStr) {
        if (dbtypeStr == null || dbtypeStr.trim().isEmpty()) {
            return JDBCType.MySQL; // 默认MySQL
        }
        
        String type = dbtypeStr.trim().toLowerCase();
        switch (type) {
            case "mysql":
                return JDBCType.MySQL;
            case "postgresql":
            case "postgres":
                return JDBCType.PostgreSQL;
            case "h2":
                return JDBCType.H2DB;
            case "oracle":
                return JDBCType.MySQL; // 暂时使用MySQL，后续可以添加Oracle支持
            case "sqlserver":
            case "mssql":
                return JDBCType.MySQL; // 暂时使用MySQL，后续可以添加SQL Server支持
            default:
                // 如果无法识别，尝试通过字符串匹配
                if (type.contains("mysql")) {
                    return JDBCType.MySQL;
                } else if (type.contains("postgres")) {
                    return JDBCType.PostgreSQL;
                } else if (type.contains("h2")) {
                    return JDBCType.H2DB;
                } else if (type.contains("oracle")) {
                    return JDBCType.MySQL; // 暂时使用MySQL
                } else if (type.contains("sqlserver") || type.contains("mssql")) {
                    return JDBCType.MySQL; // 暂时使用MySQL
                }
                return JDBCType.MySQL; // 默认返回MySQL
        }
    }
}
