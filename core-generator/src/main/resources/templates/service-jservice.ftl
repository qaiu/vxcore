<#-- JService 接口模板 -->
package ${package.servicePackage};

import cn.qaiu.db.dsl.lambda.JService;
import ${package.entityPackage}.${entity.className};
import io.vertx.core.Future;
import java.util.List;

<#if entity.description?has_content>
/**
 * ${entity.description}业务服务接口
 * 基于 JService 提供完整的数据访问服务
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public interface ${entity.className}Service extends JService<${entity.className}, <#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> {

<#-- 生成业务方法 -->
<#if entity.description?has_content>
    /**
     * 查找所有${entity.description}
     * 
     * @return ${entity.description}列表
     */
    Future<List<${entity.className}>> findAll${entity.className}s();
</#if>

<#-- 根据字段生成查询方法 -->
<#list entity.fields as field>
<#if field.fieldName != "id" && field.fieldName != "createTime" && field.fieldName != "updateTime">
    /**
     * 根据${field.fieldName}查询
     * 
     * @param ${field.fieldName} ${field.fieldName}
     * @return 查询结果
     */
    Future<List<${entity.className}>> findBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName});

    /**
     * 根据${field.fieldName}查询单个
     * 
     * @param ${field.fieldName} ${field.fieldName}
     * @return 查询结果
     */
    Future<${entity.className}> findOneBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName});

    /**
     * 检查${field.fieldName}是否存在
     * 
     * @param ${field.fieldName} ${field.fieldName}
     * @return 是否存在
     */
    Future<Boolean> existsBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName});

    /**
     * 统计${field.fieldName}的数量
     * 
     * @param ${field.fieldName} ${field.fieldName}
     * @return 数量
     */
    Future<Long> countBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName});
</#if>
</#list>

<#-- 生成状态相关方法 -->
<#if entity.fields?seq_contains("status")>
    /**
     * 查找活跃记录
     * 
     * @return 活跃记录列表
     */
    Future<List<${entity.className}>> findActive${entity.className}s();

    /**
     * 统计活跃记录数量
     * 
     * @return 活跃记录数量
     */
    Future<Long> countActive${entity.className}s();
</#if>

<#-- 生成时间相关方法 -->
<#if entity.fields?seq_contains("createTime")>
    /**
     * 根据创建时间范围查询
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询结果
     */
    Future<List<${entity.className}>> findByCreateTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
</#if>

<#-- 生成搜索方法 -->
<#if entity.fields?seq_contains("name")>
    /**
     * 根据名称模糊查询
     * 
     * @param keyword 关键词
     * @return 查询结果
     */
    Future<List<${entity.className}>> searchByName(String keyword);
</#if>

<#-- 生成分页查询方法 -->
    /**
     * 分页查询
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Future<cn.qaiu.db.dsl.lambda.LambdaPageResult<${entity.className}>> find${entity.className}sByPage(long page, long size);

<#-- 生成统计方法 -->
    /**
     * 统计总数量
     * 
     * @return 总数量
     */
    Future<Long> count${entity.className}s();
}
