package cn.qaiu.generator.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 继承泛型接口的测试接口 - 用于验证泛型参数处理
 * 
 * 功能：
 * 1. 测试泛型参数解析和替换
 * 2. 测试父接口方法继承
 * 3. 测试泛型类型映射
 * 
 * @author vxcore
 * @version 1.0
 */
@GenerateServiceGen(idType = Long.class, generateProxy = true)
public interface TestGenericInterface extends GenericParentInterface<JsonObject, Long> {
    
    /**
     * 自定义查找方法
     * @return Future 包装的查找结果
     */
    Future<JsonObject> customFind();
    
    /**
     * 自定义保存方法
     * @return Future 包装的保存结果
     */
    Future<Long> customSave();
    
    /**
     * 批量操作
     * @return Future 包装的批量操作结果
     */
    Future<List<JsonObject>> batchOperation();
}
