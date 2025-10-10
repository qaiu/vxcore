<#-- 控制器类模板 -->
package ${package.controllerPackage};

import ${package.entityPackage}.${entity.className};
import ${package.daoPackage}.${entity.className}Dao;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.RouteMethod;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.util.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;

<#if entity.description?has_content>
/**
 * ${entity.description}控制器
 * 
 * @author ${entity.author}
 * @version ${entity.version}
 * @since ${generatedDate}
 */
</#if>
@RouteHandler("/api/${entity.className?lower_case}")
public class ${entity.className}Controller {

    private final ${entity.className}Dao ${entity.className?uncap_first}Dao;

    public ${entity.className}Controller(${entity.className}Dao ${entity.className?uncap_first}Dao) {
        this.${entity.className?uncap_first}Dao = ${entity.className?uncap_first}Dao;
    }

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 查询结果
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<JsonResult> getById(@PathVariable("id") <#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return ${entity.className?uncap_first}Dao.findById(id)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return JsonResult.success(optional.get());
                    } else {
                        return JsonResult.fail(404, "${entity.className} not found");
                    }
                });
    }

    /**
     * 查询所有记录
     * 
     * @return 所有记录
     */
    @RouteMapping(value = "/", method = RouteMethod.GET)
    public Future<JsonResult> getAll() {
        return ${entity.className?uncap_first}Dao.findAll()
                .map(JsonResult::success);
    }

    /**
     * 分页查询
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 查询结果
     */
    @RouteMapping(value = "/page", method = RouteMethod.GET)
    public Future<JsonResult> getPage(@RequestParam("page") Integer page, 
                                     @RequestParam("size") Integer size) {
        return ${entity.className?uncap_first}Dao.findPage(page, size)
                .map(JsonResult::success);
    }

    /**
     * 统计记录数
     * 
     * @return 记录数
     */
    @RouteMapping(value = "/count", method = RouteMethod.GET)
    public Future<JsonResult> getCount() {
        return ${entity.className?uncap_first}Dao.count()
                .map(JsonResult::success);
    }

    /**
     * 创建记录
     * 
     * @param entity 实体对象
     * @return 创建结果
     */
    @RouteMapping(value = "/", method = RouteMethod.POST)
    public Future<JsonResult> create(@RequestBody ${entity.className} entity) {
        return ${entity.className?uncap_first}Dao.save(entity)
                .map(JsonResult::success);
    }

    /**
     * 更新记录
     * 
     * @param id 主键ID
     * @param entity 实体对象
     * @return 更新结果
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
    public Future<JsonResult> update(@PathVariable("id") <#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id, 
                                      @RequestBody ${entity.className} entity) {
        <#if entity.primaryKeyField??>
        entity.set${entity.primaryKeyField.capitalizedFieldName}(id);
        <#else>
        entity.setId(id);
        </#if>
        return ${entity.className?uncap_first}Dao.update(entity)
                .map(JsonResult::success);
    }

    /**
     * 删除记录
     * 
     * @param id 主键ID
     * @return 删除结果
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
    public Future<JsonResult> delete(@PathVariable("id") <#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if> id) {
        return ${entity.className?uncap_first}Dao.deleteById(id)
                .map(deletedCount -> {
                    if (deletedCount > 0) {
                        return JsonResult.success("Deleted successfully");
                    } else {
                        return JsonResult.fail(404, "${entity.className} not found");
                    }
                });
    }

    /**
     * 批量创建
     * 
     * @param entities 实体列表
     * @return 创建结果
     */
    @RouteMapping(value = "/batch", method = RouteMethod.POST)
    public Future<JsonResult> createBatch(@RequestBody List<${entity.className}> entities) {
        return ${entity.className?uncap_first}Dao.saveAll(entities)
                .map(JsonResult::success);
    }

    /**
     * 批量更新
     * 
     * @param entities 实体列表
     * @return 更新结果
     */
    @RouteMapping(value = "/batch", method = RouteMethod.PUT)
    public Future<JsonResult> updateBatch(@RequestBody List<${entity.className}> entities) {
        return ${entity.className?uncap_first}Dao.updateAll(entities)
                .map(JsonResult::success);
    }

    /**
     * 批量删除
     * 
     * @param ids ID列表
     * @return 删除结果
     */
    @RouteMapping(value = "/batch", method = RouteMethod.DELETE)
    public Future<JsonResult> deleteBatch(@RequestBody List<<#if entity.primaryKeyField??>${entity.primaryKeyField.fieldType}<#else>Long</#if>> ids) {
        return ${entity.className?uncap_first}Dao.deleteAllById(ids)
                .map(deletedCount -> JsonResult.success("Deleted " + deletedCount + " records"));
    }
}
