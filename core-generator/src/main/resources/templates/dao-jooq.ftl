<#-- jOOQ 风格 DAO 模板 -->
package ${package.daoPackage};

import ${package.entityPackage}.${entity.className};
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

<#if entity.description?has_content>
/**
 * ${entity.description}数据访问对象 (jOOQ 风格)
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}Dao {

    private final DSLContext dsl;
    private final ${entity.className?upper_case} ${entity.className?lower_case} = ${entity.className?upper_case}.${entity.className?upper_case};

    public ${entity.className}Dao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 查询结果
     */
    public Future<Optional<${entity.className}>> findById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return Future.fromCompletionStage(
            dsl.selectFrom(${entity.className?lower_case})
                .where(<#if entity.primaryKeyField??>${entity.className?lower_case}.${entity.primaryKeyField.columnName?upper_case}<#else>${entity.className?lower_case}.ID</#if>.eq(id))
                .fetchOptionalAsync()
        ).map(optional -> optional.map(this::mapRecord));
    }

    /**
     * 查询所有记录
     * 
     * @return 所有记录
     */
    public Future<List<${entity.className}>> findAll() {
        return Future.fromCompletionStage(
            dsl.selectFrom(${entity.className?lower_case})
                .fetchAsync()
        ).map(result -> {
            List<${entity.className}> entities = new ArrayList<>();
            for (Record record : result) {
                entities.add(mapRecord(record));
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
        return Future.fromCompletionStage(
            dsl.selectFrom(${entity.className?lower_case})
                .limit(size)
                .offset(offset)
                .fetchAsync()
        ).map(result -> {
            List<${entity.className}> entities = new ArrayList<>();
            for (Record record : result) {
                entities.add(mapRecord(record));
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
        return Future.fromCompletionStage(
            dsl.selectCount()
                .from(${entity.className?lower_case})
                .fetchOneAsync()
        ).map(record -> record.getValue(0, Long.class));
    }

    /**
     * 保存实体
     * 
     * @param entity 实体对象
     * @return 保存后的实体
     */
    public Future<${entity.className}> save(${entity.className} entity) {
        return Future.fromCompletionStage(
            dsl.insertInto(${entity.className?lower_case})
                .set(<#list entity.fields as field><#if !field.primaryKey>${entity.className?lower_case}.${field.columnName?upper_case}, entity.${field.getterName}()<#if field_has_next>, </#if></#if></#list>)
                .returning()
                .fetchOneAsync()
        ).map(this::mapRecord);
    }

    /**
     * 更新实体
     * 
     * @param entity 实体对象
     * @return 更新后的实体
     */
    public Future<${entity.className}> update(${entity.className} entity) {
        return Future.fromCompletionStage(
            dsl.update(${entity.className?lower_case})
                .set(<#list entity.fields as field><#if !field.primaryKey>${entity.className?lower_case}.${field.columnName?upper_case}, entity.${field.getterName}()<#if field_has_next>, </#if></#if></#list>)
                .where(<#if entity.primaryKeyField??>${entity.className?lower_case}.${entity.primaryKeyField.columnName?upper_case}<#else>${entity.className?lower_case}.ID</#if>.eq(entity.<#if entity.primaryKeyField??>get${entity.primaryKeyField.capitalizedFieldName}()<#else>getId()</#if>))
                .returning()
                .fetchOneAsync()
        ).map(this::mapRecord);
    }

    /**
     * 根据ID删除
     * 
     * @param id 主键ID
     * @return 删除的记录数
     */
    public Future<Integer> deleteById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return Future.fromCompletionStage(
            dsl.deleteFrom(${entity.className?lower_case})
                .where(<#if entity.primaryKeyField??>${entity.className?lower_case}.${entity.primaryKeyField.columnName?upper_case}<#else>${entity.className?lower_case}.ID</#if>.eq(id))
                .executeAsync()
        );
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
        
        return Future.fromCompletionStage(
            dsl.insertInto(${entity.className?lower_case})
                .columns(<#list entity.fields as field><#if !field.primaryKey>${entity.className?lower_case}.${field.columnName?upper_case}<#if field_has_next>, </#if></#if></#list>)
                .valuesOfRecords(entities.stream()
                    .map(entity -> DSL.record(<#list entity.fields as field><#if !field.primaryKey>entity.${field.getterName}()<#if field_has_next>, </#if></#if></#list>))
                    .toList())
                .returning()
                .fetchAsync()
        ).map(result -> {
            List<${entity.className}> savedEntities = new ArrayList<>();
            for (Record record : result) {
                savedEntities.add(mapRecord(record));
            }
            return savedEntities;
        });
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
        
        return Future.fromCompletionStage(
            dsl.deleteFrom(${entity.className?lower_case})
                .where(<#if entity.primaryKeyField??>${entity.className?lower_case}.${entity.primaryKeyField.columnName?upper_case}<#else>${entity.className?lower_case}.ID</#if>.in(ids))
                .executeAsync()
        );
    }

    /**
     * 将 jOOQ Record 映射为实体对象
     * 
     * @param record jOOQ Record
     * @return 实体对象
     */
    private ${entity.className} mapRecord(Record record) {
        ${entity.className} entity = new ${entity.className}();
        
        <#list entity.fields as field>
        entity.set${field.capitalizedFieldName}(record.get(${entity.className?lower_case}.${field.columnName?upper_case}, ${field.fieldType}.class));
        </#list>
        
        return entity;
    }
}
