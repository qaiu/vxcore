<#-- JService 实现类模板 -->
package ${package.servicePackage}.impl;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import ${package.entityPackage}.${entity.className};
import ${package.servicePackage}.${entity.className}Service;
import io.vertx.core.Future;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

<#if entity.description?has_content>
/**
 * ${entity.description}业务服务实现
 * 基于 JServiceImpl 提供完整的数据访问服务
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
public class ${entity.className}ServiceImpl extends JServiceImpl<${entity.className}, <#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> implements ${entity.className}Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(${entity.className}ServiceImpl.class);

    public ${entity.className}ServiceImpl(JooqExecutor executor) {
        super(executor, ${entity.className}.class);
    }

<#-- 生成业务方法实现 -->
<#if entity.description?has_content>
    @Override
    public Future<List<${entity.className}>> findAll${entity.className}s() {
        LOGGER.info("查找所有${entity.description}");
        return list();
    }
</#if>

<#-- 根据字段生成查询方法实现 -->
<#list entity.fields as field>
<#if field.fieldName != "id" && field.fieldName != "createTime" && field.fieldName != "updateTime">
    @Override
    public Future<List<${entity.className}>> findBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName}) {
        LOGGER.info("根据${field.fieldName}查询: {}", ${field.fieldName});
        return listByField(${entity.className}::get${field.fieldName?cap_first}, ${field.fieldName});
    }

    @Override
    public Future<${entity.className}> findOneBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName}) {
        LOGGER.info("根据${field.fieldName}查询单个: {}", ${field.fieldName});
        return getByField(${entity.className}::get${field.fieldName?cap_first}, ${field.fieldName})
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("${entity.className} not found with ${field.fieldName}: " + ${field.fieldName});
                    }
                });
    }

    @Override
    public Future<Boolean> existsBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName}) {
        LOGGER.info("检查${field.fieldName}是否存在: {}", ${field.fieldName});
        return existsByField(${entity.className}::get${field.fieldName?cap_first}, ${field.fieldName});
    }

    @Override
    public Future<Long> countBy${field.fieldName?cap_first}(${field.fieldType} ${field.fieldName}) {
        LOGGER.info("统计${field.fieldName}的数量: {}", ${field.fieldName});
        return countByField(${entity.className}::get${field.fieldName?cap_first}, ${field.fieldName});
    }
</#if>
</#list>

<#-- 生成状态相关方法实现 -->
<#if entity.fields?seq_contains("status")>
    @Override
    public Future<List<${entity.className}>> findActive${entity.className}s() {
        LOGGER.info("查找活跃${entity.className?lower_case}");
        return lambdaList(lambdaQuery()
                .eq(${entity.className}::getStatus, "ACTIVE")
                .orderByDesc(${entity.className}::getCreateTime));
    }

    @Override
    public Future<Long> countActive${entity.className}s() {
        LOGGER.info("统计活跃${entity.className?lower_case}数量");
        return lambdaCount(lambdaQuery()
                .eq(${entity.className}::getStatus, "ACTIVE"));
    }
</#if>

<#-- 生成时间相关方法实现 -->
<#if entity.fields?seq_contains("createTime")>
    @Override
    public Future<List<${entity.className}>> findByCreateTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LOGGER.info("根据创建时间范围查询: {} - {}", startTime, endTime);
        return lambdaList(lambdaQuery()
                .ge(${entity.className}::getCreateTime, startTime)
                .le(${entity.className}::getCreateTime, endTime)
                .orderByDesc(${entity.className}::getCreateTime));
    }
</#if>

<#-- 生成搜索方法实现 -->
<#if entity.fields?seq_contains("name")>
    @Override
    public Future<List<${entity.className}>> searchByName(String keyword) {
        LOGGER.info("根据名称模糊查询: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return Future.succeededFuture(List.of());
        }
        
        return lambdaList(lambdaQuery()
                .like(${entity.className}::getName, keyword)
                .orderByDesc(${entity.className}::getCreateTime)
                .limit(20));
    }
</#if>

<#-- 生成分页查询方法实现 -->
    @Override
    public Future<LambdaPageResult<${entity.className}>> find${entity.className}sByPage(long page, long size) {
        LOGGER.info("分页查询${entity.className?lower_case}: 页码={}, 每页={}", page, size);
        return lambdaPage(lambdaQuery()
                .orderByDesc(${entity.className}::getCreateTime), page, size);
    }

<#-- 生成统计方法实现 -->
    @Override
    public Future<Long> count${entity.className}s() {
        LOGGER.info("统计${entity.className?lower_case}总数量");
        return count();
    }
}
