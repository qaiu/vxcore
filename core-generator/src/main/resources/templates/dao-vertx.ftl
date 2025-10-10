<#-- Vert.x SQL 风格 DAO 模板 -->
package ${package.daoPackage};

import ${package.entityPackage}.${entity.className};
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.function.Function;

<#if entity.description?has_content>
/**
 * ${entity.description}数据访问对象 (Vert.x SQL 风格)
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}Dao {

    private final SqlClient sqlClient;
    private final String tableName = "${entity.tableName}";

    public ${entity.className}Dao(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 查询结果
     */
    public Future<Optional<${entity.className}>> findById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        String sql = "SELECT * FROM " + tableName + " WHERE <#if entity.primaryKeyField??>${entity.primaryKeyField.columnName}<#else>id</#if> = ?";
        return sqlClient.preparedQuery(sql)
                .execute(Tuple.of(id))
                .map(rows -> {
                    if (rows.size() > 0) {
                        return Optional.of(mapRow(rows.iterator().next()));
                    } else {
                        return Optional.empty();
                    }
                });
    }

    /**
     * 查询所有记录
     * 
     * @return 所有记录
     */
    public Future<List<${entity.className}>> findAll() {
        String sql = "SELECT * FROM " + tableName;
        return sqlClient.preparedQuery(sql)
                .execute()
                .map(rows -> {
                    List<${entity.className}> entities = new ArrayList<>();
                    for (Row row : rows) {
                        entities.add(mapRow(row));
                    }
                    return entities;
                });
    }

    /**
     * 分页查询
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 查询结果
     */
    public Future<List<${entity.className}>> findPage(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        return sqlClient.preparedQuery(sql)
                .execute(Tuple.of(size, offset))
                .map(rows -> {
                    List<${entity.className}> entities = new ArrayList<>();
                    for (Row row : rows) {
                        entities.add(mapRow(row));
                    }
                    return entities;
                });
    }

    /**
     * 统计记录数
     * 
     * @return 记录数
     */
    public Future<Long> count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return sqlClient.preparedQuery(sql)
                .execute()
                .map(rows -> {
                    if (rows.size() > 0) {
                        return rows.iterator().next().getLong(0);
                    }
                    return 0L;
                });
    }

    /**
     * 保存实体
     * 
     * @param entity 实体对象
     * @return 保存后的实体
     */
    public Future<${entity.className}> save(${entity.className} entity) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder(" VALUES (");
        List<Object> params = new ArrayList<>();
        
        <#list entity.fields as field>
        <#if !field.primaryKey>
        sql.append("${field.columnName}, ");
        values.append("?, ");
        params.add(entity.${field.getterName}());
        </#if>
        </#list>
        
        // 移除最后的逗号和空格
        if (sql.length() > 0) {
            sql.setLength(sql.length() - 2);
            values.setLength(values.length() - 2);
        }
        
        sql.append(")").append(values).append(")");
        
        return sqlClient.preparedQuery(sql.toString())
                .execute(Tuple.tuple(params))
                .map(rows -> {
                    <#if entity.primaryKeyField??>
                    // 获取生成的主键
                    if (rows.size() > 0) {
                        Row row = rows.iterator().next();
                        entity.set${entity.primaryKeyField.capitalizedFieldName}(row.get${entity.primaryKeyField.fieldType}("${entity.primaryKeyField.columnName}"));
                    }
                    </#if>
                    return entity;
                });
    }

    /**
     * 更新实体
     * 
     * @param entity 实体对象
     * @return 更新后的实体
     */
    public Future<${entity.className}> update(${entity.className} entity) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> params = new ArrayList<>();
        
        <#list entity.fields as field>
        <#if !field.primaryKey>
        sql.append("${field.columnName} = ?, ");
        params.add(entity.${field.getterName}());
        </#if>
        </#list>
        
        // 移除最后的逗号和空格
        if (sql.length() > 0) {
            sql.setLength(sql.length() - 2);
        }
        
        sql.append(" WHERE <#if entity.primaryKeyField??>${entity.primaryKeyField.columnName}<#else>id</#if> = ?");
        params.add(entity.<#if entity.primaryKeyField??>get${entity.primaryKeyField.capitalizedFieldName}()<#else>getId()</#if>);
        
        return sqlClient.preparedQuery(sql.toString())
                .execute(Tuple.tuple(params))
                .map(rows -> entity);
    }

    /**
     * 根据ID删除
     * 
     * @param id 主键ID
     * @return 删除的记录数
     */
    public Future<Integer> deleteById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        String sql = "DELETE FROM " + tableName + " WHERE <#if entity.primaryKeyField??>${entity.primaryKeyField.columnName}<#else>id</#if> = ?";
        return sqlClient.preparedQuery(sql)
                .execute(Tuple.of(id))
                .map(RowSet::rowCount);
    }

    /**
     * 批量保存
     * 
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    public Future<List<${entity.className}>> saveAll(List<${entity.className}> entities) {
        if (entities.isEmpty()) {
            return Future.succeededFuture(entities);
        }
        
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder(" VALUES (");
        
        <#list entity.fields as field>
        <#if !field.primaryKey>
        sql.append("${field.columnName}, ");
        values.append("?, ");
        </#if>
        </#list>
        
        // 移除最后的逗号和空格
        if (sql.length() > 0) {
            sql.setLength(sql.length() - 2);
            values.setLength(values.length() - 2);
        }
        
        sql.append(")").append(values).append(")");
        
        List<Tuple> batch = new ArrayList<>();
        for (${entity.className} entity : entities) {
            List<Object> params = new ArrayList<>();
            <#list entity.fields as field>
            <#if !field.primaryKey>
            params.add(entity.${field.getterName}());
            </#if>
            </#list>
            batch.add(Tuple.tuple(params));
        }
        
        return sqlClient.preparedQuery(sql.toString())
                .executeBatch(batch)
                .map(rows -> entities);
    }

    /**
     * 批量删除
     * 
     * @param ids ID列表
     * @return 删除的记录数
     */
    public Future<Integer> deleteAllById(List<<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> ids) {
        if (ids.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        StringBuilder sql = new StringBuilder("DELETE FROM " + tableName + " WHERE <#if entity.primaryKeyField??>${entity.primaryKeyField.columnName}<#else>id</#if> IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        
        return sqlClient.preparedQuery(sql.toString())
                .execute(Tuple.tuple(ids))
                .map(RowSet::rowCount);
    }

    /**
     * 将数据库行映射为实体对象
     * 
     * @param row 数据库行
     * @return 实体对象
     */
    private ${entity.className} mapRow(Row row) {
        ${entity.className} entity = new ${entity.className}();
        
        <#list entity.fields as field>
        <#if field.fieldType == "String">
        entity.set${field.capitalizedFieldName}(row.getString("${field.columnName}"));
        <#elseif field.fieldType == "Integer">
        entity.set${field.capitalizedFieldName}(row.getInteger("${field.columnName}"));
        <#elseif field.fieldType == "Long">
        entity.set${field.capitalizedFieldName}(row.getLong("${field.columnName}"));
        <#elseif field.fieldType == "Short">
        entity.set${field.capitalizedFieldName}(row.getShort("${field.columnName}"));
        <#elseif field.fieldType == "Boolean">
        entity.set${field.capitalizedFieldName}(row.getBoolean("${field.columnName}"));
        <#elseif field.fieldType == "BigDecimal">
        entity.set${field.capitalizedFieldName}(row.getBigDecimal("${field.columnName}"));
        <#elseif field.fieldType == "Float">
        entity.set${field.capitalizedFieldName}(row.getFloat("${field.columnName}"));
        <#elseif field.fieldType == "Double">
        entity.set${field.capitalizedFieldName}(row.getDouble("${field.columnName}"));
        <#elseif field.fieldType == "LocalDateTime">
        entity.set${field.capitalizedFieldName}(row.getLocalDateTime("${field.columnName}"));
        <#elseif field.fieldType == "LocalDate">
        entity.set${field.capitalizedFieldName}(row.getLocalDate("${field.columnName}"));
        <#elseif field.fieldType == "LocalTime">
        entity.set${field.capitalizedFieldName}(row.getLocalTime("${field.columnName}"));
        <#else>
        entity.set${field.capitalizedFieldName}(row.getValue("${field.columnName}"));
        </#if>
        </#list>
        
        return entity;
    }
}
