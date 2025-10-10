<#-- Service 接口模板 -->
package ${package.servicePackage};

import ${package.entityPackage}.${entity.className};
import ${package.daoPackage}.${entity.className}Dao;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;

<#if entity.description?has_content>
/**
 * ${entity.description}业务服务接口
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public interface ${entity.className}Service {

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 查询结果
     */
    Future<Optional<${entity.className}>> findById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id);

    /**
     * 查询所有记录
     * 
     * @return 所有记录
     */
    Future<List<${entity.className}>> findAll();

    /**
     * 分页查询
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 查询结果
     */
    Future<List<${entity.className}>> findPage(int page, int size);

    /**
     * 统计记录数
     * 
     * @return 记录数
     */
    Future<Long> count();

    /**
     * 创建记录
     * 
     * @param entity 实体对象
     * @return 创建后的实体
     */
    Future<${entity.className}> create(${entity.className} entity);

    /**
     * 更新记录
     * 
     * @param entity 实体对象
     * @return 更新后的实体
     */
    Future<${entity.className}> update(${entity.className} entity);

    /**
     * 删除记录
     * 
     * @param id 主键ID
     * @return 删除结果
     */
    Future<Boolean> deleteById(<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id);

    /**
     * 批量创建
     * 
     * @param entities 实体列表
     * @return 创建后的实体列表
     */
    Future<List<${entity.className}>> createBatch(List<${entity.className}> entities);

    /**
     * 批量更新
     * 
     * @param entities 实体列表
     * @return 更新后的实体列表
     */
    Future<List<${entity.className}>> updateBatch(List<${entity.className}> entities);

    /**
     * 批量删除
     * 
     * @param ids ID列表
     * @return 删除结果
     */
    Future<Boolean> deleteBatch(List<<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> ids);
}
