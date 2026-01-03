package cn.qaiu.generator.processor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Optional;

/**
 * 测试参照接口 - 用于验证参照接口功能
 * 
 * 功能：
 * 1. 测试参照接口方法生成
 * 2. 测试泛型参数替换
 * 3. 验证 Future 返回类型生成
 * 
 * @author vxcore
 * @version 1.0
 */
public interface TestReferenceInterface<T, ID> {
    
    /**
     * 插入实体
     */
    Future<Optional<T>> insert(T entity);
    
    /**
     * 更新实体（基于ID）
     */
    Future<Optional<T>> update(T entity);
    
    /**
     * 根据ID删除实体
     */
    Future<Boolean> delete(ID id);
    
    /**
     * 根据ID查找实体
     */
    Future<Optional<T>> findById(ID id);
    
    /**
     * 查找所有实体
     */
    Future<List<T>> findAll();
    
    /**
     * 统计数量
     */
    Future<Long> count();
    
    /**
     * 检查实体是否存在
     */
    Future<Boolean> exists(ID id);
}
