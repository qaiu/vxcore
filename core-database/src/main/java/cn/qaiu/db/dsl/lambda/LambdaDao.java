package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.core.EnhancedDao;
import cn.qaiu.db.dsl.mapper.EntityMapper;
import cn.qaiu.db.dsl.mapper.DefaultMapper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;

/**
 * Lambda DAO基类
 * 提供类似MyBatis-Plus的Lambda查询功能
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author qaiu
 */
public abstract class LambdaDao<T, ID> extends EnhancedDao<T, ID> {
    
    public LambdaDao(JooqExecutor executor, Class<T> entityClass) {
        super(executor, entityClass);
    }
    
    /**
     * 创建Lambda查询包装器
     */
    public LambdaQueryWrapper<T> lambdaQuery() {
        Table<?> table = DSL.table(DSL.name(tableName));
        return new LambdaQueryWrapper<>(executor.dsl(), table, entityClass);
    }
    
    /**
     * Lambda查询 - 根据条件查询列表
     */
    public Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper) {
        Query select = wrapper.buildSelect();
        return executor.executeQuery(select)
                .map(rows -> {
                    // 手动映射行数据到实体对象
                    List<T> result = new java.util.ArrayList<>();
                    for (io.vertx.sqlclient.Row row : rows) {
                        T entity = entityMapper.fromRow(row);
                        result.add(entity);
                    }
                    return result;
                });
    }
    
    /**
     * Lambda查询 - 根据条件查询单个对象
     */
    public Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper) {
        return lambdaList(wrapper.limit(1))
                .map(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)));
    }
    
    /**
     * Lambda查询 - 根据条件查询数量
     */
    public Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper) {
        Query countSelect = wrapper.buildCount();
        return executor.executeQuery(countSelect)
                .map(rows -> {
                    if (rows.size() > 0) {
                        return rows.iterator().next().getLong(0);
                    }
                    return 0L;
                });
    }
    
    /**
     * Lambda查询 - 检查是否存在
     */
    public Future<Boolean> lambdaExists(LambdaQueryWrapper<T> wrapper) {
        Query existsSelect = wrapper.buildExists();
        return executor.executeQuery(existsSelect)
                .map(rows -> rows.size() > 0);
    }
    
    /**
     * Lambda查询 - 分页查询
     */
    public Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size) {
        // 先查询总数
        Future<Long> countFuture = lambdaCount(wrapper);
        
        // 再查询分页数据
        Future<List<T>> listFuture = lambdaList(wrapper.page(current, size));
        
        return Future.all(countFuture, listFuture)
                .map(compositeFuture -> {
                    Long total = compositeFuture.resultAt(0);
                    List<T> records = compositeFuture.resultAt(1);
                    
                    return new LambdaPageResult<>(records, total, current, size);
                });
    }
    
    /**
     * Lambda查询 - 根据条件删除
     */
    public Future<Integer> lambdaDelete(LambdaQueryWrapper<T> wrapper) {
        Condition condition = wrapper.buildCondition();
        if (condition == DSL.noCondition()) {
            // 如果没有条件，不允许删除所有数据
            return Future.failedFuture("Delete operation without condition is not allowed");
        }
        
        DeleteConditionStep<?> delete = executor.dsl()
                .deleteFrom(DSL.table(DSL.name(tableName)))
                .where(condition);
        
        return executor.executeUpdate(delete);
    }
    
    /**
     * Lambda查询 - 根据条件更新
     */
    public Future<Integer> lambdaUpdate(LambdaQueryWrapper<T> wrapper, T entity) {
        Condition condition = wrapper.buildCondition();
        if (condition == DSL.noCondition()) {
            // 如果没有条件，不允许更新所有数据
            return Future.failedFuture("Update operation without condition is not allowed");
        }
        
        // 将实体转换为更新字段
        EntityMapper<T> mapper = new DefaultMapper<>(entityClass, tableName);
        JsonObject updateData = mapper.toJsonObject(entity);
        
        // 移除主键字段，避免更新主键
        updateData.remove(primaryKeyField);
        
        UpdateSetFirstStep<?> update = executor.dsl()
                .update(DSL.table(DSL.name(tableName)));
        
        UpdateSetMoreStep<?> setStep = null;
        for (String field : updateData.fieldNames()) {
            Object value = updateData.getValue(field);
            if (value != null) {
                if (setStep == null) {
                    setStep = update.set(DSL.field(DSL.name(field)), value);
                } else {
                    setStep = setStep.set(DSL.field(DSL.name(field)), value);
                }
            }
        }
        
        if (setStep == null) {
            return Future.succeededFuture(0);
        }
        
        UpdateConditionStep<?> whereStep = setStep.where(condition);
        
        return executor.executeUpdate(whereStep)
                .map(result -> result);
    }
    
    /**
     * 便捷方法 - 根据字段等值查询
     */
    public <R> Future<List<T>> lambdaList(SFunction<T, R> column, R value) {
        return lambdaList(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值查询单个
     */
    public <R> Future<Optional<T>> lambdaOne(SFunction<T, R> column, R value) {
        return lambdaOne(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值查询数量
     */
    public <R> Future<Long> lambdaCount(SFunction<T, R> column, R value) {
        return lambdaCount(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值检查存在
     */
    public <R> Future<Boolean> lambdaExists(SFunction<T, R> column, R value) {
        return lambdaExists(lambdaQuery().eq(column, value));
    }
}
