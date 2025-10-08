package cn.qaiu.db.dsl;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体映射器接口
 * 
 * 负责将数据库查询结果（Row）映射为 Java 对象
 * 结合 Vert.x CodeGen 和现有 DDL 系统
 * 
 * @param <T> 实体类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface EntityMapper<T extends BaseEntity> {
    
    /**
     * 将数据库行映射为实体对象
     * 
     * @param row 数据库行
     * @return 实体对象
     */
    T from(Row row);
    
    /**
     * 将实体对象转换为 JsonObject（用于序列化）
     * 
     * @param entity 实体对象
     * @return JSON 对象
     */
    JsonObject toJson(T entity);
    
    /**
     * 将单行 RowSet 映射为单个实体对象
     * 
     * @param rowSet 数据库行集合
     * @return 实体对象，如果没有数据则返回 null
     */
    default T fromSingle(RowSet<Row> rowSet) {
        RowIterator<Row> iterator = rowSet.iterator();
        if (iterator.hasNext()) {
            Row row = iterator.next();
            return from(row);
        }
        return null;
    }
    
    /**
     * 将多行 RowSet 映射为实体对象列表
     * 
     * @param rowSet 数据库行集合
     * @return 实体对象列表
     */
    default List<T> fromMultiple(RowSet<Row> rowSet) {
        List<T> entities = new ArrayList<>();
        for (Row row : rowSet) {
            T entity = from(row);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
