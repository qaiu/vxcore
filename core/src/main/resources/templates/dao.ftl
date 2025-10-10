<#-- DAO类模板 -->
package ${package.daoPackage};

import ${package.entityPackage}.${entity.className};
import cn.qaiu.db.dsl.core.LambdaDao;
import cn.qaiu.db.dsl.core.LambdaQueryWrapper;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

<#if entity.description?has_content>
/**
 * ${entity.description}数据访问对象
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}Dao extends LambdaDao<${entity.className}> {

    public ${entity.className}Dao() {
        super(${entity.className}.class);
    }

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 查询结果
     */
    public Future<Optional<${entity.className}>> findById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return lambdaQuery()
                .eq(<#if entity.primaryKeyField??>${entity.className}::get${entity.primaryKeyField.capitalizedFieldName}<#else>${entity.className}::getId</#if>, id)
                .one();
    }

    /**
     * 查询所有记录
     * 
     * @return 所有记录
     */
    public Future<List<${entity.className}>> findAll() {
        return lambdaQuery().list();
    }

    /**
     * 根据条件查询
     * 
     * @param wrapper 查询条件
     * @return 查询结果
     */
    public Future<List<${entity.className}>> findByCondition(LambdaQueryWrapper<${entity.className}> wrapper) {
        return wrapper.list();
    }

    /**
     * 保存实体
     * 
     * @param entity 实体对象
     * @return 保存后的实体
     */
    public Future<${entity.className}> save(${entity.className} entity) {
        return insert(entity);
    }

    /**
     * 更新实体
     * 
     * @param entity 实体对象
     * @return 更新后的实体
     */
    public Future<${entity.className}> update(${entity.className} entity) {
        return updateById(entity);
    }

    /**
     * 根据ID删除
     * 
     * @param id 主键ID
     * @return 删除的记录数
     */
    public Future<Integer> deleteById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return lambdaQuery()
                .eq(<#if entity.primaryKeyField??>${entity.className}::get${entity.primaryKeyField.capitalizedFieldName}<#else>${entity.className}::getId</#if>, id)
                .delete();
    }

    /**
     * 批量保存
     * 
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    public Future<List<${entity.className}>> saveAll(List<${entity.className}> entities) {
        return batchInsert(entities);
    }

    /**
     * 批量更新
     * 
     * @param entities 实体列表
     * @return 更新后的实体列表
     */
    public Future<List<${entity.className}>> updateAll(List<${entity.className}> entities) {
        return batchUpdate(entities);
    }

    /**
     * 批量删除
     * 
     * @param ids ID列表
     * @return 删除的记录数
     */
    public Future<Integer> deleteAllById(List<<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> ids) {
        return lambdaQuery()
                .in(<#if entity.primaryKeyField??>${entity.className}::get${entity.primaryKeyField.capitalizedFieldName}<#else>${entity.className}::getId</#if>, ids)
                .delete();
    }

    /**
     * 统计记录数
     * 
     * @return 记录数
     */
    public Future<Long> count() {
        return lambdaQuery().count();
    }

    /**
     * 根据条件统计记录数
     * 
     * @param wrapper 查询条件
     * @return 记录数
     */
    public Future<Long> countByCondition(LambdaQueryWrapper<${entity.className}> wrapper) {
        return wrapper.count();
    }

    /**
     * 分页查询
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 查询结果
     */
    public Future<List<${entity.className}>> findPage(int page, int size) {
        return lambdaQuery()
                .page(page, size)
                .list();
    }

    /**
     * 根据条件分页查询
     * 
     * @param wrapper 查询条件
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 查询结果
     */
    public Future<List<${entity.className}>> findPageByCondition(LambdaQueryWrapper<${entity.className}> wrapper, int page, int size) {
        return wrapper.page(page, size).list();
    }
}
