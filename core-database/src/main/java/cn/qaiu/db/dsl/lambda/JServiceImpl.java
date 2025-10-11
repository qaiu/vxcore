package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.common.PageRequest;
import cn.qaiu.db.dsl.common.PageResult;
import cn.qaiu.db.dsl.common.QueryCondition;
import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import org.jooq.Condition;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JService接口的默认实现类
 * 基于EnhancedDao和LambdaDao提供完整的服务层功能
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public abstract class JServiceImpl<T, ID> extends LambdaDao<T, ID> implements JService<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JServiceImpl.class);

    public JServiceImpl(JooqExecutor executor, Class<T> entityClass) {
        super(executor, entityClass);
    }

    // =================== 基础CRUD方法实现 ===================

    @Override
    public Future<Optional<T>> save(T entity) {
        return insert(entity);
    }

    @Override
    public Future<List<T>> saveBatch(List<T> entities) {
        return batchInsert(entities);
    }

    @Override
    public Future<List<T>> saveBatch(List<T> entities, int batchSize) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        List<Future<List<T>>> batchFutures = new ArrayList<>();
        for (int i = 0; i < entities.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, entities.size());
            List<T> batch = entities.subList(i, endIndex);
            batchFutures.add(batchInsert(batch));
        }

        return Future.all(batchFutures)
                .map(compositeFuture -> {
                    List<T> allResults = new ArrayList<>();
                    for (int i = 0; i < compositeFuture.size(); i++) {
                        List<T> batchResult = compositeFuture.resultAt(i);
                        allResults.addAll(batchResult);
                    }
                    return allResults;
                });
    }

    @Override
    public Future<Optional<T>> updateById(T entity) {
        return update(entity);
    }

    @Override
    public Future<List<T>> updateBatchById(List<T> entities) {
        return batchUpdate(entities);
    }

    @Override
    public Future<List<T>> updateBatchById(List<T> entities, int batchSize) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        List<Future<List<T>>> batchFutures = new ArrayList<>();
        for (int i = 0; i < entities.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, entities.size());
            List<T> batch = entities.subList(i, endIndex);
            batchFutures.add(batchUpdate(batch));
        }

        return Future.all(batchFutures)
                .map(compositeFuture -> {
                    List<T> allResults = new ArrayList<>();
                    for (int i = 0; i < compositeFuture.size(); i++) {
                        List<T> batchResult = compositeFuture.resultAt(i);
                        allResults.addAll(batchResult);
                    }
                    return allResults;
                });
    }

    @Override
    public Future<Boolean> removeById(ID id) {
        return delete(id);
    }

    @Override
    public Future<Integer> removeByIds(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Future.succeededFuture(0);
        }
        return batchDelete(new ArrayList<>(ids));
    }

    @Override
    public Future<Integer> remove(Condition condition) {
        try {
            Query deleteQuery = dslBuilder.buildDelete(tableName, condition);
            return executor.executeUpdate(deleteQuery);
        } catch (Exception e) {
            LOGGER.error("Failed to remove entities by condition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> getById(ID id) {
        return findById(id);
    }

    @Override
    public Future<List<T>> listByIds(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }
        
        try {
            Condition condition = DSL.field(primaryKeyField).in(ids.toArray());
            return findByCondition(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to list entities by IDs", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<List<T>> list() {
        return findAll();
    }

    @Override
    public Future<List<T>> list(Condition condition) {
        return findByCondition(condition);
    }

    @Override
    public Future<List<T>> list(QueryCondition queryCondition) {
        try {
            Condition condition = convertQueryConditionToJooq(queryCondition);
            return findByCondition(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to list entities by QueryCondition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<List<T>> list(List<QueryCondition> conditions) {
        try {
            Condition condition = convertQueryConditionsToJooq(conditions, QueryCondition.ConditionType.AND);
            return findByCondition(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to list entities by conditions", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> getOne(Condition condition) {
        try {
            Query selectQuery = dslBuilder.buildSelect(tableName, condition);
            selectQuery = DSL.query(selectQuery.toString() + " LIMIT 1");
            
            return executor.executeQuery(selectQuery)
                    .map(rowSet -> {
                        if (rowSet.size() > 0) {
                            return Optional.of(entityMapper.fromSingle(rowSet).orElse(null));
                        }
                        return Optional.empty();
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to get one entity by condition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> getOne(QueryCondition queryCondition) {
        try {
            Condition condition = convertQueryConditionToJooq(queryCondition);
            return getOne(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to get one entity by QueryCondition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Optional<T>> getOne(List<QueryCondition> conditions) {
        try {
            Condition condition = convertQueryConditionsToJooq(conditions, QueryCondition.ConditionType.AND);
            return getOne(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to get one entity by conditions", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Long> count() {
        return super.count();
    }

    @Override
    public Future<Long> count(Condition condition) {
        return super.count(condition);
    }

    @Override
    public Future<Long> count(QueryCondition queryCondition) {
        try {
            Condition condition = convertQueryConditionToJooq(queryCondition);
            return super.count(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to count entities by QueryCondition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Long> count(List<QueryCondition> conditions) {
        try {
            Condition condition = convertQueryConditionsToJooq(conditions, QueryCondition.ConditionType.AND);
            return super.count(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to count entities by conditions", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Boolean> existsById(ID id) {
        return super.exists(id);
    }

    @Override
    public Future<Boolean> exists(Condition condition) {
        return super.exists(condition);
    }

    @Override
    public Future<Boolean> exists(QueryCondition queryCondition) {
        try {
            Condition condition = convertQueryConditionToJooq(queryCondition);
            return super.exists(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to check existence by QueryCondition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Boolean> exists(List<QueryCondition> conditions) {
        try {
            Condition condition = convertQueryConditionsToJooq(conditions, QueryCondition.ConditionType.AND);
            return super.exists(condition);
        } catch (Exception e) {
            LOGGER.error("Failed to check existence by conditions", e);
            return Future.failedFuture(e);
        }
    }

    // =================== 分页查询方法实现 ===================

    @Override
    public Future<PageResult> page(PageRequest pageRequest) {
        return findPage(pageRequest);
    }

    @Override
    public Future<PageResult> page(PageRequest pageRequest, Condition condition) {
        return findPage(pageRequest, condition);
    }

    @Override
    public Future<PageResult> page(PageRequest pageRequest, QueryCondition queryCondition) {
        return findPage(pageRequest, queryCondition);
    }

    @Override
    public Future<PageResult> page(PageRequest pageRequest, List<QueryCondition> conditions) {
        return findPage(pageRequest, conditions);
    }

    // =================== 排序查询方法实现 ===================

    @Override
    public Future<List<T>> listByOrder(String sortField, boolean ascending, Integer limit) {
        return findByOrder(sortField, ascending, limit);
    }

    @Override
    public Future<List<T>> listByOrder(Map<String, Boolean> sortFields, Integer limit) {
        return findByOrder(sortFields, limit);
    }

    // =================== 聚合查询方法实现 ===================

    @Override
    public Future<Optional<Object>> max(String field) {
        return findMax(field);
    }

    @Override
    public Future<Optional<Object>> min(String field) {
        return findMin(field);
    }

    @Override
    public Future<Optional<Object>> avg(String field) {
        return findAvg(field);
    }

    @Override
    public Future<Optional<Object>> sum(String field) {
        return findSum(field);
    }

    // =================== Lambda查询方法实现 ===================

    @Override
    public Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper) {
        return super.lambdaList(wrapper);
    }

    @Override
    public Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper) {
        return super.lambdaOne(wrapper);
    }

    @Override
    public Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper) {
        return super.lambdaCount(wrapper);
    }

    @Override
    public Future<Boolean> lambdaExists(LambdaQueryWrapper<T> wrapper) {
        return super.lambdaExists(wrapper);
    }

    @Override
    public Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size) {
        return super.lambdaPage(wrapper, current, size);
    }

    @Override
    public Future<Integer> lambdaDelete(LambdaQueryWrapper<T> wrapper) {
        return super.lambdaDelete(wrapper);
    }

    @Override
    public Future<Integer> lambdaUpdate(LambdaQueryWrapper<T> wrapper, T entity) {
        return super.lambdaUpdate(wrapper, entity);
    }

    // =================== 便捷查询方法实现 ===================

    @Override
    public <R> Future<List<T>> listByField(SFunction<T, R> column, R value) {
        return lambdaList(lambdaQuery().eq(column, value));
    }

    @Override
    public <R> Future<Optional<T>> getByField(SFunction<T, R> column, R value) {
        return lambdaOne(lambdaQuery().eq(column, value));
    }

    @Override
    public <R> Future<Long> countByField(SFunction<T, R> column, R value) {
        return lambdaCount(lambdaQuery().eq(column, value));
    }

    @Override
    public <R> Future<Boolean> existsByField(SFunction<T, R> column, R value) {
        return lambdaExists(lambdaQuery().eq(column, value));
    }

    // =================== UPSERT操作方法实现 ===================

    @Override
    public Future<LambdaDao.UpsertResult<T>> saveOrUpdate(T entity) {
        return insertOrUpdate(entity);
    }

    @Override
    public Future<LambdaDao.BatchUpsertResult<T>> saveOrUpdateBatch(List<T> entities) {
        return batchInsertOrUpdate(entities);
    }

    @Override
    public Future<LambdaDao.BatchUpsertResult<T>> saveOrUpdateBatch(List<T> entities, int batchSize) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new LambdaDao.BatchUpsertResult<>(0, 0));
        }

        List<Future<LambdaDao.BatchUpsertResult<T>>> batchFutures = new ArrayList<>();
        for (int i = 0; i < entities.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, entities.size());
            List<T> batch = entities.subList(i, endIndex);
            
            List<Future<LambdaDao.UpsertResult<T>>> batchItemFutures = batch.stream()
                    .map(this::insertOrUpdate)
                    .collect(Collectors.toList());
            
            Future<LambdaDao.BatchUpsertResult<T>> batchFuture = Future.all(batchItemFutures)
                    .map(compositeFuture -> {
                        int insertCount = 0;
                        int updateCount = 0;
                        List<T> results = new ArrayList<>();
                        
                        for (int j = 0; j < compositeFuture.size(); j++) {
                            LambdaDao.UpsertResult<T> result = compositeFuture.resultAt(j);
                            results.add(result.getEntity());
                            if (result.isInserted()) {
                                insertCount++;
                            } else {
                                updateCount++;
                            }
                        }
                        
                        return new LambdaDao.BatchUpsertResult<>(insertCount, updateCount, results);
                    });
            batchFutures.add(batchFuture);
        }

        return Future.all(batchFutures)
                .map(compositeFuture -> {
                    int totalInsertCount = 0;
                    int totalUpdateCount = 0;
                    List<T> allResults = new ArrayList<>();
                    
                    for (int i = 0; i < compositeFuture.size(); i++) {
                        LambdaDao.BatchUpsertResult<T> batchResult = compositeFuture.resultAt(i);
                        totalInsertCount += batchResult.getInsertCount();
                        totalUpdateCount += batchResult.getUpdateCount();
                        allResults.addAll(batchResult.getEntities());
                    }
                    
                    return new LambdaDao.BatchUpsertResult<>(totalInsertCount, totalUpdateCount, allResults);
                });
    }

    @Override
    public Future<LambdaDao.UpsertResult<T>> saveOrUpdateOnConflict(T entity, List<String> conflictColumns) {
        return insertOrUpdateOnConflict(entity, conflictColumns);
    }
}
