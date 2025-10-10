<#-- Service 实现类模板 -->
package ${package.servicePackage}.impl;

import ${package.entityPackage}.${entity.className};
import ${package.daoPackage}.${entity.className}Dao;
import ${package.servicePackage}.${entity.className}Service;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

<#if entity.description?has_content>
/**
 * ${entity.description}业务服务实现
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}ServiceImpl implements ${entity.className}Service {

    private final ${entity.className}Dao ${entity.className?uncap_first}Dao;

    public ${entity.className}ServiceImpl(${entity.className}Dao ${entity.className?uncap_first}Dao) {
        this.${entity.className?uncap_first}Dao = ${entity.className?uncap_first}Dao;
    }

    @Override
    public Future<Optional<${entity.className}>> findById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return ${entity.className?uncap_first}Dao.findById(id);
    }

    @Override
    public Future<List<${entity.className}>> findAll() {
        return ${entity.className?uncap_first}Dao.findAll();
    }

    @Override
    public Future<List<${entity.className}>> findPage(int page, int size) {
        return ${entity.className?uncap_first}Dao.findPage(page, size);
    }

    @Override
    public Future<Long> count() {
        return ${entity.className?uncap_first}Dao.count();
    }

    @Override
    public Future<${entity.className}> create(${entity.className} entity) {
        return ${entity.className?uncap_first}Dao.save(entity);
    }

    @Override
    public Future<${entity.className}> update(${entity.className} entity) {
        return ${entity.className?uncap_first}Dao.update(entity);
    }

    @Override
    public Future<Boolean> deleteById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return ${entity.className?uncap_first}Dao.deleteById(id)
                .map(deletedCount -> deletedCount > 0);
    }

    @Override
    public Future<List<${entity.className}>> createBatch(List<${entity.className}> entities) {
        return ${entity.className?uncap_first}Dao.saveAll(entities);
    }

    @Override
    public Future<List<${entity.className}>> updateBatch(List<${entity.className}> entities) {
        return ${entity.className?uncap_first}Dao.updateAll(entities);
    }

    @Override
    public Future<Boolean> deleteBatch(List<<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> ids) {
        return ${entity.className?uncap_first}Dao.deleteAllById(ids)
                .map(deletedCount -> deletedCount > 0);
    }
}
