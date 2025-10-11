package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.common.PageRequest;
import cn.qaiu.db.dsl.common.PageResult;
import cn.qaiu.db.dsl.common.QueryCondition;
import io.vertx.core.Future;
import org.jooq.Condition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JService接口 - 类似MyBatis-Plus的IService
 * 提供基础的数据访问服务接口，支持泛型操作
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface JService<T, ID> {

    // =================== 基础CRUD方法 ===================

    /**
     * 插入实体
     * 
     * @param entity 实体对象
     * @return 插入后的实体（包含生成的ID）
     */
    Future<Optional<T>> save(T entity);

    /**
     * 批量插入实体
     * 
     * @param entities 实体列表
     * @return 插入成功的实体列表
     */
    Future<List<T>> saveBatch(List<T> entities);

    /**
     * 批量插入实体（指定批次大小）
     * 
     * @param entities 实体列表
     * @param batchSize 批次大小
     * @return 插入成功的实体列表
     */
    Future<List<T>> saveBatch(List<T> entities, int batchSize);

    /**
     * 更新实体
     * 
     * @param entity 实体对象
     * @return 更新后的实体
     */
    Future<Optional<T>> updateById(T entity);

    /**
     * 批量更新实体
     * 
     * @param entities 实体列表
     * @return 更新成功的实体列表
     */
    Future<List<T>> updateBatchById(List<T> entities);

    /**
     * 批量更新实体（指定批次大小）
     * 
     * @param entities 实体列表
     * @param batchSize 批次大小
     * @return 更新成功的实体列表
     */
    Future<List<T>> updateBatchById(List<T> entities, int batchSize);

    /**
     * 根据ID删除
     * 
     * @param id 主键ID
     * @return 是否删除成功
     */
    Future<Boolean> removeById(ID id);

    /**
     * 根据ID列表批量删除
     * 
     * @param ids ID列表
     * @return 删除成功的数量
     */
    Future<Integer> removeByIds(Collection<ID> ids);

    /**
     * 根据条件删除
     * 
     * @param condition 删除条件
     * @return 删除成功的数量
     */
    Future<Integer> remove(Condition condition);

    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 实体对象
     */
    Future<Optional<T>> getById(ID id);

    /**
     * 根据ID列表查询
     * 
     * @param ids ID列表
     * @return 实体列表
     */
    Future<List<T>> listByIds(Collection<ID> ids);

    /**
     * 查询所有数据
     * 
     * @return 实体列表
     */
    Future<List<T>> list();

    /**
     * 根据条件查询列表
     * 
     * @param condition 查询条件
     * @return 实体列表
     */
    Future<List<T>> list(Condition condition);

    /**
     * 根据QueryCondition查询列表
     * 
     * @param queryCondition 查询条件
     * @return 实体列表
     */
    Future<List<T>> list(QueryCondition queryCondition);

    /**
     * 根据多个QueryCondition查询列表（AND连接）
     * 
     * @param conditions 查询条件列表
     * @return 实体列表
     */
    Future<List<T>> list(List<QueryCondition> conditions);

    /**
     * 查询单个实体
     * 
     * @param condition 查询条件
     * @return 实体对象
     */
    Future<Optional<T>> getOne(Condition condition);

    /**
     * 根据QueryCondition查询单个实体
     * 
     * @param queryCondition 查询条件
     * @return 实体对象
     */
    Future<Optional<T>> getOne(QueryCondition queryCondition);

    /**
     * 根据多个QueryCondition查询单个实体（AND连接）
     * 
     * @param conditions 查询条件列表
     * @return 实体对象
     */
    Future<Optional<T>> getOne(List<QueryCondition> conditions);

    /**
     * 统计总数
     * 
     * @return 总记录数
     */
    Future<Long> count();

    /**
     * 根据条件统计
     * 
     * @param condition 查询条件
     * @return 总记录数
     */
    Future<Long> count(Condition condition);

    /**
     * 根据QueryCondition统计
     * 
     * @param queryCondition 查询条件
     * @return 总记录数
     */
    Future<Long> count(QueryCondition queryCondition);

    /**
     * 根据多个QueryCondition统计（AND连接）
     * 
     * @param conditions 查询条件列表
     * @return 总记录数
     */
    Future<Long> count(List<QueryCondition> conditions);

    /**
     * 检查是否存在
     * 
     * @param id 主键ID
     * @return 是否存在
     */
    Future<Boolean> existsById(ID id);

    /**
     * 根据条件检查是否存在
     * 
     * @param condition 查询条件
     * @return 是否存在
     */
    Future<Boolean> exists(Condition condition);

    /**
     * 根据QueryCondition检查是否存在
     * 
     * @param queryCondition 查询条件
     * @return 是否存在
     */
    Future<Boolean> exists(QueryCondition queryCondition);

    /**
     * 根据多个QueryCondition检查是否存在（AND连接）
     * 
     * @param conditions 查询条件列表
     * @return 是否存在
     */
    Future<Boolean> exists(List<QueryCondition> conditions);

    // =================== 分页查询方法 ===================

    /**
     * 分页查询所有数据
     * 
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    Future<PageResult> page(PageRequest pageRequest);

    /**
     * 根据条件分页查询
     * 
     * @param pageRequest 分页请求
     * @param condition 查询条件
     * @return 分页结果
     */
    Future<PageResult> page(PageRequest pageRequest, Condition condition);

    /**
     * 根据QueryCondition分页查询
     * 
     * @param pageRequest 分页请求
     * @param queryCondition 查询条件
     * @return 分页结果
     */
    Future<PageResult> page(PageRequest pageRequest, QueryCondition queryCondition);

    /**
     * 根据多个QueryCondition分页查询（AND连接）
     * 
     * @param pageRequest 分页请求
     * @param conditions 查询条件列表
     * @return 分页结果
     */
    Future<PageResult> page(PageRequest pageRequest, List<QueryCondition> conditions);

    // =================== 排序查询方法 ===================

    /**
     * 根据字段排序查询
     * 
     * @param sortField 排序字段
     * @param ascending 是否升序
     * @param limit 限制数量
     * @return 实体列表
     */
    Future<List<T>> listByOrder(String sortField, boolean ascending, Integer limit);

    /**
     * 根据多个字段排序查询
     * 
     * @param sortFields 排序字段映射（字段名 -> 是否升序）
     * @param limit 限制数量
     * @return 实体列表
     */
    Future<List<T>> listByOrder(Map<String, Boolean> sortFields, Integer limit);

    // =================== 聚合查询方法 ===================

    /**
     * 聚合查询：统计指定字段的最大值
     * 
     * @param field 字段名
     * @return 最大值
     */
    Future<Optional<Object>> max(String field);

    /**
     * 聚合查询：统计指定字段的最小值
     * 
     * @param field 字段名
     * @return 最小值
     */
    Future<Optional<Object>> min(String field);

    /**
     * 聚合查询：统计指定字段的平均值
     * 
     * @param field 字段名
     * @return 平均值
     */
    Future<Optional<Object>> avg(String field);

    /**
     * 聚合查询：统计指定字段的和
     * 
     * @param field 字段名
     * @return 和
     */
    Future<Optional<Object>> sum(String field);

    // =================== Lambda查询方法 ===================

    /**
     * 创建Lambda查询包装器
     * 
     * @return Lambda查询包装器
     */
    LambdaQueryWrapper<T> lambdaQuery();

    /**
     * Lambda查询 - 根据条件查询列表
     * 
     * @param wrapper Lambda查询包装器
     * @return 实体列表
     */
    Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper);

    /**
     * Lambda查询 - 根据条件查询单个对象
     * 
     * @param wrapper Lambda查询包装器
     * @return 实体对象
     */
    Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper);

    /**
     * Lambda查询 - 根据条件查询数量
     * 
     * @param wrapper Lambda查询包装器
     * @return 数量
     */
    Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper);

    /**
     * Lambda查询 - 检查是否存在
     * 
     * @param wrapper Lambda查询包装器
     * @return 是否存在
     */
    Future<Boolean> lambdaExists(LambdaQueryWrapper<T> wrapper);

    /**
     * Lambda查询 - 分页查询
     * 
     * @param wrapper Lambda查询包装器
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size);

    /**
     * Lambda查询 - 根据条件删除
     * 
     * @param wrapper Lambda查询包装器
     * @return 删除成功的数量
     */
    Future<Integer> lambdaDelete(LambdaQueryWrapper<T> wrapper);

    /**
     * Lambda查询 - 根据条件更新
     * 
     * @param wrapper Lambda查询包装器
     * @param entity 更新实体
     * @return 更新成功的数量
     */
    Future<Integer> lambdaUpdate(LambdaQueryWrapper<T> wrapper, T entity);

    // =================== 便捷查询方法 ===================

    /**
     * 便捷方法 - 根据字段等值查询
     * 
     * @param column Lambda字段表达式
     * @param value 值
     * @return 实体列表
     */
    <R> Future<List<T>> listByField(SFunction<T, R> column, R value);

    /**
     * 便捷方法 - 根据字段等值查询单个
     * 
     * @param column Lambda字段表达式
     * @param value 值
     * @return 实体对象
     */
    <R> Future<Optional<T>> getByField(SFunction<T, R> column, R value);

    /**
     * 便捷方法 - 根据字段等值查询数量
     * 
     * @param column Lambda字段表达式
     * @param value 值
     * @return 数量
     */
    <R> Future<Long> countByField(SFunction<T, R> column, R value);

    /**
     * 便捷方法 - 根据字段等值检查存在
     * 
     * @param column Lambda字段表达式
     * @param value 值
     * @return 是否存在
     */
    <R> Future<Boolean> existsByField(SFunction<T, R> column, R value);

    // =================== UPSERT操作方法 ===================

    /**
     * 插入或更新（UPSERT）操作
     * 根据主键判断是插入还是更新
     * 
     * @param entity 实体对象
     * @return 操作结果，包含是否为新插入的记录
     */
    Future<LambdaDao.UpsertResult<T>> saveOrUpdate(T entity);

    /**
     * 批量插入或更新（UPSERT）操作
     * 
     * @param entities 实体对象列表
     * @return 操作结果统计
     */
    Future<LambdaDao.BatchUpsertResult<T>> saveOrUpdateBatch(List<T> entities);

    /**
     * 批量插入或更新（UPSERT）操作（指定批次大小）
     * 
     * @param entities 实体对象列表
     * @param batchSize 批次大小
     * @return 操作结果统计
     */
    Future<LambdaDao.BatchUpsertResult<T>> saveOrUpdateBatch(List<T> entities, int batchSize);

    /**
     * 根据条件插入或更新（UPSERT）操作
     * 使用ON DUPLICATE KEY UPDATE语法（MySQL）或ON CONFLICT语法（PostgreSQL）
     * 
     * @param entity 实体对象
     * @param conflictColumns 冲突字段列表（用于ON CONFLICT判断）
     * @return 操作结果
     */
    Future<LambdaDao.UpsertResult<T>> saveOrUpdateOnConflict(T entity, List<String> conflictColumns);
}
