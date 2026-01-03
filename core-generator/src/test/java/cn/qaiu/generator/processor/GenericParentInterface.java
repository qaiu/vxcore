package cn.qaiu.generator.processor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 泛型父接口 - 用于测试泛型参数处理
 * 
 * @param <T> 实体类型
 * @param <ID> ID类型
 */
public interface GenericParentInterface<T, ID> {
    
    /**
     * 根据ID查找实体
     * @return Future 包装的实体
     */
    Future<T> findById();
    
    /**
     * 查找所有实体
     * @return Future 包装的实体列表
     */
    Future<List<T>> findAll();
    
    /**
     * 保存实体
     * @return Future 包装的保存结果
     */
    Future<ID> save();
    
    /**
     * 删除实体
     * @return Future 包装的删除结果
     */
    Future<Boolean> delete();
}
