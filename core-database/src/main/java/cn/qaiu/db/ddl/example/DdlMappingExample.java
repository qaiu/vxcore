package cn.qaiu.db.ddl.example;

import cn.qaiu.db.ddl.EnhancedCreateTable;
import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DDL映射使用示例
 * 演示如何使用严格的DDL映射功能
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DdlMappingExample {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlMappingExample.class);
    
    /**
     * 示例1：创建表并启用严格DDL映射
     */
    public static Future<Void> example1_CreateTableWithStrictMapping(Pool pool) {
        LOGGER.info("=== 示例1：创建表并启用严格DDL映射 ===");
        
        return EnhancedCreateTable.createTableWithStrictMapping(pool, JDBCType.PostgreSQL)
            .onSuccess(v -> LOGGER.info("表创建和同步完成"))
            .onFailure(throwable -> LOGGER.error("表创建失败", throwable));
    }
    
    /**
     * 示例2：同步现有表结构
     */
    public static Future<Void> example2_SynchronizeExistingTables(Pool pool) {
        LOGGER.info("=== 示例2：同步现有表结构 ===");
        
        return EnhancedCreateTable.synchronizeTables(pool, JDBCType.PostgreSQL)
            .onSuccess(differences -> LOGGER.info("表结构同步完成，发现 {} 个差异", differences.size()))
            .onFailure(throwable -> LOGGER.error("表结构同步失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 示例3：同步指定表
     */
    public static Future<Void> example3_SynchronizeSpecificTable(Pool pool) {
        LOGGER.info("=== 示例3：同步指定表 ===");
        
        return EnhancedCreateTable.synchronizeTable(pool, ExampleUser.class, JDBCType.PostgreSQL)
            .onSuccess(differences -> LOGGER.info("ExampleUser表同步完成，发现 {} 个差异", differences.size()))
            .onFailure(throwable -> LOGGER.error("ExampleUser表同步失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 示例4：强制同步表结构
     */
    public static Future<Void> example4_ForceSynchronizeTable(Pool pool) {
        LOGGER.info("=== 示例4：强制同步表结构 ===");
        
        return EnhancedCreateTable.forceSynchronizeTable(pool, ExampleUser.class, JDBCType.PostgreSQL)
            .onSuccess(differences -> LOGGER.info("ExampleUser表强制同步完成，发现 {} 个差异", differences.size()))
            .onFailure(throwable -> LOGGER.error("ExampleUser表强制同步失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 示例5：检查表是否需要同步
     */
    public static Future<Void> example5_CheckSynchronizationStatus(Pool pool) {
        LOGGER.info("=== 示例5：检查表是否需要同步 ===");
        
        return EnhancedCreateTable.needsSynchronization(pool, ExampleUser.class, JDBCType.PostgreSQL)
            .onSuccess(needsSync -> {
                if (needsSync) {
                    LOGGER.info("ExampleUser表需要同步");
                } else {
                    LOGGER.info("ExampleUser表已同步");
                }
            })
            .onFailure(throwable -> LOGGER.error("检查同步状态失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 示例6：生成表结构报告
     */
    public static Future<Void> example6_GenerateTableStructureReport(Pool pool) {
        LOGGER.info("=== 示例6：生成表结构报告 ===");
        
        return EnhancedCreateTable.generateTableStructureReport(pool, JDBCType.PostgreSQL)
            .onSuccess(report -> LOGGER.info("表结构报告:\n{}", report))
            .onFailure(throwable -> LOGGER.error("生成表结构报告失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 示例7：检查是否有表需要同步
     */
    public static Future<Void> example7_CheckIfAnyTablesNeedSync(Pool pool) {
        LOGGER.info("=== 示例7：检查是否有表需要同步 ===");
        
        return EnhancedCreateTable.hasTablesNeedingSync(pool, JDBCType.PostgreSQL)
            .onSuccess(hasTablesNeedSync -> {
                if (hasTablesNeedSync) {
                    LOGGER.info("有表需要同步");
                } else {
                    LOGGER.info("所有表都已同步");
                }
            })
            .onFailure(throwable -> LOGGER.error("检查表同步状态失败", throwable))
            .map(v -> null); // 转换为Void
    }
    
    /**
     * 运行所有示例
     */
    public static Future<Void> runAllExamples(Pool pool) {
        LOGGER.info("开始运行DDL映射示例...");
        
        return example1_CreateTableWithStrictMapping(pool)
            .compose(v -> example2_SynchronizeExistingTables(pool))
            .compose(v -> example3_SynchronizeSpecificTable(pool))
            .compose(v -> example4_ForceSynchronizeTable(pool))
            .compose(v -> example5_CheckSynchronizationStatus(pool))
            .compose(v -> example6_GenerateTableStructureReport(pool))
            .compose(v -> example7_CheckIfAnyTablesNeedSync(pool))
            .onSuccess(v -> LOGGER.info("所有DDL映射示例运行完成"))
            .onFailure(throwable -> LOGGER.error("DDL映射示例运行失败", throwable));
    }

//    public static void main(String[] args) throws InterruptedException {
//        var vertx = Vertx.vertx();
//        // 设置配置
//        SharedData sharedData = vertx.sharedData();
//        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
//        localMap.put(GLOBAL_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));
//        localMap.put(CUSTOM_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));
//
//        VertxHolder.init(vertx);
//
//        // 创建H2数据库连接 - 使用MySQL模式
//        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
//        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
//                .setJdbcUrl("jdbc:postgresql://localhost:5432/testdb")
//                .setUser("testuser")
//                .setPassword("testpass");
//
//        Pool pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
//        runAllExamples(pool).onComplete(ar -> {
//            pool.close();
//            vertx.close();
//        });
////        TimeUnit.SECONDS.sleep(10);
//    }
}
