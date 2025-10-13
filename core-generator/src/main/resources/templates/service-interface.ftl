package ${packageName};

import cn.qaiu.db.dsl.lambda.JService;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import ${entityPackage}.${entityName};
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * ${entityName}服务接口
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ProxyGen
public interface ${serviceName} extends JService<${entityName}, Long> {

    /**
     * 根据ID获取${entityName}
     * 
     * @param id ${entityName}ID
     * @return ${entityName}信息
     */
    Future<${entityName}> get${entityName}ById(Long id);

    /**
     * 获取所有${entityName}
     * 
     * @return ${entityName}列表
     */
    Future<List<${entityName}>> getAll${entityName}s();

    /**
     * 分页查询${entityName}
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Future<LambdaPageResult<${entityName}>> find${entityName}s(long page, long size);

    /**
     * 统计${entityName}数量
     * 
     * @return 数量
     */
    Future<Long> count${entityName}s();

    /**
     * 创建${entityName}
     * 
     * @param ${entityName?uncap_first} ${entityName}信息
     * @return 创建的${entityName}
     */
    Future<${entityName}> create${entityName}(${entityName} ${entityName?uncap_first});

    /**
     * 更新${entityName}
     * 
     * @param ${entityName?uncap_first} ${entityName}信息
     * @return 是否更新成功
     */
    Future<Boolean> update${entityName}(${entityName} ${entityName?uncap_first});

    /**
     * 删除${entityName}
     * 
     * @param id ${entityName}ID
     * @return 是否删除成功
     */
    Future<Boolean> delete${entityName}(Long id);

    /**
     * 搜索${entityName}
     * 
     * @param keyword 关键词
     * @return ${entityName}列表
     */
    Future<List<${entityName}>> search${entityName}s(String keyword);

    /**
     * 获取${entityName}统计信息
     * 
     * @return 统计信息
     */
    Future<JsonObject> get${entityName}Statistics();
}
