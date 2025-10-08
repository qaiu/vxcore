package cn.qaiu.db.dsl.mapper;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.List;
import java.util.Optional;

/**
 * 实体映射器接口
 * 负责数据库行与Java实体之间的转换
 */
public interface EntityMapper<T> {
    
    /**
     * 从单行数据映射为实体
     */
    T fromRow(Row row);
    
    /**
     * 从RowSet映射为单个实体（如果存在）
     */
    Optional<T> fromSingle(RowSet<Row> rowSet);
    
    /**
     * 从RowSet映射为实体列表
     */
    List<T> fromMultiple(RowSet<Row> rowSet);
    
    /**
     * 将实体转换为JsonObject
     */
    JsonObject toJsonObject(T entity);
    
    /**
     * 获取实体对应的表名
     */
    String getTableName();
}
