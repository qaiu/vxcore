package cn.qaiu.generator.reader;

import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import io.vertx.core.Future;

import java.util.List;

/**
 * 数据库元数据读取器接口
 * 用于从数据库或配置文件中读取表结构信息
 * 
 * @author QAIU
 */
public interface DatabaseMetadataReader {
    
    /**
     * 读取指定模式下的所有表
     * 
     * @param schema 数据库模式名，null表示默认模式
     * @return 表信息列表
     */
    Future<List<TableInfo>> readAllTables(String schema);
    
    /**
     * 读取指定表的信息
     * 
     * @param tableName 表名
     * @return 表信息
     */
    Future<TableInfo> readTable(String tableName);
    
    /**
     * 读取指定表的列信息
     * 
     * @param tableName 表名
     * @return 列信息列表
     */
    Future<List<ColumnInfo>> readColumns(String tableName);
    
    /**
     * 读取指定表的列信息
     * 
     * @param schema 数据库模式名
     * @param tableName 表名
     * @return 列信息列表
     */
    Future<List<ColumnInfo>> readColumns(String schema, String tableName);
    
    /**
     * 测试数据库连接
     * 
     * @return 连接测试结果
     */
    Future<Boolean> testConnection();
    
    /**
     * 关闭数据库连接
     * 
     * @return 关闭结果
     */
    Future<Void> close();
}
