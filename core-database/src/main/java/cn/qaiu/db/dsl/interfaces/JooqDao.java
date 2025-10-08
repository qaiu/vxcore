package cn.qaiu.db.dsl.interfaces;

import io.vertx.core.Future;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

/**
 * jOOQ DAO统一接口
 * 定义所有DAO的基本CRUD操作
 */
public interface JooqDao<T, ID> {
    
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
     * 根据条件查找实体列表
     */
    Future<List<T>> findByCondition(Condition condition);
    
    /**
     * 统计数量
     */
    Future<Long> count();
    
    /**
     * 根据条件统计数量
     */
    Future<Long> count(Condition condition);
    
    /**
     * 检查实体是否存在
     */
    Future<Boolean> exists(ID id);
    
    /**
     * 根据条件检查实体是否存在
     */
    Future<Boolean> exists(Condition condition);
}
